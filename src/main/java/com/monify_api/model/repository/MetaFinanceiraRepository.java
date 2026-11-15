package com.monify_api.model.repository;

import com.monify_api.model.entity.MetaFinanceira;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, Long> {
    List<MetaFinanceira> findByUsuario(Usuario usuario);

    Optional<MetaFinanceira> findByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
