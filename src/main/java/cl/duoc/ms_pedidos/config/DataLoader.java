package cl.duoc.ms_pedidos.config;

import cl.duoc.ms_pedidos.dto.ItemCarritoRequest;
import cl.duoc.ms_pedidos.dto.PedidoRequest;
import cl.duoc.ms_pedidos.service.PedidoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final PedidoService pedidoService;

    // 1. Quitamos ObjectMapper del constructor
    public DataLoader(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 2. Lo instanciamos manualmente solo cuando se necesita
        ObjectMapper objectMapper = new ObjectMapper();
        
        InputStream inputStream = new ClassPathResource("pedidos-ejemplo.json").getInputStream();
        
        List<PedidoCarga> pedidos = objectMapper.readValue(inputStream, new TypeReference<List<PedidoCarga>>() {});
        
        for (PedidoCarga p : pedidos) {
            PedidoRequest request = new PedidoRequest(p.items());
            pedidoService.crearPedido(p.usuarioOid(), request);
        }
        
        System.out.println("Pedidos de prueba cargados desde JSON exitosamente.");
    }

    record PedidoCarga(String usuarioOid, List<ItemCarritoRequest> items) {}
}