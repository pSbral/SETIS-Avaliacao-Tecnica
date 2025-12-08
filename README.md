
# SETIS – API de Usuários (versão revisada)

API REST para gerenciamento de usuários, desenvolvida em **Java + Spring Boot**, utilizando **PostgreSQL** e **Docker**.  

---

## 1. Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.x**   
- Spring Web (REST)
- Spring Data JPA (Hibernate)
- Bean Validation (Jakarta Validation)
- Lombok
- PostgreSQL
- H2 (para testes)   
- **Flyway** (migrações de banco)
- **ModelMapper** (mapeamento entre entidades e DTOs)
- **ULID Creator** (IDs string únicas)
- **ArchUnit** (testes de arquitetura)
- Springdoc OpenAPI (Swagger UI)   
- JUnit 5 + Mockito

---

## 2. Arquitetura do Projeto

Pacotes principais:

```text
src/main/java/com/psbral/projeto
    ├── config/
    │     └── ConfigModelMapper.java
    ├── controllers/
    │     └── UserController.java
    ├── dto/
    │     └── UserDTO.java      // Request e Response
    ├── models/
    │     └── User.java
    ├── repository/
    │     └── UserRepository.java
    ├── services/
    │     ├── ServiceRepository.java
    │     └── UserService.java
    └── services/exceptions/
          ├── ApiExceptionHandler.java
          └── models/ApiError.java

src/test/java/com/psbral/projeto
    ├── services/
    │     └── UserServiceTest.java
    ├── controllers/
    │     └── UserControllerTest.java
    └── ArchitectureTest.java
````

Padrões adotados:

* Controller → Service → Repository → Model
* Controller **não acessa** diretamente o Repository (validado em `ArchitectureTest` com ArchUnit).
* ModelMapper para conversão entre `User` e `UserDTO`.

---

## 3. Modelo de Usuário

### Entidade `User`

Campos principais:

| Campo      | Tipo          | Regra / Observação                        |
| ---------- | ------------- | ----------------------------------------- |
| id         | String        | ULID gerado automaticamente (@PrePersist) |
| name       | String        | 4 a 50 caracteres                         |
| email      | String        | Único, formato válido                     |
| birthDate  | LocalDate     | Não pode ser futura                       |
| createdAt  | LocalDateTime | Definido automaticamente na criação       |
| lastUpdate | LocalDateTime | Atualizado automaticamente                |

IDs são gerados com `UlidCreator` no `@PrePersist`.

### DTOs `UserDTO`

`UserDTO` é um record que agrupa dois subrecords:

* `UserDTO.Request`

    * Campos de entrada (request body)
    * Possui validações de Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@PastOrPresent`, etc.)
    * Pode incluir campos internos quando necessário (ex.: `id`, `createdAt`, `lastUpdate`) para cenários de atualização.

* `UserDTO.Response`

    * **Somente campos que podem ser expostos ao cliente:**

        * `name`
        * `email`
        * `birthDate`
    * Não expõe `id`, `createdAt`, `lastUpdate` nem outros detalhes internos.

---

## 4. Banco de Dados e Configuração

### 4.1 `application.properties`

Banco PostgreSQL local:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/usuarios
spring.datasource.username=setis
spring.datasource.password=setis123

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

Observações:

* `ddl-auto=none`: o schema é controlado via **Flyway** (migrações).
* As migrações devem estar em `src/main/resources/db/migration` seguindo o padrão `V1__...sql`, `V2__...sql`, etc.

---

## 5. Execução com Docker e Docker Compose

O projeto inclui:

* `Dockerfile` para construir a imagem da aplicação
* `docker-compose.yml` para subir **PostgreSQL + aplicação** juntos

### 5.1 Subir tudo com Docker Compose

Na raiz do projeto:

```bash
docker compose up -d
```

* Serviço `postgres` sobe o banco com:

    * DB: `usuarios`
    * Usuário: `setis`
    * Senha: `setis123`
* Serviço `app`:

    * Builda o jar via Maven
    * Sobe a aplicação na porta `8080`
    * Conecta no serviço `postgres` via `jdbc:postgresql://postgres:5432/usuarios`

