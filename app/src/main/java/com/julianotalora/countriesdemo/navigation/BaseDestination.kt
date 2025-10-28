package com.julianotalora.countriesdemo.navigation

import android.net.Uri

interface BaseDestination {
    val route: String
    val destination: String

    fun destinationWithArguments(vararg arguments: Pair<String, Any>): String {
        return with(Uri.Builder()) {
            path(destination)
            arguments.forEach {
                appendQueryParameter(it.first, it.second.toString())
            }
            build().toString()
        }
    }

    fun fullDestination(vararg argNames: String): String {
        return with(Uri.Builder()) {
            path(destination)
            argNames.forEach {
                appendQueryParameter(it, "{$it}")
            }
            build().toString()
        }
    }
}