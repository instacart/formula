## Extending Action Interface

If you need to use a different mechanism for asynchronous events, you can extend `Action` interface.
```kotlin
interface Action<Event> {
    fun start(scope: CoroutineScope, emitter: Emitter<Event>): Cancelable?
    fun key(): Any?
}
```

For example, let's say we want to track network status (I'm going to use mock network status APIs).
```kotlin
class GetNetworkStatusAction(
    val manager: NetworkStatusManager
) : Action<NetworkStatus> {

    override fun start(scope: CoroutineScope, emitter: Emitter<NetworkStatus>): Cancelable? {
        val listener = object: NetworkStatusListener {
            override fun onNetworkStatusChanged(status: NetworkStatus) {
                emitter.onEvent(status)
            }
        }

        manager.addNetworkStatusListener(listener)
        return Cancelable {
            manager.removeNetworkStatusListener(listener)
        }
    }

    override fun key(): Any? = null
}
```

We can now hook this up within our Formula:
```kotlin
class MyFormula(
    private val getNetworkStatusAction: GetNetworkStatusAction,
): Formula<Input, State, Output> {

    override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            actions = context.actions {
                getNetworkStatusAction.onEvent { status ->
                    val newState = state.copy(isOnline = status.isOnline)
                    transition(newState)
                }
            }
        )
    }
}
```
