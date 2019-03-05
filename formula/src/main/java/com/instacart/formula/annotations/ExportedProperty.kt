package com.instacart.formula.annotations

/**
 * Marks an exported property on a [State] data class. Exported property means
 * that the management of this property is exported to another view model.
 *
 * @param isDirectInput Generates code so you can directly update a property. Similar to [DirectInput]
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ExportedProperty(val isDirectInput: Boolean = false)
