package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.Aporte;
import com.monify_api.model.entity.MetaFinanceira;
import com.monify_api.model.repository.AporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AporteService {

    private AporteRepository repository;
    private MetaFinanceiraService metaFinanceiraService;
    private UsuarioLogadoService usuarioLogadoService;

    public AporteService(AporteRepository repository, MetaFinanceiraService metaFinanceiraService,
                         UsuarioLogadoService usuarioLogadoService) {
        this.repository = repository;
        this.metaFinanceiraService = metaFinanceiraService;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<Aporte> getAportes() {
        return repository.findByMetaFinanceiraUsuario(usuarioLogadoService.getUsuarioLogado());
    }

    public Optional<Aporte> getAporteById(Long id) {
        return repository.findByIdAndMetaFinanceiraUsuario(id, usuarioLogadoService.getUsuarioLogado());
    }

    // a meta informada ja foi buscada restrita ao usuario logado (AporteController.converter)
    @Transactional
    public Aporte salvar(Aporte aporte) { // 5
        validar(aporte);
        // Se o aporte mudou de meta, a meta anterior tambem precisa ter o status recalculado
        MetaFinanceira metaAnterior = aporte.getId() == null ? null
                : repository.findById(aporte.getId()).map(Aporte::getMetaFinanceira).orElse(null); // 1
        Long idMetaAnterior = metaAnterior == null ? null : metaAnterior.getId(); // 2

        Aporte salvo = repository.save(aporte);
        metaFinanceiraService.atualizarStatus(salvo.getMetaFinanceira());
        if (idMetaAnterior != null && !idMetaAnterior.equals(salvo.getMetaFinanceira().getId())) { // 3 e 4
            metaFinanceiraService.getMetaFinanceiraById(idMetaAnterior)
                    .ifPresent(metaFinanceiraService::atualizarStatus);
        }
        return salvo;
    }

    @Transactional
    public void excluir(Aporte aporte) { // 1
        Objects.requireNonNull(aporte.getId());
        MetaFinanceira meta = aporte.getMetaFinanceira();
        repository.delete(aporte);
        repository.flush();
        metaFinanceiraService.atualizarStatus(meta);
    }

    public void validar(Aporte aporte) { // 7
        if (aporte.getMetaFinanceira() == null || aporte.getMetaFinanceira().getId() == null
                || aporte.getMetaFinanceira().getId() == 0) { // 1, 2 e 3
            throw new RegraNegocioException("Meta financeira inválida");
        }
        if (aporte.getValor() == null || aporte.getValor() <= 0) { // 4 e 5
            throw new RegraNegocioException("Valor inválido");
        }
        if (aporte.getDataEnvio() == null) { // 6
            throw new RegraNegocioException("Data de envio inválida");
        }
    }
}
