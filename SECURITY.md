# Checklist de Segurança e Hardening (Fase 7)

Revisão transversal de tudo construído nas Fases 1-6 contra a checklist OWASP do projeto. Cada item abaixo foi verificado no código (arquivo/linha) e, quando aplicável, coberto por teste automatizado.

## 1. Injeção (SQL Injection)

✅ **OK.** `ClienteRepository`/`UsuarioRepository` usam apenas *query methods* derivados do Spring Data (`existsByCpf`, `findByNomeContainingIgnoreCaseAndCpfContaining`, `findByLogin`) — nenhum `@Query` nativo, nenhuma concatenação de String. JPA parametriza todas as consultas geradas.

## 2. Mass Assignment / bind direto para Entity

✅ **OK.** Nenhum `@RequestBody`/`@PathVariable` de controller usa uma classe `entity.*` — sempre `dto.*`:

- `AuthController#login`: `LoginRequest` → `LoginResponse`
- `ClienteController`: `ClienteRequestDTO` → `ClienteResponseDTO`
- `EnderecoController`: apenas `@PathVariable String cep` (validado por `@Pattern`) → `EnderecoResponseDTO`

A conversão DTO ↔ Entity é sempre explícita via `mapper/*`.

## 3. XSS / sanitização de entrada

✅ **OK, com uma correção nesta fase.** Todo campo de texto livre passa por `TextSanitizer` (remove `<`, `>`, caracteres de controle, colapsa espaços) antes de persistir:

- `nome` (`ClienteMapper`) — também restrito por `@Pattern` (só letras/números/espaços).
- `logradouro`, `bairro`, `cidade`, `complemento` (`EnderecoMapper`).
- **`endereco` de e-mail (`EmailMapper`) não estava sendo sanitizado** — corrigido nesta fase (`EmailMapper#toEntity` agora chama `TextSanitizer.sanitize`). Coberto por `EmailMapperTest`.
- `cpf`, `cep`, `numero` de telefone: nunca dependem de sanitização de texto — `DigitExtractor.onlyDigits` já descarta qualquer caractere que não seja dígito, então HTML/script injetado nesses campos nunca sobrevive.
- `uf`: totalmente restrito por `@Pattern` (2 letras).

Nenhum campo de texto do usuário é devolvido como HTML — todas as respostas são JSON serializado (`Content-Type: application/json`), o que já neutraliza XSS refletido nesse formato.

## 4. Controle de acesso

✅ **OK.** RBAC por rota em `SecurityConfig`: `GET /clientes*` para `ADMIN`/`USER`; `POST/PUT/DELETE /clientes*` e `GET /enderecos/**` restritos a `ADMIN`. Verificado em `SecurityAuthorizationTest` e `ClienteControllerTest` (200/403 para cada combinação de rota × perfil).

## 5. Autenticação e JWT

✅ **OK.**
- Senhas com `BCryptPasswordEncoder` (`PasswordEncoderConfig`), nunca texto plano (`UsuarioSeeder`).
- JWT assinado com HS256, expiração curta (900s por padrão, configurável via `JWT_EXPIRATION_MS`), sem renovação automática (login novo é exigido após expirar — consistente com o escopo do projeto).
- ⚠️ **Atenção em produção:** `application.yml` traz um `JWT_SECRET` padrão versionado no repositório (`jwt.secret: ${JWT_SECRET:...}`). Funciona para o ambiente de teste técnico, mas **deve ser sobrescrito por variável de ambiente antes de qualquer deploy real** — já documentado no `README.md`.

## 6. Tratamento de erros / exposição de informação

✅ **OK, com duas correções nesta fase.** `@ControllerAdvice` (`GlobalExceptionHandler`) e `RestAuthErrorHandler` cobrem todos os erros de negócio, validação e segurança com resposta padronizada e sem stacktrace (`server.error.include-stacktrace: never` reforça isso mesmo para erros não capturados). Nesta fase, dois gaps foram encontrados e corrigidos — ambos caíam no handler genérico e voltavam `500` em vez do `400` correto:

- `GET /clientes/{id}` com `id` não numérico (`MethodArgumentTypeMismatchException`) — coberto por `ClienteControllerTest#deveRetornar400QuandoIdNaoENumerico`.
- `GET /clientes?sort=campoInexistente` (`PropertyReferenceException` do Spring Data ao validar a propriedade de ordenação).

## 7. Headers de segurança

✅ **OK.** `SecurityConfig` aplica `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Content-Security-Policy: default-src 'self'; frame-ancestors 'none'` e `Cache-Control` — e, por estarem no `HeaderWriterFilter` da própria `SecurityFilterChain` (que casa com `anyRequest()`), valem para **toda** rota, incluindo as públicas e as respostas de erro. Confirmado nesta fase com dois testes novos (antes só havia cobertura para uma resposta `200` autenticada):

- `AuthControllerTest#deveExporHeadersDeSegurancaMesmoSendoRotaPublica` (`POST /auth/login`, sem token).
- `SecurityAuthorizationTest#deveExporHeadersDeSegurancaMesmoEmRespostaDeErro401` (`GET /clientes` sem token, resposta `401`).

## 8. CORS

