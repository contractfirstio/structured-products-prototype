---
name: bdd
description: >-
  Grill the user through feature definition and produce a rock-solid Cucumber
  .feature file aligned with the project's existing BDD. Use when the user wants
  to write, define, or specify a new feature in Gherkin, create BDD scenarios,
  or says /bdd.
disable-model-invocation: true
---

# BDD Authoring

Turn a fuzzy feature idea into a **challenged** Cucumber artifact — Gherkin that matches the **current project's** conventions, reuses existing steps where possible, and survives implementation without surprise gaps.

Two jobs, one session:

1. **Grill** — relentless interview until behaviour is unambiguous (adapted from [mattpocock/skills `grilling`](https://github.com/mattpocock/skills)).
2. **Author** — write the `.feature` file only after the grill reaches shared understanding.

Do not write the feature file, create step definitions, or start implementation until the user confirms the spec is complete.

## Audience: Business Analyst

The person at the keyboard is a **Business Analyst (BA)** — not a developer. They have **no technical knowledge** and that is expected.

| Ask the BA for | Do not ask the BA for |
|----------------|----------------------|
| Raw business features — who does what, under which rules, with what outcomes | UI designs, wireframes, screen layouts, component placement |
| Business language — actors, capabilities, policies, exceptions | Frameworks, APIs, databases, selectors, file paths |
| Business facts they own — eligibility rules, calculations, approvals, legal constraints | How data is stored, table shapes, or schema design |
| Clarification when business rules are ambiguous | Technical trade-offs or implementation choices |

**Data modelling:** the BA may **contribute** — naming entities, describing what must be recorded, relationships in business terms ("an order has line items"). They do **not own** data modelling. Developers own persistence, schema, and technical shape. When data structure matters for the spec, ask the BA what the business needs to capture and know; infer or defer technical structure from code yourself. Flag open data-modelling decisions for developers in the deliverable, not as grill questions.

**Your job as agent:** absorb all technical detail from the codebase during Orient. Translate the BA's business answers into testable Gherkin in Draft — the BA reviews outcomes in plain language, not implementation.

**Question style:** plain business English. Never ask the BA to read code, name a route, or design a screen. If you need a UI detail the BA hasn't specified, propose a reasonable user-visible outcome based on existing app patterns and ask: *"When [business action] succeeds, should the user see a confirmation, return to a list, or something else?"* — not *"Should we show a modal or a toast?"*

## Session progress

Copy this checklist and update it as you go:

```
BDD session:
- [ ] Orient — catalogued existing features, steps, conventions, and domain code
- [ ] Seed — feature scope named by user
- [ ] Grill — decision tree walked; no open gaps
- [ ] Outline — scenario list approved by user
- [ ] Draft — Gherkin reviewed with user
- [ ] Gate — quality-gate checklist passed
- [ ] Deliver — .feature file written; step reuse report produced
```

---

## Phase 0: Orient (legwork — no questions yet)

Before asking anything, explore **the workspace you are in**. Never assume paths, tags, frameworks, or step wording from a previous project.

### Discover BDD layout

1. Find all `*.feature` files (`**/*.feature`) and read every one.
2. Find step-definition glue — common patterns:
   - `**/steps/**`, `**/stepdefs/**`, `**/step_definitions/**`
   - `*Steps.{kt,java,ts,js,py,rb}`, `@Given` / `@When` / `@Then` annotations
   - `defineStep`, `Given(`, `When(`, `Then(` in JS/TS
3. Build a **step catalog**: exact step text → source file.
4. Find how tests are run — search build files (`package.json`, `build.gradle*`, `pom.xml`, `Makefile`, `pyproject.toml`, CI config) for cucumber, gherkin, e2e, or feature-related tasks.

### Infer project conventions

From existing features and step defs, derive a **conventions snapshot** (see [conventions.md](conventions.md) for what to extract). Record:

| Convention | Infer from |
|------------|------------|
| Feature file directory | Where existing `.feature` files live |
| File naming | Existing feature filenames |
| Tags | Tags on existing `Feature:` or `Scenario:` lines |
| Step voice | Person (I/we), tense, abstraction level in existing steps |
| Setup patterns | Recurring `Given` chains, context nouns (`current X`, `the logged-in user`) |
| Parameter style | `{string}`, `{int}`, `<name>` in existing steps |
| Scenario grouping | One feature per screen vs per capability |

If the project has **no** existing BDD, apply sensible defaults from [conventions.md](conventions.md) and note technical setup for developers — do not ask the BA about test runners or file paths.

### Skim domain code

Skim routes, views, API handlers, and domain types related to the feature area the BA will describe. Note field names, validation rules, and user-visible labels — use these in Draft, not as grill questions to the BA.

### Orient summary

Present 3–5 bullets to the user in **plain business language** — no jargon from the step catalog or codebase unless the BA already uses those terms:

- What kind of features this project already specifies (e.g. "end-to-end user journeys") — not file paths
- Domain terms the application already uses (from code and existing features)
- That you will handle technical test wording; they focus on behaviour

Keep the technical conventions snapshot (paths, tags, reusable steps, run command) in your working notes for Draft — do not dump it on the BA unless they ask.

---

## Phase 1: Seed (one question)

Ask exactly one question in business terms:

> **What business capability or behaviour do you want to specify?** In one or two sentences: who is involved, what they need to achieve, and what should happen when it goes right.

Wait for the answer. Do not ask follow-ups yet.

---

## Phase 2: Grill (relentless, one question at a time)

Interview relentlessly until every branch of the behaviour tree is resolved. This is the core of the session.

### Rules (non-negotiable)

- **One question per message.** Multiple questions bewilder. Wait for each answer.
- **Business decisions from the BA, technical facts from the code.** If the answer is in the repo (labels, existing flows, field names), look it up — do not ask the BA. If it is a business rule or policy, ask the BA.
- **Recommend an answer** with each question when you have a strong opinion — framed as a business outcome, not a technical design.
- **Challenge gaps in business logic** — missing rules, unclear actors, undefined edge cases. Do not challenge the BA on how to build it.
- **Use the project's domain language** when the BA already uses it; otherwise prefer the BA's terms and align to code only in Draft.
- **Do not author Gherkin yet.** Stay in interview mode until Phase 4.
- **Do not ask for UI design.** Ask what the user/business needs to **know**, **see**, or **be able to do next** — you map that to steps later.

### Decision tree (walk every relevant branch)

For each branch below, ask at least one **business-level** question if it applies. Skip branches that are clearly out of scope — but say why you're skipping. Resolve technical equivalents yourself after the grill.

| Branch | Probe for (ask the BA) | Agent resolves (do not ask) |
|--------|------------------------|----------------------------|
| **Actor** | Which role or person? Customer, admin, partner? | Login mechanics, session handling |
| **Trigger** | What business event or decision starts this? | Which page or API triggers it |
| **Preconditions** | What must already be true in the business? (e.g. "account is active", "order is shipped") | How setup steps are worded in glue |
| **Happy path** | Step-by-step business flow — what happens from start to successful outcome? | UI layout, buttons, navigation path |
| **Observable outcomes** | What should the actor know or receive when it succeeds? (confirmation, updated status, document) | Exact widget text unless BA specifies copy |
| **Validation failures** | What inputs or situations are not allowed? What should the actor be told? | Error component, HTTP codes |
| **Authorization** | Who is not allowed to do this? What should happen for them? | Role checks in code |
| **Empty / zero state** | What should happen when there is nothing to act on yet? | Empty-state UI pattern |
| **Concurrency / duplicates** | Can the same action happen twice? What should the business allow? | Idempotency implementation |
| **Destructive actions** | Should destructive actions be reversible? Is confirmation required by policy? | Delete dialog design |
| **Boundaries** | Business limits — amounts, dates, quantities, eligibility thresholds | Input field maxLength |
| **Error recovery** | If the action cannot complete, what should the actor do? | Retry UI, timeout handling |
| **Persistence** | After leaving and returning, what business state must still hold? | Storage mechanism |
| **Cross-feature effects** | Does this affect other business processes or records? | Integration points |
| **Data captured** | What business information must be recorded or shown? (BA contributes) | Schema, tables, types — **developer-owned** |

When data modelling surfaces during the grill, capture it as **business data requirements** (e.g. "we need to record the cancellation reason and effective date"). Note items needing developer design in **For developers** — do not press the BA to finalise structure.

### Gap-detection heuristics

Stop and drill deeper when you notice:

- **Orphan outcomes** — a Then with no When that plausibly causes it
- **Hidden state** — behaviour that depends on data the scenarios never set up
- **Magic strings** — values that need to be parameterized (`{string}`, `{int}`)
- **Step-definition mismatch** — phrasing that won't match existing glue (check the step catalog)
- **Implementation leakage** — steps naming CSS selectors, DB tables, or REST paths instead of user-visible behaviour
- **Scenario coupling** — scenarios that only work if run in a fixed order (prefer independent scenarios with their own Given setup)

### Grill completion criterion

The grill is done when:

- Every applicable branch is resolved or explicitly marked out of scope by the BA
- You can narrate the full happy path in business terms without saying "probably" or "I assume"
- The BA confirms: **"Yes, we have shared understanding."**

If they hesitate, you are not done — find the open branch and ask one more question.

---

## Phase 3: Outline (get approval before writing)

Present a **scenario outline** in business language (not Gherkin yet):

```
Feature: [title]

Scenarios to cover:
1. [Happy path] — ...
2. [Alternate] — ...
3. [Edge case] — ...
...

Deferred / out of scope:
- ...

For developers (data modelling & technical design):
- ...
```

Ask: **"Does this scenario list capture the business behaviour you need, or should we add/remove/rename any?"**

Wait for approval before drafting Gherkin.

---

## Phase 4: Draft (Gherkin)

Translate the approved business outline into Gherkin using the **conventions snapshot** from Phase 0. This translation is **agent work** — map business outcomes to existing steps and user-visible assertions without asking the BA to design UI.

### Authoring rules

- Reuse **exact** step text from the step catalog when a step already exists.
- For new steps, match the voice and abstraction level of existing steps in this project.
- Phrase new steps for reuse across scenarios (parameterize variable data).
- Prefer **declarative** steps describing what the business actor achieves — derive navigation/UI from existing app patterns, not BA wireframes.
- One scenario = one behaviour. Split combined flows unless the project already combines them.
- Use `Given` for setup, `When` for actions, `Then`/`And` for assertions.
- Parameterize variable data with the placeholder style this project already uses.

Show the full draft to the BA with a **plain-language summary** alongside the Gherkin (what each scenario proves in business terms). Ask: **"Does this match the behaviour you described, or should any outcomes be reworded?"**

---

## Phase 5: Gate (quality checklist)

Run every item in [quality-gate.md](quality-gate.md). For each failure, fix the Gherkin or ask one clarifying question — do not skip items.

Present a short gate report:

```
Quality gate: PASS | N issues fixed
- [item]: [what you changed]
```

Gate must be **PASS** before delivery.

---

## Phase 6: Deliver

1. **Write** the `.feature` file to the discovered features directory using the project's file-naming pattern. If the BA specifies a different path, use theirs.
2. **Step reuse report** — for developers: each step as `REUSE` or `NEW`, with file reference for reused steps.
3. **Suggested stubs** — for NEW steps only, suggested step-definition signatures (no implementation unless asked).
4. **For developers** — list business data requirements captured during the grill that need data-model design; run command for BDD/e2e tests if discovered.
5. **For the BA** — one-paragraph summary of what was specified and what was deferred.

Do not implement step definitions or application code unless explicitly asked in the same session.

---

## Anti-patterns (do not produce these)

| Bad | Why | Better |
|-----|-----|--------|
| Asking the BA "which screen should this be on?" | UI design is not their job | "After success, should the user stay on the form or return to a list?" |
| Asking the BA to define table columns | Data modelling is developer-owned | "What business information must we record?" → flag schema for developers |
| `When I click #save-btn` | Implementation detail | `When I save the [entity]` |
| `Then the API returns 200` | Not user-observable in UI e2e | `Then I should see the [confirmation message]` |
| Scenarios named `Scenario 1` | Not readable | `Scenario: [User-visible outcome]` |
| One giant scenario with 20 steps | Hard to debug | Several focused scenarios |
| Steps copied from another project | Won't match glue or voice | Reuse exact steps from the step catalog |

---

## Additional resources

- How to discover conventions in any project: [conventions.md](conventions.md)
- Pre-delivery checklist: [quality-gate.md](quality-gate.md)
