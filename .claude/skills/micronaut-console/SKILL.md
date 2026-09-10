---
name: micronaut-console
description: Add Micronaut Console to a Micronaut application and execute Groovy (or any JSR-223) scripts against the running app through `POST /console/execute` and `/console/execute/result`. Use this whenever the task involves running a script inside a live Micronaut service, checking beans, configuration or data on a deployed instance, enabling or securing the console (`console.enabled`, `console.addresses`, `console.header-name`), writing `.http` console requests, adding `BindingProvider`, `AuditService` or `SecurityAdvisor` beans, or the Lambda `ConsoleHandler` function. Prefer it over guessing the endpoint or response format from memory.
---

# Micronaut Console

Library `com.agorapulse:micronaut-console` (this repository). It lets an operator send a script to a running Micronaut application and get back what the script printed and what it returned. No UI: the client is cURL, the IntelliJ HTTP client, or any HTTP tool. Full guide: https://agorapulse.github.io/micronaut-console/ (sources under `docs/guide/src/docs/asciidoc/`).

## Install and enable

```groovy
dependencies {
    implementation 'com.agorapulse:micronaut-console:<version>'
    runtimeOnly 'org.apache.groovy:groovy'          // Groovy scripts (default language)
    // optional: 'org.jetbrains.kotlin:kotlin-scripting-jsr223' for text/x-kotlin, any other JSR-223 engine
}
```

The console is **disabled by default**. Enable it explicitly:

```yaml
console:
  enabled: true                      # or `until: 2026-12-31T00:00:00Z` for a temporary window
  path: /console                     # optional, default /console
  language: groovy                   # default language when Content-Type does not say
```

Groovy is compiled with the application classpath; the script sees every class and bean the application sees, and runs with the application's credentials. Treat access to the endpoint as root access to the app.

## Endpoints

| Method | Path | Body | Response |
| --- | --- | --- | --- |
| `POST` | `/console/execute` | script, `Content-Type` selects the language (`text/groovy`, `application/javascript`, `text/x-kotlin`) | JSON `{"result": <last expression>, "out": "<printed text>"}` |
| `POST` | `/console/execute/result` | same | plain text: `# Out #` section then `# Result #` section, or only the result when nothing was printed |
| `GET` | `/console/dsl/text` | – | list of the binding variables and their types |
| `GET` | `/console/dsl/gdsl`, `/console/dsl/dsld` | – | IDE descriptor for IntelliJ / Eclipse code completion |

Status codes: `400` with the sanitized exception message and the script echoed back when the script fails or does not compile, `401` when a `SecurityAdvisor` refuses (disabled, address or user not allowed, `until` expired), `403` from `ConsoleHeadersFilter` when the configured header is missing or has the wrong value.

Requests are executed on the blocking executor (`@ExecuteOn(TaskExecutors.BLOCKING)`), so scripts may call blocking HTTP clients and JDBC safely; a script that runs for minutes still holds that thread for the whole request.

## Script contract

Default bindings (add more with a `BindingProvider` bean returning a `Map<String, Object>`):

| Variable | Type | Notes |
| --- | --- | --- |
| `ctx` | `io.micronaut.context.ApplicationContext` | `ctx.getBean(Type)`, `ctx.environment`, `ctx.containsBean(Type)` |
| `user` | `com.agorapulse.micronaut.console.User` | id, name, address; `User.anonymous()` when no binder is present |
| `request` | `io.micronaut.http.HttpRequest` | HTTP only, absent in the function handler |

```groovy
import com.fasterxml.jackson.databind.ObjectMapper

FooService foo = ctx.getBean(FooService)
println ctx.getBean(ObjectMapper).writerWithDefaultPrettyPrinter().writeValueAsString(foo.load('id'))
''    // last expression is the result; return '' when everything useful was already printed
```

Imports are explicit (only `java.lang`, `java.util`, `java.io`, `java.net` and `groovy.lang` are implicit), no `@Grab`, each request is a fresh script with no memory of the previous one. A `CompilerConfigurationCustomizer` bean can add default imports or AST transformations application-wide.

## Calling it

```bash
curl -X POST -H "Content-Type: text/groovy" --data-binary @script.groovy http://localhost:8080/console/execute/result
```

IntelliJ HTTP client (`.http` file, several requests separated by `###`):

```http
POST http://{{host}}/console/execute/result
Content-Type: text/groovy
Accept: text/plain
X-Console-Verify: {{consoleHeader}}

// language=groovy
println 'console ok'
```

`// language=groovy` gives IntelliJ Groovy completion in the request body; `< script.groovy` instead of an inline body sends a file. Hosts and secrets belong in `http-client.env.json` / `http-client.private.env.json` (the private one gitignored). With Micronaut Security, log in first and pass `Authorization: Bearer <token>`; `examples/micronaut-console-example-application/src/test/resources/` has `execute.sh`, `external.http` and `credentials.json` showing the flow.

## Security layers

All layers are additive; any advisor that refuses blocks the execution.

```yaml
console:
  enabled: true
  addresses: [/127.0.0.1, /0:0:0:0:0:0:0:1]   # leading slash is required; behind a proxy configure hostname resolution
  users: [alice, bob]                          # needs Micronaut Security or a TypedRequestArgumentBinder<User>
  header-name: X-Console-Verify                # POST must carry this header (SSRF protection)
  header-value: ${CONSOLE_HEADER_VALUE}        # optional; presence only is checked when absent
```

Extension points, all plain beans:

- `SecurityAdvisor` — custom rule (roles, time windows); built-ins live in `advisors/` (`AddressAdvisor`, `EnabledAdvisor`, `UntilAdvisor`, `UsersAdvisor`).
- `AuditService` — `beforeExecute(script, bindings)`, `afterExecute(script, result)`, `onError(script, throwable)`; the default logs through SLF4J. Replace it (`@Replaces(DefaultAuditService)`) to ship events to an audit sink.
- `BindingProvider` — extra script variables.
- `CompilerConfigurationCustomizer` — Groovy compiler configuration.

Recommended production posture: console reachable only from localhost through an SSH/SSM tunnel, header check on, audit events shipped to monitoring, WAF rule blocking `/console` from the public side.

## Functions (AWS Lambda)

`com.agorapulse.micronaut.console.function.ConsoleHandler` is a `@FunctionBean("console")` `UnaryOperator<String>`: the payload is the script body, the result is the same text as `/console/execute/result`, and the user is anonymous. `AuthConsoleHandler` (`@FunctionBean("auth-console")`) takes an `AuthorizedScript` JSON payload with `body` and a `user` object (`id`, `name`, `address`) so the audit trail carries the caller's identity. Configure `console.enabled`/`console.until` the same way; the address and header advisors do not apply because there is no HTTP request.

## Working in this repository

- Library code: `libs/micronaut-console/src/main/groovy/com/agorapulse/micronaut/console/` (Java sources, Groovy source set for the tests). Tests are Spock plus Gru fixtures under `src/test/resources/.../ConsoleControllerSpec/` (`printer.groovy` → `printer.txt`/`printer.json` document the exact response formats).
- Examples: `examples/micronaut-console-example-application` (Java, Micronaut Security) and `examples/micronaut-console-example-kotlin`.
- Docs: AsciiDoc under `docs/guide/src/docs/asciidoc/`; snippets are included from the sources and tests, so change the example file, not the doc, when behaviour changes.
- Build: `./gradlew check` (Checkstyle and CodeNarc configs in `config/`, license headers required on sources).
