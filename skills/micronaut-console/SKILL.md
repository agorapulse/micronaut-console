---
name: micronaut-console
description: Run Groovy (or any JSR-223) scripts inside a running Micronaut application through Micronaut Console (`POST /console/execute` and `/console/execute/result`) to inspect live beans, configuration and data, verify an experiment or a deployed change, trigger a job, or run a one-off data fix. Use this whenever the user wants to check something "on the real environment" without writing a test, mentions a console `.http` script, `X-Console-Verify`, an SSH/SSM tunnel to an API, `ctx.getBean` in a script, a runbook step that runs a script, or asks "does this actually work / what does the data look like on beta or prod". Also use it when adding the console to an application (`console.enabled`, `console.addresses`, `console.header-name`), extending it with `BindingProvider`, `AuditService` or `SecurityAdvisor` beans, or writing and reviewing console scripts.
argument-hint: "[host or environment] [what to check or run]"
allowed-tools: Bash(python3 ${CLAUDE_SKILL_DIR}/scripts/console.py *)
---

# Micronaut Console

Micronaut Console (`com.agorapulse:micronaut-console`, inspired by the Grails Console plugin) is an HTTP endpoint, or a function, that executes a script **inside the running application**: same classpath, same beans, same database connections, same cloud credentials. That is what makes it the fastest way to verify an experiment against real data, and also what makes it dangerous. Full guide: https://agorapulse.github.io/micronaut-console/

## When to reach for it

- Verify an experiment or a freshly deployed change: read the real state of a bean, a table, a cache, a queue.
- Reproduce a bug with real data instead of guessing from logs.
- Trigger a job, replay an event, warm a cache, invalidate an entry.
- One-off data migration that is too small or too entangled with application services for SQL alone.

Do not use it as a substitute for a test when the behaviour is deterministic and testable locally, and do not use it to read secrets or personal data you do not need for the task.

## The request

One script = one HTTP request. Body is the script source, `Content-Type` selects the language: `text/groovy` or `application/groovy` (default, `console.language`), `application/javascript` or `text/x-kotlin` when a JSR-223 engine for it is on the classpath. Groovy scripts also get an `out` binding (the `PrintWriter` behind `println`).

```http
POST http://{{host}}/console/execute/result
Content-Type: text/groovy
Accept: text/plain
X-Console-Verify: {{consoleHeader}}

// language=groovy
import com.fasterxml.jackson.databind.ObjectMapper

FooService foo = ctx.getBean(FooService)
println ctx.getBean(ObjectMapper).writerWithDefaultPrettyPrinter().writeValueAsString(foo.load('id'))
''
```

| Method | Path | Response |
| --- | --- | --- |
| `POST` | `/console/execute/result` | plain text: `# Out #` section with everything printed, then `# Result #` with the last expression; only the result when nothing was printed |
| `POST` | `/console/execute` | JSON `{"result": <last expression>, "out": "<printed text>"}`; prefer it when a program parses the answer |
| `GET` | `/console/dsl/text` | list of binding variables and their types |
| `GET` | `/console/dsl/gdsl`, `/console/dsl/dsld` | IntelliJ / Eclipse descriptor for code completion; `curl http://localhost:8080/console/dsl/gdsl > console.gdsl` next to the scripts gives completion for the bindings |