✅ **OK.** `CorsConfig` restringe origem a uma allowlist explícita (`cors.allowed-origins`, padrão `http://localhost:5173`), métodos e headers explícitos — sem wildcard `*` combinado com credenciais.

## 9. Logging seguro

✅ **OK** (implementado na Fase 6, revalidado aqui). `AuthService`/`ClienteService`/`RestAuthErrorHandler`/`JwtAuthenticationFilter` nunca logam senha, token ou CPF completo — apenas usuário, id e CPF mascarado. Verificado automaticamente em `LoggingSecurityTest`.

## 10. Componentes vulneráveis (dependências)

Levantamento manual das versões resolvidas (`mvn dependency:tree`), já que este ambiente sandbox não tem acesso à base NVD para rodar o OWASP Dependency-Check completo (o plugin precisa baixar/atualizar a base do NVD, o que exige API key e vários minutos — recomendado rodar em CI com `org.owasp:dependency-check-maven`):

| Dependência | Versão resolvida | Situação |
|---|---|---|
| Spring Boot / Framework | 2.7.18 / 5.3.31 | Última release da série 2.7 (EOL). Sem CVEs críticos conhecidos nessa versão, mas a série não recebe mais patches — ver item 11. |
| `org.yaml:snakeyaml` | 1.30 → **2.2 (corrigido nesta fase)** | CVE-2022-1471 (RCE via deserialização). Risco real era baixo (só carrega os `application*.yml` do próprio projeto, nunca YAML de entrada do usuário), mas a versão foi sobrescrita via propriedade no `pom.xml` para não aparecer em nenhum scanner de SCA. |
| `com.fasterxml.jackson.core:jackson-databind` | 2.13.5 | Já inclui o fix de CVE-2022-42003/42004 (>= 2.13.4.2). OK. |
| `org.apache.tomcat.embed:*` | 9.0.83 | Sem CVE crítico conhecido na versão; fixado pelo BOM do Boot 2.7.18 (mesma ressalva de EOL do item 11). |
| `org.hibernate:hibernate-core` | 5.6.15.Final | OK. |
| `org.hibernate.validator:hibernate-validator` | 6.2.5.Final | OK. |
| `com.zaxxer:HikariCP` | 4.0.3 | OK. |
| `io.jsonwebtoken:jjwt-*` | 0.11.5 | Biblioteca ativa, sem CVE conhecido; exige `Key` explícita para assinar/validar (não é vulnerável a "alg=none"). |
| `org.xerial:sqlite-jdbc` | 3.49.1.0 | Muito acima da 3.41.2.2 (fix da CVE-2023-32697). OK. |
| `org.webjars:swagger-ui` (via springdoc 1.8.0) | 5.11.8 | Muito acima da linha 4.1.3 (fix dos XSS históricos do Swagger UI). OK. |

## 11. OWASP Top 10 (2021) — mapeamento

| Categoria | Status | Onde |
|---|:---:|---|
| A01 Broken Access Control | ✅ | RBAC em `SecurityConfig`, DTO-only binding |
| A02 Cryptographic Failures | ✅ | BCrypt, JWT HS256, segredo via env var em produção |
| A03 Injection | ✅ | Queries parametrizadas, `TextSanitizer`, `DigitExtractor` |
| A04 Insecure Design | ✅ | Perfis únicos (ADMIN/USER) simples e explícitos, sem lógica implícita de permissão |
| A05 Security Misconfiguration | ✅ | Headers de segurança, `actuator` restrito a `health`, `server.error.*: never` |
| A06 Vulnerable and Outdated Components | ⚠️ | Ver item 10 — Spring Boot 2.7.x está em EOL (ver item 12) |
| A07 Identification and Authentication Failures | ✅ | JWT com expiração curta, BCrypt, 401 uniforme para credenciais inválidas |
| A08 Software and Data Integrity Failures | ✅ | Sem deserialização de dados não confiáveis; dependências vêm do Maven Central com checksum |
| A09 Security Logging and Monitoring Failures | ✅ | Fase 6 — logging estruturado sem dado sensível |
| A10 Server-Side Request Forgery | ➖ | N/A — única chamada de saída (`ViaCepService`) tem URL fixa (`viacep.com.br`), não construída a partir de entrada do usuário |

Mapeamento equivalente ao **OWASP ASVS** (nível 1): autenticação (V2), controle de acesso (V4), validação/sanitização (V5), tratamento de erros e logging (V7) e configuração (V14) — cobertos pelos itens acima.

## 12. Débito técnico conhecido (fora do escopo desta fase)

- **Spring Boot 2.7.x é EOL** (última release da série). Uma migração para Spring Boot 3.x (Jakarta EE, baseline Java 17) resolveria de raiz o item 10, mas é incompatível com o requisito do projeto de "Java 8+" e está fora do escopo de um teste técnico — registrado aqui para uma decisão consciente, não como bug.
- **Gate de cobertura mínima de 80% no build** (`jacoco:check` com `rules`) ainda não está configurado no `pom.xml` — é entregável da Fase 8, não desta.
- Rodar `org.owasp:dependency-check-maven` com acesso real ao NVD (via API key) em CI para automatizar o item 10 continuamente, em vez da revisão manual feita aqui.
