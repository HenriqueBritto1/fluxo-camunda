import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Insere nas páginas HTML do Pitest as informações por operador de mutação, lidas do mutations.xml.
 * Uso: java RelatorioOperadores.java <pasta do relatório> <raiz do repositório>
 */
public class RelatorioOperadores {

    private static final String MARCADOR_INICIO = "<!-- operadores:inicio -->";
    private static final String MARCADOR_FIM = "<!-- operadores:fim -->";
    private static final String ANCORA_LINHA = "org.pitest.mutationtest.report.html.SourceFile_";
    private static final Pattern ID_ALEATORIO_DA_ANCORA = Pattern.compile("SourceFile@[0-9a-f]+");
    private static final Pattern OPERADOR_ATIVO = Pattern.compile("<li class='mutator'>([^<]+)</li>");
    private static final Pattern CLASSE_DO_TESTE = Pattern.compile("\\[class:([^\\]]+)\\]");
    private static final Pattern METODO_DO_TESTE = Pattern.compile("\\[(?:method|test-template):([^\\]]+)\\]");

    private static final String ESTILO = """
            <style>
            .operadores { background: #fff; color: #222; margin: 24px 0; }
            .operadores table { border-collapse: collapse; width: 100%; font-size: 13px; }
            .operadores th, .operadores td { border: 1px solid #ccc; padding: 6px 8px; text-align: left; vertical-align: top; }
            .operadores th { background: #eee; }
            .operadores .rolagem { overflow-x: auto; }
            .operadores .nota { color: #555; font-size: 12px; }
            .operadores .numero { text-align: right; white-space: nowrap; }
            .operadores code { white-space: nowrap; }
            .operadores .descricao { overflow-wrap: anywhere; min-width: 320px; }
            .operadores .total td { font-weight: bold; }
            .operadores .status { font-weight: bold; white-space: nowrap; }
            .operadores .status-detectado { color: #1b6e2c; }
            .operadores .status-sobreviveu { color: #b3261e; }
            .operadores .status-sem-cobertura { color: #8a5a00; }
            </style>
            """;

