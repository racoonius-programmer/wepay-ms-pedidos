package cl.duoc.ms_pedidos.dto;

public record ItemCarritoRequest(
    String nombre,
    Double precio,
    Integer cantidad
) {}