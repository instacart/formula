package lombok

@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Generated
