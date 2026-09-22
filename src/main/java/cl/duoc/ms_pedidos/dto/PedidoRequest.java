package cl.duoc.ms_pedidos.dto;

import java.util.List;

public record PedidoRequest(
    List<ItemCarritoRequest> items
) {}