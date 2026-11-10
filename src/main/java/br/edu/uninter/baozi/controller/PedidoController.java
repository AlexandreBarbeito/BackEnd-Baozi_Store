package br.edu.uninter.baozi.controller;

import br.edu.uninter.baozi.model.*;
import br.edu.uninter.baozi.repository.*;
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


@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoRepository repositorio;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;

    public PedidoController(PedidoRepository repositorio, ClienteRepository clientes, ProdutoRepository produtos) {
        this.repositorio = repositorio;
        this.clientes = clientes;
        this.produtos = produtos;
    }

    @PostMapping
    public ResponseEntity<Resumo> criar(@Valid @RequestBody Cadastro dados) {
        Cliente cliente = clientes.findById(dados.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));
        Produto produto = produtos.findById(dados.produtoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        if (!Boolean.TRUE.equals(produto.getEstoque())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto indisponível.");
        }

        Pedido salvo = repositorio.save(new Pedido(cliente, produto, dados.quantidade()));
        return ResponseEntity.created(URI.create("/api/pedidos/" + salvo.getId()))
                .body(Resumo.de(salvo));
    }

    @GetMapping
    public List<Resumo> listar() {
        return repositorio.findAll(Sort.by("id")).stream().map(Resumo::de).toList();
    }

    @GetMapping("/{id}")
    public Resumo consultar(@PathVariable Long id) {
        return Resumo.de(buscar(id));
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

    private Pedido buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado."));
    }

    public record Cadastro(@NotNull @Positive Long clienteId, @NotNull @Positive Long produtoId, @NotNull @Positive Integer quantidade) {
    }

    public record Resumo(Long id, Long clienteId, Long produtoId, Integer quantidade) {
        static Resumo de(Pedido pedido) {
            return new Resumo(pedido.getId(), pedido.getCliente().getId(),
                    pedido.getProduto().getId(), pedido.getQuantidade());
        }
    }
}
