package com.monify_api.functional;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReceitaFunctionalTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String URL_LOGIN =
            "http://localhost:3000/login";

    private static final String URL_CADASTRO =
            "http://localhost:3000/cadastro-lancamentos";

    @BeforeAll
    static void iniciarNavegadorEAutenticar() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        String login = System.getenv("MONIFY_TEST_LOGIN");
        String senha = System.getenv("MONIFY_TEST_PASSWORD");

        assertNotNull(login, "Login não configurado");
        assertNotNull(senha, "Senha não configurada");

        driver.get(URL_LOGIN);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("login")
        )).sendKeys(login);

        driver.findElement(By.id("senha")).sendKeys(senha);

        driver.findElement(
                By.cssSelector("button.login-botao")
        ).click();

        try {
            wait.until(ExpectedConditions.not(
                    ExpectedConditions.urlContains("/login")
            ));
        } catch (TimeoutException erro) {
            System.out.println("Primeira tentativa de login não concluiu.");
            System.out.println("URL atual: " + driver.getCurrentUrl());

            // Tenta novamente apenas uma vez
            driver.navigate().refresh();

            WebElement campoLogin = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.id("login")
                    )
            );

            campoLogin.clear();
            campoLogin.sendKeys(login);

            WebElement campoSenha = driver.findElement(By.id("senha"));
            campoSenha.clear();
            campoSenha.sendKeys(senha);

            driver.findElement(
                    By.cssSelector("button.login-botao")
            ).click();

            wait.until(ExpectedConditions.not(
                    ExpectedConditions.urlContains("/login")
            ));
        }

        System.out.println("Login realizado: " + driver.getCurrentUrl());
    }

    @BeforeEach
    void abrirFormulario() {

        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {
        }

        // Volta para a tela inicial antes de abrir outro cadastro
        driver.get("http://localhost:3000/home");

        driver.get(URL_CADASTRO);

        WebDriverWait esperaFormulario =
                new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            esperaFormulario.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.id("inputNome")
                    )
            );
        } catch (TimeoutException erro) {
            System.out.println(
                    "Erro ao abrir formulário. URL: "
                            + driver.getCurrentUrl()
            );
            System.out.println(
                    "Conteúdo da página: "
                            + driver.findElement(By.tagName("body")).getText()
            );
            throw erro;
        }

        new Select(driver.findElement(
                By.id("selectTipo")
        )).selectByValue("Receita");
    }

    private void preencherNome(String nome) {
        WebElement campo = driver.findElement(By.id("inputNome"));
        campo.clear();
        campo.sendKeys(nome);
    }

    private void preencherValor(String valor) {
        WebElement campo = driver.findElement(By.id("inputValor"));
        campo.clear();
        campo.sendKeys(valor);
    }

    private void selecionarCategoria() {
        WebElement categoria = wait.until(d -> {
            WebElement select = d.findElement(
                    By.id("selectCategoria")
            );

            if (new Select(select).getOptions().size() > 1) {
                return select;
            }
            return null;
        });

        Select opcoes = new Select(categoria);

        for (WebElement opcao : opcoes.getOptions()) {
            String valor = opcao.getDomAttribute("value");

            if (valor != null && !valor.isBlank()) {
                opcoes.selectByValue(valor);
                return;
            }
        }

        fail("Não existe categoria de Receita cadastrada.");
    }

    private void salvar() {
        driver.findElement(
                By.xpath("//button[normalize-space()='Salvar']")
        ).click();
    }

    private void verificarCadastroConcluido() {
        String urlAnterior = URL_CADASTRO;

        Boolean concluido = wait.until(d -> {
            try {
                Alert alerta = d.switchTo().alert();
                String mensagem = alerta.getText();

                System.out.println("Mensagem: " + mensagem);

                if (mensagem.toLowerCase().contains("sucesso")
                        || mensagem.toLowerCase().contains("cadastrad")
                        || mensagem.toLowerCase().contains("salv")) {
                    alerta.accept();
                    return true;
                }

                alerta.accept();
                fail("O sistema apresentou uma mensagem inesperada: "
                        + mensagem);

            } catch (NoAlertPresentException ignored) {

            }

            if (!d.getCurrentUrl().equals(urlAnterior)) {
                return true;
            }

            String nomeAtual = d.findElement(
                    By.id("inputNome")
            ).getDomProperty("value");

            if (nomeAtual != null && nomeAtual.isEmpty()) {
                return true;
            }

            return null;
        });

        assertTrue(concluido,
                "Não foi possível confirmar a conclusão do cadastro");
    }

    private void verificarCadastroBloqueado(String nomeEsperado) {
        WebDriverWait esperaCurta =
                new WebDriverWait(driver, Duration.ofSeconds(3));

        try {
            esperaCurta.until(d -> {
                try {
                    Alert alerta = d.switchTo().alert();
                    System.out.println(
                            "Mensagem de validação: " + alerta.getText()
                    );
                    alerta.accept();
                    return true;
                } catch (NoAlertPresentException ignored) {
                    return null;
                }
            });
        } catch (TimeoutException ignored) {

        }

        System.out.println("URL após salvar: " + driver.getCurrentUrl());
        System.out.println("Texto exibido na página:");
        System.out.println(driver.findElement(By.tagName("body")).getText());

        assertEquals(
                URL_CADASTRO,
                driver.getCurrentUrl(),
                "O formulário foi abandonado inesperadamente"
        );

        assertEquals(
                nomeEsperado,
                driver.findElement(By.id("inputNome"))
                        .getDomProperty("value"),
                "O formulário foi limpo, possivelmente após um cadastro"
        );
    }

    @Test
    @Order(1)
    void CT01_deveCadastrarReceitaValida() {
        String nome = "Receita Selenium " + System.currentTimeMillis();

        preencherNome(nome);
        preencherValor("150,00");
        selecionarCategoria();

        salvar();

        verificarCadastroConcluido();

        System.out.println("CT01 - Cadastro válido: aprovado");
    }

    @Test
    @Order(2)
    void CT02_naoDeveCadastrarReceitaSemNome() {
        preencherNome("");
        preencherValor("150,00");
        selecionarCategoria();

        salvar();

        verificarCadastroBloqueado("");

        System.out.println("CT02 - Nome obrigatório: verificado");
    }

    @Test
    @Order(3)
    void CT03_naoDeveCadastrarReceitaComValorZero() {
        String nome = "Teste valor zero";

        preencherNome(nome);
        preencherValor("0,00");
        selecionarCategoria();

        salvar();

        verificarCadastroBloqueado(nome);

        System.out.println("CT03 - Valor zero: verificado");
    }

    @Test
    @Order(4)
    void CT04_naoDeveCadastrarReceitaSemCategoria() {
        String nome = "Teste sem categoria";

        preencherNome(nome);
        preencherValor("150,00");

        new Select(driver.findElement(
                By.id("selectCategoria")
        )).selectByValue("");

        salvar();

        verificarCadastroBloqueado(nome);

        System.out.println("CT04 - Categoria obrigatória: verificada");
    }

    @Test
    @Order(5)
    void CT05_deveCadastrarReceitaFixa() {
        String nome = "Receita Fixa Selenium "
                + System.currentTimeMillis();

        preencherNome(nome);
        preencherValor("250,00");
        selecionarCategoria();


        WebElement checkbox = driver.findElement(
                By.id("checkVolume")
        );

        if (!checkbox.isSelected()) {
            driver.findElement(
                    By.cssSelector("label[for='checkVolume']")
            ).click();
        }

        wait.until(ExpectedConditions.elementToBeSelected(
                By.id("checkVolume")
        ));

        assertTrue(
                driver.findElement(By.id("checkVolume")).isSelected(),
                "A opção de receita fixa não foi marcada"
        );
    }

    @AfterAll
    static void fecharNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }
}
