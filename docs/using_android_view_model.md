Define `ViewModel` which runs `Formula.state` stream until `onCleared` is called.
```kotlin
class CounterViewModel() : ViewModel {
  private val formula = CounterFormula()
  private val disposables = CompositeDisposable()
    
  val renderModels = formula.start(Unit).replay(1).apply {
    connect { disposables.add(it) }
  }

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
```

In our activity, we then subscribe to the Render Model changes and pass them to the Render View.
```kotlin
class MyActivity : AppCompatActivity() {
  private val disposables = CompositeDisposable()

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)
    setContentView(R.string.my_screen)
        
    val renderView = CounterRenderView(findViewById(R.id.counter))
    val viewModel = ViewModelProviders.of(this).get(CounterViewModel::class.java)
        
    disposables.add(viewModel.renderModels.subscribe(renderView.renderer::render))
  }
    
  override fun onDestroy() {
    disposables.clear()
    super.onDestroy()
  }
}
```
