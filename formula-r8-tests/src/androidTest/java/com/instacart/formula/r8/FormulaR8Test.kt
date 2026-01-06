package com.instacart.formula.r8

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instacart.formula.r8.interactors.AbstractFormulaTypeInheritanceInteractor
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * R8 Integration Test for Formula.
 *
 * ## The Problem
 * Formula uses class types as part of the key for child formulas, actions, and listeners. When
 * R8 merges lambda classes, previously distinct types become the same, causing bugs.
 *
 * This test runs on actual Android device/emulator with R8-minified code.
 */
@RunWith(AndroidJUnit4::class)
class FormulaR8Test {

    @Test fun actionFormulas() = runTest {
        val interactor = AbstractFormulaTypeInheritanceInteractor(this)
        interactor.start()
        interactor.assertValue(3)
    }
}
