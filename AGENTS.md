# Formula - AI Assistant Instructions

## Project Structure

- `formula/` - Core framework (state management, actions, formulas)
- `formula-android/` - Android integration (fragments, activities, views)
- `formula-android-compose/` - Jetpack Compose integration
- `formula-android-tests/` - Android tests
- `formula-test/` - Testing utilities and test formulas
- `formula-rxjava3/` - RxJava3 interop
- `formula-lint/` - Custom lint rules
- `formula-performance/` - JMH benchmarks
- `test-utils/` - Test utils used by formula-android and formula-android-tests
- `samples/` - Sample applications

## Core Concepts

**Input/Output/State:**
- `Input` - Immutable data from parent containing data and event listeners. Changes trigger re-evaluation. Use `Unit` if none needed.
- `State` - Internal mutable state managed by formula. Changes trigger re-evaluation.
- `Output` - Immutable data returned containing data and event listeners. Called "render model" when used for UI.

**Evaluate:**
- Pure function called on Input/State/Child changes. Receives `Snapshot<Input, State>` with current values and `FormulaContext`.
- Returns `Evaluation<Output>` containing output and optional actions.
- Must be side-effect free. Side effects only in event listeners or transition effects.

**Transitions:**
- State changes happen via `Transition<Input, State, Event>` function that returns `Transition.Result<State>`.
- Result types: `Stateful(state, effects)` (new state + optional effects), `OnlyEffects(effects)` (no state change), `None` (nothing).
- Effects execute after state changes. Types: `Effect.Main`, `Effect.Background`, `Effect.Unconfined`.
- Use `TransitionContext.transition(state)` to create results.

**Listeners:**
- Created via `context.onEvent<Event>(key?) { transition() }` - returns `Listener<Event>` which is `(Event) -> Unit`.
- Created via `context.callback(key?) { transition() }` - returns `() -> Unit`.
- Listeners must be returned in Output or passed to children via Input to remain active.
- Keying: Listeners use transition type + optional key. Same key + type = same listener.

**Actions:**
- Declarative async operations. Declared in `context.actions { }` block within Evaluation.
- Actions run when returned in Evaluation, cancelled when not returned or key changes.
- Common: `Action.onInit()`, `Action.onData(value)`, `Action.onTerminate()`, `Action.fromFlow { }`, `Action.launch { }`.
- Attach handler: `action.onEvent { event -> transition() }`.
- Actions identified by key + call site (positional memoization).

**Composition:**
- `context.child(formula, input)` - runs child formula, returns latest output, manages lifecycle.
- Child re-evaluates when its input changes or its internal state changes.
- Parent re-evaluates when child output changes.

**Formula Lifecycle:**
- `initialState(input)` - creates initial state when formula starts or when `key(input)` changes.
- `onInputChanged(oldInput, input, state)` - called before evaluate when Input changes. Returns new state or existing state.
- `key(input)` - optional identity for formula instance. If key changes, formula restarts with `initialState`.
- Evaluate called on: initial start, input change, state change, child output change.

**ExecutionType:**
- Controls threading/timing for transitions and actions.
- `Transition.Immediate` - process on arrival thread immediately (for UI events/navigation).
- `Transition.Background` - process on background thread (for expensive operations).
- `Transition.Batched(scheduler)` - collect and process as batch (experimental).
- Apply to transitions: `context.onEventWithExecutionType(type, key) { }` or `context.callbackWithExecutionType(type, key) { }`.
- Apply to actions: `action.onEventWithExecutionType(type) { }`.

**Helper Formulas:**
- `StatelessFormula<Input, Output>` - formula without state (State = Unit).
- `ActionFormula<Input, Output>` - converts Action to Formula, emits initialValue until action produces value. Resubscribes on input change.

**Mental Model (Jetpack Compose similarities):**
- `evaluate()` ~ Composable function - pure, re-executed on state/input/child changes (recomposition).
- `Action` ~ `LaunchedEffect` - declarative side effects tied to keys, cancelled when removed.
- `Transition` ~ state updates in Compose - deferred effects after state change.
- `context.child()` ~ calling composables - automatic lifecycle management.

## Key Classes

**Core API:**
- `Formula<Input, State, Output>` - Base stateful formula class
- `StatelessFormula<Input, Output>` - Formula without internal state
- `ActionFormula<Input, Output>` - Converts Action to Formula
- `FormulaContext` - Provides composition and action APIs
- `Action` - Represents asynchronous side effects
- `Transition` - Represents state changes
- `Evaluation` - Contains output and optional actions
- `Snapshot` - Contains current Input, State, and FormulaContext
- `FormulaRuntime` - Entry point for starting formulas
- `Listener<Event>` - Function type for event handlers: `(Event) -> Unit`
- `TransitionContext` - Provides utilities for creating Transition.Result
- `Effect` - Side-effect executed after state changes
- `DeferredAction` - Action combined with event listener returned in Evaluation

**Android:**
- `FormulaFragment` - Base fragment for Formula integration
- `FeatureFactory` - Creates features from fragments/keys
- `ViewFactory` - Creates views for render models
- `RenderView` - Interface for rendering output to views
- `ComposeViewFactory` - Creates Jetpack Compose views for render models

## Key Implementation Classes

**Internal (formula/src/main/java/com/instacart/formula/internal/):**
- `FormulaManager` / `FormulaManagerImpl` - Manages formula lifecycle and evaluation
- `ActionManager` - Manages running actions
- `ChildrenManager` - Manages running child formulas
- `Listeners` - Manages active listeners
- `SynchronizedUpdateQueue` - Coordinates state updates ensuring thread safe evaluation
- `Frame` - Represents an evaluation frame

## Example Patterns

**Basic Formula:**
```kotlin
class CounterFormula : Formula<Unit, Int, CounterRenderModel>() {
    override fun initialState(input: Unit) = 0

    override fun Snapshot<Unit, Int>.evaluate(): Evaluation<CounterRenderModel> {
        return Evaluation(
            output = CounterRenderModel(
                count = state,
                onIncrement = context.onEvent { transition(state + 1) }
            )
        )
    }
}
```

**With Actions:**
```kotlin
override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
    return Evaluation(
        output = /* output */,
        actions = context.actions {
            if (state.isRunning) {
                Action.fromFlow { ticker() }.onEvent {
                    transition(state.copy(time = state.time + 1))
                }
            }
        }
    )
}
```

**With Child Formulas:**
```kotlin
val childOutput = context.child(
    formula = ChildFormula(),
    input = ChildInput(
        data = state.data,
        onEvent = context.onEvent { event ->
            transition(/* new state */)
        }
    )
)
```

## Testing

**TestFormulaObserver:**
- `formula.test()` - creates test observer for formula.
- `observer.input(value)` - provides input to formula.
- `observer.output { /* assert */ }` - asserts on latest output.
- `observer.assertOutputCount(n)` - asserts output emission count.
- `observer.assertNoErrors()` / `observer.assertHasErrors()` - error assertions.
- `observer.values()` / `observer.errors()` - get all outputs/errors.

**Test Utilities:**
- `withSnapshot(input, state) { /* use context */ }` - test functions requiring FormulaContext/Snapshot.
- `formula.testFormula(initialOutput)` - creates TestFormula mock with fixed output.
- `TestCallback()` - mock callback for testing.
- `TestListener<Event>()` - mock listener for testing.

## Build Commands

```bash
./gradlew build                      # Build all modules
./gradlew test                       # Run all tests
./gradlew formula:test               # Test specific module
./gradlew formula-android:testDebug  # Test Android module
./gradlew formula-performance:jmh    # Run performance benchmarks
```
