package com.vagell.kv4pht.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vagell.kv4pht.radio.RadioUiState

enum class ShellDestination { RADIO, SPECTRUM, DATA, MAIL, LOG }

private val Canvas = Color(0xFF0E1114)
private val Panel = Color(0xFF1A1F24)
private val Ink = Color(0xFFF2F0E8)
private val Muted = Color(0xFFA7A296)
private val Gold = Color(0xFFC4A574)
private val Rx = Color(0xFF3DDC84)
private val Tx = Color(0xFFE23B3B)
private val Scan = Color(0xFFE0A100)
private val Shape = RoundedCornerShape(12.dp)

@Composable
fun AtleyRadioScreen(
    ui: RadioUiState,
    frequency: String,
    callsign: String,
    squelch: Int,
    volume: Int,
    volumeMax: Int,
    meter: Int,
    destination: ShellDestination,
    traceText: String,
    onDestination: (ShellDestination) -> Unit,
    onDigit: (Int) -> Unit,
    onSquelch: (Int) -> Unit,
    onVolume: (Int) -> Unit,
    onPttDown: () -> Unit,
    onPttUp: () -> Unit,
    onUnkey: () -> Unit,
    onScan: () -> Unit,
    onLegacy: () -> Unit,
    onExport: () -> Unit
) {
    BoxWithLayout(ui, frequency, callsign, squelch, volume, volumeMax, meter, destination, traceText, onDestination, onDigit, onSquelch, onVolume, onPttDown, onPttUp, onUnkey, onScan, onLegacy, onExport)
}

@Composable
private fun BoxWithLayout(
    ui: RadioUiState,
    frequency: String,
    callsign: String,
    squelch: Int,
    volume: Int,
    volumeMax: Int,
    meter: Int,
    destination: ShellDestination,
    traceText: String,
    onDestination: (ShellDestination) -> Unit,
    onDigit: (Int) -> Unit,
    onSquelch: (Int) -> Unit,
    onVolume: (Int) -> Unit,
    onPttDown: () -> Unit,
    onPttUp: () -> Unit,
    onUnkey: () -> Unit,
    onScan: () -> Unit,
    onLegacy: () -> Unit,
    onExport: () -> Unit
) {
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().background(Canvas)) {
        val landscape = maxWidth > maxHeight
        if (landscape) {
            Row(Modifier.fillMaxSize()) {
                Dock(destination, onDestination, vertical = true)
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    StatusBand(ui, callsign, onUnkey, onLegacy)
                    Body(ui, frequency, squelch, volume, volumeMax, meter, destination, traceText, onDigit, onSquelch, onVolume, onPttDown, onPttUp, onScan, onExport, Modifier.weight(1f))
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                StatusBand(ui, callsign, onUnkey, onLegacy)
                Body(ui, frequency, squelch, volume, volumeMax, meter, destination, traceText, onDigit, onSquelch, onVolume, onPttDown, onPttUp, onScan, onExport, Modifier.weight(1f))
                Dock(destination, onDestination, vertical = false)
            }
        }
    }
}

@Composable
private fun StatusBand(ui: RadioUiState, callsign: String, onUnkey: () -> Unit, onLegacy: () -> Unit) {
    val chip = when (ui.operating) {
        RadioUiState.Operating.TRANSMITTING -> Tx
        RadioUiState.Operating.SCANNING -> Scan
        RadioUiState.Operating.RECEIVING, RadioUiState.Operating.READY -> Rx
        RadioUiState.Operating.ERROR -> Tx
        else -> Muted
    }
    Row(
        Modifier.fillMaxWidth().height(56.dp).background(Panel).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("KV4P HT", color = Gold, fontSize = 12.sp, letterSpacing = 1.sp)
            Text(if (callsign.isBlank()) "NO CALL" else callsign, color = Ink, fontSize = 14.sp)
        }
        Text(ui.statusLabel, color = chip, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (ui.unkeyVisible) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.height(40.dp).border(1.dp, Tx, Shape).clickable(onClick = onUnkey).padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) { Text("UNKEY", color = Tx, fontSize = 14.sp) }
        }
        Spacer(Modifier.width(8.dp))
        Text("LEGACY", color = Muted, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onLegacy))
    }
}