- **Discover the bindings before writing a script.** `GET /console/dsl/text` (or `console.py --bindings`) lists every variable the application injects with its type. `ctx` (`io.micronaut.context.ApplicationContext`), `user` (`com.agorapulse.micronaut.console.User`) and `request` (`HttpRequest`, HTTP only) are always there; applications add their own through `BindingProvider` beans (a pre-resolved service, a tenant, a repository), and using those beats a chain of `ctx.getBean` calls. The `gdsl` variant gives IntelliJ completion for them.
- **Choose the response format on purpose.** `/console/execute/result` is for eyes: everything printed, then the last expression as text. `/console/execute` returns JSON with `out` (printed text) and `result` (the last expression serialized by Jackson, so a `Map` or `List` comes back as a JSON object or array). Use JSON when you will parse the answer: comparing counts, diffing state before and after, feeding another script.
- The last expression is the result. End with `''` or `null` when the last call returns a large object you do not want serialized into the response.
- Status codes: `400` with the sanitized exception and the script echoed back on a compile or runtime error, `401` when a security advisor refuses (console disabled, `until` expired, address or user not allowed), `403` from `ConsoleHeadersFilter` when the configured header is missing or wrong.
- Scripts run on the blocking executor (`@ExecuteOn(TaskExecutors.BLOCKING)`), so blocking clients and JDBC are safe, but a long loop holds a thread and a connection for the whole request and can hit a proxy or tunnel timeout. Batch, print progress, re-invoke.
- Imports are explicit (only `java.lang`, `java.util`, `java.io`, `java.net` and `groovy.lang` are implicit), no `@Grab`, each request is a fresh script with no memory of the previous one. `groovy-json` is often absent; use the Jackson `ObjectMapper` bean.

## Calling it

**cURL**

```bash
curl -X POST -H "Content-Type: text/groovy" -H "X-Console-Verify: $CONSOLE_HEADER" \
     --data-binary @probe.groovy http://localhost:8080/console/execute/result
```

**IntelliJ HTTP client**: `.http` files with one request per `###` block, hosts in `http-client.env.json`, secrets in the gitignored `http-client.private.env.json`, `// language=groovy` for completion, `< script.groovy` to send a file. Layout and conventions in [references/http-client.md](references/http-client.md).

**Helper script** (what to use from a shell). It reads the same two environment files, found next to the script, in the directory passed with `--env-dir` or `$CONSOLE_ENV_DIR`, or in any parent directory:

```bash
# Is the console reachable and enabled?
python3 ${CLAUDE_SKILL_DIR}/scripts/console.py --env development --host apiHost --check

# Which variables can a script use? (GET /console/dsl/text)
python3 ${CLAUDE_SKILL_DIR}/scripts/console.py --env development --host apiHost --bindings

# Run a Groovy file
python3 ${CLAUDE_SKILL_DIR}/scripts/console.py --env development --host apiHost probe.groovy

# Run request #2 of an existing .http file; host and header come from its {{variables}}
python3 ${CLAUDE_SKILL_DIR}/scripts/console.py --env beta --http console/fix-links.http --request 2

# Explicit URL, script on stdin, JSON response
echo "ctx.environment.activeNames" | python3 ${CLAUDE_SKILL_DIR}/scripts/console.py --url http://localhost:8081 --header-value "$CONSOLE_HEADER" --json -
```

`--dry-run` prints the resolved URL, headers and body without sending. Use it before any run on a shared environment.

With Micronaut Security instead of (or in addition to) the header, log in first and send `Authorization: Bearer <token>` (`--bearer` on the helper). `examples/micronaut-console-example-application/src/test/resources/` shows the flow with `execute.sh`, `external.http` and `credentials.json`.

## Safety rules

Executions are audited (`AuditService`, SLF4J by default, often forwarded to monitoring). Behave accordingly.

1. **Read-only first.** Probe before you mutate. Mark read-only scripts with `READ ONLY` in the header comment so the next person knows.
2. **Local, then staging, then production.** Same script, same result shape, no surprises. Never run anything on production the user has not explicitly asked to run there, and confirm host and environment out loud (or with `--dry-run`) before sending.
3. **Mutations get a `dryRun` flag**, defaulting to `true`, plus `batchSize` and a `sleepTimeMs` throttle. Print what would change, then flip the flag. Select candidates by their current state so a re-run after a partial failure is idempotent.
4. **Log through a named SLF4J logger** for anything that must be traceable later (`LoggerFactory.getLogger('sc-123456_cache_invalidation')`). `println` only reaches the HTTP response, which is lost when the tunnel drops.
5. **No secrets in scripts or in the repository.** Header values and tokens live only in the private environment file. Do not print tokens, passwords or whole property sources on shared environments.
6. **Keep the script self-contained.** Full imports, explicit bean lookups, no reliance on a previous request.

## Verifying an experiment

