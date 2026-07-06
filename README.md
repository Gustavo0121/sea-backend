# sea-backend

API REST para cadastro de clientes (CRUD), com autenticação e autorização por perfil (Admin / Usuário Padrão).

> Projeto em desenvolvimento incremental. Login (JWT) e autorização por perfil já estão prontos (Fase 2), assim como os DTOs, validações e mappers do domínio Cliente (Fase 3). O CRUD de `/clientes` propriamente dito (Controller/Service/Repository) chega na Fase 5.

## Stack

- Java 8+
- Spring Boot 2.7 (Web, Data JPA, Validation, Actuator, Security)
- SQLite (via `sqlite-jdbc`)
- Spring Security + JWT (`jjwt`) com BCrypt
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

3. A aplicação sobe em `http://localhost:8080`. O banco SQLite (`sea-backend-dev.db`) é criado automaticamente na raiz do projeto, com as tabelas e os dois usuários iniciais já persistidos.

## Usuários iniciais

Criados automaticamente na primeira subida da aplicação (senha armazenada com BCrypt):

| Perfil  | Login   | Senha        |
|---------|---------|--------------|
| Admin   | `admin` | `123qwe!@#`  |
| Usuário | `user`  | `123qwe123`  |

## Autenticação (Fase 2)

Login gera um JWT (expiração padrão de 15 minutos, configurável via `JWT_EXPIRATION_MS`):

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","senha":"123qwe!@#"}'
```

Resposta:
```json
{ "token": "<jwt>", "tipo": "Bearer", "expiraEmSegundos": 900 }
```

Use o token nas rotas protegidas com `Authorization: Bearer <token>`.

Regras de autorização:

| Rota                          | ADMIN | USER |
|-------------------------------|:-----:|:----:|
| `GET /clientes`, `GET /clientes/{id}` | ✅ | ✅ |
| `POST /clientes`              | ✅    | ❌ (403) |
| `PUT /clientes/{id}`          | ✅    | ❌ (403) |
| `DELETE /clientes/{id}`       | ✅    | ❌ (403) |

Requisições sem token ou com token inválido/expirado retornam `401`. O CRUD de `/clientes` propriamente dito chega na Fase 5 — por ora as regras acima já valem para qualquer rota sob esse prefixo.

Em produção, sobrescreva o segredo padrão via variável de ambiente `JWT_SECRET` (nunca reutilize o valor de desenvolvimento do `application.yml`).

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
├── entity       # entidades JPA (Cliente, Endereco, Telefone, Email, Usuario, Role, TipoTelefone)
├── dto          # objetos de entrada/saída da API
├── mapper       # conversão entre Entity e DTO
├── config       # configurações (OpenAPI, dialect SQLite, seed de usuários, etc.)
├── security     # autenticação e autorização (JWT)
├── validation   # validadores customizados
├── exception    # tratamento global de erros
└── utils        # utilitários
```

## Modelo de dados (Fase 1)

- **Usuario**: `login`, `senha` (BCrypt), `role` (`ADMIN` ou `USER`).
- **Cliente**: `nome`, `cpf` (único), um `Endereco` e listas de `Telefone`/`Email`.
- **Endereco**: `cep`, `logradouro`, `bairro`, `cidade`, `uf`, `complemento` (opcional).
- **Telefone**: `tipo` (`RESIDENCIAL`, `COMERCIAL`, `CELULAR`) e `numero`.
- **Email**: `endereco`.

## DTOs, validação e mapeamento (Fase 3)

Os contratos de entrada/saída de `Cliente` já estão prontos, embora ainda sem endpoint (chega na Fase 5):

- **`ClienteRequestDTO`** (entrada): `nome` (3-100 caracteres, apenas letras/números/espaços), `cpf` (validado por dígito verificador via `@CpfValido`), `endereco` (`EnderecoRequestDTO` aninhado), `telefones`/`emails` (listas, mínimo 1 item cada). `TelefoneRequestDTO` valida a quantidade de dígitos conforme o `tipo` (`@TelefoneValido`: 11 dígitos para `CELULAR`, 10 para `RESIDENCIAL`/`COMERCIAL`).
- **`ClienteResponseDTO`** (saída): nunca expõe a Entity — CPF, CEP e telefone já retornam mascarados.
- **`ClienteMapper`** (+ `EnderecoMapper`, `TelefoneMapper`, `EmailMapper`, em `mapper/`): convertem DTO ↔ Entity, normalizando entrada (`utils.DigitExtractor` extrai só dígitos de CPF/CEP/telefone, `utils.TextSanitizer` colapsa espaços duplicados e remove caracteres de risco de XSS) e mascarando saída (`utils.MaskUtils`).

A checagem de duplicidade de CPF depende do `ClienteRepository`/`ClienteService`, ainda inexistentes — fica para a Fase 5 junto com o restante do CRUD.

## Segurança (Fase 2)

- Autenticação stateless via JWT (`jjwt`, HS256, expiração curta).
- `JwtAuthenticationFilter` valida o token e popula o contexto de segurança em cada requisição.
- Falhas de autenticação/autorização nunca vazam stacktrace: `RestAuthErrorHandler` trata 401/403 no filtro de segurança, e `GlobalExceptionHandler` trata os demais erros (`@ControllerAdvice`).
- Headers de segurança aplicados em todas as respostas: `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`, `Cache-Control`.
