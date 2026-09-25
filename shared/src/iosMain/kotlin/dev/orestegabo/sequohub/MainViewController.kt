package dev.orestegabo.sequohub

import androidx.compose.ui.window.ComposeUIViewController
import dev.orestegabo.sequohub.core.auth.GoogleSignInResult

fun MainViewController() = ComposeUIViewController { App() }

fun MainViewController(googleSignInProvider: IosGoogleSignInProvider) =
    ComposeUIViewController {
        App(onGoogleSignIn = { googleSignInProvider.signIn() })
    }

interface IosGoogleSignInProvider {
    suspend fun signIn(): GoogleSignInResult
}
