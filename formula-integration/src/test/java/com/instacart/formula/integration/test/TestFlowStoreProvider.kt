package com.instacart.formula.integration.test

import com.instacart.formula.fragment.FragmentFlowStore
import io.reactivex.Flowable

class TestFlowStoreProvider {

    fun createStore(): FragmentFlowStore {
        return FragmentFlowStore.init(TestAppComponent()) {
            bind(TestAuthFlowIntegration())
            bind(TestTaskListContract::class) { key ->
                Flowable.empty()
            }
            bind(TestAccountFragmentContract::class) { component, key ->
                Flowable.empty()
            }
        }
    }
}
