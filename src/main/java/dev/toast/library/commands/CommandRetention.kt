package dev.toast.library.commands



@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class WLCommandArgs(vararg val args: WLCommandArg)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class WLCommandArg(val name: String, val type: ArgumentType = ArgumentType.STRING)

enum class ArgumentType {
    STRING,
    INTEGER,
    BOOLEAN
}