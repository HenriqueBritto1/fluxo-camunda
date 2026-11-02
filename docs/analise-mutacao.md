# Análise de testes de mutação

Registro do trabalho de teste de mutação no backend (`engine` e `worker`) com o Pitest. 

Rodando o Pitest com os testes unitários antes produzidos e os testes produzidos após o resultado da ferramenta, chegamos nas seguintes informações:
## Configuração

| Item | Valor |
|---|---|
| Ferramenta | Pitest 1.30.1, com `pitest-junit5-plugin` 1.2.3 |
| Operadores | Grupo `ALL`, com 26 operadores ativos, incluindo os experimentais |
| Classes analisadas | Todo o código dos dois módulos, sem exclusões |
| Testes | Unitários, com JUnit 5 e Mockito, sem iniciar o Spring |
| Comando | `mvn verify -Pmutacao` |
| Relatórios | `docs/pit-reports/engine/index.html` e `docs/pit-reports/worker/index.html` |

Com o grupo `STRONGER` e as exclusões anteriores, o Pitest gerava 43 mutantes. Com `ALL` e sem exclusões, passou a gerar **144**.

Algumas classes não geram mutantes:

- **`Ferias`:** os métodos gerados pelo Lombok têm a anotação `@lombok.Generated`, e o Pitest ignora código com essa anotação.
- **`FeriasController`:** a classe está vazia.
- **`FeriasRepository`:** é só uma interface, sem código executável.

## Definição do escore

- **Escore do Pitest:** mutantes mortos ÷ total de mutantes.
- **Escore ajustado:** mutantes mortos ÷ (total de mutantes − equivalentes).

Um mutante só é contado como equivalente quando há uma justificativa escrita mostrando que nenhum teste consegue diferenciá-lo do código original.

## Resultado

| Módulo | Mutantes | Mortos (inicial) | Escore inicial | Mortos (final) | Escore final |
|---|---|---|---|---|---|
| `engine` | 57 | 45 | 78,9% | 57 | 100% |
| `worker` | 87 | 77 | 88,5% | 86 | 98,9% |
| **Total** | **144** | **122** | **84,7%** | **143** | **99,3%** |

| Escore | Inicial | Final |
|---|---|---|
| Escore do Pitest | 122/144 = 84,7% | 143/144 = 99,3% |
| Escore ajustado (1 equivalente) | 122/143 = 85,3% | 143/143 = **100%** |

Na execução inicial, os 22 mutantes vivos eram 21 sem cobertura (`NO_COVERAGE`) e 1 sobrevivente (`SURVIVED`). Na execução final, só resta o sobrevivente, que é equivalente.

### Mutantes por operador (execução final)

| Operador | `engine` | `worker` | Total |
|---|---|---|---|
| `NON_VOID_METHOD_CALLS` | 20 | 30 | 50 |
| `VOID_METHOD_CALLS` | 12 | 16 | 28 |
| `EXPERIMENTAL_ARGUMENT_PROPAGATION` | 6 | 8 | 14 |
| `INLINE_CONSTS` | 2 | 6 | 8 |
| `EXPERIMENTAL_NAKED_RECEIVER` | 1 | 6 | 7 |
| `NULL_RETURNS` | 5 | 1 | 6 |
| `CONSTRUCTOR_CALLS` | 4 | 1 | 5 |
| `EXPERIMENTAL_MEMBER_VARIABLE` | 2 | 3 | 5 |
| `NEGATE_CONDITIONALS` | 1 | 4 | 5 |
| `REMOVE_CONDITIONALS_EQUAL_ELSE` | 1 | 4 | 5 |
| `REMOVE_CONDITIONALS_EQUAL_IF` | 1 | 4 | 5 |
| `EMPTY_RETURNS` | 0 | 3 | 3 |
| `MATH` | 1 | 1 | 2 |
| `PRIMITIVE_RETURNS` | 1 | 0 | 1 |
| **Total** | **57** | **87** | **144** |

Os outros 12 operadores ativos não encontraram código onde atuar: `CONDITIONALS_BOUNDARY`, `EXPERIMENTAL_BIG_DECIMAL`, `EXPERIMENTAL_BIG_INTEGER`, `EXPERIMENTAL_REMOVE_SWITCH_MUTATOR_[0-99]`, `EXPERIMENTAL_SWITCH`, `FALSE_RETURNS`, `INCREMENTS`, `INVERT_NEGS`, `REMOVE_CONDITIONALS_ORDER_ELSE`, `REMOVE_CONDITIONALS_ORDER_IF`, `REMOVE_INCREMENTS` e `TRUE_RETURNS`. O código não tem, por exemplo, comparações de ordem (`<`, `>`), `switch`, incrementos nem métodos que retornam `boolean`.

## Análise dos mutantes vivos

### Grupo 1: `CorsConfig` (11 mutantes sem cobertura)

**Por que estavam vivos:** a classe estava excluída da análise, então nenhum teste a executava.

