Formula is a mix of functional, reactive and declarative programming. One aspect that might seem quite unusual is the
way that it handles asynchronous actions such as RxJava observables, Kotlin Flows, etc. Most developers are used to
explicitly managing subscription lifecycle.
```kotlin
val fetchUserObservable = repository.fetchUser()
disposables += fetchUserObservable.subscribe { userResult ->
    // Do something
}
```

Formula does things a bit differently. It manages the lifecycle of the asynchronous actions for you. Instead of manually subscribing and unsubscribing,
you define the conditions for which the asynchronous action should run and the callback which handles events produced by the action.
```kotlin
val fetchUserStream = RxStream.fromObservable { repository.fetchUser() }
fetchUserStream.onEvent { userResult ->
  // Do something
}
```

The logic looks very similar to the first option, but the key difference here is that `Observable.subscribe` hasn’t run yet - the execution
is deferred. It might not be clear why this is useful just from this example, but deferring execution allows us to provide a declarative API.
For example, we can add conditional logic to only fetch user when user id is set.
```kotlin
if (state.userId != null) {
    val fetchUserStream = RxStream.fromObservable { repository.fetchUser(state.userId) }
    fetchUserStream.onEvent { userResult ->
        // Do something with the result
    }
}
```

What if we want to fetch user information only after user clicks on some button to enable this. We can just expand on the conditional logic.
```kotlin
if (state.isUserFetchEnabled && state.userId != null) {
    // Logic here is the same as the previous example
}
```

Here, we don’t care what controls `isUserFetchEnabled` boolean. Formula will start execution when these
conditions are met and will dispose of the action if `state.isUserFetchedEnabled` becomes `false` again.

What if for some unusual reason the userId could change and we would want to refetch? We can
define this behavior using `key` parameter
```kotlin
if (state.isUserFetchEnabled && state.userId != null) {
    val fetchUserStream = RxStream.fromObservable(key = state.userId) { repository.fetchUser(state.userId) }
    fetchUserStream.onEvent { userResult ->
        // Do something with the result
    }
}
```

To understand how all this works, we will make some simplified assumptions about Formula APIs:

- Each `Formula` will define an immutable `State` data class.
- We use `State` object in the `evaluate(state: State)` function which defines the `Pair<UIModel, Actions>`
- Any time there is a `State` change, the Formula runtime will call `evaluate` again.

In code, this might look like this
```kotlin
val actionCache: MutableMap<Key, RunningAction> = mutableMapOf()

fun onStateChanged(state: State) {
    val (uiModel, actions) = formula.evaluate(state)
    // Stop actions 
    actionCache.forEach { (key, runningAction) ->
        if (!actions.contains(key)) {
             actionCache.remove(key)
             runningAction.stop()
        } 
    }    

    // Start actions
    actions.forEach { action ->
        if (!actionCache.contains(key)) {
            actionCache[key] = action.start()
        }
    }  
}
```