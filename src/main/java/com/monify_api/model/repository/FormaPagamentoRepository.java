package com.monify_api.model.repository;

import com.monify_api.model.entity.FormaPagamento;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {
    List<FormaPagamento> findByUsuario(Usuario usuario);

    Optional<FormaPagamento> findByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
