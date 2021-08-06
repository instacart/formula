Formula is a functional reactive framework built in Kotlin for managing state and side effects. It enables building 
deterministic, composable, testable applications.

### Quick Start
To demonstrate some of the concepts you will make a simple counter application. It shows the current count and 
has two buttons to increment and decrement it.

One of the best practises when working with Formula is to first think what the UI needs for rendering and what 
actions the user will be able to take. This concept is called a `RenderModel` and is represented by a Kotlin data class.

### Project Setup
To get started create a new Android application called `Counter Application` with a blank activity. Next, rename (via refactor) 
your `MainActivity` to `MyActivity`. Note: this is to make it easier for you to copy and past the example code later on.

### Dependencies
Now that you have your project set up, add the following libraries to your app level gradle file:

```groovy
dependencies {
  implementation 'com.instacart.formula:formula-rxjava3:0.7.0'
  implementation 'com.instacart.formula:formula-android:0.7.0'
}
```

#### Render Model
Render Model is an immutable representation of your view. It will be used to update Android views. Typically,
it will also contain callbacks that will be invoked when user interacts with the UI. Create a file called
`CounterRenderModel` in the same package as your activity and add the following content.
```kotlin
data class CounterRenderModel(
  val count: String,
  val onDecrement: () -> Unit,
  val onIncrement: () -> Unit 
)
```

Now that you have defined a Render Model, you can create a `RenderView` which is responsible for taking a `RenderModel`
and applying it to Android Views.

#### Render View
Render View is an interface which is responsible for applying `RenderModel` to Android Views.
This interface requires you to provide a `render` implementation by creating a `Renderer`. Renderer 
is a class that has an internal mechanism that checks the previous Render Model applied 
and only re-renders if it has changed.

Create a another class in the same package as before called `CounterRenderView` and add the following contents to it.

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

This is createing a `Renderer` that has a TextView for displaying the current count value state along with a
increment and decrement button with click listeners that call the `onDecrement()` and `onIncrement()` methods
you created in your `CounterRenderModel`. This codce also references some view elements that you don't 
currently have. To fix that replace your activity view with the following.

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_gravity="center"
        android:layout_height="wrap_content">

        <Button
            android:id="@+id/decrement_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="-" />

        <TextView
            android:id="@+id/count_text_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            tools:text="5"/>

        <Button
            android:id="@+id/increment_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="+" />
    </LinearLayout>
</FrameLayout>
```

You now have a defined a single entry-point to your rendering. This makes debugging issues a lot easier. You haven't 
set this up in your project yet, but any time you want to update UI, you simply set a new Render Model. (Don't worry,
this will make more sense when you hook this up later on in your project).
```kotlin
renderView.render(renderModel)
```

Now that you have your rendering logic setup, you are going to define how you create the Render Model and handle user 
events. To have a dynamic UI that changes as user interacts with it requires some sort of state.

#### State 
State is a Kotlin data class that contains all the necessary information/data to render your view. In you counter
example, you need to keep track of the current count. To do that create a new data class with the following content: 
```kotlin
data class CounterState(val count: Int)
```

With more complex states you will usually reference other data classes with a snapshot of states. But, given that 
this is a simple state, you are using `Int` directly.

#### Formula
Formula is responsible for creating the Render Model. It can define an internal `State` class and respond to 
various events by transitioning to a new state. To hook that up, create a new `CounterFormula` class and
replace it with the following:

```kotlin
class CounterFormula : Formula<Unit, CounterState, CounterRenderModel> {

  override fun initialState(input: Unit): CounterState = CounterState(count = 0)

  override fun evaluate(
    input: Unit,
    state: CounterState,
    context: FormulaContext<CounterState>
  ): Evaluation<CounterRenderModel> {
    val count = state.count
    return Evaluation(
      output = CounterRenderModel(
        count = "Count: $count",
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

The most import part is the `Formula.evaluate` function. It gives you the current `State` and expects an
`Evaluation<RenderModel>` back. Any time you transition to a new state (in this example when onDecrement or
onIncrement ard called), evaluate is called again and new Render Model is created.

There is also a special object called `FormulaContext` being passed. Formula Context allows you to respond to events by
declaring transitions. You use `context.callback` for both `onIncrement` and `onDecrement`. Let's look at one of these
functions closer.

```kotlin
onDecrement = context.callback {
  transition(state.copy(count = count - 1))
}
```

In response to the decrement event, you take the current `count` in your state object, subtract `1` from it and then
copy the the rest of the object replacing the count value with the new value. Then, you call the `transition`
method to create `Transition<CounterState>` object that you return. 

The logic currently allows a user to decrement to a number below 0. If you want, you can update the transition logic to 
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

Now that you defined your state management, let's connect it to your `RenderView`.

#### Using Formula

Ultimately you are going to need to hook everything up to your activity. To get
started open up your `MyActivity` and replace it with the following:

```kotlin
class MyActivity : FormulaAppCompatActivity() {
  private lateinit var counterRenderView: CounterRenderView

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)
    setContentView(R.layout.activity_my)

    counterRenderView = CounterRenderView(findViewById(R.id.activity_content))
  }

  fun render(model: CounterRenderModel) {
    counterRenderView.render(model)
  }
}
```

This is attaching your `CounterRenderView` to the the `FrameLayout` that contains the view elements you want 
to bind and creating a function that allows you to pass a CounterRenderModel into it to set state. 
Next, connect` CounterFormula` to `MyActivity.render` you function by creating a `MyApp` class and
replacing the contents with the following:
```kotlin
class MyApp : Application() {
    
  override fun onCreate() {
    super.onCreate()
        
    FormulaAndroid.init(this) {
      activity<MyActivity> {
        store(
          streams = {
            val formula = CounterFormula()
            update(formula.toObservable(), MyActivity::render)
          }
        )
      }
    }
  }
}
```

This over-rides `onCreate()` to call `init` on FormulaAndroid. The important thing here is that you have a stream that creates an new
instance of your `CounterFormula` and calls update passing in the formula as an observable and a reference
to the `render` function in your Activity.

Next you will need to update your application in your `manifest.xml` to reference your new app class. Your new one 
should look like this:
```xml
  <application
    android:allowBackup="true"
    android:name=".MyApp"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.CounterApplication">
    . 
    . 
    .
</application>
```

And that's it. You can see the full <a href="https://github.com/instacart/formula/tree/master/samples/counter" target="_blank">sample here</a>.

Formula is agnostic to other layers of abstraction. It can be used within activity or a fragment. You can
convert `Formula` to an RxJava2 `Observable` by using the `toObservable()` extension function.
```kotlin
val formula = CounterFormula()
val state: Observable<CounterRenderModel> = formula.toObservable(input = Unit)
```

Ideally, it would be placed within a surface that survives configuration changes such as Android Components ViewModel.
In this example, we will use Formula Android module. For using Formula with AndroidX ViewModel, take
a look at [AndroidX Guide](using_android_view_model.md).

To learn more about Formula Android module see [Formula Android Guide](Formula-Android.md).

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