A API ficará acessível em:

```text
http://localhost:8080
```

---

## 6. Execução Local (sem Docker)

Pré-requisitos:

* Java 17+
* Maven
* PostgreSQL rodando localmente com:

    * DB: `usuarios`
    * USER: `setis`
    * PASSWORD: `setis123`

Passos:

```bash
# Rodar somente a aplicação
mvn spring-boot:run
```

Ou:

```bash
# Gerar o jar
mvn clean package

# Executar o jar
java -jar target/SETIS-Avaliacao-Tecnica-0.0.1-SNAPSHOT.jar
```

---

## 7. Endpoints

Base path (exemplo): `/users`

### 7.1 Criar Usuário

`POST /users`

Request body (UserDTO.Request):

```json
{
  "name": "Joao Silva",
  "email": "joao@example.com",
  "birthDate": "1990-05-10"
}
```

Response (UserDTO.Response):

```json
{
  "name": "Joao Silva",
  "email": "joao@example.com",
  "birthDate": "1990-05-10"
}
```

Retorna `201 Created` com header `Location` apontando para `/users/{id}`.

---

### 7.2 Listar Usuários

`GET /users`

Response:

```json
[
  {
    "name": "Joao Silva",
    "email": "joao@example.com",
    "birthDate": "1990-05-10"
  },
  {
    "name": "Maria Souza",
    "email": "maria@example.com",
    "birthDate": "1988-11-20"
  }
]
```

---

### 7.3 Buscar por ID

`GET /users/{id}`

Response (200):

```json
{
  "name": "Joao Silva",
  "email": "joao@example.com",
  "birthDate": "1990-05-10"
}
```

Se não existir, retorna erro padrão `ApiError` com `404 Not Found`.

---

### 7.4 Atualizar Usuário

`PUT /users/{id}`

Request:

```json
{
  "name": "Joao da Silva",
  "email": "joao.silva@example.com",
  "birthDate": "1990-05-10"
}
```

Response:

```json
{
  "name": "Joao da Silva",
  "email": "joao.silva@example.com",
  "birthDate": "1990-05-10"
}
```

---

### 7.5 Deletar Usuário

`DELETE /users/{id}`

* Sucesso: `204 No Content`
* Se não existir: erro `404` com `ApiError`.

---

## 8. Padrão de Erros (`ApiError`)

Todos os erros tratados pelo `ApiExceptionHandler` retornam um JSON com o seguinte formato (`ApiError`):

```json
{
  "timestamp": "2025-12-08T12:34:56.789Z",
  "value": 400,
  "message": "E-mail já cadastrado: joao@example.com",
  "error": "Bad Request",
  "path": "/users"
}
```

Erros cobertos:

* `MethodArgumentNotValidException`

    * Campos inválidos (Bean Validation) → 400
* `EntityNotFoundException`

    * Usuário não encontrado → 404
* `IllegalArgumentException`

    * Negócio (e-mail duplicado, integridade referencial) → 400
* `Exception` (fallback)

    * Erro inesperado → 500 “Unexpected error”

---

## 9. Regras de Negócio

* **E-mail único**:

    * Ao criar ou atualizar, o service verifica `existsByEmail` no `UserRepository`.
    * Em caso de duplicidade, lança `IllegalArgumentException` com mensagem específica.

* **Data de nascimento**:

    * `@PastOrPresent`: não permite datas futuras.

* **IDs ULID**:

    * Gerados automaticamente no `@PrePersist` se `id == null`.

---

## 10. Documentação Swagger

Disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

JSON OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

(springdoc configurado via `springdoc-openapi-starter-webmvc-ui`).

---

## 11. Testes

* `UserServiceTest`

    * Inserção com e sem e-mail duplicado
    * Atualização com verificação de conflito
    * Busca por ID inexistente
    * Remoção com falha de integridade
    * Remoção de usuário existente

* `UserControllerTest`

    * Valida o comportamento da camada REST (ex.: status, payload, validações).

* `ArchitectureTest`

    * Garante que controllers não acessam diretamente o package `repository`.
    * Mantém a separação entre camadas e boas práticas de arquitetura.

