package br.edu.uninter.baozi.repository;

import br.edu.uninter.baozi.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
