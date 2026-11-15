package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.FormaPagamento;
import com.monify_api.model.repository.DespesaRepository;
import com.monify_api.model.repository.FormaPagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FormaPagamentoService {

    private FormaPagamentoRepository repository;
    private DespesaRepository despesaRepository;
    private UsuarioLogadoService usuarioLogadoService;

    public FormaPagamentoService(FormaPagamentoRepository repository, DespesaRepository despesaRepository,
                                 UsuarioLogadoService usuarioLogadoService) {
        this.repository = repository;
        this.despesaRepository = despesaRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<FormaPagamento> getFormasPagamento() {
        return repository.findByUsuario(usuarioLogadoService.getUsuarioLogado());
    }

    public Optional<FormaPagamento> getFormaPagamentoById(Long id) {
        return repository.findByIdAndUsuario(id, usuarioLogadoService.getUsuarioLogado());
    }

    @Transactional
    public FormaPagamento salvar(FormaPagamento formaPagamento) {
        validar(formaPagamento);
        formaPagamento.setUsuario(usuarioLogadoService.getUsuarioLogado());
        return repository.save(formaPagamento);
    }

    @Transactional
    public void excluir(FormaPagamento formaPagamento) {
        Objects.requireNonNull(formaPagamento.getId());
        if (despesaRepository.existsByFormaPagamento(formaPagamento)) {
            throw new RegraNegocioException("Forma de pagamento possui despesas vinculadas");
        }
        repository.delete(formaPagamento);
    }

    public void validar(FormaPagamento formaPagamento) {
        if (formaPagamento.getNome() == null || formaPagamento.getNome().trim().equals("")) {
            throw new RegraNegocioException("Nome inválido");
        }
    }
}
