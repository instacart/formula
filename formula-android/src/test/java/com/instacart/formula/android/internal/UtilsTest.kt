package com.instacart.formula.android.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilsTest {

    @Test
    fun `assertMainThread does nothing on main thread`() {
        Truth.assertThat(Utils.isMainThread()).isTrue()
        Utils.assertMainThread()
    }

    @Test
    fun `assertMainThread throws exception when on bg thread`() {
        val observer = Observable.fromCallable {
            Truth.assertThat(Utils.isMainThread()).isFalse()
            runCatching { Utils.assertMainThread() }
        }.subscribeOn(Schedulers.newThread()).test()

        observer.awaitCount(1)
        Truth.assertThat(observer.values().first().exceptionOrNull()).hasMessageThat().contains(
            "should be called on main thread:"
        )
    }
}