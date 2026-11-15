package com.monify_api.model.repository;

import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.entity.Despesa;
import com.monify_api.model.entity.FormaPagamento;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    List<Despesa> findByUsuario(Usuario usuario);

    Optional<Despesa> findByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByUsuario(Usuario usuario);

    List<Despesa> findByCategoriaDespesa(CategoriaDespesa categoriaDespesa);

    boolean existsByCategoriaDespesa(CategoriaDespesa categoriaDespesa);

    boolean existsByFormaPagamento(FormaPagamento formaPagamento);

    // a categoria ja pertence a um unico usuario, entao a soma fica restrita a ele
    @Query("select coalesce(sum(d.valor), 0) from Despesa d " +
            "where d.categoriaDespesa = :categoria and d.data between :inicio and :fim and d.id <> :idIgnorado")
    Double somarValorPorCategoriaNoPeriodo(@Param("categoria") CategoriaDespesa categoria,
                                           @Param("inicio") LocalDate inicio,
                                           @Param("fim") LocalDate fim,
                                           @Param("idIgnorado") Long idIgnorado);
}
