package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test

class ScopedFlowStoreTest {
    class AppComponent() {
        val disposed: MutableList<Any> = mutableListOf()

        fun signUpComponent(): DisposableScope<SignUpComponent> {
            val component = SignUpComponent()
            return DisposableScope(
                component = component,
                onDispose = {
                    disposed.add(component)
                }
            )
        }

        fun loggedInComponent(): DisposableScope<LoggedInComponent> {
            val component = LoggedInComponent()
            return DisposableScope(
                component = component,
                onDispose = {
                    disposed.add(component)
                }
            )
        }
    }

    class SignUpComponent() {
        val loginScreenState = BehaviorRelay.createDefault("login-initial")
        val registerScreenState = BehaviorRelay.createDefault("register-initial")
    }

    class LoggedInComponent() {
        val browseScreenState = BehaviorRelay.createDefault("browse-initial")
        val accountScreenState = BehaviorRelay.createDefault("account-initial")
    }

    sealed class Key {
        // Sign up
        object Login : Key()
        object Register : Key()

        // Logged in
        object Browse : Key()
        object Account : Key()
    }

    lateinit var keys: BehaviorRelay<BackStack<Key>>
    lateinit var store: FlowStore<Key>

    lateinit var subscriber: TestSubscriber<FlowState<Key>>

    lateinit var rootComponent: AppComponent

    @Before
    fun setup() {
        keys = BehaviorRelay.createDefault(BackStack(emptyList()))

        rootComponent = AppComponent()
        store = FlowStore.init(
            rootComponent = rootComponent,
            state = keys.toFlowable(BackpressureStrategy.LATEST)
        ) {
            withScope(AppComponent::signUpComponent) {
                bind(Key.Login::class, init = { component, key ->
                    component.loginScreenState.toFlowable(BackpressureStrategy.LATEST)
                })

                bind(Key.Register::class, init = { component, key ->
                    component.registerScreenState.toFlowable(BackpressureStrategy.LATEST)
                })
            }

            withScope(AppComponent::loggedInComponent) {
                bind(Key.Browse::class, init = { component, key ->
                    component.browseScreenState.toFlowable(BackpressureStrategy.LATEST)
                })

                bind(Key.Account::class, init = { component, key ->
                    component.accountScreenState.toFlowable(BackpressureStrategy.LATEST)
                })
            }
        }

        subscriber = TestSubscriber()
        store.state().subscribe(subscriber)
    }

    @After
    fun cleanUp() {
        subscriber.dispose()
    }

    @Test
    fun `dispose is triggered`() {
        keys.accept(BackStack(listOf(Key.Login)))
        keys.accept(BackStack(listOf(Key.Browse)))

        val disposed = rootComponent.disposed
        assertThat(disposed).hasSize(1)
        disposed[0].let {
            assertThat(it).isInstanceOf(SignUpComponent::class.java)
        }
    }

    @Test
    fun `all components are cleared on dispose`() {
        keys.accept(BackStack(listOf(Key.Login)))
        keys.accept(
            BackStack(
                listOf(
                    Key.Login,
                    Key.Browse
                )
            )
        )

        subscriber.dispose()

        val disposed = rootComponent.disposed
        assertThat(disposed).hasSize(2)
    }
}
