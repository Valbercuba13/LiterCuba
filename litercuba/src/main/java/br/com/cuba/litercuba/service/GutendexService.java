package br.com.cuba.litercuba.service;

import br.com.cuba.litercuba.model.Autor;
import br.com.cuba.litercuba.model.Livro;
import br.com.cuba.litercuba.repository.AutorRepository;
import br.com.cuba.litercuba.repository.LivroRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GutendexService {

    private final String API_BASE_URL = "https://gutendex.com/books/";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos desconhecidos no JSON
    private static class LivroDTO {
        @JsonProperty("title")
        private String titulo;
        private JsonNode authors;


        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public JsonNode getAuthors() {
            return authors;
        }

        public void setAuthors(JsonNode authors) {
            this.authors = authors;
        }
    }

    public Livro buscarLivroPorTitulo(String titulo) {
        try {
            String encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
            String url = API_BASE_URL + "?search=" + encodedTitulo;
            JsonNode jsonResponse = fazerRequisicao(url);

            JsonNode primeiroLivroNode = jsonResponse.path("results").path(0);
            if (!primeiroLivroNode.isMissingNode()) {
                LivroDTO livroDTO = objectMapper.convertValue(primeiroLivroNode, LivroDTO.class);
                Livro livro = new Livro();
                livro.setTitulo(livroDTO.getTitulo());

                JsonNode primeiroAutor = livroDTO.getAuthors().path(0);
                if (!primeiroAutor.isMissingNode()) {
                    Autor autor = objectMapper.convertValue(primeiroAutor, Autor.class);
                    autor = autorRepository.save(autor);
                    livro.setAutor(autor);
                }

                return livroRepository.save(livro);
            } else {
                return null; // pobremas de livros não encontrado
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar livro por título: " + e.getMessage(), e);
        }
    }

    public List<Livro> listarLivros() {
        String url = API_BASE_URL;
        JsonNode jsonResponse = fazerRequisicao(url);

        List<Livro> livros = new ArrayList<>();
        for (JsonNode livroNode : jsonResponse.path("results")) {
            LivroDTO livroDTO = objectMapper.convertValue(livroNode, LivroDTO.class); // Usar LivroDTO aqui
            Livro livro = new Livro();
            livro.setTitulo(livroDTO.getTitulo());

            JsonNode primeiroAutor = livroDTO.getAuthors().path(0);
            if (!primeiroAutor.isMissingNode()) {
                Autor autor = objectMapper.convertValue(primeiroAutor, Autor.class);
                autor = autorRepository.save(autor);
                livro.setAutor(autor);
            }

            livros.add(livroRepository.save(livro));
        }

        return livros;
    }

    public List<Livro> listarLivrosPorIdioma(String idioma) {
        String url = API_BASE_URL + "?languages=" + idioma;
        JsonNode jsonResponse = fazerRequisicao(url);

        List<Livro> livros = new ArrayList<>();
        for (JsonNode livroNode : jsonResponse.path("results")) {
            LivroDTO livroDTO = objectMapper.convertValue(livroNode, LivroDTO.class);
            Livro livro = new Livro();
            livro.setTitulo(livroDTO.getTitulo());

            JsonNode primeiroAutor = livroDTO.getAuthors().path(0);
            if (!primeiroAutor.isMissingNode()) {
                Autor autor = objectMapper.convertValue(primeiroAutor, Autor.class);
                autor = autorRepository.save(autor);
                livro.setAutor(autor);
            }

            livros.add(livroRepository.save(livro));
        }

        return livros;
    }

    public List<Autor> listarAutores() {
        List<Livro> livros = livroRepository.findAll();
        return livros.stream()
                .map(Livro::getAutor)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Autor> listarAutoresVivosEm(int ano) {
        return autorRepository.findByAnoNascimentoLessThanEqualAndAnoFalecimentoGreaterThanEqual(ano, ano);
    }


    private JsonNode fazerRequisicao(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readTree(response.body());
            } else {
                throw new RuntimeException("Erro ao fazer requisição à API Gutendex. Código de status: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer requisição à API Gutendex: " + e.getMessage(), e);
        }
    }
}
