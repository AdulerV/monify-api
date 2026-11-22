package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaReceita;
import com.monify_api.model.entity.Receita;
import com.monify_api.model.repository.ReceitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReceitaService {

    private ReceitaRepository repository;
    private UsuarioLogadoService usuarioLogadoService;

    public ReceitaService(ReceitaRepository repository, UsuarioLogadoService usuarioLogadoService) {
        this.repository = repository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<Receita> getReceitas() {
        return repository.findByUsuario(usuarioLogadoService.getUsuarioLogado());
    }

    public Optional<Receita> getReceitaById(Long id) {
        return repository.findByIdAndUsuario(id, usuarioLogadoService.getUsuarioLogado());
    }

    public List<Receita> getReceitasByCategoria(CategoriaReceita categoria) { // 1
        return repository.findByCategoriaReceita(categoria);
    }

    @Transactional
    public Receita salvar(Receita receita) { // 1
        validar(receita);
        receita.setUsuario(usuarioLogadoService.getUsuarioLogado());
        return repository.save(receita);
    }

    @Transactional
    public void excluir(Receita receita) { // 1
        Objects.requireNonNull(receita.getId());
        repository.delete(receita);
    }

    public void validar(Receita receita) { // 9
        if (receita.getNome() == null || receita.getNome().trim().equals("")) { // 1 e 2
            throw new RegraNegocioException("Nome inválido");
        }
        if (receita.getData() == null) { // 3
            throw new RegraNegocioException("Data inválida");
        }
        if (receita.getValor() == null || receita.getValor() <= 0) { // 4 e 5
            throw new RegraNegocioException("Valor inválido");
        }
        if (receita.getCategoriaReceita() == null || receita.getCategoriaReceita().getId() == null
                || receita.getCategoriaReceita().getId() == 0) { // 6, 7 e 8
            throw new RegraNegocioException("Categoria de receita inválida");
        }
    }
}
