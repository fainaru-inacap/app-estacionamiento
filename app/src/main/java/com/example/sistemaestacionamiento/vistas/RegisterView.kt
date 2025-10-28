package com.example.sistemaestacionamiento.vistas

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import com.example.sistemaestacionamiento.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterView(navController: NavController, auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Estacionamiento",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 32.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF0277BD),
            modifier = Modifier.padding(bottom = 16.dp)
        )


        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar/ocultar contraseña"
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar/ocultar contraseña"
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                validarRegistro(email, password, confirmPassword, auth, context, onSuccess = {
                    navController.popBackStack()
                    navController.navigate("login")
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
        ) {
            Text("Crear cuenta", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))


        TextButton(
            onClick = {
                navController.navigate("login")
            }
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = Color(0xFF0288D1))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun validarRegistro(
    email: String,
    password: String,
    confirmPassword: String,
    auth: FirebaseAuth,
    context: Context,
    onSuccess: () -> Unit
) {
    when {
        email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }

        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
            Toast.makeText(context, "Correo inválido", Toast.LENGTH_SHORT).show()
        }

        password.length < 6 -> {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        }

        password != confirmPassword -> {
            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
        }

        else -> {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else {
                        Toast.makeText(
                            context,
                            "Error al registrar usuario: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
