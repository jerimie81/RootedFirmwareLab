package com.redrum.rootedfirmwarelab.core.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class TerminalSession(private val scope: CoroutineScope) {
    private var process: Process? = null
    private var outputStream: OutputStream? = null
    private val _outputFlow = MutableSharedFlow<String>()
    val outputFlow = _outputFlow.asSharedFlow()
    private var job: Job? = null

    fun start() {
        if (process != null) return
        
        scope.launch(Dispatchers.IO) {
            try {
                process = ProcessBuilder("su").redirectErrorStream(true).start()
                outputStream = process?.outputStream
                
                job = launch {
                    val reader = process?.inputStream?.bufferedReader()
                    reader?.let {
                        val buffer = CharArray(1024)
                        while (true) {
                            val read = it.read(buffer)
                            if (read == -1) break
                            _outputFlow.emit(String(buffer, 0, read))
                        }
                    }
                }
                
                process?.waitFor()
                _outputFlow.emit("\n[Process exited]")
                process = null
            } catch (e: Exception) {
                _outputFlow.emit("\n[Error starting terminal: ${e.message}]")
            }
        }
    }

    fun sendCommand(command: String) {
        scope.launch(Dispatchers.IO) {
            outputStream?.let {
                it.write((command + "\n").toByteArray())
                it.flush()
            }
        }
    }

    fun stop() {
        job?.cancel()
        process?.destroy()
        process = null
    }
}