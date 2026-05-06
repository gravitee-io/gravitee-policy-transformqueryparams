---
name: gravitee-policy-docgen-migration
description: Migrate a Gravitee APIM policy repository from README.adoc-based documentation to gravitee-doc-gen sources and outputs. Use when a repo has README.adoc policy docs and needs .docgen files (overview.md, matrix.yaml, examples.yaml, .docgen/examples/*), generated docs, and src/assembly/policy-assembly.xml updated to package docs/.
---

# Gravitee Policy Doc-Gen Migration

## Overview

Migrate documentation for a Gravitee policy plugin from legacy `README.adoc` sources to the doc-gen workflow with deterministic validation.

Keep runtime policy behavior unchanged. Limit changes to documentation sources, generated docs, and packaging inputs.

## Inputs

Require these repository files before starting:

- `README.adoc`
- `src/assembly/policy-assembly.xml`
- `src/main/resources/schemas/schema-form.json`

Prefer existing V4 API fixtures as examples source:

- `src/test/resources/apis/v4/*.json`

If no V4 fixtures exist, fallback in this order:

- `src/test/resources/apis/*.json`
- `src/test/resources/apis/v2/*.json`

## Migration Workflow

1. Inspect current docs and examples.
- Read `README.adoc` sections: description, compatibility, configuration, identifier, notices.
- Read fixture JSON files and extract only policy `configuration` objects.
- Keep examples grounded in existing fixtures; do not invent structures unless no fixture exists.

2. Create doc-gen source files.
- Create `.docgen/overview.md` from README narrative sections. Remove or don't include phase information in overview.md - docgen extracts this from policy.properties automatically. The phase property in examples.yaml is only for scoping that specific example, not declaring policy phases.
- Create `.docgen/matrix.yaml` from compatibility history.
- Create `.docgen/examples.yaml` with `genExamples: []` and `rawExamples` entries.
- Create `.docgen/examples/*.json` files with policy configuration payloads.
- Create a `.docgen/errors.yaml` file with error codes and descriptions. You can check the source code to find ExecutionFailure keys

3. Preflight schema compatibility for doc-gen.
- Ensure JSON Schema keywords have valid types in `src/main/resources/schemas/schema-form.json`.
- Specifically, `deprecated` must be a boolean (`true`/`false`), not a string (`"true"`/`"false"`).
- Apply only typing fixes required for schema validity; do not alter runtime semantics.

4. Update packaging input.
- Edit `src/assembly/policy-assembly.xml`.
- Replace README fileSet include from `README.adoc` to a docs include that preserves directory structure:
```xml
<fileSet>
    <directory>${basedir}</directory>
    <includes>
        <include>docs/**</include>
    </includes>
    <outputDirectory>./</outputDirectory>
</fileSet>
```
- Avoid `<directory>docs</directory>` with `./` output because it can flatten files in the ZIP.

5. Generate docs.
- Prompt user for a doc-gen config directory path (absolute path to folder containing files like `bootstrap.yaml` and `policy/default.yaml`).
- Run:
```bash
DOCGEN_CONFIG_DIR="<value provided by the user>"
docker run --rm -v "$DOCGEN_CONFIG_DIR":/config -v ./:/plugin graviteeio/doc-gen
```
- Optional compatibility alias:
```bash
export DOCGEN_ROOT="$DOCGEN_CONFIG_DIR"
```

6. Validate.
- Validate YAML files parse.
- Validate JSON examples parse.
- Run `mvn -DskipTests package`.
- Confirm generated ZIP contains `docs/` and `docs/POLICY_STUDIO_DOC.md`.

## File Conventions

Use this minimum file set:

- `.docgen/overview.md`
- `.docgen/errors.yaml`
- `.docgen/matrix.yaml`
- `.docgen/examples.yaml`
- `.docgen/examples/<example>.json`
- `README.md` (generated)
- `docs/POLICY_STUDIO_DOC.md` (generated)

Keep `README.adoc` in the repository unless explicitly requested to remove it.

## Examples YAML Pattern

Use this shape:

```yaml
genExamples: []
rawExamples:
  - title: <example title>
    templateRef: v4-api-proxy
    language: json
    properties:
      phase: request
    file: .docgen/examples/<file>.json
```

Use `phase: response` for response-scope examples.

## Validation Commands

```bash
ruby -e 'require "yaml"; YAML.load_file(".docgen/examples.yaml"); YAML.load_file(".docgen/matrix.yaml"); YAML.load_file(".docgen/errors.yaml")'
for f in .docgen/examples/*.json; do jq empty "$f"; done
mvn -DskipTests package
unzip -l target/*.zip | rg "docs/|POLICY_STUDIO_DOC"
```

## Troubleshooting

### YAML Validation Failures

**Symptom**: `ruby -e 'require "yaml"; YAML.load_file(...)'` fails with parse error.

**Fix**:
- Check indentation (use spaces, not tabs).
- Validate quotes around strings with special characters.
- Ensure list items use `- ` prefix consistently.
- Use online YAML validator for complex structures.

### JSON Validation Failures

**Symptom**: `jq empty` exits non-zero.

**Fix**:
- Run `jq . .docgen/examples/<file>.json` to see parse error location.
- Check for trailing commas (invalid in JSON).
- Ensure all strings are double-quoted.
- Validate brackets/braces are balanced.

### Docker Not Available

**Symptom**: `docker: command not found` or permission denied.

**Fix**:
- Ask user to install Docker

### Doc-Gen Image Pull Failures

**Symptom**: Cannot pull `graviteeio/doc-gen` image.

**Fix**:
- Check network connectivity.
- Try explicit pull: `docker pull graviteeio/doc-gen:latest`.

### Generated Docs Missing from ZIP

**Symptom**: `unzip -l target/*.zip | rg docs/` shows no results.

**Fix**:
- Verify `policy-assembly.xml` includes docs fileSet with:
    - `<directory>${basedir}</directory>`
    - `<include>docs/**</include>`
    - `<outputDirectory>./</outputDirectory>`
- If docs file is present but appears at ZIP root (`POLICY_STUDIO_DOC.md`), switch to `docs/**` include from `${basedir}` to preserve `docs/` path.
- Check `docs/POLICY_STUDIO_DOC.md` exists before running `mvn package`.
- Run `mvn clean package` to force rebuild.

### Doc-Gen Config Path Issues

**Symptom**: Docker container fails to find config or plugin files.

**Fix**:
- Use absolute paths: `DOCGEN_CONFIG_DIR="/full/path/to/gravitee-doc-gen-config/src"`.
- Ensure config directory contains required doc-gen config files.
- Check path doesn't contain spaces or special characters.
- On Windows, use WSL2 paths or convert: `/c/Users/...` not `C:\Users\...`.

### JSON Schema Type Validation Failures in Doc-Gen

**Symptom**: Doc-gen fails with an error like `expected boolean, but got string` for `/src/main/resources/schemas/schema-form.json`.

**Fix**:
- Locate the referenced key (commonly `deprecated`).
- Replace string values (`"true"` / `"false"`) with booleans (`true` / `false`).
- Re-run doc-gen after the typing fix.

### Maven Build Warnings About Missing README

**Symptom**: Build warns about missing README in assembly.

**Fix**:
- This is expected if old `README.adoc` fileSet not removed from `policy-assembly.xml`.
- Safe to ignore if new docs fileSet is correct.
- Optionally clean up old fileSet reference.

## Guardrails

- Do not change Java code, runtime schema semantics, or policy identifiers.
- Do not change CI unless explicitly requested.
- Keep content concise and factual in `overview.md`.
- Prefer examples derived from existing test fixtures over inventing new structures.
