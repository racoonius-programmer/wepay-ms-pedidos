package cl.duoc.ms_pedidos.dto;

public record ItemCarritoRequest(
    Long productoId,
    String nombre,
    Double precio,
    Integer cantidad
) {}