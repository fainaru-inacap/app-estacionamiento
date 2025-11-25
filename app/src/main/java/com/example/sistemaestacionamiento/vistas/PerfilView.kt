package com.example.sistemaestacionamiento.vistas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PerfilView(navController: NavController, auth: FirebaseAuth) {
    val user = auth.currentUser
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier.size(128.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(128.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = user?.email ?: "",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            navController.navigate("change_password")
        }) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = "Cambiar contraseña")
            Text("Cambiar contraseña", modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            auth.signOut()
            navController.navigate("login") {
                popUpTo(0) // Limpia el back stack para que el usuario no pueda volver
            }
        }) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
            Text("Cerrar sesión", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
