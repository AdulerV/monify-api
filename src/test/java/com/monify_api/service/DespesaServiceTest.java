package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.entity.Despesa;
import com.monify_api.model.entity.FormaPagamento;
import com.monify_api.model.repository.DespesaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DespesaServiceTest {
    DespesaRepository despesaRepository;
    DespesaService despesaService;

    @BeforeEach
    public void setUp() {
        despesaRepository = mock(DespesaRepository.class);
        despesaService = new DespesaService(despesaRepository, null, null);
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

}
