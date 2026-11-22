package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaReceita;
import com.monify_api.model.repository.CategoriaReceitaRepository;
import com.monify_api.model.repository.ReceitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CategoriaReceitaService {

    private CategoriaReceitaRepository repository;
    private ReceitaRepository receitaRepository;
    private UsuarioLogadoService usuarioLogadoService;

    public CategoriaReceitaService(CategoriaReceitaRepository repository, ReceitaRepository receitaRepository,
                                   UsuarioLogadoService usuarioLogadoService) {
        this.repository = repository;
        this.receitaRepository = receitaRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<CategoriaReceita> getCategoriasReceita() {
        return repository.findByUsuario(usuarioLogadoService.getUsuarioLogado());
    }

    public Optional<CategoriaReceita> getCategoriaReceitaById(Long id) {
        return repository.findByIdAndUsuario(id, usuarioLogadoService.getUsuarioLogado());
    }

    @Transactional
    public CategoriaReceita salvar(CategoriaReceita categoria) { // 1
        validar(categoria);
        categoria.setUsuario(usuarioLogadoService.getUsuarioLogado());
        return repository.save(categoria);
    }

    @Transactional
    public void excluir(CategoriaReceita categoria) { // 2
        Objects.requireNonNull(categoria.getId());
        if (receitaRepository.existsByCategoriaReceita(categoria)) { // 1
            throw new RegraNegocioException("Categoria possui receitas vinculadas");
        }
        repository.delete(categoria);
    }

    public void validar(CategoriaReceita categoria) { // 3
        if (categoria.getNome() == null || categoria.getNome().trim().equals("")) { // 1 e 2
            throw new RegraNegocioException("Nome inválido");
        }
    }
}
