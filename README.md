Segue um README atualizado, refletindo o estado atual do projeto. 

---

# SETIS – Avaliação Técnica

API REST para Gerenciamento de Usuários

Projeto desenvolvido como parte da Avaliação Técnica da SETIS.
Consiste em uma **API REST** construída com Java + Spring Boot, utilizando **PostgreSQL em Docker**, **Flyway** para migrações e **ULID** como identificador de usuário.

A aplicação expõe operações CRUD para usuários, garantindo:

* Validação de campos via Jakarta Bean Validation
* E-mail único na base
* IDs string gerados como ULID
* Datas gerenciadas automaticamente na entidade
* Regras transacionais no `Service` (@Transactional readOnly / write)
* Tratamento centralizado de erros com payload padronizado
* Documentação Swagger (Springdoc OpenAPI)
* Testes unitários com JUnit 5 + Mockito

---

## 1. Tecnologias Utilizadas

* **Java 17**
* **Spring Boot 3.5.x**
* **Spring Web (REST)**
* **Spring Data JPA (Hibernate)**
* **PostgreSQL (via Docker)**
* **Flyway (migrações de banco)**
* **Jakarta Bean Validation**
* **ModelMapper**
* **ULID Creator** (IDs string)
* **Lombok**
* **Springdoc OpenAPI (Swagger UI)**
* **JUnit 5 + Mockito**

---

## 2. Arquitetura do Projeto

```text
src/main/java/com/psbral/projeto/
    ├── config/
    │     └── ConfigModelMapper.java
    ├── controllers/
    │     └── UserController.java
    ├── dto/
    │     └── UserDTO.java
    ├── models/
    │     └── User.java
    ├── repository/
    │     ├── UserRepository.java
    │     └── ServiceRepository.java
    ├── services/
    │     └── UserService.java
    ├── services/exceptions/
    │     └── ApiExceptionHandler.java
    ├── services/exceptions/models/
    │     └── ApiError.java
    └── SetisAvaliacaoTecnicaApplication.java

src/main/resources/
    ├── application.properties
    └── db/migration/
        └── (scripts Flyway para criar/atualizar o schema)

src/test/java/com/psbral/projeto/services/
    └── UserServiceTest.java
```

---

## 3. Modelo de Usuário (Entidade `User`)

| Campo      | Tipo          | Regra / Observação                                                      |
| ---------- | ------------- | ----------------------------------------------------------------------- |
| id         | String (ULID) | 26 caracteres, gerado automaticamente em `@PrePersist`, não atualizável |
| name       | String        | Obrigatório, máx. 50 caracteres                                         |
| email      | String        | Obrigatório, formato válido, máx. 254 caracteres, **único** na base     |
| birthDate  | LocalDate     | Obrigatória, não pode ser futura                                        |
| createdAt  | LocalDateTime | Preenchido em `@PrePersist`                                             |
| lastUpdate | LocalDateTime | Preenchido em `@PrePersist` e atualizado em `@PreUpdate`                |

---

## 4. DTO e Validações

O tráfego da API (request/response) usa o `UserDTO`:

```java
public record UserDTO(
        String id,
        @NotBlank @Size(min = 4, max = 50) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotNull @PastOrPresent LocalDate birthDate,
        LocalDate createdAt,
        LocalDate lastUpdate
) {}
```

Nos controllers, os endpoints que recebem o DTO usam `@Valid` para acionar as validações:

```java
@PostMapping
public ResponseEntity<UserDTO> insert(@RequestBody @Valid UserDTO dto) { ... }

@PutMapping("/{id}")
public UserDTO update(@PathVariable String id,
                      @Valid @RequestBody UserDTO user) { ... }
```

---

## 5. Regras de Negócio no Service

Classe: `UserService`

* **E-mail único no insert**

  ```java
  @Transactional
  public UserDTO insert(UserDTO dto) {
      if (repository.existsByEmail(dto.email())) {
          throw new IllegalArgumentException("E-mail já cadastrado: " + dto.email());
      }
      ...
  }
  ```

* **E-mail único no update (considerando alteração)**

  ```java
  @Transactional
  public UserDTO update(String id, UserDTO dto) {
      User user = repository.getReferenceById(id);

      if (!user.getEmail().equals(dto.email())
              && repository.existsByEmail(dto.email())) {
          throw new IllegalArgumentException("E-mail já cadastrado: " + dto.email());
      }
      ...
  }
  ```

