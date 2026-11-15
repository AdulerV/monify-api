cd ..# Monify API

API REST de controle financeiro pessoal, implementada a partir do diagrama de classes do Monify e da arquitetura da `saaapi` (controller → DTO → service → repository → entity, ModelMapper, autenticação JWT).

## Stack

Java 17, Spring Boot 4.1.2, Spring Data JPA, Spring Security (JWT com jjwt 0.12), MySQL 8+, springdoc-openapi (Swagger), Lombok, ModelMapper.

## Como executar

1. Tenha um MySQL rodando (padrão: `localhost:3306`, usuário `root`, senha `root`). O banco `monify` e as tabelas são criados automaticamente na primeira execução.
   Para sobrescrever: variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`.
2. `./mvnw spring-boot:run`
3. Swagger: http://localhost:8080/swagger-ui.html

Testes (usam H2 em memória no modo de compatibilidade MySQL, sem precisar do servidor): `./mvnw test`.
O relatório de cobertura do JaCoCo é gerado em `target/site/jacoco/index.html`.

## Autenticação

1. `POST /api/v1/usuarios` com `login`, `cpf`, `senha`, `senhaRepeticao`, `admin`
2. `POST /api/v1/usuarios/auth` com `login` e `senha` → devolve `token`
3. Envie `Authorization: Bearer <token>` nas demais chamadas

A resposta do `/auth` traz `login`, `token` e `admin`. O CORS é tratado no Spring Security e libera, por padrão, o front em `http://localhost:3000` (configurável em `app.cors.origens` / variável `CORS_ORIGENS`).

## Endpoints

| Recurso | Rota base | Extras |
|---|---|---|
| Receitas | `/api/v1/receitas` | |
| Despesas | `/api/v1/despesas` | `GET /{id}/parcelas` |
| Parcelas | `/api/v1/parcelas` | |
| Formas de pagamento | `/api/v1/formaspagamento` | |
| Categorias de receita | `/api/v1/categoriasreceita` | `GET /{id}/receitas` |
| Categorias de despesa | `/api/v1/categoriasdespesa` | `GET /{id}/despesas` |
| Metas financeiras | `/api/v1/metasfinanceiras` | `GET /{id}/aportes` |
| Aportes | `/api/v1/aportes` | |
| Usuários | `/api/v1/usuarios` | `POST /auth` |

Todos os recursos têm `GET`, `GET /{id}`, `POST`, `PUT /{id}` e `DELETE /{id}`. Datas no formato ISO (`2026-11-11`).

## Classes de domínio

São 11 classes de domínio: as 10 do diagrama (`Lancamento`, `Receita`, `Despesa`, `Parcela`, `FormaPagamento`, `CategoriaLancamento`, `CategoriaReceita`, `CategoriaDespesa`, `MetaFinanceira`, `Aporte`) mais `Usuario`, além do enum `SituacaoMeta`.

Em relação ao diagrama original, `Usuario` passou a ser dono dos dados: `Usuario 1 —— 0..* Lancamento`, `CategoriaLancamento`, `FormaPagamento` e `MetaFinanceira`. `Parcela` e `Aporte` pertencem ao mesmo usuário da `Despesa` e da `MetaFinanceira` (composição).

## Regras de negócio

- **Dados por usuário**: cada usuário enxerga e altera apenas os próprios lançamentos, categorias, formas de pagamento, metas, parcelas e aportes. O dono é identificado pelo token JWT (`UsuarioLogadoService`). Um id de outro usuário responde `404`, e usar a categoria, a forma de pagamento ou a meta de outro usuário é recusado. Um usuário com dados cadastrados não pode ser excluído.
- **Despesa** (`DespesaService.validar`):
  - nome obrigatório, com no máximo 255 caracteres; data obrigatória; valor maior que zero;
  - forma de pagamento e categoria obrigatórias (e do próprio usuário);
  - despesa parcelada: quantidade de parcelas entre 2 e 120 (no diagrama original o campo é float; foi alterado para Integer por ser uma contagem);
  - **limite de gasto**: se a categoria tem `limiteGasto = true`, a soma das despesas da categoria no mês da despesa (sem contar a própria despesa, numa alteração) não pode ultrapassar `valorLimite`.
- **Parcelas**: uma despesa parcelada gera `n` parcelas automaticamente (os centavos de arredondamento ficam na última). Alterar o valor ou a quantidade regera as parcelas, exceto se alguma já estiver paga.
- **Meta financeira**: `status` é calculado automaticamente (`true` quando investimento inicial + aportes ≥ valor) e recalculado a cada aporte incluído, alterado ou excluído.
- **Situação da meta** (`MetaFinanceiraService.calcularSituacao`, devolvida em `situacao`), comparando o progresso financeiro com o progresso do prazo:

  | Situação | Condição |
  |---|---|
  | `CONCLUIDA` | investimento inicial + aportes ≥ valor da meta |
  | `NAO_INICIADA` | hoje é anterior à data de envio (início) |
  | `ATRASADA` | a data alvo já passou e a meta não foi concluída |
  | `NO_PRAZO` | progresso real ≥ progresso esperado (dias decorridos ÷ prazo total) |
  | `ATENCAO` | progresso real entre 80% e 100% do esperado |
  | `EM_RISCO` | progresso real abaixo de 80% do esperado, ou meta para o próprio dia ainda não concluída |

- **Composições**: excluir uma despesa exclui suas parcelas; excluir uma meta exclui seus aportes.
- Formas de pagamento e categorias em uso não podem ser excluídas.

## Métodos com complexidade ciclomática maior que 10

| Método | CC estrita (1 + `if`/ternários) | CC contando `&&` e `\|\|` (valor do JaCoCo) | Testes unitários | Cobertura |
|---|---|---|---|---|
| `DespesaService.validar(Despesa)` | 12 | 21 | `DespesaServiceValidarTest` (26 casos, repositório simulado com Mockito) | 100% das linhas e dos desvios |
| `MetaFinanceiraService.calcularSituacao(MetaFinanceira, double, LocalDate)` | 11 | 13 | `MetaFinanceiraSituacaoTest` (17 casos) | 100% das linhas e dos desvios |

**Como as contagens foram feitas**, com CC = número de decisões + 1:
- `validar`: 9 `if` de validação, mais o `if` do limite de gasto, mais o ternário de `idIgnorado` (11 decisões); somando cada condição extra de `&&`/`||`, são 20 decisões.
- `calcularSituacao`: 9 `if`, mais o ternário do investimento inicial (10 decisões); somando cada condição extra de `||`, são 12 decisões.

Os casos de teste seguem a Análise do Valor Limite: nome com 255 e 256 caracteres, valor 0,00 e 0,01, 1/2/120/121 parcelas, total do mês igual ao limite e 0,01 acima dele, um dia antes e no dia do início, no dia e um dia depois da data alvo, progresso igual a 100% e a 80% do esperado.