Recipes for every step are in [references/script-patterns.md](references/script-patterns.md).

1. **Check the console and its bindings**: `--check`, then `--bindings`. A binding the application already provides (a service, a repository, a tenant) is the shortest path; fall back to `ctx.getBean(Type)` for singletons, `ctx.getBean(Type, Qualifiers.byName('name'))` for named beans, `ctx.containsBean(Type)` when a bean is conditional.
2. **Find the seam in code**: which bean, repository, cache or queue holds the state you need to see.
3. **Write a probe** that returns the state as a `Map` or `List` and run it with `--json`, so the values can be compared mechanically before and after. Print with `println` only when the output is for a human. Add `ctx.environment.activeNames` and the relevant `ctx.environment.getProperty('key', String)` when configuration is part of the question.
4. **Run it against the environment the experiment lives on**, capture the output, compare with the expected values from the ticket or the code change.
5. **Exercise the behaviour** if reading is not enough: call the service method, send the message, run the job, then re-run the probe.
6. **Report** with the real values, the environment, the host and the time, so the result is reproducible. Do not paraphrase numbers.

## Writing a reusable script

Keep scripts worth repeating next to the application they target (a `console/` folder is the usual choice) as `.http` files, and link them from the runbook that tells the operator when to run them, with which tunables, and what to check afterwards.

```http
###
# One-line purpose — READ ONLY or MUTATES <what>
#
# Ticket or reason. What "good" output looks like.
# How to use: environment, tunnel command if any, tunables to set.
###

POST http://{{apiHost}}/console/execute/result
Content-Type: text/groovy
Accept: text/plain
X-Console-Verify: {{apiConsoleHeader}}

// language=groovy
import ...

boolean dryRun = true
int batchSize = 100
```

Use `{{variables}}` for hosts and headers so the same file runs on every environment.

## Install, enable and secure

```groovy
dependencies {
    implementation 'com.agorapulse:micronaut-console:<version>'
    runtimeOnly 'org.apache.groovy:groovy'                                        // Groovy scripts (text/groovy)
    // runtimeOnly "org.jetbrains.kotlin:kotlin-scripting-jsr223:${kotlinVersion}"  // Kotlin scripts (text/x-kotlin)
    // runtimeOnly 'org.openjdk.nashorn:nashorn-core:<version>'                     // JavaScript (application/javascript); Nashorn left the JDK in 15
}
```

Any JSR-223 engine on the classpath is picked up by `JavaScriptingConsoleEngineFactory` and selected through the `Content-Type` of the request.

The console is disabled by default (since 2.0.0). Minimal local configuration is `console.enabled: true`, or the `CONSOLE_ENABLED=true` environment variable. For anything shared, stack the layers; any advisor that refuses blocks the execution:

```yaml
console:
  enabled: true                                # or `until: 2026-12-31T00:00:00Z` for a temporary window
  path: /console                               # optional
  addresses: [/127.0.0.1, /0:0:0:0:0:0:0:1]    # leading slash required; behind a proxy configure hostname resolution
  users: [alice, bob]                          # needs Micronaut Security or a TypedRequestArgumentBinder<User>
  header-name: X-Console-Verify                # every POST must carry it (SSRF protection)
  header-value: ${CONSOLE_HEADER_VALUE}        # optional; only presence is checked when absent
```

How the layers behave:

- `enabled` / `until`: `EnabledAdvisor` allows execution when `enabled` is true or `until` is still in the future. In the `function` environment (AWS Lambda and similar) it always allows, because the function's own authorization is expected to gate the call.
- `addresses`: compared with the remote address of the request as Micronaut sees it. Behind a reverse proxy (Nginx, a load balancer) configure hostname resolution so the real client address is used, otherwise every caller shares the proxy address: https://sergiodelamo.com/blog/host-and-ip-resolution-micronaut-load-balancer-elastic-beanstalk.html
- `users`: compared with `user.id`, so a user binder must supply one (see Micronaut Security below). Without a binder every request is anonymous and the advisor refuses everything.
- `header-name` / `header-value`: `ConsoleHeadersFilter` checks every `POST` under `console.path` and answers `403` with "Missing verification header" or "Wrong value of the verification header". `GET` (the DSL endpoints) is not checked. Leaving `header-value` unset while `header-name` is set logs an error at startup and makes every POST fail.

