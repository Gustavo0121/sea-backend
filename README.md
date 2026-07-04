# sea-backend

API REST para cadastro de clientes (CRUD), com autenticação e autorização por perfil (Admin / Usuário Padrão).

> Projeto em desenvolvimento incremental. Nesta fase (Fase 0 — setup) a aplicação já sobe, com Swagger e health-check disponíveis; autenticação e CRUD de clientes chegam nas próximas fases.

## Stack

- Java 8+
- Spring Boot 2.7 (Web, Data JPA, Validation, Actuator)
- SQLite (via `sqlite-jdbc`)
- Maven
- springdoc-openapi (Swagger)
- JUnit 5 + Mockito + Jacoco

## Pré-requisitos

- JDK 8 ou superior instalado (`java -version`)
- Maven 3.6+ instalado (`mvn -version`)

## Como rodar localmente

1. Clone o repositório e entre na pasta do projeto:
   ```bash
   git clone <url-do-repositorio>
   cd sea-backend
   ```

2. Suba a aplicação com o perfil de desenvolvimento (padrão):
   ```bash
   mvn spring-boot:run
   ```

   Ou, para gerar o `.jar` e rodar separadamente:
   ```bash
   mvn clean package
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

3. A aplicação sobe em `http://localhost:8080`. O banco SQLite (`sea-backend-dev.db`) é criado automaticamente na raiz do projeto.

## Verificando se está no ar

- Health-check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) → `{"status":"UP"}`
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Perfis

- `dev` (ativo por padrão): SQLite em arquivo (`sea-backend-dev.db`), SQL logado no console.
- `test`: SQLite em memória, schema recriado a cada execução (`ddl-auto: create-drop`).

Para rodar com outro perfil:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## Rodando os testes

```bash
mvn test
```

O relatório de cobertura (Jacoco) é gerado em `target/site/jacoco/index.html` após a execução dos testes.

## Estrutura do projeto

```
src/main/java/com/sea/backend
├── controller   # endpoints REST
├── service      # regras de negócio
├── repository   # acesso a dados (Spring Data JPA)
├── entity       # entidades JPA
├── dto          # objetos de entrada/saída da API
├── mapper       # conversão entre Entity e DTO
├── config       # configurações (OpenAPI, dialect SQLite, etc.)
├── security     # autenticação e autorização (JWT)
├── validation   # validadores customizados
├── exception    # tratamento global de erros
└── utils        # utilitários
```
