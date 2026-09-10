#!/usr/bin/env python3
"""Execute a script on a Micronaut Console endpoint.

Resolves the target host and the verification header the same way the IntelliJ HTTP
client does: from http-client.env.json (hosts) and http-client.private.env.json
(secrets). Both files are searched next to the script and in its parent directories,
then in --env-dir / $CONSOLE_ENV_DIR, then in the working directory and its parents.

Examples:
  console.py --env development --host apiHost --check
  console.py --env development --host apiHost probe.groovy
  console.py --env beta --http console/fix-links.http --request 2 --dry-run
  echo "ctx.environment.activeNames" | console.py --url http://localhost:8081 --header-value "$CONSOLE_HEADER" --json -
"""

import argparse
import json
import os
import re
import sys
import urllib.error
import urllib.request

ENV_FILE = "http-client.env.json"
PRIVATE_ENV_FILE = "http-client.private.env.json"
HEADER_SUFFIX = "ConsoleHeader"


def ancestors(path):
    directory = os.path.abspath(path)
    while True:
        yield directory
        parent = os.path.dirname(directory)
        if parent == directory:
            return
        directory = parent


def candidate_dirs(script_path, env_dir):
    dirs = []
    if script_path and script_path != "-":
        dirs.extend(ancestors(os.path.dirname(os.path.abspath(script_path))))
    if env_dir:
        dirs.append(os.path.abspath(env_dir))
    dirs.extend(ancestors(os.getcwd()))
    seen = set()
    return [d for d in dirs if not (d in seen or seen.add(d))]


def load_variables(env, script_path, env_dir):
    files = []
    for directory in reversed(candidate_dirs(script_path, env_dir)):
        for name in (ENV_FILE, PRIVATE_ENV_FILE):
            path = os.path.join(directory, name)
            if not os.path.isfile(path):
                continue
            try:
                with open(path, encoding="utf-8") as handle:
                    files.append((path, json.load(handle)))
            except (OSError, ValueError) as error:
                print(f"warning: cannot read {path}: {error}", file=sys.stderr)
    environments = sorted({key for _, data in files for key in data})
    if env is None:
        if "development" in environments or not environments:
            env = "development"
        elif len(environments) == 1:
            env = environments[0]
        else:
            sys.exit(f"several environments available ({', '.join(environments)}); pass --env")
    variables = {}
    for _, data in files:
        variables.update(data.get(env, {}))
    return env, variables, [path for path, _ in files]


def parse_http_file(path, index):
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    blocks = [b for b in re.split(r"^###.*$", text, flags=re.MULTILINE) if re.search(r"^\s*POST\s", b, flags=re.MULTILINE)]
    if not blocks:
        sys.exit(f"no POST request found in {path}")
    if index < 1 or index > len(blocks):
        sys.exit(f"{path} has {len(blocks)} POST request(s); --request must be between 1 and {len(blocks)}")
    lines = blocks[index - 1].lstrip("\n").split("\n")
    start = next(i for i, line in enumerate(lines) if re.match(r"\s*POST\s", line))
    url = lines[start].strip().split(None, 1)[1].strip()
    headers = {}
    body_start = len(lines)
    for i in range(start + 1, len(lines)):
        line = lines[i]
        if not line.strip():
            body_start = i + 1
            break
        if ":" in line:
            key, value = line.split(":", 1)
            headers[key.strip()] = value.strip()
    body = "\n".join(lines[body_start:]).strip("\n") + "\n"
    if body.startswith("<"):
        included = body[1:].strip()
        with open(os.path.join(os.path.dirname(os.path.abspath(path)), included), encoding="utf-8") as handle:
            body = handle.read()
    return url, headers, body