    private static final List<Operador> CATALOGO = List.of(
            new Operador("ConditionalsBoundaryMutator", "CONDITIONALS_BOUNDARY",
                    "Muda o limite de uma comparação, incluindo ou excluindo o valor exato.", "a < b  →  a <= b"),
            new Operador("EmptyObjectReturnValsMutator", "EMPTY_RETURNS",
                    "Faz o método retornar um valor vazio do seu tipo, como texto vazio ou lista vazia.", "return nome;  →  return \"\";"),
            new Operador("SwitchMutator", "EXPERIMENTAL_SWITCH",
                    "Troca o rótulo default de um switch por outro rótulo, e os demais rótulos pelo default.", "case A: x; default: y  →  case A: y; default: x"),
            new Operador("BooleanFalseReturnValsMutator", "FALSE_RETURNS",
                    "Faz um método booleano sempre retornar false.", "return ativo;  →  return false;"),
            new Operador("IncrementsMutator", "INCREMENTS",
                    "Inverte incrementos e decrementos de variáveis locais.", "i++  →  i--"),
            new Operador("InvertNegsMutator", "INVERT_NEGS",
                    "Remove a negação de um número.", "return -x;  →  return x;"),
            new Operador("MathMutator", "MATH",
                    "Troca uma operação matemática por outra.", "a - b  →  a + b"),
            new Operador("NullReturnValsMutator", "NULL_RETURNS",
                    "Faz um método que retorna objeto retornar null.", "return ferias;  →  return null;"),
            new Operador("PrimitiveReturnsMutator", "PRIMITIVE_RETURNS",
                    "Faz um método que retorna número primitivo retornar 0.", "return valor;  →  return 0;"),
            new Operador("RemoveConditionalMutator_EQUAL_ELSE", "REMOVE_CONDITIONALS_EQUAL_ELSE",
                    "Remove uma verificação de igualdade, forçando sempre um dos caminhos da instrução.", "if (a == b)  →  if (false)"),
            new Operador("RemoveConditionalMutator_EQUAL_IF", "REMOVE_CONDITIONALS_EQUAL_IF",
                    "Remove uma verificação de igualdade, forçando sempre o outro caminho da instrução.", "if (a == b)  →  if (true)"),
            new Operador("RemoveConditionalMutator_ORDER_ELSE", "REMOVE_CONDITIONALS_ORDER_ELSE",
                    "Remove uma comparação de ordem, forçando sempre um dos caminhos da instrução.", "if (a < b)  →  if (false)"),
            new Operador("RemoveConditionalMutator_ORDER_IF", "REMOVE_CONDITIONALS_ORDER_IF",
                    "Remove uma comparação de ordem, forçando sempre o outro caminho da instrução.", "if (a < b)  →  if (true)"),
            new Operador("BooleanTrueReturnValsMutator", "TRUE_RETURNS",
                    "Faz um método booleano sempre retornar true.", "return ativo;  →  return true;"),
            new Operador("VoidMethodCallMutator", "VOID_METHOD_CALLS",
                    "Remove a chamada a um método void.", "mailSender.send(msg);  →  (removido)"),
            new Operador("ConstructorCallMutator", "CONSTRUCTOR_CALLS",
                    "Troca a criação de um objeto por null.", "new CorsConfiguration()  →  null"),
            new Operador("NonVoidMethodCallMutator", "NON_VOID_METHOD_CALLS",
                    "Remove a chamada a um método que retorna valor, usando o valor padrão do tipo (null, 0 ou false).", "x = calcular();  →  x = null;"),
            new Operador("InlineConstantMutator", "INLINE_CONSTS",
                    "Altera uma constante literal escrita no código.", "10000  →  10001"),
            new Operador("NegateConditionalsMutator", "NEGATE_CONDITIONALS",
                    "Inverte uma condição.", "a == b  →  a != b"),
            new Operador("RemoveIncrementsMutator", "REMOVE_INCREMENTS",
                    "Remove um incremento ou decremento de variável local.", "i++  →  (removido)"),
            new Operador("ArgumentPropagationMutator", "EXPERIMENTAL_ARGUMENT_PROPAGATION",
                    "Troca a chamada de um método por um dos seus argumentos de mesmo tipo do retorno.", "x = ajustar(y);  →  x = y;"),
            new Operador("NakedReceiverMutator", "EXPERIMENTAL_NAKED_RECEIVER",
                    "Troca a chamada de um método pelo próprio objeto que recebe a chamada.", "builder.baseUrl(url)  →  builder"),
            new Operador("MemberVariableMutator", "EXPERIMENTAL_MEMBER_VARIABLE",
                    "Remove a atribuição a um campo, que fica com o valor padrão.", "this.servico = servico;  →  (removido)"),
            new Operador("BigDecimalMutator", "EXPERIMENTAL_BIG_DECIMAL",
                    "Troca uma operação de BigDecimal por outra.", "a.add(b)  →  a.subtract(b)"),
            new Operador("BigIntegerMutator", "EXPERIMENTAL_BIG_INTEGER",
                    "Troca uma operação de BigInteger por outra.", "a.add(b)  →  a.subtract(b)"),
            new Operador("RemoveSwitchMutator", "EXPERIMENTAL_REMOVE_SWITCH_MUTATOR_[0-99]",
                    "Remove um case de um switch, fazendo esse caso cair no default.", "case A: x;  →  (vai para o default)")
    );

    public static void main(String[] args) throws Exception {
        Path pastaRelatorio = Path.of(args[0]);
        Path raizRepositorio = Path.of(args[1]);
        Path arquivoMutacoes = pastaRelatorio.resolve("mutations.xml");

        if (!Files.exists(arquivoMutacoes)) {
            System.out.println("Sem mutations.xml em " + pastaRelatorio + "; nada a fazer.");
            return;
        }

        List<Mutante> mutantes = lerMutantes(arquivoMutacoes);
        List<Path> paginasDeClasse = listarPaginasDeClasse(pastaRelatorio);
        Set<String> operadoresAtivos = lerOperadoresAtivos(paginasDeClasse);

        for (Path pagina : paginasDeClasse) {
            String caminho = pastaRelatorio.relativize(pagina).toString().replace('\\', '/');
            List<Mutante> daPagina = mutantes.stream().filter(m -> m.pagina().equals(caminho)).toList();
            String secao = montarSecao(daPagina, "", "", false);
            gravarPagina(pagina, secao, "</h1>", false);
        }

        String cabecalho = montarDadosDaExecucao(raizRepositorio);
        String rodape = montarOperadoresSemMutantes(operadoresAtivos, mutantes);
        gravarPagina(pastaRelatorio.resolve("index.html"), montarSecao(mutantes, cabecalho, rodape, true), "Report generated by", true);

        System.out.println("Operadores inseridos em " + pastaRelatorio
                + " (" + (paginasDeClasse.size() + 1) + " arquivos HTML, " + mutantes.size() + " mutantes).");
    }

