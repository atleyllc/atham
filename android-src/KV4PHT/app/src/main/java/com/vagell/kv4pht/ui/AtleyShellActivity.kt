package com.vagell.kv4pht.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.vagell.kv4pht.data.AppDatabase
import com.vagell.kv4pht.data.AppSetting
import com.vagell.kv4pht.radio.Kv4pDriver
import com.vagell.kv4pht.radio.RadioAudioService
import com.vagell.kv4pht.radio.RadioConnectionState
import com.vagell.kv4pht.radio.RadioTrace
import com.vagell.kv4pht.radio.RadioUiState
import java.util.concurrent.Executors

class AtleyShellActivity : ComponentActivity() {
    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()
    private val trace = RadioTrace()
    private var radio: RadioAudioService? = null
    private var driver: Kv4pDriver? = null
    private var bound = false

    var ui by mutableStateOf(RadioUiState.from(RadioConnectionState.DISCONNECTED, false, false))
    var frequency by mutableStateOf("146.5200")
    var callsign by mutableStateOf("")
    var squelch by mutableIntStateOf(0)
    var volume by mutableIntStateOf(0)
    var volumeMax by mutableIntStateOf(15)
    var meter by mutableIntStateOf(0)
    var destination by mutableStateOf(ShellDestination.RADIO)
    var traceText by mutableStateOf("No events yet.")

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            startRadio()
        } else {
            note("permission", "denied")
        }
    }

    private val connection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName, service: android.os.IBinder) {
            val boundRadio = (service as RadioAudioService.RadioBinder).service
            radio = boundRadio
            driver = Kv4pDriver(boundRadio)
            bound = true
            boundRadio.setCallbacks(callbacks)
            boundRadio.start()
            frequency = driver?.frequency() ?: frequency
            refreshFromRadio()
            note("connection", "service-bound")
        }

        override fun onServiceDisconnected(name: android.content.ComponentName) {
            bound = false
            radio = null
            driver = null
            ui = RadioUiState.from(RadioConnectionState.DISCONNECTED, false, false)
            note("usb-detach", "service-disconnected")
        }
    }

    private val callbacks = object : RadioAudioService.RadioAudioServiceCallbacks {
        override fun radioMissing() {
            main.post {
                ui = RadioUiState.from(RadioConnectionState.DISCONNECTED, false, false)
                note("connection", "radio-missing")
            }
        }

        override fun radioConnected() {
            main.post {
                driver?.tune(frequency)
                refreshFromRadio()
                note("connection", "radio-connected")
            }
        }

        override fun connectionStateChanged(snapshot: com.vagell.kv4pht.radio.RadioConnectionSnapshot) {
            main.post {
                ui = RadioUiState.from(snapshot.state, driver?.squelchOpen() == true, driver?.txAllowed() == true)
                note("connection", snapshot.state.name)
            }
        }

        override fun tunedToFreq(frequencyStr: String) {
            main.post {
                frequency = frequencyStr
                note("tune", "ok")
            }
        }

        override fun txStarted() {
            main.post {
                ui = RadioUiState.from(RadioConnectionState.TRANSMITTING, false, true)
                note("tx-start", "voice")
            }
        }

        override fun txEnded() {
            main.post {
                refreshFromRadio()
                note("tx-stop", "voice")
            }
        }

        override fun moduleStateChanged(txActive: Boolean, squelched: Boolean) {
            main.post {
                meter = driver?.meter() ?: 0
                if (txActive) {
                    ui = RadioUiState.from(RadioConnectionState.TRANSMITTING, !squelched, true)
                }
            }
        }

        override fun sMeterUpdate(value: Int) {
            main.post { meter = value }
        }

        override fun outdatedFirmware(firmwareVer: Int) {
            main.post {
                ui = RadioUiState.from(RadioConnectionState.FIRMWARE_INCOMPATIBLE, false, false)
                note("firmware", "mismatch")
            }
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == ACTION_USB_PERMISSION && !intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                radio?.onUsbPermissionDenied()
                note("permission", "usb-denied")
                return
            }
            if (action == ACTION_USB_PERMISSION || action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                note("discovery", "usb-attached")
                radio?.reconnectViaUSB()
            } else if (action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                note("usb-detach", "device-removed")
                radio?.setScanning(false, true)
                radio?.forceUnkey()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Appearance.apply(this)
        super.onCreate(savedInstanceState)
        val audio = getSystemService(AudioManager::class.java)
        volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        io.execute {
            val stored = AppDatabase.getInstance(this).appSettingDao().getByName(AppSetting.SETTING_CALLSIGN)
            val sign = stored?.value ?: ""
            main.post { callsign = sign }
        }
        setContent {
            AtleyRadioScreen(
                ui = ui,
                frequency = frequency,
                callsign = callsign,
                squelch = squelch,
                volume = volume,
                volumeMax = volumeMax,
                meter = meter,
                destination = destination,
                traceText = traceText,
                onDestination = { destination = it },
                onDigit = { index -> stepDigit(index) },
                onSquelch = { level ->
                    squelch = level
                    driver?.setSquelch(level)
                    note("squelch", level.toString())
                },
                onVolume = { level ->
                    volume = level
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
                    note("audio-route", "phone-volume")
                },
                onPttDown = {
                    note("tx-request", "voice")
                    driver?.holdPtt()
                },
                onPttUp = { driver?.releasePtt() },
                onUnkey = {
                    driver?.forceUnkey()
                    note("forced-unkey", "operator")
                },
                onScan = {
                    if (driver?.scanning() == true) {
                        driver?.stopScan()
                        note("rx-stop", "scan")
                    } else {
                        driver?.startScan()
                        note("rx-start", "scan")
                    }
                    refreshFromRadio()
                },
                onLegacy = { startActivity(Intent(this, MainActivity::class.java)) },
                onExport = { shareTrace() }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        ContextCompat.registerReceiver(this, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        ensurePermissions()
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(usbReceiver)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        io.shutdown()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        super.onDestroy()
    }

    private fun ensurePermissions() {
        val needed = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            needed.add(Manifest.permission.BLUETOOTH_SCAN)
            needed.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            startRadio()
        } else {
            note("permission", "request")
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startRadio() {
        if (bound) {
            return
        }
        val svc = Intent(this, RadioAudioService::class.java)
            .putExtra(AppSetting.SETTING_CALLSIGN, callsign)
            .putExtra("activeMemoryId", -1)
            .putExtra("activeFrequencyStr", frequency)
            .putExtra("squelch", squelch)
        startForegroundService(svc)
        bindService(svc, connection, Context.BIND_AUTO_CREATE)
    }

    private fun stepDigit(index: Int) {
        val chars = frequency.padEnd(8, '0').toCharArray()
        if (index !in chars.indices || !chars[index].isDigit()) {
            return
        }
        chars[index] = ('0'.code + ((chars[index].code - '0'.code + 1) % 10)).toChar()
        frequency = String(chars)
        driver?.tune(frequency)
        note("tune", "digit")
    }

    private fun refreshFromRadio() {
        val current = radio ?: return
        val face = driver ?: return
        val open = face.squelchOpen()
        val allowed = current.isTxAllowed
        ui = when {
            face.transmitting() -> RadioUiState.from(RadioConnectionState.TRANSMITTING, open, allowed)
            face.scanning() -> RadioUiState.from(RadioConnectionState.SCANNING, open, allowed)
            current.isRadioConnected && open -> RadioUiState.from(RadioConnectionState.RECEIVING, open, allowed)
            current.isRadioConnected -> RadioUiState.from(RadioConnectionState.CONNECTED_IDLE, open, allowed)
            else -> RadioUiState.from(current.getConnectionSnapshot().state, open, allowed)
        }
        meter = driver?.meter() ?: meter
    }

    private fun note(kind: String, detail: String) {
        trace.add(kind, detail)
        traceText = trace.exportText()
    }

    private fun shareTrace() {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "Atley HT radio trace")
            .putExtra(Intent.EXTRA_TEXT, trace.exportText())
        startActivity(Intent.createChooser(send, "Export trace"))
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.vagell.kv4pht.USB_PERMISSION"
    }
}
