package cl.duoc.ms_pedidos.repository;

import cl.duoc.ms_pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Spring crea la consulta SQL automáticamente gracias al nombre del método
    List<Pedido> findByUsuarioOid(String usuarioOid);
}