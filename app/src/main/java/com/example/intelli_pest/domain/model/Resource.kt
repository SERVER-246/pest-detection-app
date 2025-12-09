package com.example.intelli_pest.domain.model

/**
 * Sealed class representing different states of operations
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Exception? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    fun isLoading() = this is Loading
    fun isSuccess() = this is Success
    fun isError() = this is Error

    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}

