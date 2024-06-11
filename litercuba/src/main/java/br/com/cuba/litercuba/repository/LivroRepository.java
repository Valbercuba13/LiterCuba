package br.com.cuba.litercuba.repository;

import br.com.cuba.litercuba.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
}