    private static List<Mutante> lerMutantes(Path arquivo) throws Exception {
        Document documento = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(arquivo.toFile());
        NodeList nos = documento.getElementsByTagName("mutation");
        List<Mutante> mutantes = new ArrayList<>();

        for (int i = 0; i < nos.getLength(); i++) {
            Element no = (Element) nos.item(i);
            String mutador = texto(no, "mutator");
            mutantes.add(new Mutante(
                    texto(no, "mutatedClass"),
                    texto(no, "sourceFile"),
                    texto(no, "mutatedMethod"),
                    Integer.parseInt(texto(no, "lineNumber")),
                    mutador.substring(mutador.lastIndexOf('.') + 1),
                    texto(no, "description"),
                    no.getAttribute("status"),
                    Boolean.parseBoolean(no.getAttribute("detected")),
                    texto(no, "killingTest")));
        }

        mutantes.sort(Comparator.comparingInt(Mutante::prioridadeDoStatus)
                .thenComparing(Mutante::classe)
                .thenComparingInt(Mutante::linha)
                .thenComparing(Mutante::operador)
                .thenComparing(Mutante::descricao));
        return mutantes;
    }

    private static String texto(Element no, String tag) {
        NodeList filhos = no.getElementsByTagName(tag);
        return filhos.getLength() == 0 ? "" : filhos.item(0).getTextContent().trim();
    }

    private static List<Path> listarPaginasDeClasse(Path pastaRelatorio) throws IOException {
        try (Stream<Path> arquivos = Files.walk(pastaRelatorio, 2)) {
            return arquivos.filter(a -> a.getFileName().toString().endsWith(".java.html")).sorted().toList();
        }
    }

    private static Set<String> lerOperadoresAtivos(List<Path> paginasDeClasse) throws IOException {
        Set<String> ativos = new LinkedHashSet<>();
        if (!paginasDeClasse.isEmpty()) {
            Matcher itens = OPERADOR_ATIVO.matcher(Files.readString(paginasDeClasse.get(0), StandardCharsets.UTF_8));
            while (itens.find()) {
                ativos.add(itens.group(1));
            }
        }
        return ativos;
    }

    private static String montarSecao(List<Mutante> mutantes, String cabecalho, String rodape, boolean noIndice) {
        return MARCADOR_INICIO + "\n" + ESTILO + "<section class=\"operadores\">\n"
                + "<h2>Operadores de mutação</h2>\n" + cabecalho
                + montarTabelaDeOperadores(mutantes) + rodape
                + "<h2>Mutantes</h2>\n" + montarTabelaDeMutantes(mutantes, noIndice)
                + "<p class=\"nota\">Este relatório mostra o status informado pelo Pitest e não classifica mutantes como equivalentes.</p>\n"
                + "</section>\n" + MARCADOR_FIM + "\n";
    }