Recommended production posture: reachable only from localhost through an SSH/SSM tunnel, header check on, audit events shipped to monitoring, WAF rule blocking `/console` from the public side.

### Micronaut Security

```groovy
implementation 'io.micronaut.security:micronaut-security'
implementation 'io.micronaut.security:micronaut-security-jwt'
```

```yaml
micronaut:
  security:
    enabled: true
    endpoints:
      login:
        enabled: true
    token:
      jwt:
        enabled: true
        signatures:
          secret:
            generator:
              secret: ${JWT_GENERATOR_SECRET}
              jws-algorithm: HS256
    intercept-url-map:
      - pattern: /console/**
        http-method: GET             # DSL descriptors stay anonymous so IDEs can fetch them
        access: [isAnonymous()]
      - pattern: /console/**
        http-method: POST            # scripts need a logged-in user
        access: [isAuthenticated()]
    authentication: bearer
```

With security enabled, `MicronautSecurityUserArgumentBinder` fills `user` from the authenticated principal (`id` = principal name, `address` = remote address), which is what `console.users` and the audit log rely on. Without Micronaut Security, provide your own `TypedRequestArgumentBinder<User>` (see `SimpleUserBinder` in the tests) to identify callers. Callers log in first (`POST /login` with `{"username", "password"}`), then send `Authorization: Bearer <access_token>`; `execute.sh` does it with cURL and `jq`, `external.http` with an IntelliJ response handler, both reading credentials from a gitignored `credentials.json` / `http-client.private.env.json`.

Extension points are plain beans:

- `SecurityAdvisor` — custom rules (roles, time windows); built-ins in `advisors/` (`AddressAdvisor`, `EnabledAdvisor`, `UntilAdvisor`, `UsersAdvisor`).
- `AuditService` — `beforeExecute`, `afterExecute`, `onError`; `@Replaces(DefaultAuditService)` to ship events elsewhere.
- `BindingProvider` — extra script variables.
- `CompilerConfigurationCustomizer` — default imports, AST transformations, static compilation.

## Functions (AWS Lambda and friends)

`ConsoleHandler` (`@FunctionBean("console")`) is a `UnaryOperator<String>`: the payload is the script body, the reply is the same text as `/console/execute/result`, the user is anonymous. `AuthConsoleHandler` (`@FunctionBean("auth-console")`) takes an `AuthorizedScript` JSON payload with `body` and a `user` object (`id`, `name`, `address`) so the audit trail carries the caller. In the `function` environment the console is enabled without `console.enabled` (the function's own authorization is the gate); `console.until`, when set, still limits it. The address and header advisors do not apply because there is no HTTP request.

## Versions

| Version | Requires | Notes |
| --- | --- | --- |
| 4.x | Micronaut 4/5, JDK 17+ | `ConsoleHeadersFilter` on Project Reactor; RxJava 2 no longer pulled transitively, declare it yourself if you still need it |
| 3.x | Micronaut 4, JDK 17+, Groovy 4 | |
| 2.x | | console disabled by default; `console.enabled` or `CONSOLE_ENABLED=true` required |

## Working in this repository

- Library code: `libs/micronaut-console/src/main/groovy/com/agorapulse/micronaut/console/` (Java sources; the Groovy source set hosts the Spock tests). Gru fixtures under `src/test/resources/.../ConsoleControllerSpec/` (`printer.groovy` → `printer.txt` / `printer.json`) document the exact response formats.
- Examples: `examples/micronaut-console-example-application` (Java, Micronaut Security) and `examples/micronaut-console-example-kotlin`.
- Docs: AsciiDoc under `docs/guide/src/docs/asciidoc/`; snippets are included from sources and tests, so change the example file, not the doc.
- Build: `./gradlew check` (Checkstyle and CodeNarc in `config/`, license headers on sources).
