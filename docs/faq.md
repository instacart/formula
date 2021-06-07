
### Only thread that created it can trigger transitions
The state management should be initialized on the main thread and all the transitions should also happen on the main thread. You 
will get the following exception if that is not the case.
```
Caused by: java.lang.IllegalStateException: Only thread that created it can trigger transitions. Expected: main, Was: Network 1
```

### Transition already happened.
After each transition, formula is re-evaluated and new event listeners are created. If you use an old listener
you will see the following exception.
```
Caused by: java.lang.IllegalStateException: Transition already happened. This is using old transition callback: $it.
```

### Callback is already defined.
TODO..

### After evaluation finished
If a `callback` or `eventCallback` is called after the Formula evaluation is finished, you will see
this exception.
```
Caused by: java.lang.IllegalStateException: cannot call this after evaluation finished.
```
This can happen for a number of reasons. Likely, you are creating a `callback` or `eventCallback`
within the `onEvent` or `events` method of your `updates` lambda for the given Formula. This can
cause your callbacks to be scoped to a stale state instance. Instead, you should create your callbacks
within the `evaluate` function itself, passing the data you might be using from the `onEvent` into
the `State` defined for that Formula. For example, instead of:
```
class RelatedSearchesFormula @Inject constructor(
    private val repo: RelatedSearchesRepo,
) : Formula<RelatedSearchesFormula.Input, RelatedSearchesFormula.State, List<Any>> {

    data class Input(
        val query: String
    )

    data class State(
        val rows: List<Any> = emptyList()
    )

    override fun initialState(input: Input) = State()

    override fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<List<Any>> {
        return Evaluation(
            output = state.rows,
            updates = context.updates {
                RxStream.fromObservable {
                    val resultsInput = RelatedSearchesRepo.Input(
                        query = input.query,
                    )
                    repo.fetchResults(resultsInput)
                }.onEvent { data ->
                    val rows = mutableListOf<Any>()
                    data.forEach { suggestion ->
                        rows += createRow(suggestion)
                    }
                    transition(state.copy(rows = rows))
                }
            }
        )
    }
}
```
which creates rows and then stores them in the `State`, we would store the data from the RxStream in
the state and then construct the rows in the `evaluation` function itself:
```
class RelatedSearchesFormula @Inject constructor(
    private val repo: RelatedSearchesRepo,
) : Formula<RelatedSearchesFormula.Input, RelatedSearchesFormula.State, List<Any>> {

    data class Input(
        val query: String
    )

    data class State(
        val data: List<RelatedSearches> = emptyList()
    )

    override fun initialState(input: Input) = State()

    override fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<List<Any>> {
        val rows = mutableListOf<Any>()
        state.data.forEach { suggestion ->
            rows += createRow(suggestion)
        }
        return Evaluation(
            output = rows,
            updates = context.updates {
                RxStream.fromObservable {
                    val resultsInput = RelatedSearchesRepo.Input(
                        query = input.query,
                    )
                    repo.fetchResults(resultsInput)
                }.onEvent { data ->
                    transition(state.copy(data = data))
                }
            }
        )
    }
}
```
Notice that the `rows` are no longer stored in the state, but instead constructed on each
call to `evaluate` so that the callbacks are never stale.