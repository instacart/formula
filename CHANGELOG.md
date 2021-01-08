# Changelog
## [0.6.2] - TBD
- **Breaking**: Remove `events(observable) { }` utility function.
- Removing `rxjava3` from core module.
- Moving `key` function into `IFormula`
- **Breaking**: Remove `state.noEffects()` extension function. Use `transition(state)` instead.
- Added `StreamFormula` and `ObservableFormula`.
- Allow nullable formula state.

## [0.6.1] - November 18, 2020
- Bugfix: Fix runtime ignoring `Formula.key` for the root formula.
- [formula-android] Adding main thread check before notifying fragments. 
- **Breaking**: Crash when duplicate fragment contract is registered.
- Migrated `formula-android` to use core `Formula` interface.
- **Breaking**: Delete `FlowStore` and `FlowState`.
- Adding `formula-rxjava3` module.

## [0.6.0] - July 27, 2020
- **Breaking**: Changing from RxJava 2.x to RxJava 3.x
- Change callback key type from String to Any.
- Removed deprecated `state()` extension
- Updated AndroidX appcompat to 1.1.0
- Updated AndroidX fragment-ktx extensions to 1.2.1
- Bugfix: Fix `Stream.onTerminate` causing illegal state exception.
- **Breaking**: In formula-android, replacing lastEntry() with visibleState() in FragmentFlowState.
- Allow down-casting `ActivityStoreContext<MyActivity>` to `ActivityStoreContext<FragmentActivity>`
- Enable global fragment error logging.
- **Breaking**: Replacing `Renderer` function `render` with `invoke`. You can now directly call `renderer(value)`. 
- **Breaking**: Renaming `RenderView` property `renderer` to `render`.
- **Breaking**: Replaced `TestFormulaObserver.childInput` functions with `TestFormula`.
- **Breaking**: `StatelessFormula` no longer implements `Formula`, but rather implements `IFormula`.
- **Breaking**: Removing `child` builder.
- **Breaking**: Renaming `Evaluation.renderModel` to `Evaluation.output`.
- **Breaking**: Moving child `key` into `Formula` and `StatelessFormula` interfaces.

## [0.5.3] - December 10, 2019
- Change child formula key from String to Any.

## [0.5.2] - October 25, 2019
- Maven central release

## [0.5.2-alpha2] - October 22, 2019
- Bugfix: Fix `context.key` exit scope logic.

## [0.5.2-alpha1] - October 22, 2019
- **Breaking**: Replacing `message` API with post transition side-effect block. [#115](https://github.com/instacart/formula/pull/115)

Before:
```kotlin
transition(state.copy()) {
  message(analytics::trackEvent)
}
```

After:
```kotlin
transition(state.copy()) {
  analytics.trackEvent()
}
```

## [0.5.1] - October 7, 2019
- Initial release.
