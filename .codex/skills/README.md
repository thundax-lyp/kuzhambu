# Kuzhambu Codex Skills

This directory stores project-scoped Codex skills.

Codex loads these skills when working in this repository. Keep skills here when
the workflow is specific to Kuzhambu and should be versioned with the project.

Each skill must live in its own directory and include a `SKILL.md` file:

```text
.codex/skills/
  kuzhambu-example/
    SKILL.md
```

Use `${CODEX_HOME:-~/.codex}/skills` only for skills that should apply across
multiple repositories.
