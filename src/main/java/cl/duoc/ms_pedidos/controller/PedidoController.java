package cl.duoc.ms_pedidos.controller;

import cl.duoc.ms_pedidos.dto.PedidoRequest;
import cl.duoc.ms_pedidos.dto.PedidoResponse;
import cl.duoc.ms_pedidos.entity.EstadoPedido;
import cl.duoc.ms_pedidos.service.PedidoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/crear/{oid}")
    public PedidoResponse crearPedido(@PathVariable String oid, @RequestBody PedidoRequest request) {
        return pedidoService.crearPedido(oid, request);
    }

    @GetMapping
    public List<PedidoResponse> obtenerTodos() {
        return pedidoService.obtenerTodos();
    }

    @GetMapping("/usuario/{oid}")
    public List<PedidoResponse> obtenerPorUsuario(@PathVariable String oid) {
        return pedidoService.obtenerPorUsuario(oid);
    }

    // NUEVO ENDPOINT: Requerido por el caso para cambiar el estado del pedido
    @PutMapping("/{id}/status")
    public PedidoResponse actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        // Extraemos el valor "status" del JSON que envíe el cliente
        String status = body.get("status");
        
        // Convertimos el String a nuestro Enum EstadoPedido
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(status.toUpperCase());
        
        return pedidoService.actualizarEstado(id, nuevoEstado);
    }
}