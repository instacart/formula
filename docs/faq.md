
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
Caused by: java.lang.IllegalStateException: Transition already happened. This is using old event listener: $it.
```

### Listener is already defined.
TODO..

### After evaluation finished
If a `onEvent` is called after the Formula evaluation is finished, you will see this exception.
```
Caused by: java.lang.IllegalStateException: Cannot call this after evaluation finished.
```
This means that you called `onEvent` after `Formula.evalute` already returned `Evaluation`
object. This can happen when you are calling `onEvent` within `transition` block. This is not 
allowed because your listener would be scoped to a stale state instance. Instead, you should 
create your listeners within the `evaluate` function itself, passing the data you might be 
using from the `onEvent` into the `State` defined for that Formula. For example, instead of:
```
class TaskDetailFormula @Inject constructor(
    private val repo: TasksRepo,
) : Formula<TaskDetailFormula.Input, TaskDetailFormula.State, TaskDetailRenderModel> {

    data class Input(
        val taskId: String
    )

    data class State(
        val task: TaskDetailRenderModel? = null
    )

    override fun initialState(input: Input) = State()

    override fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<TaskDetailRenderModel?> {
        return Evaluation(
            output = state.task,
            updates = context.updates {
                RxStream.fromObservable { repo.fetchTask(input.taskId) }.onEvent { task ->
                  val renderModel = TaskDetailRenderModel(
                      title = task.title,
                      // Don't do: calling context.onEvent within "onEvent" will cause a crash described above
                      onDeleteSelected = context.onEvent {
                        ...
                      }
                   )
                   transition(state.copy(task = renderModel))
                }
            }
        )
    }
}
```
which the render model and then stores it in the `State`, we would store the fetched task from the RxStream in
the state and then construct the render model in the `evaluation` function itself:
```
class TaskDetailFormula @Inject constructor(
    private val repo: TasksRepo,
) : Formula<TaskDetailFormula.Input, TaskDetailFormula.State, TaskDetailRenderModel> {

    data class Input(
        val taskId: String
    )

    data class State(
        val task: Task? = null
    )

    override fun initialState(input: Input) = State()

    override fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<TaskDetailRenderModel?> {
        // Note that this is correct because the render model and therefore listener is constructed
        // within `evaluate` instead of within `onEvent`
        val renderModel = state.task?.let {
            TaskDetailRenderModel(
              title = it.title,
              onDeleteSelected = context.onEvent {
                ...
              }
            )
        }
        return Evaluation(
            output = renderModel,
            updates = context.updates {
                RxStream.fromObservable { repo.fetchTask(input.taskId) }.onEvent { task ->
                   transition(state.copy(task = renderModel))
                }
            }
        )
    }
}
```
Notice that the render model is no longer stored in the state, but instead constructed on each
call to `evaluate` so that the listeners are never stale.