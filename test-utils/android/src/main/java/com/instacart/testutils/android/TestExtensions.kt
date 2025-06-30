package com.instacart.testutils.android

import android.app.Activity
import android.app.Application
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.FormulaAndroid
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.robolectric.Shadows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun <A: Activity> ActivityScenario<A>.activity(): A {
    return get { this }
}

fun <A: Activity, T> ActivityScenario<A>.get(select: A.() -> T): T {
    val list: MutableList<T> = mutableListOf()
    onActivity {
        list.add(it.select())
    }
    return list.first()
}

fun CountDownLatch.throwOnTimeout() {
    if (!await(100, TimeUnit.MILLISECONDS)) {
        throw IllegalStateException("timeout")
    }
}

fun executeOnBackgroundThread(action: () -> Unit) {
    val initLatch = CountDownLatch(1)
    Executors.newSingleThreadExecutor().execute {
        action()
        initLatch.countDown()
    }
    initLatch.throwOnTimeout()
    Shadows.shadowOf(Looper.getMainLooper()).idle()
}

fun withFormulaAndroid(
    configure: TestActivityConfigurator.() -> Unit = {},
    continuation: (FormulaAndroidInteractor) -> Unit,
) {
    val errors = mutableListOf<Throwable>()
    RxJavaPlugins.reset()
    RxJavaPlugins.setErrorHandler { errors.add(it) }

    try {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val interactor = FormulaAndroidInteractor()
        FormulaAndroid.init(context) {
            val testActivityConfigurator = TestActivityConfigurator(this) {
                interactor.onActivityContextInitialized(it)
            }
            configure(testActivityConfigurator)
        }
        continuation(interactor)
    } finally {
        RxJavaPlugins.reset()
        FormulaAndroid.reset()
        assertThat(errors).isEmpty()
    }
}
