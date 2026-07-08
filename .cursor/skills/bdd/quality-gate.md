# BDD Quality Gate

Run every check before writing the file. Fix or ask one clarifying question per failure.

## Structure

- [ ] Feature has the correct tag(s) for this project (match tags from the conventions snapshot)
- [ ] Feature title names a **capability**, not a screen or ticket ID
- [ ] Each scenario title describes an **outcome** a product owner would recognize
- [ ] Scenarios are **independent** — each has enough `Given` setup to run alone
- [ ] No scenario depends on another scenario having run first

## Gherkin discipline

- [ ] Every scenario follows Given → When → Then (And only extends the same clause type)
- [ ] No more than ~10 steps per scenario; split if longer
- [ ] No duplicate scenarios testing the same behaviour with different wording
- [ ] Placeholders (`{string}`, `{int}`) used for data that varies between runs

## Testability

- [ ] Every `Then` asserts something **observable** (UI text, visibility, list content, confirmation message)
- [ ] No `Then` asserts internal state, DB rows, or HTTP codes (unless this project explicitly tests API features in Gherkin)
- [ ] Every `When` has a plausible path to its `Then` outcomes in the same scenario
- [ ] Preconditions in `Given` match what the application actually requires (verified against code)

## Step reuse

- [ ] Existing steps are reused with **identical** wording where behaviour matches
- [ ] New steps are phrased for reuse across scenarios (no one-off UUIDs or timestamps in step text)
- [ ] New steps do not collide with existing step definitions (ambiguous regex matches)

## Completeness (grill coverage)

- [ ] Happy path is covered
- [ ] Each failure mode the user confirmed in the grill has a scenario OR is listed under deferred scope
- [ ] Destructive flows (delete, cancel) match confirmed confirmation/irreversibility behaviour
- [ ] Empty-state behaviour is covered if the user said it matters

## Readability

- [ ] A developer who has not attended the grill session can read the feature file cold and understand expected behaviour
- [ ] Domain terms match the codebase and existing features (no synonym drift)

## Delivery metadata (agent produces after PASS)

- [ ] Step reuse report lists every step as REUSE or NEW
- [ ] NEW steps have suggested step-definition signatures
- [ ] File path follows project convention
