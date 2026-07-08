# BDD Skill — User Guide

Grill a feature idea into a rock-solid Cucumber `.feature` file. The agent interviews you one question at a time, cross-checks against your codebase, and only writes Gherkin after you confirm shared understanding.

Works in **any project** — conventions are discovered from existing `.feature` files and step definitions in the workspace.

## Quick start

In Cursor chat:

```
@bdd I want to specify [your feature in one or two sentences]
```

Or say: *"Use the bdd skill"* / `/bdd` (if your Cursor setup maps skills to slash commands).

## Where to install

| Location | Scope |
|----------|--------|
| `.cursor/skills/bdd/` in a repo | Shared with the team via git |
| `~/.cursor/skills/bdd/` | Available in all your projects |

Copy this entire `bdd/` folder to either location. The skill must contain `SKILL.md` at minimum.

## What happens in a session

The agent runs six phases. You mostly answer questions during the grill.

| Phase | What the agent does | What you do |
|-------|---------------------|-------------|
| **Orient** | Finds `.feature` files, step defs, tags, naming patterns, test commands | Wait — review the short summary |
| **Seed** | Asks what feature to specify | Describe it in 1–2 sentences |
| **Grill** | One question at a time — actors, errors, empty states, permissions, etc. | Answer each question; push back if something's wrong |
| **Outline** | Lists scenarios to cover (not Gherkin yet) | Approve, add, or remove scenarios |
| **Draft** | Writes full Gherkin matching your project's style | Review wording |
| **Deliver** | Writes the `.feature` file + step reuse report | Confirm you're done |

The agent **will not** write the feature file until you say you have **shared understanding** and the quality gate passes.

## Tips for a good session

- **Start vague** — "Users can archive orders" is enough; the grill fills in gaps.
- **One answer per turn** during the grill — the skill enforces this on purpose.
- **Use domain language** from your product; the agent will align with terms in your code and existing features.
- **Say when you're done** — e.g. *"Yes, we have shared understanding"* — to move from grill to outline.
- **Redirect freely** — if a question misses the point, say so.

## Example opening prompts

```
@bdd I want to specify cancelling a subscription from account settings.
```

```
@bdd New feature: bulk import of product templates from CSV.
Admin uploads a file and sees validation errors per row.
```

```
@bdd Use the bdd skill — we're adding password reset via email link.
```

## What you get at the end

1. **`.feature` file** — written to your project's features directory (discovered from existing BDD layout)
2. **Step reuse report** — each step marked `REUSE` (already in glue) or `NEW` (needs implementation)
3. **Suggested stubs** — signatures for new step definitions (no code unless you ask)
4. **Run command** — how to execute BDD/e2e tests in this project, if found in build config

## What this skill does *not* do

Unless you explicitly ask in the same session:

- Implement step definitions
- Write application code
- Auto-run tests

## Files in this folder

| File | Audience |
|------|----------|
| `README.md` | You (this guide) |
| `SKILL.md` | Agent — main workflow |
| `conventions.md` | Agent — how to discover BDD patterns in any repo |
| `quality-gate.md` | Agent — checklist before writing the file |

## Inspired by

The grilling loop is adapted from [mattpocock/skills](https://github.com/mattpocock/skills) (`grill-me` / `grilling`), with output focused on Cucumber Gherkin instead of a general plan.
