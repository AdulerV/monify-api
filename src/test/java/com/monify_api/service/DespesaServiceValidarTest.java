package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.entity.Despesa;
import com.monify_api.model.entity.FormaPagamento;
import com.monify_api.model.repository.DespesaRepository;
import com.monify_api.model.repository.ParcelaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios de DespesaService.validar (complexidade ciclomatica 12),
 * com casos derivados da Analise do Valor Limite. O DespesaRepository e um objeto mock:
 * a soma das despesas do mes e simulada, sem banco de dados.
 */
@ExtendWith(MockitoExtension.class)
class DespesaServiceValidarTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 10);

    @Mock
    private DespesaRepository repository;

    @Mock
    private ParcelaRepository parcelaRepository;

    @Mock
    private UsuarioLogadoService usuarioLogadoService;

    @InjectMocks
    private DespesaService service;

    private Despesa despesa;
    private CategoriaDespesa categoria;

    @BeforeEach
    void criarDespesaValida() {
        FormaPagamento forma = new FormaPagamento();
        forma.setId(1L);
        forma.setNome("Pix");

        categoria = new CategoriaDespesa(false, null);
        categoria.setId(1L);
        categoria.setNome("Lazer");

        despesa = new Despesa();
        despesa.setNome("Cinema");
        despesa.setData(DATA);
        despesa.setValor(100f);
        despesa.setFormaPagamento(forma);
        despesa.setCategoriaDespesa(categoria);
        despesa.setParcelada(false);
    }

    private void assertInvalida(String mensagemEsperada) {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> service.validar(despesa));
        assertTrue(erro.getMessage().startsWith(mensagemEsperada), erro.getMessage());
    }

    // ---------- caminho feliz ----------

    @Test
    @DisplayName("Despesa valida sem parcelamento e sem limite: nao consulta o repositorio")
    void despesaValidaSemLimite() {
        assertDoesNotThrow(() -> service.validar(despesa));
        verifyNoInteractions(repository);
    }

    // ---------- nome ----------

    @Test
    void nomeNulo() {
        despesa.setNome(null);
        assertInvalida("Nome inválido");
    }

    @Test
    void nomeEmBranco() {
        despesa.setNome("   ");
        assertInvalida("Nome inválido");
    }

    @Test
    @DisplayName("Nome com 1 caractere (limite inferior valido)")
    void nomeComUmCaractere() {
        despesa.setNome("x");
        assertDoesNotThrow(() -> service.validar(despesa));
    }

    @Test
    @DisplayName("Nome com 255 caracteres (limite superior valido)")
    void nomeNoLimiteMaximo() {
        despesa.setNome("a".repeat(DespesaService.TAMANHO_MAXIMO_NOME));
        assertDoesNotThrow(() -> service.validar(despesa));
    }

    @Test
    @DisplayName("Nome com 256 caracteres (logo acima do limite)")
    void nomeAcimaDoLimite() {
        despesa.setNome("a".repeat(DespesaService.TAMANHO_MAXIMO_NOME + 1));
        assertInvalida("Nome deve ter no máximo 255 caracteres");
    }

    // ---------- data e valor ----------

    @Test
    void dataNula() {
        despesa.setData(null);
        assertInvalida("Data inválida");
    }

    @Test
    void valorNulo() {
        despesa.setValor(null);
        assertInvalida("Valor inválido");
    }

    @Test
    @DisplayName("Valor 0,00 (limite invalido)")
    void valorZero() {
        despesa.setValor(0f);
        assertInvalida("Valor inválido");
    }

    @Test
    @DisplayName("Valor 0,01 (menor valor valido)")
    void valorUmCentavo() {
        despesa.setValor(0.01f);
        assertDoesNotThrow(() -> service.validar(despesa));
    }

    // ---------- forma de pagamento ----------

    @Test
    void formaPagamentoNula() {
        despesa.setFormaPagamento(null);
        assertInvalida("Forma de pagamento inválida");
    }

    @Test
    void formaPagamentoSemId() {
        despesa.getFormaPagamento().setId(null);
        assertInvalida("Forma de pagamento inválida");
    }

    @Test
    void formaPagamentoIdZero() {
        despesa.getFormaPagamento().setId(0L);
        assertInvalida("Forma de pagamento inválida");
    }

    // ---------- categoria ----------

    @Test
    void categoriaNula() {
        despesa.setCategoriaDespesa(null);
        assertInvalida("Categoria de despesa inválida");
    }

    @Test
    void categoriaSemId() {
        categoria.setId(null);
        assertInvalida("Categoria de despesa inválida");
    }

    @Test
    void categoriaIdZero() {
        categoria.setId(0L);
        assertInvalida("Categoria de despesa inválida");
    }

    // ---------- parcelamento (2 a 120 parcelas) ----------

    @Test
    void parceladaSemQuantidade() {
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(null);
        assertInvalida("Quantidade de parcelas inválida");
    }

    @Test
    @DisplayName("1 parcela (logo abaixo do minimo)")
    void parceladaComUmaParcela() {
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(1);
        assertInvalida("Quantidade de parcelas inválida");
    }

    @Test
    @DisplayName("2 parcelas (minimo valido)")
    void parceladaComDuasParcelas() {
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(2);
        assertDoesNotThrow(() -> service.validar(despesa));
    }

    @Test
    @DisplayName("120 parcelas (maximo valido)")
    void parceladaComCentoEVinteParcelas() {
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(120);
        assertDoesNotThrow(() -> service.validar(despesa));
    }

    @Test
    @DisplayName("121 parcelas (logo acima do maximo)")
    void parceladaComCentoEVinteEUmaParcelas() {
        despesa.setParcelada(true);
        despesa.setQuantidadeParcelas(121);
        assertInvalida("Quantidade de parcelas inválida");
    }

    // ---------- limite de gasto mensal da categoria (usa o mock do repositorio) ----------

    @Test
    @DisplayName("Categoria com limite ativo mas sem valor limite: nao consulta o repositorio")
    void limiteAtivoSemValor() {
        categoria.setLimiteGasto(true);
        categoria.setValorLimite(null);
        assertDoesNotThrow(() -> service.validar(despesa));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Total do mes igual ao limite (400 + 100 = 500): aceita")
    void totalIgualAoLimite() {
        categoria.setLimiteGasto(true);
        categoria.setValorLimite(500f);
        when(repository.somarValorPorCategoriaNoPeriodo(eq(categoria), any(), any(), anyLong())).thenReturn(400.0);

        assertDoesNotThrow(() -> service.validar(despesa));
    }

    @Test
    @DisplayName("Total do mes 0,01 acima do limite (400 + 100,01): recusa")
    void totalUmCentavoAcimaDoLimite() {
        categoria.setLimiteGasto(true);
        categoria.setValorLimite(500f);
        despesa.setValor(100.01f);
        when(repository.somarValorPorCategoriaNoPeriodo(eq(categoria), any(), any(), anyLong())).thenReturn(400.0);

        assertInvalida("Limite de gasto mensal da categoria Lazer excedido");
    }

    @Test
    @DisplayName("Despesa nova: soma o mes inteiro sem ignorar nenhuma despesa (id -1)")
    void despesaNovaConsultaMesInteiro() {
        categoria.setLimiteGasto(true);
        categoria.setValorLimite(500f);
        when(repository.somarValorPorCategoriaNoPeriodo(any(), any(), any(), anyLong())).thenReturn(0.0);

        service.validar(despesa);

        verify(repository).somarValorPorCategoriaNoPeriodo(
                categoria, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), -1L);
    }

    @Test
    @DisplayName("Alteracao de despesa: a propria despesa e desconsiderada na soma do mes")
    void despesaExistenteIgnoraElaMesma() {
        categoria.setLimiteGasto(true);
        categoria.setValorLimite(500f);
        despesa.setId(7L);
        when(repository.somarValorPorCategoriaNoPeriodo(any(), any(), any(), anyLong())).thenReturn(0.0);

        service.validar(despesa);

        verify(repository).somarValorPorCategoriaNoPeriodo(
                categoria, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 7L);
    }
}