@Composable
private fun Body(
    ui: RadioUiState,
    frequency: String,
    squelch: Int,
    volume: Int,
    volumeMax: Int,
    meter: Int,
    destination: ShellDestination,
    traceText: String,
    onDigit: (Int) -> Unit,
    onSquelch: (Int) -> Unit,
    onVolume: (Int) -> Unit,
    onPttDown: () -> Unit,
    onPttUp: () -> Unit,
    onScan: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier
) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        when (destination) {
            ShellDestination.RADIO -> RadioBody(ui, frequency, squelch, volume, volumeMax, meter, onDigit, onSquelch, onVolume, onPttDown, onPttUp, onScan)
            ShellDestination.SPECTRUM -> Explain("Audio spectrum", "KV4P supplies demodulated audio, not an RF waterfall. A panadapter needs IQ hardware.")
            ShellDestination.DATA -> Explain("Data", "APRS and packet stay on the legacy screen until this shell owns them. KV4P cannot do FT8.")
            ShellDestination.MAIL -> Explain("Mail", "Winlink CMS and RMS are not connected. Packet mail remains on the legacy screen.")
            ShellDestination.LOG -> Explain("Log", "The contact log remains on the legacy screen for this slice.")
        }
        Spacer(Modifier.height(16.dp))
        Text("DIAGNOSTICS", color = Muted, fontSize = 12.sp, letterSpacing = 1.sp)
        Text(traceText, color = Ink, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text("EXPORT", color = Gold, modifier = Modifier.clickable(onClick = onExport).padding(top = 8.dp))
    }
}

@Composable
private fun RadioBody(
    ui: RadioUiState,
    frequency: String,
    squelch: Int,
    volume: Int,
    volumeMax: Int,
    meter: Int,
    onDigit: (Int) -> Unit,
    onSquelch: (Int) -> Unit,
    onVolume: (Int) -> Unit,
    onPttDown: () -> Unit,
    onPttUp: () -> Unit,
    onScan: () -> Unit
) {
    Text("FM · VFO · SIMPLEX", color = Muted, fontSize = 12.sp, letterSpacing = 1.sp)
    Row {
        frequency.forEachIndexed { index, char ->
            Text(
                char.toString(),
                color = if (char.isDigit()) Ink else Muted,
                fontSize = 56.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable(enabled = char.isDigit()) { onDigit(index) }
            )
        }
    }
    Text("MHz", color = Muted, fontSize = 14.sp)
    Text(ui.detail, color = Muted, fontSize = 14.sp)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(9) { index ->
            val lit = index < meter.coerceIn(0, 9)
            Box(
                Modifier.size(width = 22.dp, height = 18.dp).background(if (lit) Rx else Color(0xFF242B31), RoundedCornerShape(2.dp))
            )
        }
    }
    Text(if (ui.operating == RadioUiState.Operating.RECEIVING) "SQL OPEN" else "SQL $squelch", color = Muted, fontSize = 12.sp)
    Text("SQUELCH $squelch", color = Ink)
    Slider(value = squelch.toFloat(), onValueChange = { onSquelch(it.toInt()) }, valueRange = 0f..8f)
    Text("PHONE VOLUME", color = Ink)
    Slider(value = volume.toFloat(), onValueChange = { onVolume(it.toInt()) }, valueRange = 0f..volumeMax.toFloat())
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (ui.operating != RadioUiState.Operating.SCANNING) {
            Text("SCAN", color = Gold, modifier = Modifier.clickable(onClick = onScan).padding(12.dp))
        } else {
            Text("STOP SCAN", color = Scan, modifier = Modifier.clickable(onClick = onScan).padding(12.dp))
        }
        Spacer(Modifier.weight(1f))
        if (ui.pttAvailable) {
            Box(
                Modifier.size(72.dp).background(Panel, RoundedCornerShape(16.dp)).border(1.dp, Rx, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            onPttDown()
                            tryAwaitRelease()
                            onPttUp()
                        })
                    },
                contentAlignment = Alignment.Center
            ) { Text("PTT", color = Ink) }
        }
    }
}

@Composable
private fun Explain(title: String, body: String) {
    Text(title, color = Ink, fontSize = 28.sp)
    Spacer(Modifier.height(8.dp))
    Text(body, color = Muted, fontSize = 16.sp)
}

@Composable
private fun Dock(selected: ShellDestination, onDestination: (ShellDestination) -> Unit, vertical: Boolean) {
    val items = ShellDestination.entries
    if (vertical) {
        Column(Modifier.width(88.dp).fillMaxHeight().background(Panel), verticalArrangement = Arrangement.SpaceEvenly) {
            items.forEach { item -> DockItem(item, item == selected, onDestination) }
        }
    } else {
        Row(Modifier.fillMaxWidth().height(64.dp).background(Panel), horizontalArrangement = Arrangement.SpaceEvenly) {
            items.forEach { item -> DockItem(item, item == selected, onDestination) }
        }
    }
}

@Composable
private fun DockItem(item: ShellDestination, selected: Boolean, onDestination: (ShellDestination) -> Unit) {
    Text(
        item.name,
        color = if (selected) Gold else Muted,
        fontSize = 11.sp,
        modifier = Modifier.clickable { onDestination(item) }.padding(8.dp)
    )
}
