### Why state & render model separation

This allows us to keep the view very stupid. Let's say we have a state class such as this

```kotlin
data class State(
  val userInfo: Lce<UserInfo>,
  val saveRequest: Lce<SaveResponse>? = null
)

// UI layer now has to contain logic to process these properties.
fun render(state: State) {
  saveButton.isEnabled = state.userInfo.hasData() &&
    (state.saveRequest == null && !state.saveRequest.isLoading())
}
```

To avoid having such logic in the view, we have created the RenderModel concept.
```kotlin
data class RenderModel(
  val isSaveEnabled: Boolean
)
```

The logic where we do this transformation is encapsulated in the RenderModelGenerator.

```kotlin
class MyRenderModelGenerator: RenderModelGenerator<State, RenderModel> {
  override fun toRenderModel(state: State): RenderModel {
    return RenderModel(
      isSaveEnabled = state.userInfo.hasData() &&
        (state.saveRequest == null && !state.saveRequest.isLoading())
    )
  }
}
```


Now, the UI would receive a RenderModel instead of State. Also, we can now easily unit test that logic separately from the view.
