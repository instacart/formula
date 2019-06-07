package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test

class AppStoreFactoryTest {

    class FakeActivity : FragmentActivity()

    @Test fun `initialized store is live`() {

        val factory = AppStoreFactory.Builder()
            .apply {
                activity(FakeActivity::class) {
                    build { }
                }
            }
            .build()

        val store = factory.init(mock<FakeActivity>())!!
        assertThat(store.subscription.isDisposed).isFalse()
    }
}
