# Formula Implementation Review
This skill guides you through a review of a Formula implementation.

## Setup

### 1. Identify the formula(s) to review
- [ ] Identify the formula class(es) to review
- [ ] Start a review document at `.ai/workplace/formula-reviews/$name.md`
- [ ] Read `/docs/Performance.md` for background on Formula's evaluation model

## Correctness Review

### 2. Review input and state equality guarantees
Input and state need correct equality. Data classes, primitive types (String, Int, etc.),
and other types with proper `equals`/`hashCode` implementations all work.
- [ ] Verify input types are data classes or have stable equality (non-data class defaults
      to referential equality, which causes child formulas to either always or never re-evaluate)
- [ ] Verify state types are data classes or have stable equality (non-data class breaks
      the `state != result.state` check in transition handling)
- [ ] Document findings in review doc

### 3. Review evaluate() for side effects
`evaluate()` should be pure — no logging, analytics, network calls, or other side effects.
Side effects belong in transition effects or actions. Since evaluate re-runs on every
input/state/child change, side effects here execute unpredictably many times.
- [ ] Check for side effects in evaluate() body
- [ ] Document findings in review doc

### 4. Review FormulaContext usage outside of evaluate()
`FormulaContext` methods (`context.onEvent`, `context.child`, `context.remember`, etc.)
should only be called during `evaluate()`. Calling them inside transition blocks produces
components not tracked by `LifecycleCache`.
- [ ] Check that transitions don't call `context` methods. Example of incorrect usage:
      ```kotlin
      onEvent { event ->
          val newState = state.copy(
              listener = context.onEvent {}
          )
          transition(newState)
      }
      ```
- [ ] Document findings in review doc

### 5. Review child formula, action, and listener usage within loops
Positional keys are unreliable within loops. Formula internally uses indexing in such cases,
but indexes are brittle — item ordering changes cause wrong component matching. Use unique
identifiers instead.
- [ ] Check that child formulas within loops declare `key(input)` with a simple identifier.
      Avoid complicated string concatenation.
- [ ] Check that listeners within loops have a unique key or the block is wrapped
      in `context.key() {}`
- [ ] Check that actions within loops use unique keys
- [ ] Document findings in review doc

### 6. Review action lifecycle assumptions
Actions with no key fire once on attach (can be controlled by conditional logic). Actions
with a key restart when the key changes, indicating request invalidation. Both are valid —
verify the assumption matches the intended behavior.
- [ ] For each action, check if the keying strategy matches the intended lifecycle
- [ ] Check if conditional logic around actions correctly controls when they run
- [ ] Document findings in review doc

## Performance Review

### 7. Review child inputs used within evaluate()
Runtime uses input equality to short-circuit child formula evaluation. Verify if input
`equals` and `hashCode` could be expensive.
- [ ] Check each `context.child()` call for the input being passed
- [ ] Flag inputs with large collections, nested objects, or non-data-class types
- [ ] Flag inline collection transformations (`.filter {}`, `.map {}`, `.sorted()`) that
      create new instances every evaluation
- [ ] Document findings in review doc

### 8. Review keys used within evaluate()
We use equality to maintain listeners, actions, and child formulas across evaluations via
`LifecycleCache`. Expensive key equality can cause performance issues.
- [ ] Check keys passed to `context.onEvent`, `context.callback`, actions, and `context.child`
- [ ] Verify keys are simple values (strings, ints, small data classes) or referentially stable
- [ ] Document findings in review doc

### 9. Review expensive work in evaluate() body
`evaluate()` re-runs on every input/state/child change. Expensive work here multiplies with
re-evaluation frequency.
- [ ] Check for sorting, filtering, mapping, or other transformations on large collections
- [ ] Suggest moving expensive computations to `context.remember` or state transitions
- [ ] Document findings in review doc

### 10. Review the frequency of state changes
Frequent state changes drive frequent re-evaluations across the tree.
- [ ] Review actions for high-frequency emissions (timers, streams, rapidly updating data)
- [ ] Review listeners for state changes that could cascade (one change triggering another)
- [ ] Document findings in review doc

### 11. Review tree depth and breadth
On every state change, evaluation propagates from root down to the changed area. Deep nesting
means more equality checks to reach the changed subtree. Wide formulas with many children,
listeners, or actions increase work per evaluation even when most children skip.
- [ ] Note formula tree depth — how many levels from root to leaf
- [ ] Note formulas with many `context.child()` calls, listeners, or actions
- [ ] Document findings in review doc

### 12. Summarize performance findings
- [ ] Update review doc with a summary of performance concerns and recommendations
- [ ] Prioritize findings by expected impact
