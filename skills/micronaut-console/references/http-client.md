# HTTP client files, environments and tunnels

How console scripts are organised so the IntelliJ HTTP client, reviewers and `scripts/console.py` all read them the same way.

## Environment files

| File | Committed | Content |
| --- | --- | --- |
| `http-client.env.json` | yes | one block per environment (`development`, `beta`, `production`, ...) with `<name>Host` variables |
| `http-client.private.env.json` | no (gitignore `**/*.private.env.json`) | same blocks with `<name>ConsoleHeader` secrets and tokens |

```json
{
  "development": { "apiHost": "localhost:8080" },
  "beta":        { "apiHost": "localhost:8081" },
  "production":  { "apiHost": "localhost:8081" }
}
```

```json
{
  "development": { "apiConsoleHeader": "nosecret" },
  "beta":        { "apiConsoleHeader": "<ask the team>" },
  "production":  { "apiConsoleHeader": "<ask the team>" }
}
```

IntelliJ picks the files up from the directory of the `.http` file or any parent. `scripts/console.py` searches the script's directory and its parents, then `--env-dir` / `$CONSOLE_ENV_DIR`, then the working directory and its parents; the private file overrides the public one. A monorepo usually keeps one shared pair (for example under a top-level `http-client/console/` folder) and lets teams add a private file next to their scripts.

## Request layout

```http
###
# Purpose — READ ONLY or MUTATES <what>
# Ticket, expected output, how to use.
###

POST http://{{apiHost}}/console/execute/result
Content-Type: text/groovy
Accept: text/plain
X-Console-Verify: {{apiConsoleHeader}}

// language=groovy
println 'console ok'
```

- `###` separates requests; the helper addresses them with `--request N` (1-based, POST requests only).
- `// language=groovy` on the first body line gives IntelliJ Groovy completion. `< probe.groovy` instead of an inline body sends a file.
- `Accept: text/plain` with `/execute/result`, `Accept: application/json` with `/execute`.
- Micronaut Security: a first request to `/login` stores the token with a response handler (`client.global.set("auth_token", response.body.access_token)`), the console request sends `Authorization: Bearer {{auth_token}}`. See `examples/micronaut-console-example-application/src/test/resources/external.http`.

## Reaching a remote application

Production consoles are normally bound to localhost (`console.addresses`) and reached through a tunnel, so the `.http` host is `localhost:<port>` for every remote environment and only the tunnel changes:

```bash
ssh -N -L 8081:localhost:8080 bastion-host                         # SSH
aws ssm start-session --target <task-or-instance> \
    --document-name AWS-StartPortForwardingSession \
    --parameters '{"portNumber":["8080"],"localPortNumber":["8081"]}'  # AWS SSM
```

Give each application its own local port when several tunnels may be open at once, and record the port next to the host variable so the mapping is not guessed. Teams usually wrap this in their CLI; use the wrapper when one exists and document the exact command in the script header.

## Where scripts live

- Next to the application: `<app>/console/*.http` or `<app>/src/test/resources/console/*.http`. One folder per application, sub-folders per topic when the list grows.
- Release runbooks link to the `.http` file and give the run recipe (dry run first, batch size, what to check afterwards, how to know when to stop re-invoking).
- Throwaway probes written during an investigation do not need to be committed; keep them in the ticket or the runbook if the result matters.
