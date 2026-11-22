package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.MetaFinanceira;
import com.monify_api.model.entity.SituacaoMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MetaFinanceiraServiceTest {
    MetaFinanceiraService metaFinanceiraService;

    @BeforeEach
    public void setUp() {
        metaFinanceiraService = new MetaFinanceiraService(null, null, null);
    }

    // MÉTODO DE MAIOR COMPLEXIDADE - calcularSituacao()

    private MetaFinanceira criarMetaFinanceiraValida() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValor(0.01f);
        meta.setInvestimentoInicial(null);
        meta.setDataEnvio(LocalDate.of(-999999999, 1, 1));
        meta.setDataAlvo(LocalDate.of(-999999999, 1, 1));
        return meta;
    }

    @Test
    public void deveImpedirMetaFinanceiraNula() {
        try {
            metaFinanceiraService.calcularSituacao(null, 0, LocalDate.of(2000, 1, 1));
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Meta financeira não informada", e.getMessage());
        }
    }

    @Test
    public void deveImpedirMetaFinanceiraComValorNulo() {
        try {
            MetaFinanceira metaFinanceira = criarMetaFinanceiraValida();
            metaFinanceira.setValor(null);
            metaFinanceiraService.calcularSituacao(metaFinanceira, 0, LocalDate.of(2000, 1, 1));
            fail();
        } catch (RegraNegocioException e) {
            assertEquals("Valor da meta inválido", e.getMessage());
        }
    }

    @Test
    public void deveImpedirMetaFinanceiraComValorIgualZeroOuNegativo() {
        try {
            MetaFinanceira metaFinanceira = criarMetaFinanceiraValida();
            metaFinanceira.setValor(0.0f);
            metaFinanceiraService.calcularSituacao(metaFinanceira, 0, LocalDate.of(2000, 1, 1));
            fail();
        }catch (RegraNegocioException e) {
            assertEquals("Valor da meta inválido", e.getMessage());
        }
    }

    @Test
    public void deveImpedirMetaFinanceiraComDataEnvioNula() {
        try {
            MetaFinanceira metaFinanceira = criarMetaFinanceiraValida();
            metaFinanceira.setDataEnvio(null);
            metaFinanceiraService.calcularSituacao(metaFinanceira, 0, LocalDate.of(2000, 1, 1));
            fail();
        }catch (RegraNegocioException e) {
            assertEquals("Período da meta inválido", e.getMessage());
        }
    }

    @Test
    public void deveImpedirMetaFinanceiraComDataAlvoNula() {
        try {
            MetaFinanceira metaFinanceira = criarMetaFinanceiraValida();
            metaFinanceira.setDataAlvo(null);
            metaFinanceiraService.calcularSituacao(metaFinanceira, 0, LocalDate.of(2000, 1, 1));
            fail();
        }catch (RegraNegocioException e) {
            assertEquals("Período da meta inválido", e.getMessage());
        }
    }

    @Test
    void deveTratarInvestimentoInicialNuloComoZero() {
        MetaFinanceira meta = criarMetaFinanceiraValida();
        meta.setInvestimentoInicial(null);

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0.01, LocalDate.of(2000, 1, 1));
        assertEquals(SituacaoMeta.CONCLUIDA, situacao);
    }

    @Test
    void deveTratarInvestimentoInicialComoInvestimentoInicial() {
        MetaFinanceira meta = criarMetaFinanceiraValida();
        meta.setInvestimentoInicial(0.01f);

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0, LocalDate.of(2000, 1, 1));
        assertEquals(SituacaoMeta.CONCLUIDA, situacao);
    }

    @Test
    void deveInterpretarSituacaoComoNaoIniciada() {
        MetaFinanceira meta = criarMetaFinanceiraValida();
        meta.setDataEnvio(LocalDate.of(-999999999, 1, 2));
        meta.setDataAlvo(LocalDate.of(-999999999, 1, 2));

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0, LocalDate.of(-999999999, 1, 1));
        assertEquals(SituacaoMeta.NAO_INICIADA, situacao);
    }

    @Test
    void deveInterpretarSituacaoComoAtrasada() {
        MetaFinanceira meta = criarMetaFinanceiraValida();

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0, LocalDate.of(-999999999, 1, 2));
        assertEquals(SituacaoMeta.ATRASADA, situacao);
    }

    @Test
    void deveInterpretarSituacaoComoEmRisco() {
        MetaFinanceira meta = criarMetaFinanceiraValida();

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0, LocalDate.of(-999999999, 1, 1));
        assertEquals(SituacaoMeta.EM_RISCO, situacao);
    }

    @Test
    void deveInterpretarSituacaoComoNoPrazo() {
        MetaFinanceira meta = criarMetaFinanceiraValida();
        meta.setDataAlvo(LocalDate.of(-999999999, 1, 2));

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0, LocalDate.of(-999999999, 1, 1));
        assertEquals(SituacaoMeta.NO_PRAZO, situacao);
    }

    @Test
    void deveInterpretarSituacaoComoAtencao() {
        MetaFinanceira meta = criarMetaFinanceiraValida();
        meta.setValor(1f);
        meta.setDataAlvo(LocalDate.of(-999999999, 1, 9));

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0.1, LocalDate.of(-999999999, 1, 2));
        assertEquals(SituacaoMeta.ATENCAO, situacao);
    }

    @Test
    void deveInterpretarSituacaoComoEmRiscoComProgressoAbaixoDeAtencao() {
        MetaFinanceira meta = criarMetaFinanceiraValida();
        meta.setValor(1f);
        meta.setDataAlvo(LocalDate.of(-999999999, 1, 9));

        SituacaoMeta situacao = metaFinanceiraService.calcularSituacao(meta, 0.09, LocalDate.of(-999999999, 1, 2));
        assertEquals(SituacaoMeta.EM_RISCO, situacao);
    }

    // RESTANTES DOS MÉTODOS DA CLASSE MetaFinanceira

}