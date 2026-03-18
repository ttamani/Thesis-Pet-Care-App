package com.learning.multipet.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.learning.multipet.R


private enum class AuthMode { Login, Register, OtpVerify }

@Composable
fun LoginScreen(
    // Supabase ready callbacks (wire later)
    onLogin: (email: String, password: String, rememberMe: Boolean) -> Unit = { _, _, _ -> },
    onRegister: (email: String, password: String) -> Unit = { _, _ -> },
    onVerifyOtp: (email: String, otp4: String) -> Unit = { _, _ -> },
    onResendOtp: (email: String) -> Unit = { _ -> },
    onGoogleSignIn: () -> Unit = {},
    // kapag okay lahat
    onAuthSuccess: () -> Unit
) {
    var mode by remember { mutableStateOf(AuthMode.Login) }

    // share email across register -> otp
    var sharedEmail by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B1220).copy(alpha = 0.78f),
                            Color(0xFF0B1220).copy(alpha = 0.55f)
                        )
                    )
                )
        )

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.10f),
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
                .fillMaxWidth()
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.16f),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    when (mode) {
                        AuthMode.Login -> "Welcome!"
                        AuthMode.Register -> "Create your account"
                        AuthMode.OtpVerify -> "Verify your email"
                    },
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    when (mode) {
                        AuthMode.Login -> "Login to continue"
                        AuthMode.Register -> "Register with your email"
                        AuthMode.OtpVerify -> "Enter the 4-digit code sent to your email"
                    },
                    color = Color.White.copy(alpha = 0.70f),
                    style = MaterialTheme.typography.bodyMedium
                )

                AnimatedContent(
                    targetState = mode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = ""
                ) { m ->
                    when (m) {
                        AuthMode.Login -> LoginForm(
                            onLogin = { email, pass, rememberMe ->
                                onLogin(email, pass, rememberMe)
                                // pansamantagal
                                // onAuthSuccess()
                            },
                            onGoogleSignIn = onGoogleSignIn,
                            onGoRegister = { mode = AuthMode.Register }
                        )

                        AuthMode.Register -> RegisterForm(
                            onCreateAccount = { email, pass ->
                                sharedEmail = email
                                onRegister(email, pass)
                                // after calling Supabase signUp (email OTP), go to OTP screen:
                                mode = AuthMode.OtpVerify
                            },
                            onGoogleSignIn = onGoogleSignIn,
                            onBackToLogin = { mode = AuthMode.Login }
                        )

                        AuthMode.OtpVerify -> OtpVerifyForm(
                            email = sharedEmail,
                            onVerify = { otp ->
                                onVerifyOtp(sharedEmail, otp)
                                // if verify success:
                                // onAuthSuccess()
                            },
                            onResend = { onResendOtp(sharedEmail) },
                            onChangeEmail = {
                                sharedEmail = ""
                                mode = AuthMode.Register
                            },
                            onBackToLogin = { mode = AuthMode.Login }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    onLogin: (String, String, Boolean) -> Unit,
    onGoogleSignIn: () -> Unit,
    onGoRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            showPassword = showPassword,
            onTogglePassword = { showPassword = !showPassword }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF14B8A6),
                    uncheckedColor = Color.White.copy(alpha = 0.7f),
                    checkmarkColor = Color.Black
                )
            )
            Text("Remember me", color = Color.White.copy(alpha = 0.90f))
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { /* later: forgot password */ }) {
                Text("Forgot?", color = Color.White)
            }
        }

        Button(
            onClick = { onLogin(email.trim(), password, rememberMe) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF14B8A6),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Login", fontWeight = FontWeight.ExtraBold)
        }

        GoogleButton(onClick = onGoogleSignIn)

        TextButton(
            onClick = onGoRegister,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("No account? Register", color = Color.White)
        }
    }
}

@Composable
private fun RegisterForm(
    onCreateAccount: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val canRegister = email.isNotBlank() && password.isNotBlank() && password == confirm

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            showPassword = showPassword,
            onTogglePassword = { showPassword = !showPassword }
        )

        AuthTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = "Confirm password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            showPassword = showConfirm,
            onTogglePassword = { showConfirm = !showConfirm }
        )

        Button(
            onClick = { onCreateAccount(email.trim(), password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canRegister,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF14B8A6),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Create account", fontWeight = FontWeight.ExtraBold)
        }

        GoogleButton(onClick = onGoogleSignIn)

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Login", color = Color.White)
        }
    }
}

@Composable
private fun OtpVerifyForm(
    email: String,
    onVerify: (otp4: String) -> Unit,
    onResend: () -> Unit,
    onChangeEmail: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var d1 by remember { mutableStateOf("") }
    var d2 by remember { mutableStateOf("") }
    var d3 by remember { mutableStateOf("") }
    var d4 by remember { mutableStateOf("") }

    val otp = (d1 + d2 + d3 + d4).trim()
    val canVerify = otp.length == 4 && otp.all { it.isDigit() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = "Code sent to:",
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = if (email.isBlank()) "—" else email,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OtpBox(d1, onValue = { d1 = it; if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Next) }, focusManager)
            OtpBox(d2, onValue = { d2 = it; if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Next) }, focusManager)
            OtpBox(d3, onValue = { d3 = it; if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Next) }, focusManager)
            OtpBox(d4, onValue = { d4 = it; if (it.isNotEmpty()) focusManager.clearFocus() }, focusManager)
        }

        Button(
            onClick = { onVerify(otp) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canVerify,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF14B8A6),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Verify", fontWeight = FontWeight.ExtraBold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onResend) { Text("Resend code", color = Color.White) }
            TextButton(onClick = onChangeEmail) { Text("Change email", color = Color.White) }
        }

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Login", color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun OtpBox(
    value: String,
    onValue: (String) -> Unit,
    focusManager: FocusManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val v = raw.takeLast(1).filter { it.isDigit() }
            onValue(v)
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) }
        ),
        modifier = Modifier
            .width(56.dp)
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF14B8A6),
            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF14B8A6),
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
        )
    )
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword && onTogglePassword != null) {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle password",
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF14B8A6),
            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF14B8A6),
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
        )
    )
}

@Composable
private fun GoogleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text("Continue with Google", fontWeight = FontWeight.SemiBold)
    }
}