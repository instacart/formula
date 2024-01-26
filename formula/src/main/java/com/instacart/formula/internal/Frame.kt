package com.instacart.formula.internal

import com.instacart.formula.Evaluation

/**
 * After each formula evaluation, we create a frame object which holds the evaluation result
 * and parameters used to create it.
 *
 * @param input Input used in the evaluation
 * @param state State used in the evaluation
 * @param evaluation Result of the evaluation which contains formula output.
 * @param associatedEvaluationId Each output is associated with a specific evaluation id. We use
 * this identifier to determine if the frame is still valid or if we need to create a new one.
 */
internal class Frame<Input, State, Output>(
    val input: Input,
    private val state: State,
    val evaluation: Evaluation<Output>,
    val associatedEvaluationId: Long,
)
