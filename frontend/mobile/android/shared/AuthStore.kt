package com.travelify.shared

/** Minimal shared auth token holder for role-specific Android apps. */
object AuthStore {
    @Volatile
    var token: String? = null

    @Volatile
    var role: String? = null

    fun clear() {
        token = null
        role = null
    }
}