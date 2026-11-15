package com.monify_api.model.repository;

import com.monify_api.model.entity.Despesa;
import com.monify_api.model.entity.Parcela;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    List<Parcela> findByDespesaOrderByIdAsc(Despesa despesa);

    // a parcela pertence ao dono da despesa
    List<Parcela> findByDespesaUsuarioOrderByIdAsc(Usuario usuario);

    Optional<Parcela> findByIdAndDespesaUsuario(Long id, Usuario usuario);
}
