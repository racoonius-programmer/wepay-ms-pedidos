package cl.duoc.ms_pedidos.dto;

import java.time.LocalDateTime;

public record PedidoResponse(
    Long id,
    String usuarioOid,
    Double total,
    LocalDateTime fechaRegistro,
    String estado
) {}