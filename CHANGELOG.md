# Changelog
## [0.5.4] - TODO
- Change callback key type from String to Any.
- Removed deprecated `state()` extension
- Updated AndroidX appcompat to 1.1.0
- Updated AndroidX fragment-ktx extensions to 1.2.1
- Bugfix: Fix `Stream.onTerminate` causing illegal state exception.
- **Breaking**: In formula-android, replacing lastEntry() with visibleState() in FragmentFlowState.
- Allow down-casting `ActivityStoreContext<MyActivity>` to `ActivityStoreContext<FragmentActivity>`
- Enable global fragment error logging.

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
