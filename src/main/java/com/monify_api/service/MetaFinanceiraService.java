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

    public List<Aporte> getAportes(MetaFinanceira meta) { // 1
        return aporteRepository.findByMetaFinanceiraOrderByDataEnvioAsc(meta);
    }

    public double getTotalAportado(MetaFinanceira meta) { // 2
        return meta.getId() == null ? 0 : aporteRepository.somarValorPorMeta(meta); // 1
    }

    public SituacaoMeta getSituacao(MetaFinanceira meta) { // 1
        return calcularSituacao(meta, getTotalAportado(meta), LocalDate.now());
    }

    @Transactional
    public MetaFinanceira salvar(MetaFinanceira meta) { // 2
        validar(meta);
        if (meta.getInvestimentoInicial() == null) { // 1
            meta.setInvestimentoInicial(0f);
        }
        meta.setUsuario(usuarioLogadoService.getUsuarioLogado());
        meta.setStatus(calcularStatus(meta));
        return repository.save(meta);
    }

    // status = true quando investimento inicial + aportes atingem o valor da meta
    @Transactional
    public void atualizarStatus(MetaFinanceira meta) { // 1
        meta.setStatus(calcularStatus(meta));
        repository.save(meta);
    }

    @Transactional
    public void excluir(MetaFinanceira meta) { // 1
        Objects.requireNonNull(meta.getId());
        // Composicao: os aportes nao existem sem a meta
        aporteRepository.deleteAll(aporteRepository.findByMetaFinanceiraOrderByDataEnvioAsc(meta));
        repository.delete(meta);
    }

    private boolean calcularStatus(MetaFinanceira meta) { // 2
        double inicial = meta.getInvestimentoInicial() == null ? 0 : meta.getInvestimentoInicial(); // 1
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
    public SituacaoMeta calcularSituacao(MetaFinanceira meta, double totalAportes, LocalDate hoje) { // 13
        if (meta == null) { // 1
            throw new RegraNegocioException("Meta financeira não informada"); // 2
        }
        if (meta.getValor() == null || meta.getValor() <= 0) { // 3 e 4
            throw new RegraNegocioException("Valor da meta inválido"); // 5
        }
        if (meta.getDataEnvio() == null || meta.getDataAlvo() == null) { // 6 e 7
            throw new RegraNegocioException("Período da meta inválido"); // 8
        }
        double inicial = meta.getInvestimentoInicial() == null ? 0 : meta.getInvestimentoInicial(); // 9 e 10
        double acumulado = inicial + totalAportes;
        if (acumulado >= meta.getValor()) { // 11
            return SituacaoMeta.CONCLUIDA; // 12
        }
        if (hoje.isBefore(meta.getDataEnvio())) { // 13
            return SituacaoMeta.NAO_INICIADA; // 14
        }
        if (hoje.isAfter(meta.getDataAlvo())) { // 15
            return SituacaoMeta.ATRASADA; // 16
        }
        long prazoTotal = ChronoUnit.DAYS.between(meta.getDataEnvio(), meta.getDataAlvo());
        if (prazoTotal == 0) { // 17
            return SituacaoMeta.EM_RISCO; // 18
        }
        double progressoEsperado = ChronoUnit.DAYS.between(meta.getDataEnvio(), hoje) / (double) prazoTotal;
        double progressoReal = acumulado / meta.getValor();
        if (progressoReal >= progressoEsperado) { // 19
            return SituacaoMeta.NO_PRAZO; // 20
        }
        if (progressoReal >= progressoEsperado * FATOR_ATENCAO) { // 21
            return SituacaoMeta.ATENCAO; // 22
        }
        return SituacaoMeta.EM_RISCO; // 23
    }

    public void validar(MetaFinanceira meta) { // 10
        if (meta.getNome() == null || meta.getNome().trim().equals("")) {  // 1 e 2
            throw new RegraNegocioException("Nome inválido");
        }
        if (meta.getValor() == null || meta.getValor() <= 0) { // 3 e 4
            throw new RegraNegocioException("Valor inválido");
        }
        if (meta.getDataEnvio() == null) { // 5
            throw new RegraNegocioException("Data de envio inválida");
        }
        if (meta.getDataAlvo() == null || meta.getDataAlvo().isBefore(meta.getDataEnvio())) { // 6 e 7
            throw new RegraNegocioException("Data alvo inválida");
        }
        if (meta.getInvestimentoInicial() != null && meta.getInvestimentoInicial() < 0) { // 8 e 9
            throw new RegraNegocioException("Investimento inicial inválido");
        }
    }
}