def substitute(template, variables):
    return re.sub(r"\{\{\s*([A-Za-z0-9_]+)\s*\}\}", lambda m: str(variables.get(m.group(1), m.group(0))), template)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("script", nargs="?", help="script file to execute, or - for stdin")
    parser.add_argument("--env", help="environment block of http-client.env.json (default: the only one, else 'development')")
    parser.add_argument("--env-dir", default=os.environ.get("CONSOLE_ENV_DIR"), help="directory holding the http-client env files (also $CONSOLE_ENV_DIR)")
    parser.add_argument("--host", help="host variable name from http-client.env.json, e.g. apiHost")
    parser.add_argument("--url", help="explicit base URL, e.g. http://localhost:8081 (overrides --host and the .http file)")
    parser.add_argument("--http", help=".http file to take the request from")
    parser.add_argument("--request", type=int, default=1, help="1-based index of the POST request inside the .http file")
    parser.add_argument("--header-name", default="X-Console-Verify", help="name of the console verification header")
    parser.add_argument("--header-value", help="explicit header value (overrides env files and $CONSOLE_HEADER)")
    parser.add_argument("--header-var", help="variable holding the header value, e.g. apiConsoleHeader")
    parser.add_argument("--bearer", help="Authorization: Bearer token for Micronaut Security (also $CONSOLE_TOKEN)")
    parser.add_argument("--content-type", help="script language mime type (default text/groovy, or the .http request's)")
    parser.add_argument("--path", default="/console", help="console base path (console.path property, default /console)")
    parser.add_argument("--json", action="store_true", help="call /console/execute and print the JSON response")
    parser.add_argument("--check", action="store_true", help="send println 'console ok' instead of a script")
    parser.add_argument("--dry-run", action="store_true", help="print the request instead of sending it")
    parser.add_argument("--timeout", type=int, default=600, help="socket timeout in seconds")
    args = parser.parse_args()

    args.env, variables, env_files = load_variables(args.env, args.http or args.script, args.env_dir)

    url = None
    headers = {"Content-Type": "text/groovy"}
    body = None

    if args.http:
        raw_url, raw_headers, body = parse_http_file(args.http, args.request)
        url = re.sub(r"/console(/execute(/result)?)?/?$", "", substitute(raw_url, variables))
        for key, value in raw_headers.items():
            if key.lower() in (args.header_name.lower(), "content-type", "authorization"):
                headers[key] = substitute(value, variables)

    if args.url:
        url = args.url.rstrip("/")
    elif args.host:
        if args.host not in variables:
            where = ", ".join(env_files) if env_files else f"no {ENV_FILE} found"
            sys.exit(f"host variable {args.host} is not defined for environment {args.env} ({where})")
        host = str(variables[args.host])
        url = host if "://" in host else "http://" + host
    if not url:
        sys.exit("target missing: pass --url, --host <variable>, or --http <file>")
    if "{{" in url:
        sys.exit(f"unresolved variable in URL {url}; add it to {ENV_FILE} or pass --url")

    if args.content_type:
        headers["Content-Type"] = args.content_type

    if args.header_value is not None:
        headers[args.header_name] = args.header_value
    elif args.header_var:
        if args.header_var not in variables:
            sys.exit(f"variable {args.header_var} is not defined for this environment")
        headers[args.header_name] = str(variables[args.header_var])
    elif os.environ.get("CONSOLE_HEADER"):
        headers[args.header_name] = os.environ["CONSOLE_HEADER"]
    elif args.header_name not in headers or "{{" in headers[args.header_name]:
        candidates = [k for k in variables if k.endswith(HEADER_SUFFIX)]
        if args.host and len(candidates) > 1:
            prefix = re.sub(r"Host$", "", args.host).lower()
            candidates = [k for k in candidates if k.lower().startswith(prefix)] or candidates
        if len(candidates) == 1:
            headers[args.header_name] = str(variables[candidates[0]])
        elif candidates:
            sys.exit(f"several header variables match ({', '.join(sorted(candidates))}); pass --header-var")
        elif args.header_name in headers:
            sys.exit(f"unresolved variable in header {headers[args.header_name]}; add it to {PRIVATE_ENV_FILE} or pass --header-var/--header-value")
        else:
            print(f"warning: no {args.header_name} value found; sending without it", file=sys.stderr)

    token = args.bearer or os.environ.get("CONSOLE_TOKEN")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if "{{" in headers.get("Authorization", ""):
        sys.exit("unresolved variable in Authorization header; pass --bearer or $CONSOLE_TOKEN")

    if args.check:
        body = "println 'console ok'\n''\n"
    elif body is None:
        if not args.script:
            sys.exit("script missing: pass a script file, - for stdin, --http <file>, or --check")
        body = sys.stdin.read() if args.script == "-" else open(args.script, encoding="utf-8").read()

    endpoint = f"{url}{args.path}/execute" + ("" if args.json else "/result")
    headers["Accept"] = "application/json" if args.json else "text/plain"

    if args.dry_run:
        print(f"POST {endpoint}")
        for key, value in headers.items():
            secret = key.lower() in (args.header_name.lower(), "authorization")
            print(f"{key}: {'<redacted>' if secret else value}")
        print()
        print(body, end="" if body.endswith("\n") else "\n")
        return 0

    request = urllib.request.Request(endpoint, data=body.encode("utf-8"), headers=headers, method="POST")
    try:
        with urllib.request.urlopen(request, timeout=args.timeout) as response:
            payload = response.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as error:
        payload = error.read().decode("utf-8", errors="replace")
        hint = {
            400: "script error, see message and echoed script below",
            401: "console disabled, until expired, or address/user not allowed",
            403: f"{args.header_name} missing or wrong",
        }.get(error.code, "")
        print(f"HTTP {error.code} {error.reason}" + (f" ({hint})" if hint else ""), file=sys.stderr)
        print(payload)
        return 1
    except urllib.error.URLError as error:
        print(f"cannot reach {endpoint}: {error.reason}. Is the application running and the tunnel open?", file=sys.stderr)
        return 2

    if args.json:
        try:
            print(json.dumps(json.loads(payload), indent=2, ensure_ascii=False))
        except ValueError:
            print(payload)
    else:
        print(payload, end="" if payload.endswith("\n") else "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
