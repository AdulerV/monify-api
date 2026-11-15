package com.monify_api.model.repository;

import com.monify_api.model.entity.CategoriaReceita;
import com.monify_api.model.entity.Receita;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {
    List<Receita> findByUsuario(Usuario usuario);

    Optional<Receita> findByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByUsuario(Usuario usuario);

    List<Receita> findByCategoriaReceita(CategoriaReceita categoriaReceita);

    boolean existsByCategoriaReceita(CategoriaReceita categoriaReceita);
}