    private static String montarTabelaDeOperadores(List<Mutante> mutantes) {
        Map<String, List<Mutante>> porOperador = new TreeMap<>(mutantes.stream().collect(Collectors.groupingBy(Mutante::operador)));
        StringBuilder html = new StringBuilder("<div class=\"rolagem\"><table>\n<thead><tr><th>Operador</th><th>O que faz</th><th>Exemplo</th>"
                + "<th>Gerados</th><th>Mortos</th><th>Sobreviventes</th><th>Sem cobertura</th><th>Nota</th></tr></thead>\n<tbody>\n");

        porOperador.forEach((nome, doOperador) -> {
            Operador operador = buscarNoCatalogo(nome);
            html.append("<tr><td><code>").append(escapar(operador.nomePitest())).append("</code></td><td>")
                    .append(escapar(operador.explicacao())).append("</td><td><code>")
                    .append(escapar(operador.exemplo())).append("</code></td>")
                    .append(celulasDeContagem(doOperador)).append("</tr>\n");
        });

        html.append("<tr class=\"total\"><td colspan=\"3\">Total</td>").append(celulasDeContagem(mutantes)).append("</tr>\n");
        html.append("</tbody></table></div>\n<p class=\"nota\">\"Mortos\" inclui os demais status que o Pitest conta como detectados, como TIMED_OUT. ")
                .append("A descrição de cada mutante se refere à instrução de bytecode; em condições compostas, como as com ||, ")
                .append("ela pode não corresponder literalmente à condição escrita em Java.</p>\n");
        return html.toString();
    }

    private static String celulasDeContagem(List<Mutante> mutantes) {
        long detectados = mutantes.stream().filter(Mutante::detectado).count();
        long sobreviventes = mutantes.stream().filter(m -> m.status().equals("SURVIVED")).count();
        long semCobertura = mutantes.stream().filter(m -> m.status().equals("NO_COVERAGE")).count();
        long nota = mutantes.isEmpty() ? 0 : Math.round(100.0 * detectados / mutantes.size());

        return Stream.of(String.valueOf(mutantes.size()), String.valueOf(detectados), String.valueOf(sobreviventes),
                        String.valueOf(semCobertura), nota + "%")
                .map(valor -> "<td class=\"numero\">" + valor + "</td>")
                .collect(Collectors.joining());
    }

    private static String montarTabelaDeMutantes(List<Mutante> mutantes, boolean noIndice) {
        StringBuilder html = new StringBuilder("<div class=\"rolagem\"><table>\n<thead><tr><th>Status</th><th>Classe</th><th>Método</th><th>Linha</th>"
                + "<th>Operador</th><th>Descrição</th><th>Morto por</th></tr></thead>\n<tbody>\n");

        for (Mutante mutante : mutantes) {
            html.append("<tr><td class=\"status ").append(mutante.classeDoStatus()).append("\">").append(escapar(mutante.status()))
                    .append("</td><td>").append(escapar(mutante.nomeSimplesDaClasse()))
                    .append("</td><td><code>").append(escapar(mutante.metodo()))
                    .append("</code></td><td class=\"numero\"><a href=\"").append(escapar(mutante.linkParaLinha(noIndice))).append("\">").append(mutante.linha())
                    .append("</a></td><td><code>").append(escapar(buscarNoCatalogo(mutante.operador()).nomePitest()))
                    .append("</code></td><td class=\"descricao\">").append(escapar(mutante.descricao()))
                    .append("</td><td>").append(escapar(mutante.testeResumido())).append("</td></tr>\n");
        }
        return html.append("</tbody></table></div>\n").toString();
    }

    private static String montarOperadoresSemMutantes(Set<String> ativos, List<Mutante> mutantes) {
        Set<String> comMutantes = mutantes.stream()
                .map(m -> buscarNoCatalogo(m.operador()).nomePitest())
                .collect(Collectors.toSet());

        StringBuilder html = new StringBuilder();
        for (String ativo : ativos) {
            if (!comMutantes.contains(ativo)) {
                String explicacao = CATALOGO.stream().filter(o -> o.nomePitest().equals(ativo))
                        .map(Operador::explicacao).findFirst().orElse("Sem explicação cadastrada.");
                html.append("<li><code>").append(escapar(ativo)).append("</code>: ").append(escapar(explicacao)).append("</li>\n");
            }
        }
        if (html.isEmpty()) {
            return "";
        }
        return "<h3>Operadores ativos sem mutantes</h3>\n<p class=\"nota\">Foram aplicados, mas não encontraram código onde atuar nas classes analisadas.</p>\n<ul>\n"
                + html + "</ul>\n";
    }

    private static Operador buscarNoCatalogo(String nomeDaClasse) {
        return CATALOGO.stream().filter(o -> o.classe().equals(nomeDaClasse)).findFirst()
                .orElse(new Operador(nomeDaClasse, nomeDaClasse, "Sem explicação cadastrada.", "-"));
    }

