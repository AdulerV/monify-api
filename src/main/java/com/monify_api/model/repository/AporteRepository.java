package com.monify_api.model.repository;

import com.monify_api.model.entity.Aporte;
import com.monify_api.model.entity.MetaFinanceira;
import com.monify_api.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AporteRepository extends JpaRepository<Aporte, Long> {
    List<Aporte> findByMetaFinanceiraOrderByDataEnvioAsc(MetaFinanceira metaFinanceira);

    // o aporte pertence ao dono da meta
    List<Aporte> findByMetaFinanceiraUsuario(Usuario usuario);

    Optional<Aporte> findByIdAndMetaFinanceiraUsuario(Long id, Usuario usuario);

    @Query("select coalesce(sum(a.valor), 0) from Aporte a where a.metaFinanceira = :meta")
    Double somarValorPorMeta(@Param("meta") MetaFinanceira meta);
}
