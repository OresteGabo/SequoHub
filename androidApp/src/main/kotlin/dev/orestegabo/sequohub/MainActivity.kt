package dev.orestegabo.sequohub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.orestegabo.sequohub.core.auth.GoogleSignInResult

class MainActivity : ComponentActivity() {
    private val credentialManager by lazy {
        CredentialManager.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(onGoogleSignIn = ::signInWithGoogle)
        }
    }

    private suspend fun signInWithGoogle(): GoogleSignInResult {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.google_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = this,
                request = request,
            )
            val credential = result.credential

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(
                    idToken = googleCredential.idToken,
                    displayName = googleCredential.displayName,
                    email = googleCredential.id,
                    profilePictureUri = googleCredential.profilePictureUri?.toString(),
                )
            } else {
                GoogleSignInResult.Failure("Google returned an unsupported credential.")
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (error: GetCredentialException) {
            GoogleSignInResult.Failure(error.message ?: "Google sign-in failed.")
        } catch (error: IllegalArgumentException) {
            GoogleSignInResult.Failure(error.message ?: "Google sign-in response was invalid.")
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
