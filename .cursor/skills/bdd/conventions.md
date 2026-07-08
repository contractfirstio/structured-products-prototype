# Discovering BDD Conventions

Use during **Orient** and **Draft**. Every item below is **inferred from the current workspace** — never hardcode paths, tags, or steps from memory or a prior project.

## What to discover

### Layout

| Question | How to find the answer |
|----------|------------------------|
| Where do `.feature` files live? | Glob `**/*.feature`; use the common parent directory |
| Where are step definitions? | Search for `@Given`, `@When`, `@Then`, `Given(`, `defineStep`, etc. |
| What is the glue/package path? | Read the Cucumber suite config, `cucumber.js`, JUnit `@ConfigurationParameter`, or equivalent |
| How are tests invoked? | Search `package.json` scripts, Gradle/Maven tasks, `Makefile`, CI workflows for cucumber/e2e/feature |

### Tags

- List every tag on existing `Feature:` and `Scenario:` lines.
- **Default rule**: new features use the same tag(s) as sibling features at the same test layer (e.g. all e2e features share one tag).
- Add scenario-level tags only if the project already uses them for filtering.

### Naming

| Element | Infer from existing files |
|---------|---------------------------|
| Feature filename | Pattern in sibling `.feature` files (`snake_case`, `kebab-case`, domain prefix) |
| Feature title | Style of existing `Feature:` lines |
| Scenario title | Style of existing `Scenario:` lines |

If no examples exist, prefer: `snake_case.feature`, capability-based feature title, imperative scenario title describing user-visible outcome.

### Step voice

Read 10–20 existing steps and note:

- **Person**: first person (`I …`) vs third person (`the user …`)
- **Tense**: present vs past
- **Abstraction**: declarative (`I am on the checkout page`) vs imperative (`I click the checkout button`)
- **Context nouns**: how setup state is referenced (`current order`, `the logged-in user`, `my cart`)
- **Parameters**: Cucumber `{string}`/`{int}` vs other styles

New steps must match the dominant voice in the step catalog.

### Scenario structure

Observe how existing features group scenarios:

- One `Feature:` per screen, per API resource, or per business capability?
- Typical Given chains for setup?
- Order of CRUD scenarios?

Mirror the dominant pattern unless the user asks otherwise.

### Context / shared state

If step defs use a scenario context object (common in e2e frameworks), note:

- What entities are tracked (`currentUser`, `lastCreatedId`, etc.)
- How steps refer to them (`the current …`, `my …`)

New steps should follow the same context naming — do not invent a parallel pattern.

## Greenfield projects (no existing BDD)

When no `.feature` files exist:

1. Ask the user: feature directory, test runner, and UI vs API vs unit layer.
2. Apply these portable defaults unless they specify otherwise:

```gherkin
Feature: [Capability name]

  Scenario: [Outcome the user cares about]
    Given [precondition]
    When [action]
    Then [observable result]
```

- First person, present tense, user-observable outcomes
- `snake_case.feature` filenames
- Parameterize varying data with `{string}` and `{int}`

## Conventions snapshot (produce in Orient)

Before the grill, write a short snapshot the draft will follow:

```
Conventions snapshot:
- Features directory: [discovered path]
- Filename pattern: [e.g. snake_case.feature]
- Feature tags: [e.g. @e2e]
- Step voice: [e.g. first person, present, declarative]
- Context pattern: [e.g. "current X" nouns, or none observed]
- Run command: [discovered command or TBD]
- Reusable steps: [count] ([link to step catalog in session])
```

Refer back to this snapshot in Phase 4 — do not re-read the whole codebase unless the user changes scope.
