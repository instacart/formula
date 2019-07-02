package com.instacart.formula.internal

class TransitionLockImpl : TransitionLock {
    var processingPass: Long = 0
    
    fun next(): Long {
        processingPass += 1
        return processingPass
    }
    
    override fun hasTransitioned(transitionNumber: Long): Boolean {
        return transitionNumber != processingPass
    }
}
