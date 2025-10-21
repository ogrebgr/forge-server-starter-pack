package org.changeme.starterpack.misc

@RequiresOptIn(message = "This method must be used only in event when user and his data is real deleted (during development test only).")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class UserRealDeleteOptIn