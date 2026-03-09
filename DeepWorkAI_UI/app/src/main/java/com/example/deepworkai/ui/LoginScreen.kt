package com.example.deepworkai.ui

import com.example.deepworkai.R
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import es.dmoral.toasty.Toasty
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.deepworkai.models.LoginRequest
import com.example.deepworkai.network.AuthService
import com.example.deepworkai.network.GoogleAuthManager
import com.example.deepworkai.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {

    val scope = rememberCoroutineScope()
    val authService = remember { AuthService() }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val googleAuthManager = remember { GoogleAuthManager(context) }
    
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            Log.d("GoogleLogin", "Google Sign-In Account: ${account?.email}")
            Log.d("GoogleLogin", "ID Token available: ${idToken != null}")

            if (idToken != null) {
                scope.launch {
                    isLoading = true
                    Toasty.info(context, "Connecting with Google...", Toast.LENGTH_SHORT, true).show()
                    val response = authService.loginWithGoogle(idToken)
                    isLoading = false
                    response.onSuccess { authResponse ->
                        Log.d("GoogleLogin", "Backend sync successful for ${authResponse.user.email}")
                        Toasty.success(context, "Welcome, ${authResponse.user.fullName}!", Toast.LENGTH_LONG, true).show()
                        onNavigateToHome()
                    }.onFailure { err ->
                        Log.e("GoogleLogin", "Backend sync failed", err)
                        Toasty.error(context, err.message ?: "Google Login failed to sync with server.", Toast.LENGTH_LONG, true).show()
                    }
                }
            } else {
                Log.e("GoogleLogin", "No ID Token returned from Google")
                Toasty.error(context, "Google login failed: No ID Token.", Toast.LENGTH_LONG, true).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Google Sign-In Failed: Code ${e.statusCode}, Message: ${e.message}")
            Toasty.error(context, "Google login failed (Code ${e.statusCode}). Check SHA-1/Client ID.", Toast.LENGTH_LONG, true).show()
        } catch (e: Exception) {
            Log.e("GoogleLogin", "Unexpected error during Google login", e)
            Toasty.error(context, "An unexpected error occurred.", Toast.LENGTH_LONG, true).show()
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DeepWorkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Logo Section ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DeepWorkBlue.copy(alpha = 0.1f))
                    .border(2.dp, DeepWorkBlue, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "DeepWork Logo",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DeepWork AI",
                style = MaterialTheme.typography.headlineLarge,
                color = DeepWorkTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Log in to track your cognitive load",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepWorkTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- Email Field ---
            CustomInputField(
                value = email,
                onValueChange = { email = it },
                label = "EMAIL ADDRESS",
                placeholder = "student@university.edu",
                leadingIcon = Icons.Default.Email
            )

            // --- Password Label Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PASSWORD", color = DeepWorkTextPrimary, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "Forgot Password?",
                    color = DeepWorkBlue,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable { /* TODO: Forgot Password */ }
                )
            }

            // --- Password Field ---
            CustomInputField(
                value = password,
                onValueChange = { password = it },
                label = "",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Login Button ---
            PrimaryButton(text = if (isLoading) "Logging in..." else "Login") {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    scope.launch {
                        isLoading = true
                        val request = LoginRequest(email, password)
                        val response = authService.login(request)
                        isLoading = false
                        response.onSuccess { authResponse ->
                            Toasty.success(context, "Welcome, ${authResponse.user.fullName}!", Toast.LENGTH_LONG, true).show()
                            onNavigateToHome()
                        }.onFailure { err ->
                            Toasty.error(context, err.message ?: "Login failed. Check your credentials.", Toast.LENGTH_LONG, true).show()
                        }
                    }
                } else {
                    Toasty.warning(context, "Please fill in all fields.", Toast.LENGTH_SHORT, true).show()
                }
            }

            // --- Social Section ---
            SocialDivider()

            SocialButton(
                icon = R.drawable.ic_google,
                text = "Continue with Google",
                onClick = { googleLauncher.launch(googleAuthManager.getSignInIntent()) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Footer ---
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = DeepWorkTextSecondary)
                Text(
                    text = "Create Account",
                    color = DeepWorkBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

// --- Components ---

@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = DeepWorkTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = DeepWorkTextSecondary) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = DeepWorkTextSecondary) },
            trailingIcon = {
                if (isPassword) {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = description, tint = DeepWorkTextSecondary)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepWorkSurface,
                unfocusedContainerColor = DeepWorkSurface,
                focusedBorderColor = DeepWorkBlue,
                unfocusedBorderColor = DeepWorkBorder,
                cursorColor = DeepWorkBlue,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    containerColor: Color = DeepWorkBlue,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
fun SocialDivider() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = DeepWorkBorder, thickness = 1.dp)
        Text(
            text = "Or continue with",
            color = DeepWorkTextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = DeepWorkBorder, thickness = 1.dp)
    }
}

@Composable
fun SocialButton(icon: Int, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DeepWorkBorder)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, color = DeepWorkTextPrimary, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}