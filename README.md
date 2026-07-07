# sea-backend

API REST para cadastro de clientes (CRUD), com autenticação e autorização por perfil (Admin / Usuário Padrão).

> Projeto em desenvolvimento incremental. Autenticação (Fase 2), DTOs/validações/mappers de Cliente (Fase 3), consulta de CEP via ViaCEP (Fase 4), o CRUD completo de clientes (Fase 5) e o tratamento global de erros/logging seguro (Fase 6) já estão implementados. Faltam hardening/segurança transversal (Fase 7) e o gate de cobertura mínima de 80% no build (Fase 8).

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
| `GET /enderecos/{cep}`        | ✅    | ❌ (403) |

Requisições sem token ou com token inválido/expirado retornam `401`.

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

## Segurança (Fase 2)

- Autenticação stateless via JWT (`jjwt`, HS256, expiração curta).
- `JwtAuthenticationFilter` valida o token e popula o contexto de segurança em cada requisição.
- Falhas de autenticação/autorização nunca vazam stacktrace: `RestAuthErrorHandler` trata 401/403 no filtro de segurança, e `GlobalExceptionHandler` trata os demais erros (`@ControllerAdvice`).
- Headers de segurança aplicados em todas as respostas: `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`, `Cache-Control`.

## DTOs, validação e mapeamento (Fase 3)

Os contratos de entrada/saída de `Cliente`:

- **`ClienteRequestDTO`** (entrada): `nome` (3-100 caracteres, apenas letras/números/espaços), `cpf` (validado por dígito verificador via `@CpfValido`), `endereco` (`EnderecoRequestDTO` aninhado), `telefones`/`emails` (listas, mínimo 1 item cada). `TelefoneRequestDTO` valida a quantidade de dígitos conforme o `tipo` (`@TelefoneValido`: 11 dígitos para `CELULAR`, 10 para `RESIDENCIAL`/`COMERCIAL`).
- **`ClienteResponseDTO`** (saída): nunca expõe a Entity — CPF, CEP e telefone já retornam mascarados.
- **`ClienteMapper`** (+ `EnderecoMapper`, `TelefoneMapper`, `EmailMapper`, em `mapper/`): convertem DTO ↔ Entity, normalizando entrada (`utils.DigitExtractor` extrai só dígitos de CPF/CEP/telefone, `utils.TextSanitizer` colapsa espaços duplicados e remove caracteres de risco de XSS) e mascarando saída (`utils.MaskUtils`). `atualizarEntity` faz a mesma normalização mutando a entidade gerenciada em vez de recriá-la, preservando o `Endereco`/`Telefone`/`Email` já persistidos (evita órfãos indesejados no `cascade + orphanRemoval`).

## Consulta de CEP via ViaCEP (Fase 4)

```bash
curl http://localhost:8080/enderecos/01310-100 \
  -H "Authorization: Bearer <token-admin>"
```

Resposta (mesmo formato de `EnderecoResponseDTO`, pronta para pré-preencher o formulário de endereço; `complemento` fica em branco para o usuário preencher):
```json
{ "cep": "01310-100", "logradouro": "Avenida Paulista", "bairro": "Bela Vista", "cidade": "São Paulo", "uf": "SP", "complemento": null }
```

- Aceita CEP com ou sem máscara (`01310100` ou `01310-100`); formato inválido retorna `400` antes de qualquer chamada externa.
- CEP inexistente (ViaCEP responde `{"erro": true}`) retorna `404`.
- Indisponibilidade/timeout do ViaCEP retorna `503`, sem stacktrace.
- Respostas são cacheadas em memória (`ConcurrentMapCacheManager`, cache `enderecos-cep`) por CEP normalizado (dígitos), evitando chamadas repetidas ao mesmo endereço.
- Restrito a `ADMIN`, já que só esse perfil cria/edita clientes.

## CRUD de clientes (Fase 5)

