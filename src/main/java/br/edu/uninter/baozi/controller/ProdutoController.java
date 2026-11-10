package br.edu.uninter.baozi.controller;

import br.edu.uninter.baozi.model.Produto;
import br.edu.uninter.baozi.repository.ProdutoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoRepository repositorio;

    public ProdutoController(ProdutoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody Cadastro dados) {
        Produto salvo = repositorio.save(new Produto(dados.nome(), dados.preco(), dados.estoque()));
        return ResponseEntity.created(URI.create("/api/produtos/" + salvo.getId()))
                .body(salvo);
    }

    @GetMapping
    public List<Produto> listar() {
        return repositorio.findAll(Sort.by("id"));
    }

    @GetMapping("/{id}")
    public Produto consultar(@PathVariable Long id) {
        return buscar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            repositorio.delete(buscar(id));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registro vinculado a um pedido.", e);
        }
        return ResponseEntity.noContent().build();
    }

    private Produto buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado."));
    }

    public record Cadastro(@NotBlank @Size(max = 150) String nome, @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal preco, @NotNull Boolean estoque) {
    }
}
