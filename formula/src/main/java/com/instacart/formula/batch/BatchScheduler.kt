package com.instacart.formula.batch

/**
 * Controls how events are batched and executed.
 */
interface BatchScheduler {

    /**
     * The batch class defines a group of events that will be executed together. The
     * [key] is used to group events together and [execute] will be called by [BatchScheduler]
     * to execute them.
     */
    interface Batch {
        /**
         * Executes the events.
         */
        fun execute()

        /**
         * Key used to group events
         */
        fun key(): Any
    }

    /**
     * Schedule the execution of a batch.
     */
    fun schedule(batch: Batch)

    /**
     * Defines the batch key.
     */
    fun key(event: Any?): Any = this

    /**
     * TODO: will allow to immediately execute certain events.
     * - Take into account ordering issues if two events from same listener happen and one
     * is batched while the other should be executed immediately.
     */
//     fun shouldBatch(event: Any?): Boolean = true
}