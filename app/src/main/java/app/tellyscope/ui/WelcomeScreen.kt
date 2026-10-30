// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.tellyscope.R
import app.tellyscope.adb.tr
import app.tellyscope.ui.theme.SmallNumber

/**
 * Shown once, before the first scan. Skip and Continue both move on: Skip only passes over
 * the reading. [askLocalNetwork] adds the line about the Android 17+ permission that follows.
 */
@Composable
fun WelcomeScreen(askLocalNetwork: Boolean, onContinue: () -> Unit, onSkip: () -> Unit) {
    Scaffold(
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onSkip) { Text(tr("Skip", "跳过")) }
                Button(onClick = onContinue) { Text(tr("Continue", "继续")) }
            }
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // The launcher icon's own teal in both themes, like the home screen and the splash.
            Box(
                Modifier.size(72.dp).clip(MaterialTheme.shapes.extraLarge).background(colorResource(R.color.splash_icon_background)),
                contentAlignment = Alignment.Center,
            ) {
                // The foreground keeps its art inside the adaptive-icon safe zone; oversize it so the art fills the tile.
                Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, Modifier.requiredSize(108.dp))
            }
            Text(tr("Welcome to Tellyscope", "欢迎使用 Tellyscope"), style = MaterialTheme.typography.headlineMedium)
            Text(
                tr(
                    "See what's running on your Android TV, and turn off the apps you don't need. All from your phone, over Wi-Fi.",
                    "查看 Android 电视上正在运行的内容，并关掉你不需要的应用。全部在手机上通过 Wi-Fi 完成。",
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    steps.forEachIndexed { i, step ->
                        Row {
                            Text("${i + 1}", Modifier.alignByBaseline(), style = SmallNumber, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(step, Modifier.alignByBaseline(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            if (askLocalNetwork) {
                Text(
                    tr(
                        tr("Android asks before an app talks to other devices on your Wi-Fi. Tellyscope only connects to the TVs you pick.", "应用要和其他 Wi-Fi 设备通信时，Android 会先询问。Tellyscope 只连接你选中的电视。"),
                        "应用要和其他 Wi-Fi 设备通信时，Android 会先询问。Tellyscope 只连接你选中的电视。",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val steps get() = listOf(
    tr("Turn on debugging on your TV. The next screen shows how for your brand.", "在电视上打开调试。下一页会按品牌说明怎么做。"),
    tr("Tellyscope finds the TV on your Wi-Fi.", "Tellyscope 会在 Wi-Fi 上找到电视。"),
    tr("Type the code the TV shows, or choose Allow when it asks.", "输入电视上显示的配对码，或在它询问时选择允许。"),
)
