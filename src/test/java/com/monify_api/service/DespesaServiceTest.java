package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.entity.Despesa;
import com.monify_api.model.entity.FormaPagamento;
import com.monify_api.model.entity.Parcela;
import com.monify_api.model.entity.Usuario;
import com.monify_api.model.repository.DespesaRepository;
import com.monify_api.model.repository.ParcelaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DespesaServiceTest {
    DespesaRepository despesaRepository;
    ParcelaRepository parcelaRepository;
    UsuarioLogadoService usuarioLogadoService;
    DespesaService despesaService;

    @BeforeEach
    public void setUp() {
        despesaRepository = mock(DespesaRepository.class);
        parcelaRepository = mock(ParcelaRepository.class);
        usuarioLogadoService = mock(UsuarioLogadoService.class);
        despesaService = new DespesaService(despesaRepository, parcelaRepository, usuarioLogadoService);
    }

    // MÉTODO DE MAIOR COMPLEXIDADE - validar()

    private Despesa criarDespesaValida() {
        FormaPagamento formaPagamento = new FormaPagamento();
        formaPagamento.setId(1L);

        CategoriaDespesa categoria = new CategoriaDespesa();
        categoria.setId(1L);
        categoria.setLimiteGasto(false);

        Despesa despesa = new Despesa();
        despesa.setNome("a");
        despesa.setData(LocalDate.of(2000, 1, 1));
        despesa.setValor(0.01f);
        despesa.setFormaPagamento(formaPagamento);
        despesa.setCategoriaDespesa(categoria);
        despesa.setParcelada(false);
        return despesa;
    }

    @Test
    void deveAceitarDespesaValida() {
        Despesa despesa = criarDespesaValida();
        despesa.setNome("a".repeat(255));

        assertDoesNotThrow(() -> despesaService.validar(despesa));
    }

    @Test
    public void deveImpedirDespesaComNomeNulo() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setNome(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Nome inválido", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComNomeVazio() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setNome("");
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Nome inválido", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComNomeMaiorQueOLimite() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setNome("a".repeat(256));
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Nome deve ter no máximo 255 caracteres", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComDataNula() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setData(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Data inválida", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComValorNulo() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setValor(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Valor inválido", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComValorIgualZeroOuNegativo() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setValor(0.0f);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Valor inválido", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComFormaPagamentoNula() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setFormaPagamento(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Forma de pagamento inválida", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComFormaPagamentoSemId() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.getFormaPagamento().setId(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Forma de pagamento inválida", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComFormaPagamentoComIdZero() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.getFormaPagamento().setId(0L);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Forma de pagamento inválida", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComCategoriaNula() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setCategoriaDespesa(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Categoria de despesa inválida", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComCategoriaSemId() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.getCategoriaDespesa().setId(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Categoria de despesa inválida", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaComCategoriaComIdZero() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.getCategoriaDespesa().setId(0L);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Categoria de despesa inválida", e.getMessage());
        }
    }

    @Test
    void deveAceitarDespesaParceladaComQuantidadeMinima() {
        Despesa despesa = criarDespesaValida();
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(2);

        assertDoesNotThrow(() -> despesaService.validar(despesa));
    }

    @Test
    public void deveImpedirDespesaParceladaComQuantidadeNula() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setParcelada(true);
            despesa.setQuantidadeParcelas(null);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Quantidade de parcelas inválida (informe um número entre 2 e 120)", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaParceladaComQuantidadeMenorQueOMinimo() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setParcelada(true);
            despesa.setQuantidadeParcelas(1);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Quantidade de parcelas inválida (informe um número entre 2 e 120)", e.getMessage());
        }
    }

    @Test
    public void deveImpedirDespesaParceladaComQuantidadeMaiorQueOMaximo() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setParcelada(true);
            despesa.setQuantidadeParcelas(121);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Quantidade de parcelas inválida (informe um número entre 2 e 120)", e.getMessage());
        }
    }

    @Test
    void deveAceitarDespesaComTotalIgualAoLimiteDeGasto() {
        Despesa despesa = criarDespesaValida();
        despesa.setValor(1f);
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(120);
        despesa.getCategoriaDespesa().setLimiteGasto(true);
        despesa.getCategoriaDespesa().setValorLimite(1f);
        when(despesaRepository.somarValorPorCategoriaNoPeriodo(any(), any(), any(), any())).thenReturn(0.0);

        assertDoesNotThrow(() -> despesaService.validar(despesa));
        verify(despesaRepository).somarValorPorCategoriaNoPeriodo(any(), any(), any(), eq(-1L));
    }

    @Test
    void deveAceitarDespesaComLimiteDeGastoSemValorLimite() {
        Despesa despesa = criarDespesaValida();
        despesa.getCategoriaDespesa().setLimiteGasto(true);
        despesa.getCategoriaDespesa().setValorLimite(null);

        assertDoesNotThrow(() -> despesaService.validar(despesa));
        verify(despesaRepository, never()).somarValorPorCategoriaNoPeriodo(any(), any(), any(), any());
    }

    @Test
    void deveIgnorarADespesaAlteradaNoCalculoDoLimite() {
        Despesa despesa = criarDespesaValida();
        despesa.setId(1L);
        despesa.setValor(1f);
        despesa.getCategoriaDespesa().setLimiteGasto(true);
        despesa.getCategoriaDespesa().setValorLimite(1f);
        when(despesaRepository.somarValorPorCategoriaNoPeriodo(any(), any(), any(), any())).thenReturn(0.0);

        assertDoesNotThrow(() -> despesaService.validar(despesa));
        verify(despesaRepository).somarValorPorCategoriaNoPeriodo(any(), any(), any(), eq(1L));
    }

    @Test
    public void deveImpedirDespesaComTotalMaiorQueLimiteDeGasto() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setValor(1f);
            despesa.getCategoriaDespesa().setNome("Alimentação");
            despesa.getCategoriaDespesa().setLimiteGasto(true);
            despesa.getCategoriaDespesa().setValorLimite(1f);
            when(despesaRepository.somarValorPorCategoriaNoPeriodo(any(), any(), any(), any())).thenReturn(0.01);
            despesaService.validar(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals(String.format("Limite de gasto mensal da categoria %s excedido (limite: %.2f, total no mês: %.2f)",
                    "Alimentação", 1f, 1.01), e.getMessage());
        }
    }

    // RESTANTES DOS MÉTODOS DA CLASSE Despesa

    // getDespesasByCategoria() - 0 condições + 1 = 1 teste

    @Test
    void deveRetornarDespesasPorCategoria() {
        CategoriaDespesa categoria = new CategoriaDespesa();
        categoria.setId(1L);
        Despesa despesa = criarDespesaValida();
        despesa.setCategoriaDespesa(categoria);
        List<Despesa> esperadas = List.of(despesa);
        when(despesaRepository.findByCategoriaDespesa(categoria)).thenReturn(esperadas);

        List<Despesa> resultado = despesaService.getDespesasByCategoria(categoria);

        assertEquals(esperadas, resultado);
        verify(despesaRepository).findByCategoriaDespesa(categoria);
    }

    // getParcelas() - 0 condições + 1 = 1 teste

    @Test
    void deveRetornarParcelasDaDespesa() {
        Despesa despesa = criarDespesaValida();
        despesa.setId(1L);
        List<Parcela> esperadas = List.of(new Parcela(1L, 0.01f, false, despesa));
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(esperadas);

        List<Parcela> resultado = despesaService.getParcelas(despesa);

        assertEquals(esperadas, resultado);
        verify(parcelaRepository).findByDespesaOrderByIdAsc(despesa);
    }

    // salvar() - 1 condição + 1 = 2 testes

    @Test
    void deveSalvarDespesaDefinindoParceladaComoFalsoQuandoNaoForVerdadeiro() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Despesa despesa = criarDespesaValida();
        despesa.setParcelada(null);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(despesaRepository.save(despesa)).thenReturn(despesa);
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(Collections.emptyList());

        Despesa salva = despesaService.salvar(despesa);

        assertSame(despesa, salva);
        assertEquals(false, salva.getParcelada());
        assertEquals(usuario, salva.getUsuario());
        verify(despesaRepository).save(despesa);
        verify(parcelaRepository, never()).saveAll(anyList());
    }

    @Test
    void deveSalvarDespesaParceladaMantendoParceladaComoVerdadeiro() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Despesa despesa = criarDespesaValida();
        despesa.setValor(0.02f);
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(2);
        when(usuarioLogadoService.getUsuarioLogado()).thenReturn(usuario);
        when(despesaRepository.save(despesa)).thenReturn(despesa);
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(Collections.emptyList());

        Despesa salva = despesaService.salvar(despesa);

        assertSame(despesa, salva);
        assertEquals(true, salva.getParcelada());
        assertEquals(usuario, salva.getUsuario());
        verify(despesaRepository).save(despesa);
        verify(parcelaRepository).saveAll(List.of(
                new Parcela(null, 0.01f, false, despesa),
                new Parcela(null, 0.01f, false, despesa)
        ));
    }

    // excluir() - 0 condições + 1 = 1 teste

    @Test
    void deveExcluirDespesaESuasParcelas() {
        Despesa despesa = criarDespesaValida();
        despesa.setId(1L);
        List<Parcela> parcelas = List.of(new Parcela(1L, 0.01f, false, despesa));
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(parcelas);

        despesaService.excluir(despesa);

        verify(parcelaRepository).findByDespesaOrderByIdAsc(despesa);
        verify(parcelaRepository).deleteAll(parcelas);
        verify(despesaRepository).delete(despesa);
    }

    // sincronizarParcelas() - 5 condições + 1 = 6 testes

    @Test
    void deveManterSemAlteracaoQuandoDespesaNaoParceladaESemParcelasAtuais() {
        Despesa despesa = criarDespesaValida();
        despesa.setParcelada(false);
        despesa.setValor(0.01f);
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(Collections.emptyList());

        despesaService.sincronizarParcelas(despesa);

        verify(parcelaRepository, never()).deleteAll(anyList());
        verify(parcelaRepository, never()).saveAll(anyList());
    }

    @Test
    void deveManterParcelasQuandoQuantidadeESomaConferem() {
        Despesa despesa = criarDespesaValida();
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(2);
        despesa.setValor(10.009f);
        List<Parcela> atuais = List.of(
                new Parcela(1L, 5.0f, false, despesa),
                new Parcela(2L, 5.0f, false, despesa)
        );
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(atuais);

        despesaService.sincronizarParcelas(despesa);

        verify(parcelaRepository, never()).deleteAll(anyList());
        verify(parcelaRepository, never()).saveAll(anyList());
    }

    @Test
    public void deveImpedirSincronizacaoQuandoSomaNaoConfereEHaParcelaPaga() {
        try {
            Despesa despesa = criarDespesaValida();
            despesa.setParcelada(true);
            despesa.setQuantidadeParcelas(2);
            despesa.setValor(10.01f);
            List<Parcela> atuais = List.of(
                    new Parcela(1L, 5.0f, true, despesa),
                    new Parcela(2L, 5.0f, false, despesa)
            );
            when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(atuais);

            despesaService.sincronizarParcelas(despesa);
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Despesa possui parcelas pagas: não é possível alterar o valor ou o parcelamento",
                    e.getMessage());
            verify(parcelaRepository, never()).deleteAll(anyList());
            verify(parcelaRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void deveRegerarParcelasQuandoSomaNaoConfereESemParcelasPagas() {
        Despesa despesa = criarDespesaValida();
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(2);
        despesa.setValor(10.01f);
        List<Parcela> atuais = List.of(
                new Parcela(1L, 5.0f, false, despesa),
                new Parcela(2L, 5.0f, false, despesa)
        );
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(atuais);

        despesaService.sincronizarParcelas(despesa);

        verify(parcelaRepository).deleteAll(atuais);
        verify(parcelaRepository).saveAll(List.of(
                new Parcela(null, 5.00f, false, despesa),
                new Parcela(null, 5.01f, false, despesa)
        ));
    }

    @Test
    void deveGerarParcelasQuandoQuantidadeDifereDasAtuaisESemParcelasPagas() {
        Despesa despesa = criarDespesaValida();
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(2);
        despesa.setValor(0.02f);
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(Collections.emptyList());

        despesaService.sincronizarParcelas(despesa);

        verify(parcelaRepository).deleteAll(Collections.emptyList());
        verify(parcelaRepository).saveAll(List.of(
                new Parcela(null, 0.01f, false, despesa),
                new Parcela(null, 0.01f, false, despesa)
        ));
    }

    @Test
    void deveExcluirParcelasSemGerarNovasQuandoDespesaDeixaDeSerParcelada() {
        Despesa despesa = criarDespesaValida();
        despesa.setParcelada(false);
        despesa.setValor(10.0f);
        List<Parcela> atuais = List.of(
                new Parcela(1L, 5.0f, false, despesa),
                new Parcela(2L, 5.0f, false, despesa)
        );
        when(parcelaRepository.findByDespesaOrderByIdAsc(despesa)).thenReturn(atuais);

        despesaService.sincronizarParcelas(despesa);

        verify(parcelaRepository).deleteAll(atuais);
        verify(parcelaRepository, never()).saveAll(anyList());
    }

    // somaConfere() - 2 condições + 1 = 3 testes

    @Test
    void deveConfirmarSomaQuandoListaDeParcelasVazia() {
        boolean resultado = despesaService.somaConfere(Collections.emptyList(), 0.01f);

        assertTrue(resultado);
    }

    @Test
    void deveConfirmarSomaQuandoDiferencaMenorQueUmCentavo() {
        List<Parcela> parcelas = List.of(
                new Parcela(1L, 5.0f, false, null),
                new Parcela(2L, 5.0f, false, null)
        );

        boolean resultado = despesaService.somaConfere(parcelas, 10.009f);

        assertTrue(resultado);
    }

    @Test
    void deveRecusarSomaQuandoDiferencaIgualOuMaiorQueUmCentavo() {
        List<Parcela> parcelas = List.of(
                new Parcela(1L, 5.0f, false, null),
                new Parcela(2L, 5.0f, false, null)
        );

        boolean resultado = despesaService.somaConfere(parcelas, 10.01f);

        assertFalse(resultado);
    }

    // gerarParcelas() - 2 condições + 1 = 3 testes

    @Test
    void deveGerarUmaParcelaComValorTotalQuandoQuantidadeIgualAUm() {
        Despesa despesa = criarDespesaValida();
        despesa.setValor(0.01f);

        List<Parcela> parcelas = despesaService.gerarParcelas(despesa, 1);

        assertEquals(1, parcelas.size());
        assertNull(parcelas.get(0).getId());
        assertEquals(0.01f, parcelas.get(0).getValorParcela());
        assertEquals(false, parcelas.get(0).getPago());
        assertSame(despesa, parcelas.get(0).getDespesa());
    }

    @Test
    void deveGerarParcelasNaQuantidadeMinimaComRestoNaUltimaParcela() {
        Despesa despesa = criarDespesaValida();
        despesa.setValor(0.03f);

        List<Parcela> parcelas = despesaService.gerarParcelas(despesa, 2);

        assertEquals(2, parcelas.size());
        assertEquals(0.01f, parcelas.get(0).getValorParcela());
        assertEquals(0.02f, parcelas.get(1).getValorParcela());
        assertEquals(false, parcelas.get(0).getPago());
        assertEquals(false, parcelas.get(1).getPago());
        assertSame(despesa, parcelas.get(0).getDespesa());
        assertSame(despesa, parcelas.get(1).getDespesa());
    }

    @Test
    void deveGerarParcelasNaQuantidadeMaximaComDivisaoExata() {
        Despesa despesa = criarDespesaValida();
        despesa.setValor(1.20f);

        List<Parcela> parcelas = despesaService.gerarParcelas(despesa, 120);

        assertEquals(120, parcelas.size());
        assertEquals(0.01f, parcelas.get(0).getValorParcela());
        assertEquals(0.01f, parcelas.get(119).getValorParcela());
        assertTrue(parcelas.stream().allMatch(p ->
                p.getId() == null
                        && Float.valueOf(0.01f).equals(p.getValorParcela())
                        && Boolean.FALSE.equals(p.getPago())
                        && p.getDespesa() == despesa));
    }

}
