package com.example.collection3

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collection3.ui.theme.Collection3Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Collection3Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CollectionCalculator()
                }
            }
        }
    }
}

@Composable
fun CollectionCalculator() {
    val context = LocalContext.current
    val denominations = listOf(100.0, 50.0, 20.0, 10.0, 5.0, 2.0, 1.0, 0.5, 0.2, 0.1, 0.05)
    val counts = remember { mutableStateListOf(*Array(denominations.size) { "" }) }

    val totals = denominations.mapIndexed { index, denom ->
        val count = counts[index].toDoubleOrNull() ?: 0.0
        count * denom
    }
    val grandTotal = totals.sum()
    val firstCollection = grandTotal * 0.2
    val secondCollection = grandTotal - firstCollection
    val BBFC = secondCollection * 0.7
    val OLOR = secondCollection - BBFC

    var showMenu by remember { mutableStateOf(false) }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let { saveToUri(context, it, counts) }
    }

    val loadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { loadFromUri(context, it, counts) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Collection Calculator", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Save (SAF)") },
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                            saveLauncher.launch("collection_$timestamp.txt")
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Load (SAF)") },
                        onClick = {
                            loadLauncher.launch(arrayOf("text/plain"))
                            showMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Denomination inputs
        denominations.forEachIndexed { index, denom ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${"%.2f".format(denom)}",
                    modifier = Modifier.width(85.dp),
                    fontSize = 16.sp
                )
                TextField(
                    value = counts[index],
                    onValueChange = { counts[index] = it },
                    placeholder = { Text("0") },
                    singleLine = true,
                    modifier = Modifier
                        .width(100.dp)
                        .padding(end = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
                Text(
                    text = "= $%.2f".format(totals[index]),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Total Collection: $%.2f".format(grandTotal), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("First Collection : $%.2f".format(firstCollection), fontSize = 16.sp)
        Text("Second Collection : $%.2f".format(secondCollection), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Breakdown of Second Collection", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text("BBFC : $%.2f".format(BBFC), fontSize = 16.sp)
        Text("OLOR : $%.2f".format(OLOR), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))
    }
}




fun saveToUri(context: Context, uri: Uri, counts: List<String>) {
    val grandTotal = counts.mapIndexed { i, value ->
        (value.toDoubleOrNull() ?: 0.0) * listOf(100.0, 50.0, 20.0, 10.0, 5.0, 2.0, 1.0, 0.5, 0.2, 0.1, 0.05)[i]
    }.sum()

    val firstCollection = grandTotal * 0.2
    val secondCollection = grandTotal - firstCollection

    val data = buildString {
        append(counts.joinToString(","))
        append("\n")
        append("%.2f,%.2f,%.2f".format(grandTotal, firstCollection, secondCollection))
    }

    try {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(data.toByteArray())
            Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun loadFromUri(context: Context, uri: Uri, counts: MutableList<String>) {
    try {
        val input = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readLines()
        if (input != null && input.isNotEmpty()) {
            val values = input[0].split(",")
            for (i in counts.indices) {
                counts[i] = values.getOrNull(i) ?: ""
            }
            Toast.makeText(context, "File loaded", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error loading: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
