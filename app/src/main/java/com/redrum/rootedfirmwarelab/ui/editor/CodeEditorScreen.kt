package com.redrum.rootedfirmwarelab.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun CodeEditorScreen(file: File) {
    var content by remember { mutableStateOf(file.readText()) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Editing: ${file.name}", style = MaterialTheme.typography.titleMedium)
        TextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        Button(onClick = { file.writeText(content) }, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
    }
}
