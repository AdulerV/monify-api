package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.MetaFinanceira;
import com.monify_api.model.entity.SituacaoMeta;
import com.monify_api.model.entity.Aporte;
import com.monify_api.model.entity.Usuario;
import com.monify_api.model.repository.AporteRepository;
import com.monify_api.model.repository.MetaFinanceiraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

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

    private MetaFinanceira criarMetaValidaParaValidacao() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setNome("Comprar um carro");
        meta.setValor(50000f);
        meta.setDataEnvio(LocalDate.of(2026, 1, 1));
        meta.setDataAlvo(LocalDate.of(2027, 1, 1));
        meta.setInvestimentoInicial(1000f);
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

    @Test
    public void deveRetornarMetasDoUsuarioLogado() {
        Usuario usuario = new Usuario();

        MetaFinanceira meta1 = new MetaFinanceira();
        meta1.setNome("Comprar um carro");

        MetaFinanceira meta2 = new MetaFinanceira();
        meta2.setNome("Viagem de férias");

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        UsuarioLogadoService usuarioLogadoService =
                mock(UsuarioLogadoService.class);

        when(usuarioLogadoService.getUsuarioLogado())
                .thenReturn(usuario);

        when(repository.findByUsuario(usuario))
                .thenReturn(List.of(meta1, meta2));

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, null, usuarioLogadoService
                );

        List<MetaFinanceira> resultado =
                service.getMetasFinanceiras();

        assertEquals(2, resultado.size());
        assertEquals(meta1, resultado.get(0));
        assertEquals(meta2, resultado.get(1));

        verify(usuarioLogadoService).getUsuarioLogado();
        verify(repository).findByUsuario(usuario);
    }

    @Test
    public void deveRetornarListaVaziaQuandoUsuarioNaoPossuirMetas() {
        Usuario usuario = new Usuario();

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        UsuarioLogadoService usuarioLogadoService =
                mock(UsuarioLogadoService.class);

        when(usuarioLogadoService.getUsuarioLogado())
                .thenReturn(usuario);

        when(repository.findByUsuario(usuario))
                .thenReturn(List.of());

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, null, usuarioLogadoService
                );

        List<MetaFinanceira> resultado =
                service.getMetasFinanceiras();

        assertTrue(resultado.isEmpty());

        verify(repository).findByUsuario(usuario);
    }

    // MÉTODO getMetaFinanceiraById()

    @Test
    public void deveRetornarMetaFinanceiraPorId() {
        Long id = 1L;

        Usuario usuario = new Usuario();

        MetaFinanceira meta = new MetaFinanceira();
        meta.setId(id);
        meta.setNome("Comprar um carro");

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        UsuarioLogadoService usuarioLogadoService =
                mock(UsuarioLogadoService.class);

        when(usuarioLogadoService.getUsuarioLogado())
                .thenReturn(usuario);

        when(repository.findByIdAndUsuario(id, usuario))
                .thenReturn(Optional.of(meta));

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, null, usuarioLogadoService
                );

        Optional<MetaFinanceira> resultado =
                service.getMetaFinanceiraById(id);

        assertTrue(resultado.isPresent());
        assertEquals(meta, resultado.get());
        assertEquals(id, resultado.get().getId());

        verify(repository).findByIdAndUsuario(id, usuario);
    }

    @Test
    public void deveRetornarVazioQuandoMetaNaoForEncontrada() {
        Long id = 99L;

        Usuario usuario = new Usuario();

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        UsuarioLogadoService usuarioLogadoService =
                mock(UsuarioLogadoService.class);

        when(usuarioLogadoService.getUsuarioLogado())
                .thenReturn(usuario);

        when(repository.findByIdAndUsuario(id, usuario))
                .thenReturn(Optional.empty());

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, null, usuarioLogadoService
                );

        Optional<MetaFinanceira> resultado =
                service.getMetaFinanceiraById(id);

        assertTrue(resultado.isEmpty());

        verify(repository).findByIdAndUsuario(id, usuario);
    }

    // MÉTODO getAportes()

    @Test
    public void deveRetornarAportesDaMetaFinanceira() {
        MetaFinanceira meta = new MetaFinanceira();

        Aporte aporte1 = new Aporte();
        aporte1.setValor(100f);
        aporte1.setDataEnvio(LocalDate.of(2026, 1, 10));

        Aporte aporte2 = new Aporte();
        aporte2.setValor(200f);
        aporte2.setDataEnvio(LocalDate.of(2026, 2, 10));

        AporteRepository aporteRepository = mock(AporteRepository.class);

        when(aporteRepository.findByMetaFinanceiraOrderByDataEnvioAsc(meta))
                .thenReturn(List.of(aporte1, aporte2));

        MetaFinanceiraService service =
                new MetaFinanceiraService(null, aporteRepository, null);

        List<Aporte> aportes = service.getAportes(meta);

        assertEquals(2, aportes.size());
        assertEquals(aporte1, aportes.get(0));
        assertEquals(aporte2, aportes.get(1));

        verify(aporteRepository)
                .findByMetaFinanceiraOrderByDataEnvioAsc(meta);
    }

    @Test
    public void deveRetornarListaVaziaQuandoNaoExistiremAportes() {
        MetaFinanceira meta = new MetaFinanceira();

        AporteRepository aporteRepository = mock(AporteRepository.class);

        when(aporteRepository.findByMetaFinanceiraOrderByDataEnvioAsc(meta))
                .thenReturn(List.of());

        MetaFinanceiraService service =
                new MetaFinanceiraService(null, aporteRepository, null);

        List<Aporte> aportes = service.getAportes(meta);

        assertTrue(aportes.isEmpty());

        verify(aporteRepository)
                .findByMetaFinanceiraOrderByDataEnvioAsc(meta);
    }

    // MÉTODO getTotalAportado()

    @Test
    public void deveRetornarZeroQuandoMetaFinanceiraNaoPossuirId() {
        MetaFinanceira meta = new MetaFinanceira();

        double total = metaFinanceiraService.getTotalAportado(meta);

        assertEquals(0.0, total);
    }

    @Test
    public void deveRetornarTotalAportadoDaMetaFinanceira() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setId(1L);

        AporteRepository aporteRepository = mock(AporteRepository.class);

        when(aporteRepository.somarValorPorMeta(meta)).thenReturn(300.0);

        MetaFinanceiraService service =
                new MetaFinanceiraService(null, aporteRepository, null);

        double total = service.getTotalAportado(meta);

        assertEquals(300.0, total);

        verify(aporteRepository).somarValorPorMeta(meta);
    }

    @Test
    public void deveRetornarZeroQuandoMetaFinanceiraNaoPossuirAportes() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setId(1L);

        AporteRepository aporteRepository = mock(AporteRepository.class);

        when(aporteRepository.somarValorPorMeta(meta)).thenReturn(0.0);

        MetaFinanceiraService service =
                new MetaFinanceiraService(null, aporteRepository, null);

        double total = service.getTotalAportado(meta);

        assertEquals(0.0, total);

        verify(aporteRepository).somarValorPorMeta(meta);
    }

    // MÉTODO getSituacao()

    @Test
    public void deveRetornarSituacaoConcluida() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValor(100f);
        meta.setInvestimentoInicial(100f);
        meta.setDataEnvio(LocalDate.now().minusDays(10));
        meta.setDataAlvo(LocalDate.now().plusDays(10));

        SituacaoMeta situacao =
                metaFinanceiraService.getSituacao(meta);

        assertEquals(SituacaoMeta.CONCLUIDA, situacao);
    }

    @Test
    public void deveRetornarSituacaoNaoIniciada() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValor(100f);
        meta.setInvestimentoInicial(0f);
        meta.setDataEnvio(LocalDate.now().plusDays(5));
        meta.setDataAlvo(LocalDate.now().plusDays(30));

        SituacaoMeta situacao =
                metaFinanceiraService.getSituacao(meta);

        assertEquals(SituacaoMeta.NAO_INICIADA, situacao);
    }

    // MÉTODO salvar()

    @Test
    public void deveSalvarMetaComInvestimentoInicialNulo() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(null);

        Usuario usuario = new Usuario();

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        UsuarioLogadoService usuarioLogadoService =
                mock(UsuarioLogadoService.class);

        when(usuarioLogadoService.getUsuarioLogado())
                .thenReturn(usuario);

        when(repository.save(meta)).thenReturn(meta);

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, null, usuarioLogadoService
                );

        MetaFinanceira resultado = service.salvar(meta);

        assertEquals(0f, resultado.getInvestimentoInicial());
        assertEquals(usuario, resultado.getUsuario());
        assertFalse(resultado.getStatus());

        verify(repository).save(meta);
    }

    @Test
    public void deveSalvarMetaComStatusConcluido() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(50000f);

        Usuario usuario = new Usuario();

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        UsuarioLogadoService usuarioLogadoService =
                mock(UsuarioLogadoService.class);

        when(usuarioLogadoService.getUsuarioLogado())
                .thenReturn(usuario);

        when(repository.save(meta)).thenReturn(meta);

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, null, usuarioLogadoService
                );

        MetaFinanceira resultado = service.salvar(meta);

        assertTrue(resultado.getStatus());
        assertEquals(usuario, resultado.getUsuario());

        verify(repository).save(meta);
    }

    @Test
    public void deveImpedirSalvarMetaInvalida() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setNome(null);

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        MetaFinanceiraService service =
                new MetaFinanceiraService(repository, null, null);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> service.salvar(meta)
        );

        assertEquals("Nome inválido", e.getMessage());

        verify(repository, never()).save(any());
    }

    // MÉTODO atualizarStatus()

    @Test
    public void deveAtualizarStatusParaNaoConcluido() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(1000f);

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        MetaFinanceiraService service =
                new MetaFinanceiraService(repository, null, null);

        service.atualizarStatus(meta);

        assertFalse(meta.getStatus());

        verify(repository).save(meta);
    }

    @Test
    public void deveAtualizarStatusParaConcluido() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(50000f);

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        MetaFinanceiraService service =
                new MetaFinanceiraService(repository, null, null);

        service.atualizarStatus(meta);

        assertTrue(meta.getStatus());

        verify(repository).save(meta);
    }

    @Test
    public void deveAtualizarStatusQuandoValorSuperaMeta() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(60000f);

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        MetaFinanceiraService service =
                new MetaFinanceiraService(repository, null, null);

        service.atualizarStatus(meta);

        assertTrue(meta.getStatus());

        verify(repository).save(meta);
    }

    @Test
    public void deveAtualizarStatusComInvestimentoInicialNulo() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(null);

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        MetaFinanceiraService service =
                new MetaFinanceiraService(repository, null, null);

        service.atualizarStatus(meta);

        assertFalse(meta.getStatus());

        verify(repository).save(meta);
    }

    // MÉTODO excluir()

    @Test
    public void deveExcluirMetaFinanceiraEAportes() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setId(1L);

        Aporte aporte = new Aporte();

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        AporteRepository aporteRepository =
                mock(AporteRepository.class);

        when(aporteRepository
                .findByMetaFinanceiraOrderByDataEnvioAsc(meta))
                .thenReturn(List.of(aporte));

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, aporteRepository, null
                );

        service.excluir(meta);

        InOrder ordem = inOrder(
                aporteRepository, repository
        );

        ordem.verify(aporteRepository)
                .findByMetaFinanceiraOrderByDataEnvioAsc(meta);

        ordem.verify(aporteRepository)
                .deleteAll(List.of(aporte));

        ordem.verify(repository).delete(meta);
    }

    @Test
    public void deveImpedirExcluirMetaSemId() {
        MetaFinanceira meta = new MetaFinanceira();

        MetaFinanceiraRepository repository =
                mock(MetaFinanceiraRepository.class);

        AporteRepository aporteRepository =
                mock(AporteRepository.class);

        MetaFinanceiraService service =
                new MetaFinanceiraService(
                        repository, aporteRepository, null
                );

        assertThrows(
                NullPointerException.class,
                () -> service.excluir(meta)
        );

        verifyNoInteractions(repository, aporteRepository);
    }

    // MÉTODO validar()

    @Test
    public void deveImpedirMetaComNomeNulo() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setNome(null);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Nome inválido", e.getMessage());
    }

    @Test
    public void deveImpedirMetaComNomeVazio() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setNome("   ");

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Nome inválido", e.getMessage());
    }

    @Test
    public void deveImpedirMetaComValorNulo() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setValor(null);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Valor inválido", e.getMessage());
    }

    @Test
    public void deveImpedirMetaComValorZero() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setValor(0f);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Valor inválido", e.getMessage());
    }

    @Test
    public void deveImpedirMetaComValorNegativo() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setValor(-100f);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Valor inválido", e.getMessage());
    }

    @Test
    public void deveImpedirMetaComDataEnvioNula() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setDataEnvio(null);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Data de envio inválida", e.getMessage());
    }

    @Test
    public void deveImpedirMetaComDataAlvoNula() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setDataAlvo(null);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Data alvo inválida", e.getMessage());
    }

    @Test
    public void deveImpedirMetaComDataAlvoAnterior() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setDataAlvo(LocalDate.of(2025, 12, 31));

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Data alvo inválida", e.getMessage());
    }

    @Test
    public void deveImpedirInvestimentoInicialNegativo() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(-1f);

        RegraNegocioException e = assertThrows(
                RegraNegocioException.class,
                () -> metaFinanceiraService.validar(meta)
        );

        assertEquals("Investimento inicial inválido", e.getMessage());
    }

    @Test
    public void devePermitirInvestimentoInicialNulo() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();
        meta.setInvestimentoInicial(null);

        assertDoesNotThrow(
                () -> metaFinanceiraService.validar(meta)
        );
    }

    @Test
    public void devePermitirMetaFinanceiraValida() {
        MetaFinanceira meta = criarMetaValidaParaValidacao();

        assertDoesNotThrow(
                () -> metaFinanceiraService.validar(meta)
        );
    }
}
