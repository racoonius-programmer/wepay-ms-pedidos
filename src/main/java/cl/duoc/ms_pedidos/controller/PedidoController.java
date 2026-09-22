package cl.duoc.ms_pedidos.controller;

import cl.duoc.ms_pedidos.dto.PedidoRequest;
import cl.duoc.ms_pedidos.dto.PedidoResponse;
import cl.duoc.ms_pedidos.service.PedidoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}