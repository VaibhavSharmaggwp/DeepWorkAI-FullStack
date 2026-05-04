package com.example.deepworkai.ui

import com.example.deepworkai.R
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import es.dmoral.toasty.Toasty
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.deepworkai.models.RegisterRequest
import com.example.deepworkai.network.AuthService
import com.example.deepworkai.ui.theme.*
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {

    val scope = rememberCoroutineScope()
    val authService = remember { AuthService() }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
            Spacer(modifier = Modifier.height(40.dp))

            // --- Logo Section ---
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "DeepWork Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = DeepWorkTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Join DeepWork AI today",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepWorkTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Full Name Field ---
            CustomInputField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "FULL NAME",
                placeholder = "John Doe",
                leadingIcon = Icons.Default.Person
            )

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

            // --- Confirm Password Label Row ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CONFIRM PASSWORD", color = DeepWorkTextPrimary, style = MaterialTheme.typography.labelLarge)
            }

            // --- Confirm Password Field ---
            CustomInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Register Button ---
            PrimaryButton(text = if (isLoading) "Creating Account..." else "Sign Up") {
                if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    if (password != confirmPassword) {
                        Toasty.warning(context, "Passwords do not match!", Toast.LENGTH_SHORT, true).show()
                    } else {
                        scope.launch {
                            isLoading = true
                            val request = RegisterRequest(fullName, email, password)
                            val response = authService.register(request)
                            isLoading = false
                            response.onSuccess { authResponse ->
                                Toasty.success(context, "Welcome, ${authResponse.user.fullName}! Please login.", Toast.LENGTH_LONG, true).show()
                                onNavigateToLogin()
                            }.onFailure { err ->
                                Toasty.error(context, err.message ?: "Registration failed.", Toast.LENGTH_LONG, true).show()
                            }
                        }
                    }
                } else {
                    Toasty.warning(context, "Please fill in all fields.", Toast.LENGTH_SHORT, true).show()
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Footer ---
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = DeepWorkTextSecondary)
                Text(
                    text = "Log In",
                    color = DeepWorkBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}
