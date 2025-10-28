package com.example.sistemaestacionamiento.vistas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavController, auth: FirebaseAuth) {
    val context = LocalContext.current

    val parkingStates = remember { mutableStateListOf(false, false, false) }

    val buttonText = remember { mutableStateOf("Abrir Barrera") }
    val isLoading = remember { mutableStateOf(false) }
    val isButtonClicked: MutableState<Boolean> = remember { mutableStateOf(false) }



    val topAppBarColor = if (parkingStates.count { it } == parkingStates.size) {
        Color(0xFFF44336) // Rojo si todos están ocupados
    } else {
        Color(0xFF4CAF50) // Verde si no todos están ocupados
    }


    LaunchedEffect(Unit) {
        while (true) {
            // cambiar el estado de un estacionamiento de forma aleatoria
            val randomIndex = (0 until parkingStates.size).random()
            parkingStates[randomIndex] = !parkingStates[randomIndex]


            delay(2000L)
        }
    }

    LaunchedEffect(isButtonClicked.value) {
        if (isButtonClicked.value) {

            buttonText.value = "Cargando..."
            isLoading.value = true


            delay(2000)


            isLoading.value = false

            Toast.makeText(context, "Barrera abierta", Toast.LENGTH_SHORT).show()

            buttonText.value = "Abrir Barrera"

            isButtonClicked.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estacionamiento Abierto",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarColor
                )
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(x = 17.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = {
                        isButtonClicked.value = true

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Text(text = buttonText.value, color = Color.White, fontSize = 18.sp)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(parkingStates.size) { index ->
                ParkingCard(
                    nombre = "Estacionamiento ${index + 1}",
                    estado = if (parkingStates[index]) "Ocupado" else "Libre",
                    onClick = {

                        parkingStates[index] = !parkingStates[index]
                    }
                )
            }
        }
    }
}

@Composable
fun ParkingCard(nombre: String, estado: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = nombre,
                fontSize = 20.sp,
                color = Color.Black
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (estado == "Ocupado") Color(0xFFF44336) else Color(0xFF00BCD4)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = estado, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}



