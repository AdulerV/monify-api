package com.monify_api.model.repository;

import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaDespesaRepository extends JpaRepository<CategoriaDespesa, Long> {
    List<CategoriaDespesa> findByUsuario(Usuario usuario);

    Optional<CategoriaDespesa> findByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
