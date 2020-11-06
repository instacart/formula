package com.instacart.formula.internal

class TransitionIdManager {

    data class TransitionIdImpl(
        val transitionID: Long,
        private val manager: TransitionIdManager
    ): TransitionId {

        fun next() = copy(transitionID = transitionID + 1)

        override fun hasTransitioned(): Boolean {
            return manager.hasTransitioned(transitionID)
        }
    }

    var transitionId = TransitionIdImpl(0, this)

    fun next(): TransitionId {
        transitionId = transitionId.next()
        return transitionId
    }

    fun hasTransitioned(transitionID: Long): Boolean {
        return transitionId.transitionID != transitionID
    }
}



