package com.instacart.formula

import io.reactivex.disposables.Disposable
import kotlin.reflect.KClass

sealed class Update {

    abstract fun keyAsString(): String

    class Effect(
        val input: Any,
        val key: String,
        val action: () -> Unit
    ): Update() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Effect

            if (input != other.input) return false
            if (key != other.key) return false

            return true
        }

        override fun hashCode(): Int {
            var result = input.hashCode()
            result = 31 * result + key.hashCode()
            return result
        }

        override fun keyAsString(): String {
            return "$key-$input"
        }
    }

    class Stream<Input : Any, Output>(
        val key: Key,
        val input: Input,
        val stream: com.instacart.formula.Stream<Input, Output>,
        onEvent: (Output) -> Unit
    ): Update() {

        /**
         * A way to ensure uniqueness and equality between [Stream]s.
         */
        data class Key(
            val input: Any,
            val processorType: KClass<*>,
            val tag: String = ""
        )

        internal var handler: (Output) -> Unit = onEvent
        internal var disposable: Disposable? = null

        internal fun start() {
            disposable = stream.subscribe(input) { next ->
                handler.invoke(next)
            }
        }

        internal fun tearDown() {
            disposable?.dispose()
            disposable = null
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Stream<*, *>

            if (key != other.key) return false

            return true
        }

        override fun hashCode(): Int {
            return key.hashCode()
        }

        override fun keyAsString(): String {
            return key.toString()
        }
    }
}
