# Micronaut Console

[![Build Status](https://github.com/agorapulse/micronaut-console/workflows/Check/badge.svg)](https://github.com/agorapulse/micronaut-console/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.agorapulse/micronaut-console.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.agorapulse%22%20AND%20a:%22micronaut-console%22)

Micronaut Console

See [Full Documentation][DOCS]

[DOCS]: https://agorapulse.github.io/micronaut-console

## Agent skill

[`skills/micronaut-console`](skills/micronaut-console) teaches AI coding agents (Claude Code and compatible tools) how to enable the console, call the endpoints, read the response formats, write safe scripts to verify experiments or run data fixes, and extend the security hooks. It ships `scripts/console.py`, a dependency-free runner for `.groovy` files and IntelliJ `.http` requests. Copy or symlink the folder into a consumer project's `.claude/skills/` to give agents working there the same knowledge.