**Teste criado:** `engine/src/test/java/com/br/fluxo/config/CorsConfigTest.java`. Ele cria o filtro e simula requisições HTTP com objetos de teste do Spring (`MockHttpServletRequest`), sem iniciar a aplicação, e verifica os cabeçalhos CORS da resposta:

- `deveCriarFiltro`: o filtro é criado e não é nulo;
- `deveLiberarPreflightDoFrontendParaQualquerMetodoECabecalho`: uma requisição `OPTIONS` vinda de `http://localhost:5173` pedindo `PUT` e um cabeçalho customizado é aceita, com origem, credenciais, método e cabeçalho liberados;
- `deveAdicionarCabecalhosCorsEmRequisicaoSimplesDoFrontend`: um `GET` do frontend segue adiante e recebe os cabeçalhos de origem e credenciais;
- `deveRejeitarOrigemNaoLiberada`: uma origem desconhecida recebe `403`.

| Linha | Operador | Mutação | Inicial | Final | Morto por |
|---|---|---|---|---|---|
| 17 | `CONSTRUCTOR_CALLS` | `removed call to org/springframework/web/cors/UrlBasedCorsConfigurationSource::<init>` | NO_COVERAGE | KILLED | `deveCriarFiltro` |
| 18 | `CONSTRUCTOR_CALLS` | `removed call to org/springframework/web/cors/CorsConfiguration::<init>` | NO_COVERAGE | KILLED | `deveCriarFiltro` |
| 20 | `INLINE_CONSTS` | `Substituted 1 with 0` (`true` → `false`) | NO_COVERAGE | KILLED | `deveAdicionarCabecalhosCorsEmRequisicaoSimplesDoFrontend` |
| 20 | `NON_VOID_METHOD_CALLS` | `removed call to java/lang/Boolean::valueOf` | NO_COVERAGE | KILLED | `deveAdicionarCabecalhosCorsEmRequisicaoSimplesDoFrontend` |
| 20 | `VOID_METHOD_CALLS` | `removed call to org/springframework/web/cors/CorsConfiguration::setAllowCredentials` | NO_COVERAGE | KILLED | `deveAdicionarCabecalhosCorsEmRequisicaoSimplesDoFrontend` |
| 21 | `VOID_METHOD_CALLS` | `removed call to org/springframework/web/cors/CorsConfiguration::addAllowedOrigin` | NO_COVERAGE | KILLED | `deveAdicionarCabecalhosCorsEmRequisicaoSimplesDoFrontend` |
| 22 | `VOID_METHOD_CALLS` | `removed call to org/springframework/web/cors/CorsConfiguration::addAllowedHeader` | NO_COVERAGE | KILLED | `deveLiberarPreflightDoFrontendParaQualquerMetodoECabecalho` |
| 23 | `VOID_METHOD_CALLS` | `removed call to org/springframework/web/cors/CorsConfiguration::addAllowedMethod` | NO_COVERAGE | KILLED | `deveLiberarPreflightDoFrontendParaQualquerMetodoECabecalho` |
| 25 | `VOID_METHOD_CALLS` | `removed call to org/springframework/web/cors/UrlBasedCorsConfigurationSource::registerCorsConfiguration` | NO_COVERAGE | KILLED | `deveAdicionarCabecalhosCorsEmRequisicaoSimplesDoFrontend` |
| 26 | `CONSTRUCTOR_CALLS` | `removed call to org/springframework/web/filter/CorsFilter::<init>` | NO_COVERAGE | KILLED | `deveCriarFiltro` |
| 26 | `NULL_RETURNS` | `replaced return value with null for com/br/fluxo/config/CorsConfig::corsFilter` | NO_COVERAGE | KILLED | `deveCriarFiltro` |

Os mutantes das linhas 22 e 23 exigem a requisição de preflight com método `PUT` e cabeçalho customizado. Sem `addAllowedMethod("*")`, o Spring só libera `GET` e `HEAD`. Sem `addAllowedHeader("*")`, ele rejeita o cabeçalho customizado.

### Grupo 2: `ExternalTaskConfig` (8 mutantes sem cobertura)

**Por que estavam vivos:** a classe estava excluída da análise.

**Teste criado:** `worker/src/test/java/com/br/fluxo/pedidoferias/config/ExternalTaskConfigTest.java`, com o caso `deveCriarClienteApontandoParaEngineLocalComTimeoutDeDezSegundos`. O método estático `ExternalTaskClient.create()` foi substituído por um mock (`mockStatic`) que devolve um construtor falso. Assim, o teste verifica a URL, o timeout e o cliente retornado sem se conectar ao Camunda.

