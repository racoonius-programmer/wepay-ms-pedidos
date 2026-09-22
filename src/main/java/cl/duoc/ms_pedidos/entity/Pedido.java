package cl.duoc.ms_pedidos.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String usuarioOid;
    private Double total;
    private LocalDateTime fechaRegistro;
    private String estado;

    // Relación: Un pedido tiene muchos detalles
    // CascadeType.ALL hace que al guardar el pedido, se guarden sus detalles automáticamente
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pedido_id") // Crea la llave foránea en la tabla detalles_pedido
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {}

    // Getters y Setters anteriores...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuarioOid() { return usuarioOid; }
    public void setUsuarioOid(String usuarioOid) { this.usuarioOid = usuarioOid; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Getter y Setter para la lista
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}