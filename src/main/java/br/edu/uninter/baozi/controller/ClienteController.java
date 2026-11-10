package br.edu.uninter.baozi.controller;

import br.edu.uninter.baozi.model.Cliente;
import br.edu.uninter.baozi.repository.ClienteRepository;
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
import java.time.LocalDate;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteRepository repositorio;

    public ClienteController(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody Cadastro dados) {
        Cliente salvo = repositorio.save(new Cliente(dados.nome(), dados.clienteDesde()));
        return ResponseEntity.created(URI.create("/api/clientes/" + salvo.getId()))
                .body(salvo);
    }

    @GetMapping
    public List<Cliente> listar() {
        return repositorio.findAll(Sort.by("id"));
    }

    @GetMapping("/{id}")
    public Cliente consultar(@PathVariable Long id) {
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

    private Cliente buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado."));
    }

    public record Cadastro(@NotBlank @Size(max = 150) String nome, @NotNull @PastOrPresent LocalDate clienteDesde) {
    }
}
