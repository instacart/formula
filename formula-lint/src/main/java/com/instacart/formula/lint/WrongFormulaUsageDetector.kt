package com.instacart.formula.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category.Companion.MESSAGES
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.TypeEvaluator
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.PsiWildcardType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import java.util.EnumSet

class WrongFormulaUsageDetector : Detector(), Detector.UastScanner {
    private val FORMULA_CONTEXT_CLASS = "com.instacart.formula.FormulaContext"
    private val SNAPSHOT_CLASS = "com.instacart.formula.Snapshot"

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(
            UExpression::class.java,
        )
    }
    sealed class FormulaReference(val name: String) {
        object Snapshot : FormulaReference("Snapshot")
        object FormulaContext : FormulaReference("FormulaContext")
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object: UElementHandler() {
            override fun visitExpression(node: UExpression) {
                if (node is UCallExpression) {
                    val resolved = node.resolve()
                    val methodOwner = getMethodOwner(context, resolved)
                    if (methodOwner != null && isWithinTransitionContext(context, node)) {
                        // Illegal reference happened within TransitionContext
                        val call = node.sourcePsi
                        val name = methodOwner.name
                        context.report(
                            Incident(
                                issue = ISSUE_ILLEGAL_CALL_WITHIN_TRANSITION_CONTEXT,
                                scope = call,
                                location = context.getLocation(call),
                                message = "Cannot use $name within transition context"
                            )
                        )
                        return
                    }

                    val parameter = findFormulaParameter(context, resolved)
                    if (parameter != null && isWithinTransitionContext(context, node)) {
                        // Illegal function call happened within TransitionContext
                        val name = parameter.name
                        val message = "Using $name within transition context is not allowed. Since ${node.methodName} takes $name as a parameter, you cannot use this function with transition context."
                        val call = node.sourcePsi
                        context.report(
                            Incident(
                                issue = ISSUE_ILLEGAL_CALL_WITHIN_TRANSITION_CONTEXT,
                                scope = call,
                                location = context.getLocation(call),
                                message = message
                            )
                        )
                        return
                    }
                }
            }
        }
    }

    private fun getFormulaReference(
        context: JavaContext,
        type: PsiType?
    ): FormulaReference? {
        val referenceClass = context.evaluator.getTypeClass(type)
        return when (referenceClass?.qualifiedName) {
            FORMULA_CONTEXT_CLASS -> FormulaReference.FormulaContext
            SNAPSHOT_CLASS -> FormulaReference.Snapshot
            else -> null
        }
    }

    private fun getMethodOwner(context: JavaContext, method: PsiMethod?): FormulaReference? {
        val referenceType = method?.containingClass?.let {
            getFormulaReference(context, context.evaluator.getClassType(it))
        }
        if (referenceType != null) {
            return referenceType
        }
        return null
    }

    private fun findFormulaParameter(
        context: JavaContext,
        resolve: PsiMethod?,
    ): FormulaReference? {
        val parameters = resolve?.parameterList?.parameters
        parameters?.forEach {
            getFormulaReference(context, it.type)?.let { found ->
                return found
            }
        }
        return null
    }

    private fun isWithinTransitionContext(context: JavaContext, node: UExpression): Boolean {
        var parent = node.uastParent
        while (parent != null) {
            if (parent is UMethod) {
                val parameters = parent.parameterList.parameters.map { it.type }
                if (isFormulaTransition(context, parameters)) {
                    return true
                }
            }
            if (parent is ULambdaExpression) {
                val type: PsiType? = TypeEvaluator.evaluate(parent)
                if (isFormulaTransition(context, type)) {
                    return true
                }
            }

            parent = parent.uastParent
        }
        return false
    }

    private fun isFormulaTransition(context: JavaContext, type: PsiType?): Boolean {
        val parameters = when (type) {
            is PsiClassType -> type.parameters
            else -> emptyArray()
        }
        return isFormulaTransition(context, parameters.toList())
    }

    private fun isFormulaTransition(
        context: JavaContext,
        parameters: List<PsiType>,
    ): Boolean {
        return parameters.any {
            val type = if (it is PsiWildcardType) {
                // unwrap wildcard type
                // Ex: "? super TransitionContext" into "TransitionContext"
                it.bound
            } else {
                it
            }

            val typeClass = context.evaluator.getTypeClass(type)
            typeClass?.qualifiedName == "com.instacart.formula.TransitionContext"
        }
    }

    companion object {
        val ISSUE_ILLEGAL_CALL_WITHIN_TRANSITION_CONTEXT = Issue.create(
            id = "InvalidFormulaContextUsage",
            briefDescription = "Cannot use Snapshot or FormulaContext within TransitionContext",
            explanation = "It is an error to use Snapshot and FormulaContext within TransitionContext.",
            category = MESSAGES,
            priority = 5,
            severity = ERROR,
            implementation = Implementation(
                WrongFormulaUsageDetector::class.java,
                EnumSet.of(Scope.ALL_JAVA_FILES)
            )
        )

        val issues = arrayOf(
            ISSUE_ILLEGAL_CALL_WITHIN_TRANSITION_CONTEXT,
        )
    }
}