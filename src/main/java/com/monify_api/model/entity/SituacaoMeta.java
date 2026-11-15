package com.monify_api.model.entity;

// Situacao de uma MetaFinanceira em relacao ao valor e ao prazo
// (calculada por MetaFinanceiraService.calcularSituacao)
public enum SituacaoMeta {
    NAO_INICIADA,
    NO_PRAZO,
    ATENCAO,
    EM_RISCO,
    ATRASADA,
    CONCLUIDA
}
