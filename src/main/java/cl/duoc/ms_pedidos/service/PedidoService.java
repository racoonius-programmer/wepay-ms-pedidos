package cl.duoc.ms_pedidos.service;

import cl.duoc.ms_pedidos.dto.ItemCarritoRequest;
import cl.duoc.ms_pedidos.dto.PedidoRequest;
import cl.duoc.ms_pedidos.dto.PedidoResponse;
import cl.duoc.ms_pedidos.entity.DetallePedido;
import cl.duoc.ms_pedidos.entity.Pedido;
import cl.duoc.ms_pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public PedidoResponse crearPedido(String oid, PedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setUsuarioOid(oid);
        pedido.setFechaRegistro(LocalDateTime.now());
        pedido.setEstado("CONFIRMADO");

        double totalCalculado = 0.0;

        // Transformamos cada item del DTO en una entidad para la base de datos
        for (ItemCarritoRequest item : request.items()) {
            DetallePedido detalle = new DetallePedido(item.nombre(), item.precio(), item.cantidad());
            pedido.getDetalles().add(detalle); // Lo vinculamos al pedido
            
            totalCalculado += item.precio() * item.cantidad();
        }

        pedido.setTotal(totalCalculado);

        // Al guardar el pedido, Hibernate guarda automáticamente todos los detalles en la otra tabla
        Pedido guardado = pedidoRepository.save(pedido);
        return convertirADto(guardado);
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
                p.getEstado()
        );
    }
}