| Linha | Operador | Mutação | Inicial | Final |
|---|---|---|---|---|
| 11 | `NON_VOID_METHOD_CALLS` | `removed call to org/camunda/bpm/client/ExternalTaskClient::create` | NO_COVERAGE | KILLED |
| 11 | `NULL_RETURNS` | `replaced return value with null for .../ExternalTaskConfig::externalTaskClient` | NO_COVERAGE | KILLED |
| 12 | `INLINE_CONSTS` | `Substituted 10000 with 10001` | NO_COVERAGE | KILLED |
| 12 | `NON_VOID_METHOD_CALLS` | `removed call to .../ExternalTaskClientBuilder::baseUrl` | NO_COVERAGE | KILLED |
| 12 | `EXPERIMENTAL_NAKED_RECEIVER` | `replaced call to .../ExternalTaskClientBuilder::baseUrl with receiver` | NO_COVERAGE | KILLED |
| 13 | `NON_VOID_METHOD_CALLS` | `removed call to .../ExternalTaskClientBuilder::asyncResponseTimeout` | NO_COVERAGE | KILLED |
| 13 | `EXPERIMENTAL_NAKED_RECEIVER` | `replaced call to .../ExternalTaskClientBuilder::asyncResponseTimeout with receiver` | NO_COVERAGE | KILLED |
| 14 | `NON_VOID_METHOD_CALLS` | `removed call to .../ExternalTaskClientBuilder::build` | NO_COVERAGE | KILLED |

O Pitest atribui o mutante `10000` → `10001` à linha 12, mas a constante está na linha 13 do código. É a mesma chamada encadeada, e o compilador registrou a linha de forma diferente.

### Grupo 3: métodos `main` (2 mutantes sem cobertura)

**Por que estavam vivos:** as classes `Application` e `ApplicationWorker` estavam excluídas, e chamar `main` de verdade iniciaria a aplicação, com banco e Camunda.

**Testes criados:** `engine/src/test/java/com/br/fluxo/ApplicationTest.java` e `worker/src/test/java/com/br/fluxo/pedidoferias/ApplicationWorkerTest.java`. Cada um substitui `SpringApplication.run` por um mock (`mockStatic`), chama `main` e verifica que `run` foi chamado com a classe certa e os argumentos recebidos. A aplicação não é iniciada.

| Classe | Linha | Operador | Mutação | Inicial | Final |
|---|---|---|---|---|---|
| `Application` | 10 | `NON_VOID_METHOD_CALLS` | `removed call to org/springframework/boot/SpringApplication::run` | NO_COVERAGE | KILLED |
| `ApplicationWorker` | 9 | `NON_VOID_METHOD_CALLS` | `removed call to org/springframework/boot/SpringApplication::run` | NO_COVERAGE | KILLED |

### Grupo 4: mutante equivalente (1 sobrevivente)

| Classe | Linha | Operador | Mutação | Inicial | Final |
|---|---|---|---|---|---|
| `EnviarNotificacaoWorker` | 109 | `REMOVE_CONDITIONALS_EQUAL_IF` | `removed conditional - replaced equality check with true` | SURVIVED | SURVIVED |

Código original:

```java
if (dataPadraoIso == null || dataPadraoIso.isEmpty()) return "";
```

A linha tem duas condições, e o Pitest gera mutantes para cada uma. Por eliminação, este mutante é o que faz o programa nunca entrar no caso `dataPadraoIso == null` e seguir para `isEmpty()`. Os outros mutantes da mesma linha mudam o resultado e foram mortos:

| Mutação na linha 109 | Efeito | Resultado |
|---|---|---|
| `isEmpty()` sempre verdadeiro | Toda data vira `""` | Morto |
| `isEmpty()` sempre falso | `LocalDate.parse("")` lança exceção | Morto |
| Sempre entra no caso `null` | Toda data vira `""` | Morto |
| **Nunca entra no caso `null`** | Segue para `isEmpty()` | **Sobrevive** |

**Por que é equivalente:**

- **Alcance:** `formatarDataParaBrasileiro` é privado, e o único chamador é a linha 91.
- **Infecção:** o valor recebido vem de `obterVariavelString`, que troca `null` por `""`, então `dataPadraoIso` nunca é `null`. Com qualquer entrada possível, o código original e o mutante seguem o mesmo caminho.
- **Propagação:** como não há diferença de estado, nada muda no resultado observável.

**Por que não foi criado um teste:** matar este mutante exigiria chamar o método privado com `null` por reflexão, uma situação que não ocorre em produção. O teste ficaria acoplado à implementação e não testaria nenhum comportamento real.

**Como eliminá-lo:** a verificação `dataPadraoIso == null` é código morto, e removê-la eliminaria o mutante. Isso não foi feito porque o trabalho exige manter o código-fonte original.

## Testes

| Módulo | Testes antes | Testes novos | Total |
|---|---|---|---|
| `engine` | 14 | 5 (`CorsConfigTest`: 4, `ApplicationTest`: 1) | 19 |
| `worker` | 10 | 2 (`ExternalTaskConfigTest`: 1, `ApplicationWorkerTest`: 1) | 12 |

## Como reproduzir

```
mvn verify -Pmutacao
```

O comando executa os testes, gera os mutantes com o Pitest e atualiza os relatórios em `docs/pit-reports`. O teste apontado como responsável por matar cada mutante pode variar entre execuções. As contagens e o status dos mutantes se repetem.