```bash
# Criar (ADMIN)
curl -X POST http://localhost:8080/clientes \
  -H "Authorization: Bearer <token-admin>" -H "Content-Type: application/json" \
  -d '{"nome":"João da Silva","cpf":"111.444.777-35","endereco":{"cep":"01310-100","logradouro":"Av. Paulista","bairro":"Bela Vista","cidade":"São Paulo","uf":"SP"},"telefones":[{"tipo":"CELULAR","numero":"11987654321"}],"emails":[{"endereco":"joao@example.com"}]}'

# Listar (ADMIN ou USER) — paginado, com ordenação e filtros
curl "http://localhost:8080/clientes?nome=joao&page=0&size=10&sort=nome,asc" \
  -H "Authorization: Bearer <token>"

# Buscar por id, atualizar (ADMIN) e excluir (ADMIN)
curl http://localhost:8080/clientes/1 -H "Authorization: Bearer <token>"
curl -X PUT http://localhost:8080/clientes/1 -H "Authorization: Bearer <token-admin>" -H "Content-Type: application/json" -d '{...}'
curl -X DELETE http://localhost:8080/clientes/1 -H "Authorization: Bearer <token-admin>"
```

- `GET /clientes` aceita `nome`/`cpf` (busca parcial, case-insensitive para nome) e os parâmetros padrão do Spring `Pageable` (`page`, `size`, `sort`); filtros ausentes não restringem o resultado.
- CPF duplicado retorna `409` (`CpfDuplicadoException`); cliente inexistente em `GET/PUT/DELETE /clientes/{id}` retorna `404` (`ClienteNaoEncontradoException`); corpo ausente/malformado retorna `400`.
- `ClienteRepository` usa apenas queries derivadas do Spring Data (parametrizadas, sem concatenação de String).
- Atualização (`PUT`) muta a entidade gerenciada em vez de substituí-la — necessário porque `Endereco`/`Telefone`/`Email` são filhos `cascade + orphanRemoval` do `Cliente`.

## Tratamento de erros e logging seguro (Fase 6)

Todo erro da API (validação, negócio, autenticação/autorização ou falha inesperada) responde no mesmo formato, sem stacktrace:

```json
{ "timestamp": "2026-07-07T18:00:00Z", "status": 404, "message": "Cliente não encontrado: 99", "path": "/clientes/99" }
```

| Cenário                                    | Status | Origem |
|---------------------------------------------|:------:|--------|
| Corpo/DTO inválido                          | 400    | `GlobalExceptionHandler` (`@ControllerAdvice`) |
| Sem token / token inválido ou expirado      | 401    | `RestAuthErrorHandler` (filtro de segurança) |
| Autenticado, mas sem permissão para a rota  | 403    | `RestAuthErrorHandler` (filtro de segurança) |
| Cliente ou CEP não encontrado               | 404    | `GlobalExceptionHandler` |
| CPF duplicado                               | 409    | `GlobalExceptionHandler` |
| ViaCEP indisponível/timeout                 | 503    | `GlobalExceptionHandler` |
| Erro interno inesperado                     | 500    | `GlobalExceptionHandler` (mensagem genérica ao cliente; stacktrace só no log do servidor) |

Falhas do filtro de segurança (401/403) nunca chegam ao `@ControllerAdvice` — são tratadas antes, no `RestAuthErrorHandler`, com o mesmo formato de resposta.

**Logging (SLF4J/Logback):** todos os pontos que manipulam dado sensível logam apenas o necessário para auditoria, nunca o valor sensível em si:

- `AuthService` loga o login tentado (usuário) em sucesso/falha — nunca a senha.
- `ClienteService` loga `id` + CPF **mascarado** (`MaskUtils.maskCpf`) ao cadastrar/atualizar/excluir — nunca o CPF completo.
- `JwtAuthenticationFilter`/`RestAuthErrorHandler` logam que um token foi rejeitado ou que houve acesso negado — nunca o token/header `Authorization`.
- `GlobalExceptionHandler` não loga `ex.getMessage()` de corpo malformado (`HttpMessageNotReadableException`), pois o Jackson pode ecoar um trecho do payload original (que poderia conter a senha de `/auth/login`).

A regra é verificada automaticamente em `LoggingSecurityTest`, que captura os logs da aplicação durante login (com e sem sucesso) e cadastro de cliente, e falha se senha, token ou CPF completo aparecerem em qualquer linha.