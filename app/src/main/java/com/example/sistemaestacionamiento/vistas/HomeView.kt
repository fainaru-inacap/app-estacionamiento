package com.example.sistemaestacionamiento.vistas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

data class ParkingSpot(
    val nombre: String = "",
    val ocupado: Boolean = false,
    val distancia: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavController, auth: FirebaseAuth) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().reference
    val firestore = FirebaseFirestore.getInstance()

    val parkingStates = remember { mutableStateListOf<ParkingSpot>() }
    val lastUpdateTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }

    val isConnected = (currentTime.value - lastUpdateTime.value) <= 5000
    val allSpotsOccupied = parkingStates.isNotEmpty() && parkingStates.all { it.ocupado }

    val estadoEstacionamiento = when {
        !isConnected -> "Desconectado"
        parkingStates.isEmpty() && isConnected -> "Cargando..."
        allSpotsOccupied -> "Estacionamiento Cerrado"
        else -> "Estacionamiento Abierto"
    }

    val topAppBarColor = when {
        !isConnected -> Color.Gray
        allSpotsOccupied -> Color(0xFFF44336)
        else -> Color(0xFF4CAF50)
    }

    val buttonText = remember { mutableStateOf("Abrir Barrera") }
    val isLoading = remember { mutableStateOf(false) }
    val isButtonClicked: MutableState<Boolean> = remember { mutableStateOf(false) }


    DisposableEffect(Unit) {
        val estacionamientoRef = database.child("estacionamiento")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)
                if (timestamp != null) {
                    lastUpdateTime.value = timestamp
                }

                val spotsList = snapshot.child("estacionamientos")
                    .children
                    .mapNotNull { it.getValue(ParkingSpot::class.java) }

                parkingStates.clear()
                parkingStates.addAll(spotsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Fallo al leer: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }

        estacionamientoRef.addValueEventListener(listener)

        onDispose {
            estacionamientoRef.removeEventListener(listener)
        }
    }

    // Ticker to check for connection status
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime.value = System.currentTimeMillis()
        }
    }

    LaunchedEffect(isButtonClicked.value) {
        if (isButtonClicked.value) {
            if (allSpotsOccupied) {
                Toast.makeText(context, "Estacionamiento lleno. No se puede abrir la barrera.", Toast.LENGTH_LONG).show()
                isButtonClicked.value = false
                return@LaunchedEffect
            }

            buttonText.value = "Cargando..."
            isLoading.value = true

            database.child("comandos").child("abrir_puerta").setValue(true)

            val user = auth.currentUser
            if (user != null) {
                val historyEntry = hashMapOf(
                    "userId" to user.uid,
                    "userEmail" to user.email,
                    "action" to "Abrir Barrera",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                firestore.collection("historial")
                    .add(historyEntry)
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al guardar historial: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            delay(2000)

            database.child("comandos").child("abrir_puerta").setValue(false)

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
                        text = estadoEstacionamiento,
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
                    enabled = isConnected && !isLoading.value,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allSpotsOccupied) Color(0xFFF44336) else Color(0xFF4CAF50),
                        disabledContainerColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(text = buttonText.value, color = Color.White, fontSize = 18.sp)
                    }
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
                val spot = parkingStates[index]
                ParkingCard(
                    nombre = spot.nombre,
                    estado = if (spot.ocupado) "Ocupado" else "Libre",
                    isEnabled = isConnected
                )
            }
        }
    }
}

@Composable
fun ParkingCard(nombre: String, estado: String, isEnabled: Boolean) {
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
                color = if (isEnabled) Color.Black else Color.Gray
            )
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = if (isEnabled) {
                        if (estado == "Ocupado") Color(0xFFF44336) else Color(0xFF4CAF50)
                    } else {
                        Color.DarkGray
                    }
                ),
                shape = RoundedCornerShape(50)
            ) {
                val estadoText = if (isEnabled) estado else "--"
                Text(text = estadoText, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
