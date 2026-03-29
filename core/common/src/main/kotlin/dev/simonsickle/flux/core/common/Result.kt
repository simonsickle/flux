package dev.simonsickle.flux.core.common

sealed class FluxResult<out T> {
    data class Success<T>(val data: T) : FluxResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : FluxResult<Nothing>()
    data object Loading : FluxResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): Error? = this as? Error
}

inline fun <T, R> FluxResult<T>.map(transform: (T) -> R): FluxResult<R> = when (this) {
    is FluxResult.Success -> FluxResult.Success(transform(data))
    is FluxResult.Error -> this
    is FluxResult.Loading -> this
}

inline fun <T> FluxResult<T>.onSuccess(action: (T) -> Unit): FluxResult<T> {
    if (this is FluxResult.Success) action(data)
    return this
}

inline fun <T> FluxResult<T>.onError(action: (FluxResult.Error) -> Unit): FluxResult<T> {
    if (this is FluxResult.Error) action(this)
    return this
}
