package com.monify_api.model.repository;

import com.monify_api.model.entity.CategoriaReceita;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaReceitaRepository extends JpaRepository<CategoriaReceita, Long> {
    List<CategoriaReceita> findByUsuario(Usuario usuario);

    Optional<CategoriaReceita> findByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
