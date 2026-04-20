package com.example.deepworkai.network



import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleAuthManager(context: Context) {

    init {
        Log.d("GoogleAuthManager", "Initializing with Client ID: ${com.example.deepworkai.BuildConfig.GOOGLE_CLIENT_ID}")
    }

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(com.example.deepworkai.BuildConfig.GOOGLE_CLIENT_ID)
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent() = googleSignInClient.signInIntent
}