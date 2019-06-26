## Processor Formula 
Processor Formula is a new concept that would replace `RenderFormula` and `Formula`. The primary difference between them
is that Process Formula recomputes everything after each state change. This makes it easier to compose multiple features.

### Basic Example
Creating a counter widget that has increment/decrement buttons.

```kotlin
class CounterProcessorFormula : ProcessorFormula<Unit, Int, Unit, CounterRenderModel> {

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int, Unit>
    ): Evaluation<CounterRenderModel> {
        return Evaluation(
            renderModel = CounterRenderModel(
                count = "Count: $state",
                onDecrement = {
                    context.transition(state - 1)
                },
                onIncrement = {
                    context.transition(state + 1)
                }
            )
        )
    }
}
```

### Listening for events
We use RxJava for listening to events.
```kotlin
class MyFormula(
    private val someObservable: Observable<MyData>
) : ProcessorFormula<...> {

    override fun evaluate(
        input: Unit,
        state: MyState,
        context: FormulaContext<MyState, ...>
    ): Evaluation<TimerRenderModel> {
        return Evaluation(
            // You can declaratively define what streams should run.
            streams = context.streams {
                // We use a key "data" to make sure that 
                // internal diffing mechanism can distinguish
                // between differen streams.
                events("data", someObservable) { update: MyData ->
                    // onEvent will always be scoped to the current `MyState` instance.
                    Transition(state.copy(myData = update))
                }
            },
            renderModel = ...
        )
    }
} 
```

### Side effects
It is very easy to define side-effects.
```kotlin
class UserProfileFormula(
    val userAnalyticsService: UserAnalyticsService
) : ProcessorFormula<...> {


    override fun evaluate(input: Unit, state: MyState, context: FormulaContext<...>): Evaluation<...> {
        return Evaluation(
            renderModel = state.name,
            streams = context.streams {
                // This will be invoked first time this formula runs.
                effect("view analytics") { 
                    userAnalyticsService.trackProfileView()
                }
            }
        )
    }
}
```

### Composing another feature
One of the primary goals when making `ProcessorFormula` was easy composability of features and widgets. Previously,
we had to listen to `Observable<ChildrenRenderModel>` and add it to our `State` and `RenderModel` classes. Also, we 
would have to use observables to pass information from parent to the child. 

```kotlin
class MainPageFormula(
    val headerFormula: HeaderFormula,
    val listFormula: ListFormula,
    val dialogFormula: DialogFormula
) : ProcessorFormula<> {
    
    override fun evaluate(input: Unit, state: MyState, context: FormulaContext<...>): Evaluation<...> {
        // "context.child" returns a RenderModel 
        val listRenderModel = context.child(listFormula, createListInput(state)) { listEvent ->
            // We can perform state transition here.
        }
        
        val headerRenderModel = context.child(headerFormula, createHeaderInput(state)) { headerEvent ->
            // perform header events
        }
        
        // We can make decisions using the current `state` about 
        // what children to show
        val dialog = if (state.showDialog) {
            context.child(dialogFormula, Unit) { dialogEvent ->
                // perform dialog event
            }
        } else {
            null
        }
    
        return Evaluation(
            renderModel = MainRenderModel(
                header = headerRenderModel,
                list = listRenderModel,
                dialog = dialog
            )
        )
    }
}
```

### Diffing
Given that we recompute everything with each state change, there is an internal diffing mechanism with Formula. This
mechanism ensures that:
1. RxJava streams are only subscribed to once.
2. Side effects are only invoked once.
2. Children state is persisted across every processing pass.

### Testing
TODO:
