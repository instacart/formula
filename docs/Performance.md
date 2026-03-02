# Performance Guide

## Evaluation performance
`evaluate()` is the core formula mechanism — it produces output and actions based on current input, 
state, and child outputs. It re-runs when **input changes**, **state changes**, or 
**child hierarchy state changes**. For every change, the root formula re-evaluates all the way down 
to the changed area.

### Maintaining state across evaluations
Actions, child formulas, and listeners are maintained across evaluations using composite
keys (positional key based on anonymous class type + optional user-provided key). During
evaluation, each `context.child()`, `context.onEvent()`, or action declaration looks up
its key in `LifecycleCache` — if a matching component exists, it is reused; if not, a new
one is initialized. Components not requested during an evaluation are cleaned up afterward.

### Equality matters
Formula relies on equality checks to prune the evaluation tree and to maintain components across
evaluations. Input and state equality determine whether a subtree needs to recompute. Key equality
determines whether actions, listeners, and child formulas are matched to their previous instances
in `LifecycleCache`.

### Equality cost
Kotlin data class `equals()` checks referential equality (`===`) first — if same instance,
comparison is instant. Structural equality (field-by-field) only runs when references differ.
This means fields that maintain the same instance across evaluations are cheap to compare.
Listeners and remembered values get this automatically via `LifecycleCache`. Data fields
passed through from parent state (e.g., `state.items`) also keep the same reference when
unchanged.

Equality cost has two dimensions:
- **How often structural equality runs** — every new instance that could have been the
  same reference forces a structural check. Common cause: rebuilding collections inline
  (`.filter {}`, `.map {}`) in evaluate() instead of caching via `context.remember` or
  computing in state transitions.
- **How expensive each check is** — cost is proportional to data size and depth. Large
  collections and deeply nested objects are expensive to compare field-by-field. Keys used
  for listeners, actions, and child formulas also pay `hashCode()` on every lookup — Kotlin
  data classes recompute `hashCode()` on every call (no caching), so complex keys add cost
  on every evaluation.

### Computation cost
`evaluate()` runs on every input, state, or child change. Expensive operations in the body
multiply with re-evaluation frequency. Common examples:
- **String operations** — concatenation, formatting, parsing
- **Collection transformations** — sorting, filtering, mapping, grouping
- **Data parsing/conversion** — JSON parsing, date formatting, number formatting

Move expensive computations to `context.remember` so they are cached across evaluations,
or compute during state transitions so they run only when data actually changes.

### Downstream output cost
Output produced by the root formula is consumed downstream — typically by UI rendering
(RecyclerView diffing, Compose recomposition, view binding). The cost of processing each
output is outside Formula's control but is driven by how often Formula produces new output
and how large/complex the output is.

### Evaluation frequency
Each state change triggers a re-evaluation pass from root down to the changed area. The
total cost is both the evaluation itself and the downstream output consumption. High-frequency
changes compound quickly, especially when multiple sources fire concurrently.

Common patterns to watch for:
- **Multiple child formulas making requests** — X formulas each making N requests, responses
  arrive at different times. Each triggers a separate evaluation pass through the tree.
- **Scroll events with position data** — continuous stream of position updates during
  scrolling, each triggering evaluation during a critical user experience where jank is
  most noticeable.
- **View events from list components** — e.g., visibility tracking or impression events for
  each list item. Multiple items becoming visible at once fires multiple events in quick
  succession.

### Summary
Performance is affected by: evaluation complexity, number of evaluations in a given time window,
and what thread they execute on.