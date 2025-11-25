package com.example.sistemaestacionamiento.vistas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import java.text.SimpleDateFormat
import java.util.Locale


data class HistoryEntry(
    val action: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val userEmail: String = "",
    val userId: String = ""
)


sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val entries: List<HistoryEntry>) : HistoryState()
    data class Error(val message: String) : HistoryState()
    object Empty : HistoryState()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialView(auth: FirebaseAuth) {

    val firestore = FirebaseFirestore.getInstance()
    var historyState by remember { mutableStateOf<HistoryState>(HistoryState.Loading) }
    val userId = auth.currentUser?.uid

    DisposableEffect(userId) {

        if (userId == null) {
            historyState = HistoryState.Error("Usuario no autenticado.")
            onDispose { }
        } else {

            val query = firestore.collection("historial")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            val registration = query.addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.e("HistorialView", "Error Firestore: ${e.message}", e)
                    historyState = HistoryState.Error("Error Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val entries = snapshot.documents.mapNotNull { it.toObject<HistoryEntry>() }
                    historyState = HistoryState.Success(entries)
                } else {
                    historyState = HistoryState.Empty
                }
            }

            onDispose { registration.remove() }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Historial de Actividad")
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Historial",
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {

            when (val state = historyState) {

                is HistoryState.Loading -> {
                    CircularProgressIndicator()
                }

                is HistoryState.Empty -> {
                    Text(
                        "No hay historial para mostrar.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is HistoryState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is HistoryState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.entries) { entry ->
                            HistoryItem(entry)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HistoryItem(entry: HistoryEntry) {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val dateString = formatter.format(entry.timestamp.toDate())

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Text(
                text = entry.action,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Fecha",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
