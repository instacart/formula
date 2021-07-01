# Changelog
## [0.7.1] - TBD
- **Breaking**: Rename `FragmentBindingBuilder` to `FragmentStoreBuilder`

## [0.7.0] - June 30, 2021
- **Breaking**: Remove `events(observable) { }` utility function.
- Removing `rxjava3` from core module.
- Moving `key` function into `IFormula`
- **Breaking**: Remove `state.noEffects()` extension function. Use `transition(state)` instead.
- Added `StreamFormula` and `ObservableFormula`.
- Allow nullable formula state.
- **Breaking**: Removing `FormulaFragment.renderView()` function
- **Breaking**: Replace `FormulaFragment.getFragmentContract()` with `getFragmentKey()`. 
- **Breaking:** Removing generic key parameter from `KeyState`.
- **Breaking**: Removing generic parameter from `FormulaFragment`
- Added new Feature API with `FeatureFactory` and other related classes.
- Update `ActivityStoreContext.isFragmentStarted` and `ActivityStoreContext.isFragmentResumed` functions.
- **Breaking**: Removed `BackStackStore` and `BackStack`.
- **Breaking**: Removed `SharedStateStore`.
- **Breaking**: Replacing `FlowDeclaration` and `FlowIntegration` with `FlowFactory`.
- Added `bind(myFeatureFactory) { component -> component.myFeatureDependency() }` binding method.
- Added `bind(myFlowFactory) { component -> component.createMyFlowDependencies() }` binding method.
- Rename `KeyState` to `FragmentState` and move it to `android` package.
- Move `ActivityStoreContext` into `android` package.
- Move `ActivityStore` into `android` package.
- Move `FragmentId` into `android` package.
- Move `FormulaAppCompatActivity` into `android` package.
- Move `ActivityResult` into `android.events` package.
- Move `FragmentKey` into `android` package.
- Move BaseFormulaFragment into `android` package.
- Move `DisposableScope` into `android` package.
- Move `FormulaFragment` into `android` package.
- Move `BackCallback` into `android` package.
- Move `FragmentLifecycleEvent` into `android.events` package.
- Move `Integration` into `android` package.
- Move `FragmentLifecycleCallback` into `android` package.
- Move `FragmentComponent` into `android` package.
- Move `FragmentContract` into `android` package.
- Move `FragmentFlowState` into `android` package.
- Move `FragmentFlowStore` into `android` package.
- **Breaking**: Replaced `formula.test(inputObservable)` with `formula.test().input()` 
- Add `TestFormulaObserver.assertNoErrors()`
- Added `formula-android-compose` module

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
