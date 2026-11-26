package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaReceita;
import com.monify_api.model.entity.Usuario;
import com.monify_api.model.repository.CategoriaReceitaRepository;
import com.monify_api.model.repository.ReceitaRepository;
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

class CategoriaReceitaServiceTest {

    CategoriaReceitaRepository categoriaReceitaRepository;
    ReceitaRepository receitaRepository;
    IUsuarioLogadoService usuarioLogadoService;
    CategoriaReceitaService categoriaReceitaService;

    @BeforeEach
    public void setUp() {
        categoriaReceitaRepository = mock(CategoriaReceitaRepository.class);
        receitaRepository = mock(ReceitaRepository.class);
        usuarioLogadoService = mock(IUsuarioLogadoService.class);
        categoriaReceitaService = new CategoriaReceitaService(
                categoriaReceitaRepository, receitaRepository, usuarioLogadoService);
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        return usuario;
    }

    private CategoriaReceita criarCategoriaValida() {
        CategoriaReceita categoria = new CategoriaReceita();
        categoria.setId(1L);
        categoria.setNome("a");
        return categoria;
    }

    @Test
    void deveRetornarCategoriasReceitaDoUsuarioLogado() {
        Usuario usuario = criarUsuario();
        CategoriaReceita categoria = criarCategoriaValida();
        categoria.setUsuario(usuario);
        List<CategoriaReceita> esperadas = List.of(categoria);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(categoriaReceitaRepository.findByUsuario(usuario)).thenReturn(esperadas);

        List<CategoriaReceita> resultado = categoriaReceitaService.getCategoriasReceita();

        assertEquals(esperadas, resultado);
        verify(usuarioLogadoService).getUsuarioLogado();
        verify(categoriaReceitaRepository).findByUsuario(usuario);
    }

    @Test
    void deveRetornarCategoriaReceitaPorIdDoUsuarioLogado() {
        Usuario usuario = criarUsuario();
        CategoriaReceita categoria = criarCategoriaValida();
        categoria.setUsuario(usuario);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(categoriaReceitaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoria));

        Optional<CategoriaReceita> resultado = categoriaReceitaService.getCategoriaReceitaById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(categoria, resultado.get());
        verify(usuarioLogadoService).getUsuarioLogado();
        verify(categoriaReceitaRepository).findByIdAndUsuario(1L, usuario);
    }

    @Test
    void deveSalvarCategoriaReceitaAssociandoUsuarioLogado() {
        Usuario usuario = criarUsuario();
        CategoriaReceita categoria = criarCategoriaValida();
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(categoriaReceitaRepository.save(categoria)).thenReturn(categoria);

        CategoriaReceita salva = categoriaReceitaService.salvar(categoria);

        assertSame(categoria, salva);
        assertEquals(usuario, salva.getUsuario());
        verify(usuarioLogadoService).getUsuarioLogado();
        verify(categoriaReceitaRepository).save(categoria);
    }

    @Test
    public void deveImpedirExclusaoQuandoCategoriaPossuiReceitasVinculadas() {
        try {
            CategoriaReceita categoria = criarCategoriaValida();
            when(receitaRepository.existsByCategoriaReceita(categoria)).thenReturn(true);

            categoriaReceitaService.excluir(categoria);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Categoria possui receitas vinculadas", e.getMessage());
            verify(categoriaReceitaRepository, never()).delete(any());
        }
    }

    @Test
    void deveExcluirCategoriaQuandoNaoPossuiReceitasVinculadas() {
        CategoriaReceita categoria = criarCategoriaValida();
        when(receitaRepository.existsByCategoriaReceita(categoria)).thenReturn(false);

        categoriaReceitaService.excluir(categoria);

        verify(receitaRepository).existsByCategoriaReceita(categoria);
        verify(categoriaReceitaRepository).delete(categoria);
    }
}