    private static String montarDadosDaExecucao(Path raizRepositorio) {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String commit = executarGit(raizRepositorio, "rev-parse", "--short", "HEAD");
        String alteracoes = executarGit(raizRepositorio, "status", "--porcelain", "--", ".", ":(exclude)docs/pit-reports");

        String estado = alteracoes.isEmpty() ? "" : " (com alterações não commitadas)";
        String descricaoCommit = commit.isEmpty() ? "indisponível" : commit + estado;
        return "<p>Execução: " + data + " | Commit: <code>" + escapar(descricaoCommit) + "</code></p>\n";
    }

    private static String executarGit(Path raizRepositorio, String... argumentos) {
        List<String> comando = new ArrayList<>(List.of("git"));
        comando.addAll(List.of(argumentos));
        try {
            Process processo = new ProcessBuilder(comando).directory(raizRepositorio.toFile())
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            try (BufferedReader saida = new BufferedReader(new InputStreamReader(processo.getInputStream(), StandardCharsets.UTF_8))) {
                String resultado = saida.lines().collect(Collectors.joining("\n")).trim();
                return processo.waitFor() == 0 ? resultado : "";
            }
        } catch (IOException excecao) {
            return "";
        } catch (InterruptedException excecao) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    private static void gravarPagina(Path pagina, String secao, String referencia, boolean inserirAntes) throws IOException {
        String html = Files.readString(pagina, StandardCharsets.UTF_8);
        html = ID_ALEATORIO_DA_ANCORA.matcher(html).replaceAll("SourceFile");

        int inicio = html.indexOf(MARCADOR_INICIO);
        if (inicio >= 0) {
            html = html.substring(0, inicio) + html.substring(html.indexOf(MARCADOR_FIM) + MARCADOR_FIM.length() + 1);
        }

        int posicao = html.indexOf(referencia);
        if (posicao < 0) {
            throw new IllegalStateException("Referência '" + referencia + "' não encontrada em " + pagina);
        }
        int insercao = inserirAntes ? posicao : posicao + referencia.length();
        Files.writeString(pagina, html.substring(0, insercao) + "\n" + secao + html.substring(insercao), StandardCharsets.UTF_8);
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private record Operador(String classe, String nomePitest, String explicacao, String exemplo) {
    }

    private record Mutante(String classe, String arquivo, String metodo, int linha, String operador,
                           String descricao, String status, boolean detectado, String teste) {

        int prioridadeDoStatus() {
            return switch (status) {
                case "SURVIVED" -> 0;
                case "NO_COVERAGE" -> 1;
                default -> detectado ? 3 : 2;
            };
        }

        String classeDoStatus() {
            return switch (status) {
                case "SURVIVED" -> "status-sobreviveu";
                case "NO_COVERAGE" -> "status-sem-cobertura";
                default -> detectado ? "status-detectado" : "";
            };
        }

        String nomeSimplesDaClasse() {
            return classe.substring(classe.lastIndexOf('.') + 1);
        }

        String pagina() {
            return classe.substring(0, classe.lastIndexOf('.')) + "/" + arquivo + ".html";
        }

        String linkParaLinha(boolean noIndice) {
            return (noIndice ? pagina() : "") + "#" + ANCORA_LINHA + linha;
        }

        String testeResumido() {
            if (teste.isEmpty()) {
                return "-";
            }
            Matcher classeDoTeste = CLASSE_DO_TESTE.matcher(teste);
            Matcher metodoDoTeste = METODO_DO_TESTE.matcher(teste);
            if (classeDoTeste.find() && metodoDoTeste.find()) {
                String nomeDaClasse = classeDoTeste.group(1);
                String nomeDoMetodo = metodoDoTeste.group(1);
                return nomeDaClasse.substring(nomeDaClasse.lastIndexOf('.') + 1) + "."
                        + nomeDoMetodo.substring(0, nomeDoMetodo.indexOf('(') < 0 ? nomeDoMetodo.length() : nomeDoMetodo.indexOf('('));
            }
            return teste;
        }
    }
}
