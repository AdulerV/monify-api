package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.Aporte;
import com.monify_api.model.entity.MetaFinanceira;
import com.monify_api.model.entity.SituacaoMeta;
import com.monify_api.model.repository.AporteRepository;
import com.monify_api.model.repository.MetaFinanceiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class MetaFinanceiraService {

    // ate 20% abaixo do progresso esperado a meta fica em ATENCAO; abaixo disso, EM_RISCO
    public static final double FATOR_ATENCAO = 0.8;

    private MetaFinanceiraRepository repository;
    private AporteRepository aporteRepository;
    private UsuarioLogadoService usuarioLogadoService;

    public MetaFinanceiraService(MetaFinanceiraRepository repository, AporteRepository aporteRepository,
                                 UsuarioLogadoService usuarioLogadoService) {
        this.repository = repository;
        this.aporteRepository = aporteRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<MetaFinanceira> getMetasFinanceiras() {
        return repository.findByUsuario(usuarioLogadoService.getUsuarioLogado());
    }

    public Optional<MetaFinanceira> getMetaFinanceiraById(Long id) {
        return repository.findByIdAndUsuario(id, usuarioLogadoService.getUsuarioLogado());
    }

    public List<Aporte> getAportes(MetaFinanceira meta) {
        return aporteRepository.findByMetaFinanceiraOrderByDataEnvioAsc(meta);
    }

    public double getTotalAportado(MetaFinanceira meta) {
        return meta.getId() == null ? 0 : aporteRepository.somarValorPorMeta(meta);
    }

    public SituacaoMeta getSituacao(MetaFinanceira meta) {
        return calcularSituacao(meta, getTotalAportado(meta), LocalDate.now());
    }

    @Transactional
    public MetaFinanceira salvar(MetaFinanceira meta) {
        validar(meta);
        if (meta.getInvestimentoInicial() == null) {
            meta.setInvestimentoInicial(0f);
        }
        meta.setUsuario(usuarioLogadoService.getUsuarioLogado());
        meta.setStatus(calcularStatus(meta));
        return repository.save(meta);
    }

    // status = true quando investimento inicial + aportes atingem o valor da meta
    @Transactional
    public void atualizarStatus(MetaFinanceira meta) {
        meta.setStatus(calcularStatus(meta));
        repository.save(meta);
    }

    @Transactional
    public void excluir(MetaFinanceira meta) {
        Objects.requireNonNull(meta.getId());
        // Composicao: os aportes nao existem sem a meta
        aporteRepository.deleteAll(aporteRepository.findByMetaFinanceiraOrderByDataEnvioAsc(meta));
        repository.delete(meta);
    }

    private boolean calcularStatus(MetaFinanceira meta) {
        double inicial = meta.getInvestimentoInicial() == null ? 0 : meta.getInvestimentoInicial();
        return inicial + getTotalAportado(meta) >= meta.getValor();
    }

    /*
     * Classifica a meta comparando o progresso financeiro com o progresso do prazo:
     *  - CONCLUIDA:    investimento inicial + aportes >= valor da meta;
     *  - NAO_INICIADA: a data de hoje e anterior a data de envio (inicio) da meta;
     *  - ATRASADA:     a data alvo ja passou e a meta nao foi concluida;
     *  - EM_RISCO:     meta para o proprio dia (prazo zero) nao concluida, ou progresso real
     *                  abaixo de 80% do progresso esperado;
     *  - NO_PRAZO:     progresso real >= progresso esperado (dias decorridos / prazo total);
     *  - ATENCAO:      progresso real entre 80% e 100% do esperado.
     */
    public SituacaoMeta calcularSituacao(MetaFinanceira meta, double totalAportes, LocalDate hoje) {
        if (meta == null) {
            throw new RegraNegocioException("Meta financeira não informada");
        }
        if (meta.getValor() == null || meta.getValor() <= 0) {
            throw new RegraNegocioException("Valor da meta inválido");
        }
        if (meta.getDataEnvio() == null || meta.getDataAlvo() == null) {
            throw new RegraNegocioException("Período da meta inválido");
        }
        double inicial = meta.getInvestimentoInicial() == null ? 0 : meta.getInvestimentoInicial();
        double acumulado = inicial + totalAportes;
        if (acumulado >= meta.getValor()) {
            return SituacaoMeta.CONCLUIDA;
        }
        if (hoje.isBefore(meta.getDataEnvio())) {
            return SituacaoMeta.NAO_INICIADA;
        }
        if (hoje.isAfter(meta.getDataAlvo())) {
            return SituacaoMeta.ATRASADA;
        }
        long prazoTotal = ChronoUnit.DAYS.between(meta.getDataEnvio(), meta.getDataAlvo());
        if (prazoTotal == 0) {
            return SituacaoMeta.EM_RISCO;
        }
        double progressoEsperado = ChronoUnit.DAYS.between(meta.getDataEnvio(), hoje) / (double) prazoTotal;
        double progressoReal = acumulado / meta.getValor();
        if (progressoReal >= progressoEsperado) {
            return SituacaoMeta.NO_PRAZO;
        }
        if (progressoReal >= progressoEsperado * FATOR_ATENCAO) {
            return SituacaoMeta.ATENCAO;
        }
        return SituacaoMeta.EM_RISCO;
    }

    public void validar(MetaFinanceira meta) {
        if (meta.getNome() == null || meta.getNome().trim().equals("")) {
            throw new RegraNegocioException("Nome inválido");
        }
        if (meta.getValor() == null || meta.getValor() <= 0) {
            throw new RegraNegocioException("Valor inválido");
        }
        if (meta.getDataEnvio() == null) {
            throw new RegraNegocioException("Data de envio inválida");
        }
        if (meta.getDataAlvo() == null || meta.getDataAlvo().isBefore(meta.getDataEnvio())) {
            throw new RegraNegocioException("Data alvo inválida");
        }
        if (meta.getInvestimentoInicial() != null && meta.getInvestimentoInicial() < 0) {
            throw new RegraNegocioException("Investimento inicial inválido");
        }
    }
}
