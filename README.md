# Baozi Store

Atividade prática de Desenvolvimento Web Back-End da UNINTER. API REST em Java 17, Spring Boot, Spring MVC, Spring Data JPA e H2.

## Executar

Requisitos: JDK 17 ou superior e Maven 3.6.3 ou superior.

```sh
mvn spring-boot:run
```

API disponível em http://localhost:8080/api/clientes, /api/produtos e /api/pedidos. Para cada recurso, use POST e GET na coleção e GET e DELETE em /{id}. A coleção Postman incluída demonstra as 12 operações com os dados do estudo de caso. Importe o JSON e execute os itens na ordem numérica. Os POSTs guardam os IDs nas variáveis da coleção. Os DELETEs removem primeiro o pedido e depois suas referências.

O banco H2 grava os dados em `./data/baozi.mv.db`. O console local fica em http://localhost:8080/h2-console. Use JDBC URL `jdbc:h2:file:./data/baozi`, usuário `sa` e senha local `senha123`.

## Endpoints e regras

| Recurso | POST e GET geral | GET por ID e DELETE |
|---|---|---|
| Cliente | `/api/clientes` | `/api/clientes/{id}` |
| Produto | `/api/produtos` | `/api/produtos/{id}` |
| Pedido | `/api/pedidos` | `/api/pedidos/{id}` |

Os POSTs retornam `201 Created`, o objeto JSON criado e o cabeçalho `Location`; as consultas retornam `200 OK` e as exclusões concluídas, `204 No Content`. O cliente exige nome e data válida; o produto exige preço positivo e usa `estoque` como indicador booleano de disponibilidade. Pedido exige referências existentes e quantidade positiva. Dados inválidos retornam `400`, IDs inexistentes `404`, e a exclusão de cliente/produto vinculado a pedido retorna `409`.

## Testes

Execute os testes automatizados de integração com `mvn clean verify`. Para os testes manuais, importe `Baozi_Store.postman_collection.json` no Postman e execute as 12 solicitações na ordem numérica; a coleção armazena e reutiliza os IDs retornados pelos POSTs.

