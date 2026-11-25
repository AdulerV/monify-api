package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.repository.CategoriaDespesaRepository;
import com.monify_api.model.repository.DespesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CategoriaDespesaService {

    private CategoriaDespesaRepository repository;
    private DespesaRepository despesaRepository;
    private IUsuarioLogadoService usuarioLogadoService;

    public CategoriaDespesaService(CategoriaDespesaRepository repository, DespesaRepository despesaRepository,
                                   IUsuarioLogadoService usuarioLogadoService) {
        this.repository = repository;
        this.despesaRepository = despesaRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<CategoriaDespesa> getCategoriasDespesa() {
        return repository.findByUsuario(usuarioLogadoService.getUsuarioLogado());
    }

    public Optional<CategoriaDespesa> getCategoriaDespesaById(Long id) {
        return repository.findByIdAndUsuario(id, usuarioLogadoService.getUsuarioLogado());
    }

    @Transactional
    public CategoriaDespesa salvar(CategoriaDespesa categoria) { // 2
        validar(categoria);
        if (!Boolean.TRUE.equals(categoria.getLimiteGasto())) {
            categoria.setLimiteGasto(false);
        }
        categoria.setUsuario(usuarioLogadoService.getUsuarioLogado());
        return repository.save(categoria);
    }

    @Transactional
    public void excluir(CategoriaDespesa categoria) { // 2
        Objects.requireNonNull(categoria.getId());
        if (despesaRepository.existsByCategoriaDespesa(categoria)) { // 1
            throw new RegraNegocioException("Categoria possui despesas vinculadas");
        }
        repository.delete(categoria);
    }

    public void validar(CategoriaDespesa categoria) { // 6
        if (categoria.getNome() == null || categoria.getNome().trim().equals("")) { // 1 e 2
            throw new RegraNegocioException("Nome inválido");
        }
        if (Boolean.TRUE.equals(categoria.getLimiteGasto())
                && (categoria.getValorLimite() == null || categoria.getValorLimite() <= 0)) { // 3, 4 e 5
            throw new RegraNegocioException("Valor limite inválido");
        }
    }
}
