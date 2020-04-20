Formula is a functional reactive framework built in Kotlin for managing state and side effects. It enables building 
deterministic, composable, testable applications.

### Quick example
To demonstrate some of the concepts we will make a simple counter application. It shows the current count and 
has two buttons to increment and decrement it.

One of the best practises when working with Formula is to first think what the UI needs for rendering and what 
actions the user will be able to take. This concept is called a `RenderModel` and is represented by a Kotlin data class.

#### Render Model
Render Model is an immutable representation of your view. It will be used to update Android views. Typically,
it will also contain callbacks that will be invoked when user interacts with the UI.
```kotlin
data class CounterRenderModel(
  val count: String,
  val onDecrement: () -> Unit,
  val onIncrement: () -> Unit 
)
```

Once we define a Render Model, we can create a `RenderView` which is responsible for taking a `RenderModel`
and applying it to Android Views.

#### Render View
Render View is an interface that we extend. It requires us to create a `Render` function that takes `RenderModel` and 
applies it to Android Views. Render is a class that has an internal mechanism that checks the previous Render Model applied
and only re-renders if it has changed. 

```kotlin
class CounterRenderView(root: ViewGroup): RenderView<CounterRenderModel> {
  private val decrementButton: Button = root.findViewById(R.id.decrement_button)
  private val incrementButton: Button = root.findViewById(R.id.increment_button)
  private val countTextView: TextView = root.findViewById(R.id.count_text_view)

  override val render: Renderer<CounterRenderModel> = Renderer { model ->
    countTextView.text = model.count
    decrementButton.setOnClickListener {
      model.onDecrement()
    }
    incrementButton.setOnClickListener {
      model.onIncrement()
    }
  }
}
```

We now defined a single entry-point to our rendering (this makes debugging issues a lot easier). Anytime 
you need to update UI, just set a new Render Model.
```kotlin
renderView.render(renderModel)
```

Now that we have our rendering logic setup, let's define how we create the Render Model and handle user events. To have
a dynamic UI that changes as user interacts with it requires some sort of state.

#### State 
State is a Kotlin data class that contains all the necessary information/data to render your view. In our counter
example, we need to keep track of the current count. 
```kotlin
data class CounterState(val count: Int)
```

Given that this is a simple state, you could also use `Int` directly.

#### Formula
Formula is responsible for creating the Render Model. It can define an internal `State` class and respond to 
various events by transitioning to a new state. 

```kotlin
class CounterFormula : Formula<Unit, CounterState, CounterRenderModel> {

  override fun initialState(input: Unit): Int = CounterState(count = 0)

  override fun evaluate(
    input: Unit,
    state: CounterState,
    context: FormulaContext<Int>
  ): Evaluation<CounterRenderModel> {
    val count = state.count
    return Evaluation(
      renderModel = CounterRenderModel(
        title = "Count: $count",
        onDecrement = context.callback {
          transition(state.copy(count = count - 1))
        },
        onIncrement = context.callback {
          transition(state.copy(count = count + 1))
        }
      )
    )
  }
}
```

The most import part is the `Formula.evaluate` function. It gives us the current `State` and expects an
`Evaluation<RenderModel>` back. Any time we transition to a new state, evaluate is called again and new Render Model 
is created.

There is also a special object called `FormulaContext` being passed. Formula Context allows us to respond to events by
declaring transitions. We use `context.callback` for both `onIncrement` and `onDecrement`. Let's look at one of these
functions closer.

```kotlin
onDecrement = context.callback {
  transition(state.copy(count = count - 1))
}
```

In response to the decrement event, we take the current `count` and subtract `1` from it. Then, we call `transition`
method to create `Transition<CounterState>` object which we return. 

If you notice, our logic currently allows user to decrement to a number below 0. We can update the transition logic to 
prevent this.
```kotlin
onDecrement = context.callback {
  if (count == 0) {
    none()
  } else {
    transition(state.copy(count = count - 1))
  }
}
```

The callback block uses a DSL to provide access to `Transition.Factory` which has the `transition` and `none`
utility functions (take a look at that class for other utility functions).

Now that we defined our state management, let's connect it to our `RenderView`.

#### Using Formula
Formula is agnostic to other layers of abstraction. It can be used within activity or a fragment. You can 
convert `Formula` to an RxJava2 `Observable` by using `start` extension function.
```kotlin
val formula = CounterFormula()
val state: Observable<CounterRenderModel> = formula.start(input = Unit)
```

Ideally, it would be placed within a surface that survives configuration changes such as Android Components ViewModel. 
In this example, we will use Formula Android module. For using Formula with AndroidX ViewModel, take
a look at [AndroidX Guide](using_android_view_model.md).
 
Let's first define our Activity.
```kotlin
class MyActivity : FormulaAppCompatActivity() {
  private lateinit var counterRenderView: CounterRenderView

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)
    setContentView(R.string.my_screen)
        
    counterRenderView = CounterRenderView(findViewById(R.id.counter))
  }
  
  fun render(model: CounterRenderModel) {
    counterRenderView.render(model)
  }
}
```

Now, let's connect` CounterFormula` to `MyActivity.render` function.
```kotlin
class MyApp : Application() {
    
  override fun onCreate() {
    super.onCreate()
        
    FormulaAndroid.init(this) {
      activity<MyActivity> {
        store(
          streams = {
            val formula = CounterFormula()
            update(formula.start(), MyActivity::render)
          }
        )
      }
    }
  }
}
```

And that's it. You can see the full <a href="https://github.com/instacart/formula/tree/master/samples/counter" target="_blank">sample here</a>.

To learn more about Formula Android module see [Formula Android Guide](Integration.md). 

### Download
Add the library to your list of dependencies:

```groovy
dependencies {
    implementation 'com.github.instacart:formula:0.5.3'
    implementation 'com.github.instacart:formula-android:0.5.3'
}
```

### Inspiration
Formula would not have been possible without ideas from other projects such as

- Elm
- Cycle.js
- React / Redux
- Mobius
- Square Workflows 

### License

```
The Clear BSD License

Copyright (c) 2019 Maplebear Inc. dba Instacart
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted 
(subject to the limitations in the disclaimer below) provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of Maplebear Inc. dba Instacart nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY 
THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.
```

