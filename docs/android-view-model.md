Define a `ViewModel` which runs the formula as a `StateFlow` until `onCleared` is called.
```kotlin
class CounterViewModel : ViewModel() {
    private val formula = CounterFormula()

    val outputs: StateFlow<CounterOutput> =
        formula.runAsStateFlow(viewModelScope, input = Unit)
}
```

In our activity, we then collect the Output changes and render them with Compose.
```kotlin
class MyActivity : ComponentActivity() {
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContent {
            val output by viewModel.outputs.collectAsStateWithLifecycle()
            CounterScreen(output)
        }
    }
}
```
