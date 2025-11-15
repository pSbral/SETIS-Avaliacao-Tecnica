# SETIS – Avaliação Técnica

API REST para Gerenciamento de Usuários

Este projeto foi desenvolvido como parte da Avaliação Técnica da SETIS.
Consiste em uma **API REST completa**, construída com Java + Spring Boot, utilizando **PostgreSQL em Docker** como diferencial técnico.

A aplicação permite realizar operações CRUD para usuários, garantindo:

* Validação de campos
* E-mail único
* Datas gerenciadas automaticamente
* Documentação Swagger
* Testes unitários com JUnit + Mockito

---

## **1. Tecnologias Utilizadas**

* **Java 17**
* **Spring Boot 3.x**
* **Spring Web (REST)**
* **Spring Data JPA (Hibernate)**
* **PostgreSQL via Docker**
* **Bean Validation**
* **Lombok**
* **Springdoc OpenAPI (Swagger UI)**
* **JUnit 5 + Mockito**

---

## **2. Arquitetura do Projeto**

```
src/main/java/com.psbral.projeto/
    ├── controllers/
    │     └── UserController.java
    ├── models/
    │     └── User.java
    ├── repository/
    │     └── UserRepository.java
    ├── services/
    │     └── UserService.java
    └── SetisAvaliacaoTecnicaApplication.java

src/test/java/com.psbral.projeto/services/
    └── UserServiceTest.java
```

---

## **3. Modelo de Usuário**

| Campo          | Tipo          | Regra                                      |
| -------------- | ------------- | ------------------------------------------ |
| id             | Long          | Gerado automaticamente                     |
| nome           | String        | Entre 4 e 50 caracteres                    |
| email          | String        | Formato válido, único, máx. 254 caracteres |
| dataNascimento | LocalDate     | Não pode ser futura                        |
| dataCriacao    | LocalDateTime | Gerado pela API (@PrePersist)              |
| dataEdicao     | LocalDateTime | Atualizado pela API (@PreUpdate)           |

---

# **4. Provisionando o Banco via Docker**

Este projeto utiliza PostgreSQL rodando em container Docker.

### **4.1 Arquivo docker-compose.yml**

Presente na raiz do projeto:

```yaml
version: '3.1'

services:
  postgres:
    image: postgres:15
    container_name: postgres-setis
    restart: always
    environment:
      POSTGRES_USER: setis
      POSTGRES_PASSWORD: setis123
      POSTGRES_DB: usuarios
    ports:
      - "5432:5432"
```

### **4.2 Subindo o banco**

No terminal (na raiz do projeto):

```bash
docker compose up -d
```

Para verificar:

```bash
docker ps
```

O container deve aparecer como `postgres-setis`.

---

## **5. Configuração do Banco**

O arquivo `application.properties` está configurado para usar o banco Docker:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/usuarios
spring.datasource.username=setis
spring.datasource.password=setis123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
```

---

# **6. Como Rodar a Aplicação**

### **Via Eclipse/IntelliJ**

Execute a classe:

```
SetisAvaliacaoTecnicaApplication
```

### **Via terminal**

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```
http://localhost:8080
```

---

# **7. Endpoints**

## **Criar usuário**

`POST /users`

```json
{
  "nome": "Joao Silva",
  "email": "joao@example.com",
  "dataNascimento": "1990-05-10"
}
```

---

## **Listar usuários**

`GET /users`

---

## **Buscar por ID**

`GET /users/{id}`

---

## **Atualizar usuário**

`PUT /users/{id}`

---

## **Deletar usuário**

`DELETE /users/{id}`

---

# **8. Testando com Insomnia/Postman**

Exemplo de JSON:

```json
{
  "nome": "Usuario Docker",
  "email": "usuario@example.com",
  "dataNascimento": "1995-10-10"
}
```

---

# **9. Documentação Swagger**

Disponível em:

```
http://localhost:8080/swagger-ui/index.html
```

A API também expõe o JSON OpenAPI em:

```
http://localhost:8080/v3/api-docs
```

---

# **10. Testes Unitários**

Classe: `UserServiceTest`

Cobertura:

* Inserção com e sem e-mail duplicado
* Atualização com validação de conflito de e-mail
* Busca por ID inexistente
* Remoção com falha de integridade
* Remoção de usuário existente
* Comportamento dos mocks
* Regras de negócio do Service

### Rodar testes:

```bash
mvn test
```

Ou pelo Eclipse → Run As → JUnit Test.

---

# **11. Mensagens de Erro Padronizadas**

### Email duplicado:

```
400 BAD REQUEST
"E-mail já cadastrado: xxx"
```

### Usuário não encontrado:

```
400 BAD REQUEST
"Usuário não encontrado - id: X"
```

### Violação de integridade:

```
400 BAD REQUEST
"Falha de integridade referencial - id: X"
```

---

# **12. Autor**

Projeto desenvolvido por **Pedro Loscilha Sobral**
Para a **Avaliação Técnica da SETIS**

Só pedir.
