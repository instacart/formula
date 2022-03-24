# Formula
A Kotlin framework for managing state and side effects. It is inspired by MVU(model, view, update) 
architecture and best of functional, declarative and reactive patterns. It enables building 
deterministic, composable, testable applications.

<img src="docs/assets/formula-mvu-graph.png" alt="MVU graph"/>

[Check documentation](https://instacart.github.io/formula/)

[Check samples](samples)

## Quick Example
As a quick example, we'll show how a counter application looks which shows 
current amount and allows you to increment or decrement it.
```kotlin
class CounterFormula : Formula<Unit, State, Output>() {
    
    data class State(
        val currentCount: Int
    )
    
    data class Output(
        val countText: String,
        val onIncrement: () -> Unit,
        val onDecrement: () -> Unit,
    )
    
    override fun initialState(input: Unit): State {
        return State(currentCount = 0)
    }
    
    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        val currentCount = state.currentCount
        val output = Output(
            countText = "Count: $currentCount",
            onIncrement = context.callback {
                val newState = state.copy(
                    currentCount = currentCount + 1
                )
                transition(newState)
            },
            onDecrement = context.callback {
                val newState = state.copy(
                    currentCount = currentCount - 1
                )
                transition(newState)
            } 
        )
        return Evaluation(output = output)
    }
}

// Vanilla Android rendering logic
val currentCountTextView: TextView = ...
val incrementButton: Button = ...    
val decrementButton: Button = ...

// Converting to Coroutine Flow.
val counterOutput: Flow<CounterFormula.Output> = CounterFormula().toFlow()
scope.launch {
    counterOutput.collect { output ->
        counterTextView.text = output.countText
        incrementButton.setOnClickListener { output.onIncrement() }
        decrementButton.setOnClickListener { output.onDecrement() } 
    }
}
    
// Alternatively, converting to RxJava observable.
val counterOutput: Observable<CounterFormula.Output> = CounterFormula().toObservable()
counterOutput.subscribe { output ->
    counterTextView.text = output.countText  
    incrementButton.setOnClickListener { output.onIncrement() } 
    decrementButton.setOnClickListener { output.onDecrement() }
}
```
## Quick Example - Testing
```kotlin
@Test fun `counter will not allow to decrement below 0`() {
    CounterFormula()
        .test()
        .output { Truth.assertThat(count).isEqualTo("Count: 0") }
        .output { onDecrement() }
        // This fails right now as we don't have this logic added.
        .output { Truth.assertThat(count).isEqualTo("Count: 0") }
}
```

## Quick Example - Async code
To display how to handle asynchronous actions, let's update previous counter example where
we persist and read current counter state from disk.
```kotlin
// We use simple in-memory implementation which could be changed to support real disk cache.
class CounterRepository {
    private var count: Int = 0
    
    fun saveCurrentCount(count: Int) {
        this.count = count
    }
    
    fun getCounterState(): Observable<Int> {
        return Observable.fromCallable { count }
    }
}

class CounterFormula(val repository: CounterRepository) : Formula<Unit, State, Output>() {
    
    // We make currentCount nullable to indicate not-loaded / loaded states.
    data class State(
        val currentCount: Int?
    )

    data class Output(
        val countText: String,
        val onIncrement: () -> Unit,
        val onDecrement: () -> Unit,
    )

    override fun initialState(input: Unit): State {
        return State(currentCount = null)
    }

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
        val currentCount = state.currentCount
        val output = if (currentCount == null) {
            Output(
                countText = "Loading",
                onIncrement = {},
                onDecrement = {}
            )
        } else {
            Output(
                countText = "Count: $currentCount",
                onIncrement = context.callback {
                    val newState = state.copy(
                        currentCount = currentCount + 1
                    )
                    transition(newState) {
                        repository.saveCurrentCount(newState.currentCount)
                    }
                },
                onDecrement = context.callback {
                    val newState = state.copy(
                        currentCount = currentCount - 1
                    )
                    transition(newState) {
                        repository.saveCurrentCount(newState.currentCount)
                    }
                }
            ) 
        }
        
        return Evaluation(
            output = output,
            actions = context.actions {
                // We add an action which gets the counter state and updates our state.
                RxAction.fromObservable { repository.getCounterState() }.onEvent { currentCount ->
                    val newState = state.copy(currentCount = currentCount)
                    transition(newState)
                }
            }
        )
    }
}
```

## Android Module
The Android module provides declarative API to connect reactive state management to Android Fragments. 
This module has been designed for gradual adoption. You can use as much or as little of it as you like.

To learn more, see our [Integration Guide](docs/Formula-Android.md).

## Download
Add the library to your list of dependencies:

```groovy
dependencies {
    implementation 'com.instacart.formula:formula-rxjava3:0.7.0'
    implementation 'com.instacart.formula:formula-android:0.7.0'
}
```

# License

```
The Clear BSD License

Copyright (c) 2022 Maplebear Inc. dba Instacart
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
