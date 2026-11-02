package com.br.fluxo.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private static final String ORIGEM_FRONTEND = "http://localhost:5173";

    private CorsFilter filtro;

    @BeforeEach
    void criarFiltro() {
        filtro = new CorsConfig().corsFilter();
    }

    @Test
    void deveCriarFiltro() {
        assertThat(filtro).isNotNull();
    }

    @Test
    void deveLiberarPreflightDoFrontendParaQualquerMetodoECabecalho() throws ServletException, IOException {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("OPTIONS", "/engine-rest/task");
        requisicao.addHeader("Origin", ORIGEM_FRONTEND);
        requisicao.addHeader("Access-Control-Request-Method", "PUT");
        requisicao.addHeader("Access-Control-Request-Headers", "X-Cabecalho-Customizado");
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(200);
        assertThat(resposta.getHeader("Access-Control-Allow-Origin")).isEqualTo(ORIGEM_FRONTEND);
        assertThat(resposta.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(resposta.getHeader("Access-Control-Allow-Methods")).contains("PUT");
        assertThat(resposta.getHeader("Access-Control-Allow-Headers")).containsIgnoringCase("X-Cabecalho-Customizado");
    }

    @Test
    void deveAdicionarCabecalhosCorsEmRequisicaoSimplesDoFrontend() throws ServletException, IOException {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/engine-rest/task");
        requisicao.addHeader("Origin", ORIGEM_FRONTEND);
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain cadeia = new MockFilterChain();

        filtro.doFilter(requisicao, resposta, cadeia);

        assertThat(cadeia.getRequest()).isSameAs(requisicao);
        assertThat(resposta.getHeader("Access-Control-Allow-Origin")).isEqualTo(ORIGEM_FRONTEND);
        assertThat(resposta.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
    }

    @Test
    void deveRejeitarOrigemNaoLiberada() throws ServletException, IOException {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/engine-rest/task");
        requisicao.addHeader("Origin", "http://site-desconhecido.com");
        MockHttpServletResponse resposta = new MockHttpServletResponse();

        filtro.doFilter(requisicao, resposta, new MockFilterChain());

        assertThat(resposta.getStatus()).isEqualTo(403);
        assertThat(resposta.getHeader("Access-Control-Allow-Origin")).isNull();
    }
}
