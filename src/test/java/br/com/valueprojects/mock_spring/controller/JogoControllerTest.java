package br.com.valueprojects.mock_spring.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class JogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void deveListarJogosComSucesso() throws Exception {
        mockMvc.perform(get("/jogos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void deveCriarNovoJogo() throws Exception {
        String descricaoJogo = "Jogo de Teste";
        
        mockMvc.perform(post("/jogos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + descricaoJogo + "\""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void deveCriarMultiplosJogos() throws Exception {
        mockMvc.perform(post("/jogos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"Jogo 1\""));
        
        mockMvc.perform(post("/jogos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"Jogo 2\""));
        
        mockMvc.perform(get("/jogos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void deveRetornarContentTypeJSON() throws Exception {
        mockMvc.perform(post("/jogos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"Teste Content-Type\""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void deveCriarJogoComDescricao() throws Exception {
        mockMvc.perform(post("/jogos/criar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"Jogo com Descrição\""))
                .andExpect(status().isOk());
    }
}
