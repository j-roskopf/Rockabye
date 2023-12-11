package com.joetr.bundle.network.data

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [S] or a failure of type [F].
 *
 * This is similar to Kotlin's [Result] type, except it allows us to specify the error type for failures.
 * This allows more flexibility in the error type and enables explicitly declared sealed error type hierarchies
 * for compile-time validation of error handling.
 *
 * This could be seen as similar to the [Result] class from Kotlin's stdlib, with the distinction being
 * we can specify the error type for failures.
 *
 * Examples usage:
 *
 * public interface ExampleService {
 *     suspend fun getData() : Either<Data, Throwable>
 * }
 *
 * val data = ExampleService.getData()
 * when(data) {
 *     is Either.Success -> handleSuccess()
 *     is Either.Failure -> handleFailure()
 * }
 */
public sealed class Either<out S, out F> {

    public val isSuccess: Boolean get() = this is Success<S>

    public val isFailure: Boolean get() = this is Failure<F>

    /**
     * Maps [Success] with [mapper] or leaves [Failure] as is.
     */
    public inline fun <R : Any> map(
        mapper: (S) -> R,
    ): Either<R, F> {
        return when (this) {
            is Success -> success(value = mapper(value))
            is Failure -> this
        }
    }

    /**
     * Unwraps this [Either] by returning [Success.value]
     * or throwing an exception produced with [onFailure].
     */
    public inline fun unwrap(
        onFailure: (F) -> Throwable,
    ): S {
        return when (this) {
            is Success -> value
            is Failure -> throw onFailure(error)
        }
    }

    public companion object {
        /**
         * Helper for creating [Success]
         */
        public fun <S> success(
            value: S,
        ): Success<S> = Success(value)

        /**
         * Helper for creating [Failure]
         */
        public fun <F> failure(
            error: F,
        ): Failure<F> = Failure(error)
    }

    public data class Success<out S>(val value: S) : Either<S, Nothing>()

    public data class Failure<out F>(val error: F) : Either<Nothing, F>()
}
