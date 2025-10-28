package com.example.sistemaestacionamiento.vistas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun LoginView(navController: NavController, auth: FirebaseAuth) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

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
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF0277BD),
            modifier = Modifier.padding(bottom = 16.dp)
        )


        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
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

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                validarCredencial(username, password, auth, context, navController)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
        ) {
            Text("Iniciar sesión", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))


        TextButton(
            onClick = {
                navController.navigate("register")
            }
        ) {
            Text("¿No tienes cuenta? Regístrate", color = Color(0xFF0288D1))
        }

        Spacer(modifier = Modifier.height(32.dp))

    }
}

private fun validarCredencial(
    username: String,
    password: String,
    auth: FirebaseAuth,
    context: Context,
    navController: NavController
) {
    if (username.isNotBlank() && password.isNotBlank()) {
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Inicio de sesión con éxito", Toast.LENGTH_SHORT).show()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        Toast.makeText(context, "Ingresa tu nombre y contraseña", Toast.LENGTH_SHORT).show()
    }
}
