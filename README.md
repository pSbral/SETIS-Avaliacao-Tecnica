# SETIS – Avaliação Técnica (API REST em Java + Spring Boot)

Este projeto consiste no desenvolvimento de uma **API REST** para gerenciamento de usuários, seguindo os requisitos da avaliação técnica SETIS.
A API foi desenvolvida utilizando **Java 17**, **Spring Boot**, **Spring Data JPA**, **H2 Database**, **Bean Validation**, **Swagger/OpenAPI** e **JUnit 5 com Mockito**.

---

## **1. Tecnologias utilizadas**

* **Java 17**
* **Spring Boot 3.x**
* **Spring Web** (REST Controller)
* **Spring Data JPA** (Hibernate)
* **H2 Database** (ambiente de desenvolvimento)
* **Bean Validation (Jakarta Validation)**
* **Lombok**
* **Swagger / OpenAPI (springdoc-openapi)**
* **JUnit 5 + Mockito** (testes unitários)

---

## **2. Funcionalidades da API**

A API permite realizar operações CRUD para o recurso **Usuário**:

* Criar usuário
* Listar todos os usuários
* Buscar usuário por ID
* Atualizar usuário
* Deletar usuário

Além disso, implementa:

* Validação de campos obrigatórios
* Validação de tamanho de campos
* Validação de e-mail único
* Datas de criação e edição gerenciadas automaticamente
* Testes unitários do serviço

---

## **3. Estrutura do Projeto**

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

src/test/java/com.psbral.projeto/
    └── services/
          └── SetisAvaliacaoTecnicaApplicationTests.java
```

---

## **4. Modelo de Usuário**

### Campos:

| Campo          | Tipo          | Regra                                          |
| -------------- | ------------- | ---------------------------------------------- |
| id             | Long          | Gerado automaticamente                         |
| nome           | String        | Obrigatório, mínimo 4, máximo 50               |
| email          | String        | Obrigatório, formato válido, único, máximo 254 |
| dataNascimento | LocalDate     | Obrigatório, não pode ser futura               |
| dataCriacao    | LocalDateTime | Gerenciado automaticamente (@PrePersist)       |
| dataEdicao     | LocalDateTime | Gerenciado automaticamente (@PreUpdate)        |

---

## **5. Como rodar o projeto**

### **Pré-requisitos**

* Java 17 instalado
* Maven instalado ou wrapper (`./mvnw`)
* Eclipse/IntelliJ opcional

### **Rodando**

No terminal:

```bash
mvn spring-boot:run
```

Ou pelo Eclipse:

* Run → Run As → Java Application
* Selecione: `ProjetoApplication`

A API subirá em:

```
http://localhost:8080
```

---

## **6. Banco H2**

A aplicação usa banco em memória.

Console H2 (opcional):

```
http://localhost:8080/h2-console
```

Configuração no `application.properties`:

```
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

---

## **7. Endpoints da API**

### **Criar usuário**

`POST /users`

```json
{
  "nome": "Joao Silva",
  "email": "joao@example.com",
  "dataNascimento": "1990-05-10"
}
```

---

### **Listar todos**

`GET /users`

---

### **Buscar por ID**

`GET /users/{id}`

---

### **Atualizar usuário**

`PUT /users/{id}`

```json
{
  "nome": "Joao Silva Atualizado",
  "email": "joao@example.com",
  "dataNascimento": "1990-05-10"
}
```

---

### **Deletar usuário**

`DELETE /users/{id}`

---

## **8. Regras de validação**

* `nome`

  * obrigatório
  * mínimo 4 caracteres
  * máximo 50

* `email`

  * obrigatório
  * formato válido
  * máximo 254
  * não pode repetir no sistema

* `dataNascimento`

  * obrigatório
  * não pode ser uma data futura

* `dataCriacao` e `dataEdicao`

  * gerenciadas pela API através de `@PrePersist` e `@PreUpdate`

---

## **9. Testes Unitários**

Os testes unitários utilizam **JUnit 5 e Mockito**, cobrindo:

* Inserção com e sem e-mail duplicado
* Atualização com validação de e-mail
* Remoção com erro ou sucesso
* Busca por ID válida e inválida
* CopyToUser (atualização de campos)
* Comportamento correto do repository mockado

Para rodar:

```bash
mvn test
```

Ou:

No Eclipse → Run As → JUnit Test

---

## **10. Documentação Swagger**

A documentação fica disponível em:

```
http://localhost:8080/swagger-ui/index.html
```

Dependência usada no `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.4</version>
</dependency>
```

---

## **11. Erros retornados**

### E-mail já cadastrado

```
400 BAD REQUEST
"E-mail já cadastrado: joao@example.com"
```

### Usuário não encontrado

```
400 BAD REQUEST
"Usuário não encontrado - id: 5"
```

### Violação de integridade ao deletar

```
400 BAD REQUEST
"Falha de integridade referencial - id: X"
```

---

## **12. Autor**

Projeto desenvolvido por **Pedro Loscilha Sobral** para a **avaliação técnica SETIS**.

