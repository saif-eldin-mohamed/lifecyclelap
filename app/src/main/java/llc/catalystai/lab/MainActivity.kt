package llc.catalystai.lab

import android.content.Context
import android.os.Bundle
import android.os.Process
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


private val Bg = Color(0xFF0C0F30)
private val PanelBg = Color(0xFF0F1230)
private val PanelHiBg = Color(0xFF141845)
private val Border = Color(0xFF1E2670)
private val Accent = Color(0xFF3B4FE8)
private val AccentLt = Color(0xFF8E9BFF)
private val BodyText = Color(0xFFA6ABD1)
private val Muted = Color(0xFF6B70A0)
private val Danger = Color(0xFFFF5C7A)
private val Ok = Color(0xFF3ED9A4)
private val Amber = Color(0xFFF5B84C)


object Lab {
    var singletonCount by mutableStateOf(0)
    val log = mutableStateListOf<String>()

    fun record(event: String) {
        log.add(String.format(Locale.ROOT, "%-22s pid %d", event, Process.myPid()))

        if (log.size > 40) log.removeAt(0)
    }
}

 class LabViewModel : ViewModel() {
    var count by mutableStateOf(0)
}


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Lab.record(if (savedInstanceState == null) "onCreate  (fresh)" else "onCreate  (RESTORED)")
        setContent { LabTheme { LabScreen() } }
    }

    override fun onStart() {
        super.onStart()
        Lab.record("onStart")
    }

    override fun onResume() {
        super.onResume()
        Lab.record("onResume")
    }

    override fun onPause() {
        Lab.record("onPause")
        super.onPause()
    }

    override fun onStop() {
        Lab.record("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Lab.record("onDestroy")
        super.onDestroy()
    }
}

@Composable
private fun LabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Color.White,
            background = Bg,
            surface = PanelBg,
            onSurface = Color.White,
        ),
        content = content,
    )
}


@Composable
fun LabScreen(vm: LabViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lab", Context.MODE_PRIVATE) }

    // 1. composition-scoped: dies whenever this composable leaves, including a rotation
    var inComposition by remember { mutableStateOf(0) }

    // 2. saved into the Activity's saved-state bundle. Survives rotation and a SYSTEM kill
    //    (am kill / low memory). A user-initiated stop — force-stop, or swiping the task
    //    away — discards the bundle, and this reads 0 again. That distinction is the lab.
    var inSavedState by rememberSaveable { mutableStateOf(0) }

    // 3. on disk: survives process death and force-stop. Cleared on uninstall / Clear Data.
    var onDisk by remember { mutableStateOf(prefs.getInt("count", 0)) }

    // A clock driven from the main thread. When the main thread is blocked, this freezes —
    // which is the whole of the main-loop lesson, visible.
    // Driven by frames, not by counting ticks. Two reasons: counting ticks silently loses
    // the time you spend blocked (so the clock would end up permanently wrong), and a
    // frame-driven clock freezes exactly when frames stop — which is the thing we want to show.
    val startNanos = remember { System.nanoTime() }
    var tenths by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { now -> tenths = ((now - startNanos) / 100_000_000L).toInt() }
        }
    }
    var lastClickThread by remember { mutableStateOf("—") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            // targetSdk 35 is edge-to-edge by default: without this the header draws
            // under the status bar and the last paragraph under the gesture pill.
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 22.dp),
    ) {
        Text(
            "LIFECYCLE LAB",
            color = Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(10.dp))

        /* -------- identity: the PID is the proof of who you are -------- */
        LabPanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Label("THIS PROCESS")
                    Text(
                        "pid ${Process.myPid()}",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "when this number changes, you died",
                        color = Muted,
                        fontSize = 11.sp,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Label("MAIN THREAD CLOCK")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(
                                    if (tenths % 10 < 5) Ok else Border,
                                    CircleShape,
                                ),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            String.format(Locale.ROOT, "%.1fs", tenths / 10f),
                            color = Ok,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    Text("stops if the main thread stops", color = Muted, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Label("THE SAME NUMBER, KEPT IN FIVE PLACES")
        Spacer(Modifier.height(8.dp))

        CounterRow("remember { }", inComposition, "dies on rotation", Danger)
        CounterRow("ViewModel field", vm.count, "dies with the process", Amber)
        CounterRow("object Lab (singleton)", Lab.singletonCount, "dies with the process", Amber)
        CounterRow("rememberSaveable { }", inSavedState, "survives a system kill", Ok)
        CounterRow("SharedPreferences", onDisk, "survives until uninstall", Ok)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                inComposition++
                vm.count++
                Lab.singletonCount++
                inSavedState++
                onDisk++
                prefs.edit().putInt("count", onDisk).apply()
                lastClickThread = Thread.currentThread().name
            },
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("+1 to all five", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                lastClickThread = Thread.currentThread().name
                // Deliberately illegal. This is the demo: hold the main thread and watch
                // the clock above stop, then watch every queued tap arrive at once.
                Thread.sleep(3000)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PanelHiBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "block the main thread for 3s",
                color = Danger,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "last click ran on: $lastClickThread",
            color = Muted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
        )

        Spacer(Modifier.height(16.dp))
        Label("LIFECYCLE LOG  ·  newest last")
        Spacer(Modifier.height(8.dp))
        LabPanel {
            Column {
                if (Lab.log.isEmpty()) {
                    Text("—", color = Muted, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Lab.log.forEach { line ->
                    Text(
                        line,
                        color = when {
                            "RESTORED" in line -> Amber
                            "onDestroy" in line -> Danger
                            "fresh" in line -> AccentLt
                            else -> BodyText
                        },
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "The log lives in the singleton — so it survives the screen being recreated, "
                + "and vanishes when the process dies.",
            color = Muted,
            fontSize = 11.5.sp,
        )
    }
}

/* ------------------------------------------------------------------ pieces */

@Composable
private fun LabPanel(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(PanelBg, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) { content() }
}

@Composable
private fun Label(text: String) {
    Text(text, color = Muted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
}

@Composable
private fun CounterRow(name: String, value: Int, fate: String, fateColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(PanelBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                name,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
            )
            Text(fate, color = fateColor, fontSize = 11.sp)
        }
        Text(
            value.toString(),
            color = fateColor,
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
    }
}