* **Transações**

  * Métodos de **leitura**:

    ```java
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() { ... }

    @Transactional(readOnly = true)
    public UserDTO findById(String id) { ... }
    ```

  * Métodos de **escrita**:

    ```java
    @Transactional
    public UserDTO insert(UserDTO dto) { ... }

    @Transactional
    public UserDTO update(String id, UserDTO dto) { ... }

    @Transactional
    public void delete(String id) { ... }
    ```

* **Delete com validação de existência + integridade referencial**

  ```java
  @Transactional
  public void delete(String id) {
      if (!repository.existsById(id)) {
          throw new IllegalArgumentException("Usuário não encontrado - id: " + id);
      }
      try {
          repository.deleteById(id);
      } catch (DataIntegrityViolationException e) {
          throw new IllegalArgumentException("Falha de integridade referencial - id: " + id);
      }
  }
  ```

---

## 6. Banco de Dados via Docker

Arquivo `docker-compose.yml` na raiz:

```yaml
services:
  postgres:
    image: postgres:14
    container_name: postgres-setis
    restart: always
    environment:
      POSTGRES_USER: setis
      POSTGRES_PASSWORD: setis123
      POSTGRES_DB: usuarios
    ports:
      - "5432:5432"
```

### Subindo o banco

Na raiz do projeto:

```bash
docker compose up -d
```

Verificar se o container está rodando:

```bash
docker ps
```

---

## 7. Configuração da Aplicação

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/usuarios
spring.datasource.username=setis
spring.datasource.password=setis123

# Schema gerenciado pelo Flyway
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

O Flyway usa os scripts em `src/main/resources/db/migration` para criar/atualizar o schema.

---

## 8. Como Rodar a Aplicação

### Via IDE (IntelliJ / Eclipse)

Executar a classe:

```text
SetisAvaliacaoTecnicaApplication
```

### Via terminal

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

---

## 9. Endpoints

Base path: `/users`

### 9.1 Criar usuário

`POST /users`

Request body:

```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "birthDate": "1990-05-10"
}
```

Resposta `201 Created`:

```json
{
  "id": "01JH6F61X5M7V3QG0K9R9W4B9C",
  "name": "João Silva",
  "email": "joao@example.com",
  "birthDate": "1990-05-10",
  "createdAt": "2025-12-05T12:34:56",
  "lastUpdate": "2025-12-05T12:34:56"
}
```

### 9.2 Listar usuários

`GET /users`

Resposta `200 OK` com lista de `UserDTO`.

### 9.3 Buscar usuário por ID

`GET /users/{id}`

Retorna `UserDTO` correspondente ou erro padronizado se não encontrar.

### 9.4 Atualizar usuário

`PUT /users/{id}`

Request body igual ao POST (sem necessidade de enviar `id`, `createdAt` ou `lastUpdate`).

### 9.5 Deletar usuário

`DELETE /users/{id}`

* `200`/`204` em caso de sucesso (dependendo da configuração do controller).
* Erro `400` se o usuário não existir ou em caso de violação de integridade.

---

## 10. Tratamento de Erros

O tratamento é centralizado em `ApiExceptionHandler`, que devolve sempre um objeto `ApiError`:

```java
public record ApiError(
    Instant timestamp,
    int value,
    String message,
    String error,
    String path
) {}
```

### Exemplos

**E-mail duplicado**

```json
{
  "timestamp": "2025-12-05T12:34:56.789Z",
  "value": 400,
  "message": "E-mail já cadastrado: joao@example.com",
  "error": "Bad Request",
  "path": "/users"
}
```

**Usuário não encontrado**

```json
{
  "timestamp": "2025-12-05T12:34:56.789Z",
  "value": 400,
  "message": "Usuário não encontrado - id: 01JH6F61X5M7V3QG0K9R9W4B9C",
  "error": "Bad Request",
  "path": "/users/01JH6F61X5M7V3QG0K9R9W4B9C"
}
```

**Erro de validação (`@Valid`)**

```json
{
  "timestamp": "2025-12-05T12:34:56.789Z",
  "value": 400,
  "message": "name: O nome deve ter entre 4 e 50 caracteres",
  "error": "Validation error",
  "path": "/users"
}
```

---

## 11. Documentação Swagger

Swagger UI disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

JSON OpenAPI em:

```text
http://localhost:8080/v3/api-docs
```

---

## 12. Testes Unitários

Classe principal de testes: `UserServiceTest`.

Cobertura (resumo):

* Inserção com e-mail novo
* Inserção com e-mail duplicado
* Atualização com verificação de e-mail duplicado
* Busca por ID inexistente
* Remoção de usuário existente
* Remoção com falha de integridade referencial
* Comportamento dos mocks do repositório e regras do `UserService`

Rodar testes:

```bash
mvn test
```
