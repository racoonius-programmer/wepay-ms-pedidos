package cl.duoc.ms_pedidos.service;

import cl.duoc.ms_pedidos.dto.ItemCarritoRequest;
import cl.duoc.ms_pedidos.dto.PedidoRequest;
import cl.duoc.ms_pedidos.dto.PedidoResponse;
import cl.duoc.ms_pedidos.entity.DetallePedido;
import cl.duoc.ms_pedidos.entity.EstadoPedido;
import cl.duoc.ms_pedidos.entity.Pedido;
import cl.duoc.ms_pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
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
            // Se asume que actualizaste ItemCarritoRequest para incluir productoId()
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
        
        // TODO: (Próximamente) Si el estado es ACEPTADO, llamar a ms-catalogo para descontar stock
        // TODO: (Próximamente) Publicar evento en Kafka y enviar comando a RabbitMQ para notificar al cliente
        
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
                p.getEstado().name() // Convertimos el Enum a String para la respuesta JSON
        );
    }
}