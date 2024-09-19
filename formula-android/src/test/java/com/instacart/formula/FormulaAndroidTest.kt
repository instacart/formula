package com.instacart.formula

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.instacart.testutils.android.TestFormulaActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormulaAndroidTest {

    @Test fun `crashes if initialized twice`() {

        try {
            val result = runCatching {
                val context = ApplicationProvider.getApplicationContext<Application>()
                FormulaAndroid.init(context) {}
                FormulaAndroid.init(context) {}
            }
            val error = result.exceptionOrNull()?.message
            Truth.assertThat(error).isEqualTo("can only initialize the store once.")
        } finally {
            FormulaAndroid.reset()
        }
    }

    @Test fun `crashes if accessed before initialization`() {
        val result = runCatching {
            FormulaAndroid.onBackPressed(TestFormulaActivity())
        }
        val errorMessage = result.exceptionOrNull()?.message
        Truth.assertThat(errorMessage).isEqualTo(
            "Need to call FormulaAndroid.init() from your Application."
        )
    }
}