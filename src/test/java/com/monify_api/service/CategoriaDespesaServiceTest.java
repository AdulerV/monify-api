package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.entity.Usuario;
import com.monify_api.model.repository.CategoriaDespesaRepository;
import com.monify_api.model.repository.DespesaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoriaDespesaServiceTest {

    CategoriaDespesaRepository categoriaDespesaRepository;
    DespesaRepository despesaRepository;
    IUsuarioLogadoService usuarioLogadoService;
    CategoriaDespesaService categoriaDespesaService;

    @BeforeEach
    public void setUp() {
        categoriaDespesaRepository = mock(CategoriaDespesaRepository.class);
        despesaRepository = mock(DespesaRepository.class);
        usuarioLogadoService = mock(IUsuarioLogadoService.class);
        categoriaDespesaService = new CategoriaDespesaService(
                categoriaDespesaRepository, despesaRepository, usuarioLogadoService);
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        return usuario;
    }

    private CategoriaDespesa criarCategoriaValida() {
        CategoriaDespesa categoria = new CategoriaDespesa();
        categoria.setId(1L);
        categoria.setNome("a");
        categoria.setLimiteGasto(false);
        return categoria;
    }

    @Test
    void deveRetornarCategoriasDespesaDoUsuarioLogado() {
        Usuario usuario = criarUsuario();
        CategoriaDespesa categoria = criarCategoriaValida();
        categoria.setUsuario(usuario);
        List<CategoriaDespesa> esperadas = List.of(categoria);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(categoriaDespesaRepository.findByUsuario(usuario)).thenReturn(esperadas);

        List<CategoriaDespesa> resultado = categoriaDespesaService.getCategoriasDespesa();

        assertEquals(esperadas, resultado);
        verify(usuarioLogadoService).getUsuarioLogado();
        verify(categoriaDespesaRepository).findByUsuario(usuario);
    }

    @Test
    void deveRetornarCategoriaDespesaPorIdDoUsuarioLogado() {
        Usuario usuario = criarUsuario();
        CategoriaDespesa categoria = criarCategoriaValida();
        categoria.setUsuario(usuario);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(categoriaDespesaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoria));

        Optional<CategoriaDespesa> resultado = categoriaDespesaService.getCategoriaDespesaById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(categoria, resultado.get());
        verify(usuarioLogadoService).getUsuarioLogado();
        verify(categoriaDespesaRepository).findByIdAndUsuario(1L, usuario);
    }

    @Test
    void deveSalvarCategoriaDespesaDefinindoLimiteGastoComoFalsoQuandoNulo() {
        Usuario usuario = criarUsuario();
        CategoriaDespesa categoria = criarCategoriaValida();
        categoria.setLimiteGasto(null);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(categoriaDespesaRepository.save(categoria)).thenReturn(categoria);

        CategoriaDespesa salva = categoriaDespesaService.salvar(categoria);

        assertSame(categoria, salva);
        assertEquals(false, salva.getLimiteGasto());
        assertEquals(usuario, salva.getUsuario());
        verify(usuarioLogadoService).getUsuarioLogado();
        verify(categoriaDespesaRepository).save(categoria);
    }

    @Test
    void deveSalvarCategoriaDespesaComLimiteGastoAtivo() {
        Usuario usuario = criarUsuario();
        CategoriaDespesa categoria = criarCategoriaValida();
        categoria.setLimiteGasto(true);
        categoria.setValorLimite(0.01f);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(categoriaDespesaRepository.save(categoria)).thenReturn(categoria);

        CategoriaDespesa salva = categoriaDespesaService.salvar(categoria);

        assertSame(categoria, salva);
        assertEquals(true, salva.getLimiteGasto());
        assertEquals(0.01f, salva.getValorLimite());
        assertEquals(usuario, salva.getUsuario());
        verify(usuarioLogadoService).getUsuarioLogado();
        verify(categoriaDespesaRepository).save(categoria);
    }

    @Test
    public void deveImpedirExclusaoQuandoCategoriaPossuiDespesasVinculadas() {
        try {
            CategoriaDespesa categoria = criarCategoriaValida();
            when(despesaRepository.existsByCategoriaDespesa(categoria)).thenReturn(true);

            categoriaDespesaService.excluir(categoria);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Categoria possui despesas vinculadas", e.getMessage());
            verify(categoriaDespesaRepository, never()).delete(any());
        }
    }

    @Test
    void deveExcluirCategoriaQuandoNaoPossuiDespesasVinculadas() {
        CategoriaDespesa categoria = criarCategoriaValida();
        when(despesaRepository.existsByCategoriaDespesa(categoria)).thenReturn(false);

        categoriaDespesaService.excluir(categoria);

        verify(despesaRepository).existsByCategoriaDespesa(categoria);
        verify(categoriaDespesaRepository).delete(categoria);
    }
}
