package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.MetaFinanceira;
import com.monify_api.model.entity.SituacaoMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios de MetaFinanceiraService.calcularSituacao (complexidade ciclomatica 11),
 * com casos derivados da Analise do Valor Limite.
 *
 * Meta de referencia: R$ 1.000,00, de 01/01/2026 a 11/04/2026 (prazo de 100 dias),
 * sem investimento inicial. No dia 21/02/2026 (50 dias) o progresso esperado e 50%.
 */
class MetaFinanceiraSituacaoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 4, 11);
    private static final LocalDate METADE_DO_PRAZO = INICIO.plusDays(50);

    // calcularSituacao nao usa repositorios, entao nao ha dependencias a simular
    private final MetaFinanceiraService service = new MetaFinanceiraService(null, null, null);

    private MetaFinanceira meta;

    @BeforeEach
    void criarMeta() {
        meta = new MetaFinanceira();
        meta.setNome("Reserva de emergência");
        meta.setValor(1000f);
        meta.setDataEnvio(INICIO);
        meta.setDataAlvo(FIM);
        meta.setInvestimentoInicial(null);
    }

    private SituacaoMeta situacao(double aportes, LocalDate hoje) {
        return service.calcularSituacao(meta, aportes, hoje);
    }

    // ---------- dados invalidos ----------

    @Test
    void metaNula() {
        assertThrows(RegraNegocioException.class, () -> service.calcularSituacao(null, 0, INICIO));
    }

    @Test
    void valorNulo() {
        meta.setValor(null);
        assertThrows(RegraNegocioException.class, () -> situacao(0, INICIO));
    }

    @Test
    @DisplayName("Valor da meta 0,00 (limite invalido)")
    void valorZero() {
        meta.setValor(0f);
        assertThrows(RegraNegocioException.class, () -> situacao(0, INICIO));
    }

    @Test
    void dataEnvioNula() {
        meta.setDataEnvio(null);
        assertThrows(RegraNegocioException.class, () -> situacao(0, INICIO));
    }

    @Test
    void dataAlvoNula() {
        meta.setDataAlvo(null);
        assertThrows(RegraNegocioException.class, () -> situacao(0, INICIO));
    }

    // ---------- CONCLUIDA: acumulado >= valor ----------

    @Test
    @DisplayName("Aportes exatamente iguais ao valor da meta: CONCLUIDA")
    void acumuladoIgualAoValor() {
        assertEquals(SituacaoMeta.CONCLUIDA, situacao(1000, METADE_DO_PRAZO));
    }

    @Test
    @DisplayName("Investimento inicial + aportes atingem o valor: CONCLUIDA")
    void investimentoInicialComAportes() {
        meta.setInvestimentoInicial(600f);
        assertEquals(SituacaoMeta.CONCLUIDA, situacao(400, METADE_DO_PRAZO));
    }

    @Test
    @DisplayName("Um centavo abaixo do valor, na data alvo: nao concluida (ATENCAO)")
    void umCentavoAbaixoDoValor() {
        assertEquals(SituacaoMeta.ATENCAO, situacao(999.99, FIM));
    }

    // ---------- periodo ----------

    @Test
    @DisplayName("Um dia antes do inicio: NAO_INICIADA")
    void umDiaAntesDoInicio() {
        assertEquals(SituacaoMeta.NAO_INICIADA, situacao(0, INICIO.minusDays(1)));
    }

    @Test
    @DisplayName("No dia do inicio, sem aportes: progresso esperado 0%, NO_PRAZO")
    void noDiaDoInicio() {
        assertEquals(SituacaoMeta.NO_PRAZO, situacao(0, INICIO));
    }

    @Test
    @DisplayName("No dia da data alvo com 50%: ainda no prazo, mas EM_RISCO")
    void noDiaDaDataAlvo() {
        assertEquals(SituacaoMeta.EM_RISCO, situacao(500, FIM));
    }

    @Test
    @DisplayName("Um dia depois da data alvo sem concluir: ATRASADA")
    void umDiaDepoisDaDataAlvo() {
        assertEquals(SituacaoMeta.ATRASADA, situacao(999.99, FIM.plusDays(1)));
    }

    @Test
    @DisplayName("Meta para o proprio dia (prazo zero) ainda nao concluida: EM_RISCO")
    void prazoZero() {
        meta.setDataAlvo(INICIO);
        assertEquals(SituacaoMeta.EM_RISCO, situacao(0, INICIO));
    }

    // ---------- ritmo: progresso real x esperado (50% na metade do prazo) ----------

    @Test
    @DisplayName("Progresso real 50% = esperado 50%: NO_PRAZO")
    void progressoIgualAoEsperado() {
        assertEquals(SituacaoMeta.NO_PRAZO, situacao(500, METADE_DO_PRAZO));
    }

    @Test
    @DisplayName("Progresso real 49,9% (logo abaixo do esperado): ATENCAO")
    void progressoLogoAbaixoDoEsperado() {
        assertEquals(SituacaoMeta.ATENCAO, situacao(499, METADE_DO_PRAZO));
    }

    @Test
    @DisplayName("Progresso real 40% = 80% do esperado (limite da tolerancia): ATENCAO")
    void progressoNoLimiteDaTolerancia() {
        assertEquals(SituacaoMeta.ATENCAO, situacao(400, METADE_DO_PRAZO));
    }

    @Test
    @DisplayName("Progresso real 39,9% (logo abaixo da tolerancia): EM_RISCO")
    void progressoAbaixoDaTolerancia() {
        assertEquals(SituacaoMeta.EM_RISCO, situacao(399, METADE_DO_PRAZO));
    }
}
