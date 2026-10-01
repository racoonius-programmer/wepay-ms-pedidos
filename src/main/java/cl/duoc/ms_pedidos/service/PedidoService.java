package cl.duoc.ms_pedidos.service;

import cl.duoc.ms_pedidos.dto.ItemCarritoRequest;
import cl.duoc.ms_pedidos.dto.PedidoRequest;
import cl.duoc.ms_pedidos.dto.PedidoResponse;
import cl.duoc.ms_pedidos.entity.DetallePedido;
import cl.duoc.ms_pedidos.entity.EstadoPedido;
import cl.duoc.ms_pedidos.entity.Pedido;
import cl.duoc.ms_pedidos.repository.PedidoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RabbitTemplate rabbitTemplate;

    public PedidoService(PedidoRepository pedidoRepository, RabbitTemplate rabbitTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public PedidoResponse crearPedido(String oid, PedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setUsuarioOid(oid);
        pedido.setFechaRegistro(LocalDateTime.now());
        // Regla de negocio: Todo pedido inicia en estado CREADO
        pedido.setEstado(EstadoPedido.CREADO); 

        double totalCalculado = 0.0;

        for (ItemCarritoRequest item : request.items()) {
            DetallePedido detalle = new DetallePedido(
                    item.productoId(), 
                    item.nombre(), 
                    item.precio(), 
                    item.cantidad()
            );
            pedido.getDetalles().add(detalle);
            
            totalCalculado += item.precio() * item.cantidad();
        }

        pedido.setTotal(totalCalculado);

        Pedido guardado = pedidoRepository.save(pedido);
        
        // TODO: (Próximamente) Publicar evento "OrderCreated" en Kafka
        
        return convertirADto(guardado);
    }
    
    @Transactional
    public PedidoResponse actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
                
        // Regla de negocio: No se puede despachar sin aceptar
        if (nuevoEstado == EstadoPedido.DESPACHADO) {
            if (pedido.getEstado() == EstadoPedido.CREADO) {
                throw new IllegalStateException("No se puede despachar un pedido que no ha sido aceptado o preparado");
            }
        }
        
        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);
        
        // --- Integración con RabbitMQ para Notificaciones ---
        // Emitimos el comando solo si el estado es diferente al inicial
        if (nuevoEstado != EstadoPedido.CREADO) {
            Map<String, Object> mensajeEmail = new HashMap<>();
            // Simulamos el correo usando el OID del usuario
            mensajeEmail.put("to", "cliente_" + actualizado.getUsuarioOid() + "@wepay.cl");
            mensajeEmail.put("subject", "Actualización de Pedido #" + actualizado.getId());
            mensajeEmail.put("body", "Tu pedido ha cambiado al estado: " + nuevoEstado.name());

            // Enviamos al exchange "cmd.direct" con el routing key "email.send"
            rabbitTemplate.convertAndSend("cmd.direct", "email.send", mensajeEmail);
        }
        // ----------------------------------------------------

        // TODO: (Próximamente) Si el estado es ACEPTADO, llamar a ms-catalogo para descontar stock
        // TODO: (Próximamente) Publicar evento de cambio de estado en Kafka
        
        return convertirADto(actualizado);
    }

    public List<PedidoResponse> obtenerTodos() {
        return pedidoRepository.findAll().stream()
                .map(this::convertirADto)
                .toList();
    }

    public List<PedidoResponse> obtenerPorUsuario(String oid) {
        return pedidoRepository.findByUsuarioOid(oid).stream()
                .map(this::convertirADto)
                .toList();
    }

    private PedidoResponse convertirADto(Pedido p) {
        return new PedidoResponse(
                p.getId(),
                p.getUsuarioOid(),
                p.getTotal(),
                p.getFechaRegistro(),
                p.getEstado().name() 
        );
    }
}