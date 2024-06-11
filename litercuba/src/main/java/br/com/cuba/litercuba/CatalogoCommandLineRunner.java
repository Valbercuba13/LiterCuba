package br.com.cuba.litercuba;

import br.com.cuba.litercuba.model.Autor;
import br.com.cuba.litercuba.model.Livro;
import br.com.cuba.litercuba.service.GutendexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

@Component
public class CatalogoCommandLineRunner implements CommandLineRunner {

    @Autowired
    private GutendexService gutendexService;

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        int opcao = -1; // Inicializa a variável 'opcao' tira o bug da opção

        do {
            System.out.println("\n=== Catálogo de Livros ===");
            System.out.println("1. Buscar livro por título");
            System.out.println("2. Listar todos os livros");
            System.out.println("3. Listar livros por idioma");
            System.out.println("4. Listar autores");
            System.out.println("5. Listar autores vivos em um ano");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Digite um número.");
                continue;
            }

            switch (opcao) {
                case 1:
                    System.out.print("Digite o título do livro: ");
                    String titulo = scanner.nextLine();
                    Livro livroEncontrado = gutendexService.buscarLivroPorTitulo(titulo);
                    if (livroEncontrado != null) {
                        System.out.println(livroEncontrado);
                    } else {
                        System.out.println("Livro não encontrado.");
                    }
                    break;
                case 2:
                    List<Livro> todosOsLivros = gutendexService.listarLivros();
                    todosOsLivros.forEach(System.out::println);
                    break;
                case 3:
                    System.out.print("Digite o idioma: ");
                    String idioma = scanner.nextLine();
                    List<Livro> livrosPorIdioma = gutendexService.listarLivrosPorIdioma(idioma);
                    livrosPorIdioma.forEach(System.out::println);
                    break;
                case 4:
                    List<Autor> todosOsAutores = gutendexService.listarAutores();
                    todosOsAutores.forEach(System.out::println);
                    break;
                case 5:
                    System.out.print("Digite o ano: ");
                    int ano = scanner.nextInt();
                    List<Autor> autoresVivos = gutendexService.listarAutoresVivosEm(ano);
                    autoresVivos.forEach(System.out::println);
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }
}
