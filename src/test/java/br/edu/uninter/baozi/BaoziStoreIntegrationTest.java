package br.edu.uninter.baozi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:baozi-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
class BaoziStoreIntegrationTest {

    @Autowired
    private MockMvc api;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void executaCrudEValidaRegrasDaApi() throws Exception {
        MvcResult clienteResponse = api.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Alexandre Garcia Barbeito Ferreira5129388","clienteDesde":"2026-11-09"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        long clienteId = objectMapper.readTree(clienteResponse.getResponse().getContentAsString())
                .path("id").asLong();

        MvcResult produtoResponse = api.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Baozi de porco e cebolinha","preco":8.50,"estoque":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        long produtoId = objectMapper.readTree(produtoResponse.getResponse().getContentAsString())
                .path("id").asLong();

        MvcResult pedidoResponse = api.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":%d,"produtoId":%d,"quantidade":3}
                                """.formatted(clienteId, produtoId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        long pedidoId = objectMapper.readTree(pedidoResponse.getResponse().getContentAsString())
                .path("id").asLong();

        assertCollectionContains("/api/clientes", clienteId);
        assertCollectionContains("/api/produtos", produtoId);
        assertCollectionContains("/api/pedidos", pedidoId);
        api.perform(get("/api/clientes/{id}", clienteId)).andExpect(status().isOk());
        api.perform(get("/api/produtos/{id}", produtoId)).andExpect(status().isOk());
        api.perform(get("/api/pedidos/{id}", pedidoId)).andExpect(status().isOk());

        api.perform(delete("/api/clientes/{id}", clienteId)).andExpect(status().isConflict());
        api.perform(delete("/api/produtos/{id}", produtoId)).andExpect(status().isConflict());
        api.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":%d,"produtoId":%d,"quantidade":0}
                                """.formatted(clienteId, produtoId)))
                .andExpect(status().isBadRequest());
        api.perform(get("/api/clientes/99999999")).andExpect(status().isNotFound());

        api.perform(delete("/api/pedidos/{id}", pedidoId)).andExpect(status().isNoContent());
        api.perform(delete("/api/produtos/{id}", produtoId)).andExpect(status().isNoContent());
        api.perform(delete("/api/clientes/{id}", clienteId)).andExpect(status().isNoContent());
    }

    private void assertCollectionContains(String path, long id) throws Exception {
        MvcResult result = api.perform(get(path))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rows = objectMapper.readTree(result.getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode row : rows) {
            if (row.path("id").asLong() == id) {
                found = true;
                break;
            }
        }
        assertTrue(found, "A lista " + path + " deve conter o registro recém-criado.");
    }
}
