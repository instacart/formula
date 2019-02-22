package com.instacart.client.mvi

import arrow.core.Option
import com.google.common.truth.Truth.assertThat
import com.instacart.client.core.rx.toFlowable
import com.instacart.client.di.scopes.DisposableScope
import com.instacart.formula.ICMviState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test

class ICScopedBasicMviFlowStoreTest {
    class SignUpComponent()
    class LoggedInComponent()

    sealed class Key {
        // Sign up
        object Login: Key()
        object Register: Key()

        // Logged in
        object Browse: Key()
        object Account: Key()
    }

    lateinit var keys: BehaviorRelay<ICActiveMviKeys<Key>>
    lateinit var store: ICBasicMviFlowStore<Key>

    // State relays
    lateinit var loginScreenState: BehaviorRelay<String>
    lateinit var registerScreenState: BehaviorRelay<String>
    lateinit var browseScreenState: BehaviorRelay<String>
    lateinit var accountScreenState: BehaviorRelay<String>

    lateinit var subscriber: TestSubscriber<Option<ICMviState<Key, *>>>

    lateinit var disposed: MutableList<Any>


    @Before
    fun setup() {
        keys = BehaviorRelay.createDefault(ICActiveMviKeys(emptyList()))
        loginScreenState = BehaviorRelay.createDefault("login-initial")
        registerScreenState = BehaviorRelay.createDefault("register-initial")
        browseScreenState = BehaviorRelay.createDefault("browse-initial")
        accountScreenState = BehaviorRelay.createDefault("account-initial")
        disposed = mutableListOf()

        store = ICBasicMviFlowStore.init(keys.toFlowable()) {
            withScope(scopeFactory = {
                val component = SignUpComponent()
                DisposableScope(
                    component = component,
                    onDispose = {
                        disposed.add(component)
                    }
                )
            }) {
                register(Key.Login::class, init = {
                    loginScreenState.toFlowable()
                })

                register(Key.Register::class, init = {
                    registerScreenState.toFlowable()
                })
            }

            withScope(scopeFactory = {
                val component = LoggedInComponent()
                DisposableScope(
                    component = component,
                    onDispose = {
                        disposed.add(component)
                    }
                )
            }) {
                register(Key.Browse::class, init = {
                    browseScreenState.toFlowable()
                })

                register(Key.Account::class, init = {
                    accountScreenState.toFlowable()
                })
            }
        }

        subscriber = TestSubscriber()
        store.screen().subscribe(subscriber)
    }

    @After
    fun cleanUp() {
        subscriber.dispose()
    }

    @Test
    fun disposeIsTriggered() {
        keys.accept(ICActiveMviKeys(listOf(Key.Login)))
        keys.accept(ICActiveMviKeys(listOf(Key.Browse)))

        assertThat(disposed).hasSize(1)
        disposed[0].let {
            assertThat(it).isInstanceOf(SignUpComponent::class.java)
        }
    }

    @Test
    fun allComponentsAreClearedOnDispose() {
        keys.accept(ICActiveMviKeys(listOf(Key.Login)))
        keys.accept(
            ICActiveMviKeys(
                listOf(
                    Key.Login,
                    Key.Browse
                )
            )
        )

        subscriber.dispose()

        assertThat(disposed).hasSize(2)
    }
}
