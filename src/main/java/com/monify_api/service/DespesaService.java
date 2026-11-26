package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.CategoriaDespesa;
import com.monify_api.model.entity.Despesa;
import com.monify_api.model.entity.Parcela;
import com.monify_api.model.repository.DespesaRepository;
import com.monify_api.model.repository.ParcelaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DespesaService {

    public static final int TAMANHO_MAXIMO_NOME = 255;
    public static final int QUANTIDADE_MINIMA_PARCELAS = 2;
    public static final int QUANTIDADE_MAXIMA_PARCELAS = 120;

    private DespesaRepository repository;
    private ParcelaRepository parcelaRepository;
    private UsuarioLogadoService usuarioLogadoService;

    public DespesaService(DespesaRepository repository, ParcelaRepository parcelaRepository,
                          UsuarioLogadoService usuarioLogadoService) {
        this.repository = repository;
        this.parcelaRepository = parcelaRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<Despesa> getDespesas() {
        return repository.findByUsuario(usuarioLogadoService.getUsuarioLogado());
    }

    public Optional<Despesa> getDespesaById(Long id) {
        return repository.findByIdAndUsuario(id, usuarioLogadoService.getUsuarioLogado());
    }

    public List<Despesa> getDespesasByCategoria(CategoriaDespesa categoria) { // 1
        return repository.findByCategoriaDespesa(categoria);
    }

    public List<Parcela> getParcelas(Despesa despesa) { // 1
        return parcelaRepository.findByDespesaOrderByIdAsc(despesa);
    }

    @Transactional
    public Despesa salvar(Despesa despesa) { // 2
        validar(despesa);
        if (!Boolean.TRUE.equals(despesa.getParcelada())) { // 1
            despesa.setParcelada(false);
        }
        despesa.setUsuario(usuarioLogadoService.getUsuarioLogado());
        Despesa salva = repository.save(despesa);
        sincronizarParcelas(salva);
        return salva;
    }

    @Transactional
    public void excluir(Despesa despesa) { // 1
        Objects.requireNonNull(despesa.getId());
        // Composicao: as parcelas nao existem sem a despesa
        parcelaRepository.deleteAll(parcelaRepository.findByDespesaOrderByIdAsc(despesa));
        repository.delete(despesa);
    }

    /*
     * Regras de negocio da Despesa (metodo de maior complexidade ciclomatica do sistema):
     *  - nome obrigatorio, com no maximo 255 caracteres;
     *  - data obrigatoria e valor maior que zero;
     *  - forma de pagamento e categoria obrigatorias;
     *  - despesa parcelada: quantidade de parcelas entre 2 e 120;
     *  - categoria com limite de gasto: a soma das despesas da categoria no mes da despesa
     *    (desconsiderando a propria despesa, quando for alteracao) nao pode ultrapassar o valorLimite.
     */
    public void validar(Despesa despesa) { // 21
        if (despesa.getNome() == null || despesa.getNome().trim().isEmpty()) { // 1 e 2
            throw new RegraNegocioException("Nome inválido");
        }
        if (despesa.getNome().length() > TAMANHO_MAXIMO_NOME) { // 3
            throw new RegraNegocioException("Nome deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres");
        }
        if (despesa.getData() == null) { // 4
            throw new RegraNegocioException("Data inválida");
        }
        if (despesa.getValor() == null || despesa.getValor() <= 0) { // 5 e 6
            throw new RegraNegocioException("Valor inválido");
        }
        if (despesa.getFormaPagamento() == null || despesa.getFormaPagamento().getId() == null
                || despesa.getFormaPagamento().getId() == 0) { // 7, 8 e 9
            throw new RegraNegocioException("Forma de pagamento inválida");
        }
        if (despesa.getCategoriaDespesa() == null || despesa.getCategoriaDespesa().getId() == null
                || despesa.getCategoriaDespesa().getId() == 0) { // 10, 11 e 12
            throw new RegraNegocioException("Categoria de despesa inválida");
        }
        if (Boolean.TRUE.equals(despesa.getParcelada())) { // 13
            Integer quantidade = despesa.getQuantidadeParcelas();
            if (quantidade == null || quantidade < QUANTIDADE_MINIMA_PARCELAS
                    || quantidade > QUANTIDADE_MAXIMA_PARCELAS) { // 14, 15 e 16
                throw new RegraNegocioException("Quantidade de parcelas inválida (informe um número entre "
                        + QUANTIDADE_MINIMA_PARCELAS + " e " + QUANTIDADE_MAXIMA_PARCELAS + ")");
            }
        }
        CategoriaDespesa categoria = despesa.getCategoriaDespesa();
        if (Boolean.TRUE.equals(categoria.getLimiteGasto()) && categoria.getValorLimite() != null) { // 17 e 18
            YearMonth mes = YearMonth.from(despesa.getData());
            Long idIgnorado = despesa.getId() == null ? -1L : despesa.getId(); // 19
            Double totalMes = repository.somarValorPorCategoriaNoPeriodo(
                    categoria, mes.atDay(1), mes.atEndOfMonth(), idIgnorado);
            double total = totalMes + despesa.getValor();
            if (total > categoria.getValorLimite()) { // 20
                throw new RegraNegocioException(String.format(
                        "Limite de gasto mensal da categoria %s excedido (limite: %.2f, total no mês: %.2f)",
                        categoria.getNome(), categoria.getValorLimite(), total));
            }
        }
    }

    // Composicao Despesa -> Parcela: gera (ou regera) as parcelas quando a despesa e parcelada.
    void sincronizarParcelas(Despesa despesa) { // 6
        List<Parcela> atuais = parcelaRepository.findByDespesaOrderByIdAsc(despesa);
        int quantidade = Boolean.TRUE.equals(despesa.getParcelada())
                ? despesa.getQuantidadeParcelas()
                : 0; // 1

        if (quantidade == atuais.size() && somaConfere(atuais, despesa.getValor())) { // 2 e 3
            return;
        }
        if (atuais.stream().anyMatch(p -> Boolean.TRUE.equals(p.getPago()))) { // 4
            throw new RegraNegocioException(
                    "Despesa possui parcelas pagas: não é possível alterar o valor ou o parcelamento");
        }
        parcelaRepository.deleteAll(atuais);
        if (quantidade > 0) { // 5
            parcelaRepository.saveAll(gerarParcelas(despesa, quantidade));
        }
    }

    boolean somaConfere(List<Parcela> parcelas, Float valor) { // 3
        if (parcelas.isEmpty()) { // 1
            return true;
        }
        double soma = parcelas.stream().mapToDouble(Parcela::getValorParcela).sum();
        return Math.abs(soma - valor) < 0.01; // 2
    }

    List<Parcela> gerarParcelas(Despesa despesa, int quantidade) { // 3
        BigDecimal total = new BigDecimal(Float.toString(despesa.getValor()));
        BigDecimal valorBase = total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.DOWN);
        BigDecimal ultima = total.subtract(valorBase.multiply(BigDecimal.valueOf(quantidade - 1)));

        List<Parcela> parcelas = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) { // 1
            BigDecimal valor = (i == quantidade) ? ultima : valorBase; // 2
            parcelas.add(new Parcela(null, valor.floatValue(), false, despesa));
        }
        return parcelas;
    }
}
