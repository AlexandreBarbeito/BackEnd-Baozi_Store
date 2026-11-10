package br.edu.uninter.baozi.repository;

import br.edu.uninter.baozi.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
