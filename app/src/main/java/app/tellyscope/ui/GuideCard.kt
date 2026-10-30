// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.tellyscope.adb.tr

/**
 * How to switch on debugging, per brand, because the menu names differ. [pair] is where the
 * pairing code lives on TVs with Wireless debugging; null where the brand only has the older switch.
 * `**…**` marks the words to find on the TV. [name] stays in English so a saved choice still matches.
 */
class TvBrand(
    val name: String,
    private val unlockEn: String,
    private val unlockZh: String,
    private val debugTitleEn: String,
    private val debugTitleZh: String,
    private val debugEn: String,
    private val debugZh: String,
    private val pairEn: String?,
    private val pairZh: String?,
    private val debugOnEn: String,
    private val debugOnZh: String,
) {
    val unlock get() = tr(unlockEn, unlockZh)
    val debugTitle get() = tr(debugTitleEn, debugTitleZh)
    val debug get() = tr(debugEn, debugZh)
    val pair: String? get() = if (pairEn == null) null else tr(pairEn, pairZh ?: pairEn)
    val debugOn get() = tr(debugOnEn, debugOnZh)
    /** Chip text. Product names stay; only Other is translated. */
    val label get() = if (name == "Other") tr("Other", "其他") else name
}

private const val ANDROID_TV_PAIR = "**Device Preferences → Developer options → Wireless debugging → Pair device with pairing code**"
private const val ANDROID_TV_PAIR_ZH = "**设备偏好设置 → 开发者选项 → 无线调试 → 使用配对码配对设备**"

private fun brand(
    name: String,
    unlockEn: String,
    unlockZh: String,
    debugTitleEn: String,
    debugTitleZh: String,
    debugEn: String,
    debugZh: String,
    pairEn: String?,
    pairZh: String?,
    debugOnEn: String,
    debugOnZh: String,
) = TvBrand(name, unlockEn, unlockZh, debugTitleEn, debugTitleZh, debugEn, debugZh, pairEn, pairZh, debugOnEn, debugOnZh)

val brands = listOf(
    brand(
        "Google TV",
        unlockEn = "On the TV: **Settings → System → About**. Press OK on **Android TV OS build** 7 times, until it says you're a developer.",
        unlockZh = "在电视上打开：**设置 → 系统 → 关于**。在 **Android TV 操作系统版本** 上按确定键 7 次，直到提示你已成为开发者。",
        debugTitleEn = "Turn on Wireless debugging",
        debugTitleZh = "打开无线调试",
        debugEn = "Back to **System → Developer options**. Turn on **Wireless debugging**. Older TVs call it **USB debugging**.",
        debugZh = "返回 **系统 → 开发者选项**。打开 **无线调试**。较旧的电视把它叫做 **USB 调试**。",
        pairEn = "**System → Developer options → Wireless debugging → Pair device with pairing code**",
        pairZh = "**系统 → 开发者选项 → 无线调试 → 使用配对码配对设备**",
        debugOnEn = "Wireless debugging on",
        debugOnZh = "无线调试已打开",
    ),
    brand(
        "Android TV",
        unlockEn = "On the TV: **Settings → Device Preferences → About**. Press OK on **Build** 7 times.",
        unlockZh = "在电视上打开：**设置 → 设备偏好设置 → 关于**。在 **版本号** 上按确定键 7 次。",
        debugTitleEn = "Turn on Wireless debugging",
        debugTitleZh = "打开无线调试",
        debugEn = "Back to **Device Preferences → Developer options**. Turn on **Wireless debugging**. Older TVs have **USB debugging** and **Network debugging** instead: turn on both.",
        debugZh = "返回 **设备偏好设置 → 开发者选项**。打开 **无线调试**。较旧的电视没有这一项，而是 **USB 调试** 和 **网络调试**：两个都打开。",
        pairEn = ANDROID_TV_PAIR,
        pairZh = ANDROID_TV_PAIR_ZH,
        debugOnEn = "Wireless debugging on",
        debugOnZh = "无线调试已打开",
    ),
    brand(
        "Fire TV",
        unlockEn = "On the TV: **Settings → My Fire TV → About**. Press OK on the first row, your Fire TV's name, 7 times.",
        unlockZh = "在电视上打开：**设置 → 我的 Fire TV → 关于**。在第一行（这台 Fire TV 的名称）上按确定键 7 次。",
        debugTitleEn = "Turn on ADB debugging",
        debugTitleZh = "打开 ADB 调试",
        debugEn = "Back to **My Fire TV → Developer options**. Turn on **ADB debugging**.",
        debugZh = "返回 **我的 Fire TV → 开发者选项**。打开 **ADB 调试**。",
        pairEn = null,
        pairZh = null,
        debugOnEn = "ADB debugging on",
        debugOnZh = "ADB 调试已打开",
    ),
    brand(
        "Xiaomi",
        unlockEn = "On the TV: **Settings → Device Preferences → About**. Press OK on **Build** 7 times.",
        unlockZh = "在电视上打开：**设置 → 设备偏好设置 → 关于**。在 **版本号** 上按确定键 7 次。",
        debugTitleEn = "Turn on Wireless debugging",
        debugTitleZh = "打开无线调试",
        debugEn = "Back to **Device Preferences → Developer options**. Turn on **Wireless debugging**. Older TVs call it **USB debugging**.",
        debugZh = "返回 **设备偏好设置 → 开发者选项**。打开 **无线调试**。较旧的电视把它叫做 **USB 调试**。",
        pairEn = ANDROID_TV_PAIR,
        pairZh = ANDROID_TV_PAIR_ZH,
        debugOnEn = "Wireless debugging on",
        debugOnZh = "无线调试已打开",
    ),
    brand(
        "XGIMI",
        unlockEn = "On the TV: **Settings → Device Preferences → About**. Press OK on **Build** 7 times.",
        unlockZh = "在电视上打开：**设置 → 设备偏好设置 → 关于**。在 **版本号** 上按确定键 7 次。",
        debugTitleEn = "Turn on Wireless debugging",
        debugTitleZh = "打开无线调试",
        debugEn = "Back to **Device Preferences → Developer options**. Turn on **Wireless debugging**. Older projectors have **USB debugging**, then **Network debugging** if it's listed: turn on both.",
        debugZh = "返回 **设备偏好设置 → 开发者选项**。打开 **无线调试**。较旧的投影仪先打开 **USB 调试**，如果列出了 **网络调试** 也一并打开。",
        pairEn = ANDROID_TV_PAIR,
        pairZh = ANDROID_TV_PAIR_ZH,
        debugOnEn = "Wireless debugging on",
        debugOnZh = "无线调试已打开",
    ),
    brand(
        "Other",
        unlockEn = "On the TV, open **Settings**. Has **System**? It's Google TV: go to **System → About** and press OK on **Android TV OS build** 7 times. Has **Device Preferences**? It's Android TV: go to **Device Preferences → About** and press OK on **Build** 7 times.",
        unlockZh = "在电视上打开 **设置**。有 **系统**？这是 Google TV：进入 **系统 → 关于**，在 **Android TV 操作系统版本** 上按确定键 7 次。有 **设备偏好设置**？这是 Android TV：进入 **设备偏好设置 → 关于**，在 **版本号** 上按确定键 7 次。",
        debugTitleEn = "Turn on Wireless debugging",
        debugTitleZh = "打开无线调试",
        debugEn = "Back one level, open **Developer options**. Turn on **Wireless debugging**. Older TVs have **USB debugging**, and some **Network debugging** too: turn on what's there.",
        debugZh = "返回上一级，打开 **开发者选项**。打开 **无线调试**。较旧的电视是 **USB 调试**，有的还有 **网络调试**：把能看到的都打开。",
        pairEn = "**Developer options → Wireless debugging → Pair device with pairing code**",
        pairZh = "**开发者选项 → 无线调试 → 使用配对码配对设备**",
        debugOnEn = "Wireless debugging on",
        debugOnZh = "无线调试已打开",
    ),
)

/** The brand chips: every brand stays visible, so nobody has to guess what a menu hides. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrandChips(brand: TvBrand, onPick: (TvBrand) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tr("Pick your TV", "选择你的电视"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            brands.forEach { b ->
                FilterChip(
                    selected = b == brand,
                    onClick = { onPick(b) },
                    label = { Text(b.label) },
                    leadingIcon = if (b == brand) ({ Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }) else null,
                )
            }
        }
    }
}

/**
 * One step open at a time: "Done, next" ticks it off and opens the next, "Back" reopens the one
 * before. The last step holds [findTvs], which is the live scan and whatever it found.
 */
@Composable
fun GuideSteps(brand: TvBrand, step: Int, onStep: (Int) -> Unit, findTvs: @Composable () -> Unit) {
    val steps = listOf(
        Triple(tr("Unlock developer options", "解锁开发者选项"), brand.unlock, tr("Developer options unlocked", "开发者选项已解锁")),
        Triple(brand.debugTitle, brand.debug, brand.debugOn),
        Triple(tr("Find your TV", "寻找你的电视"), "", ""),
    )
    Column(Modifier.animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())) {
        steps.forEachIndexed { i, (title, body, done) ->
            val last = i == steps.lastIndex
            Row(Modifier.height(IntrinsicSize.Min)) {
                Column(Modifier.width(32.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    StepDot(i + 1, done = i < step, current = i == step)
                    if (!last) Box(Modifier.padding(vertical = 4.dp).width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outlineVariant))
                }
                Column(
                    Modifier.padding(start = 16.dp, top = 4.dp, bottom = if (last) 8.dp else 24.dp).weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (i > step) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    )
                    when {
                        i < step -> Text(done, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        i == step && last -> findTvs()
                        i == step -> {
                            Text(marked(body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onStep(i + 1) }) { Text(tr("Done, next", "完成，下一步")) }
                                if (i > 0) TextButton(onClick = { onStep(i - 1) }) { Text(tr("Back", "返回")) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDot(n: Int, done: Boolean, current: Boolean) {
    val c = MaterialTheme.colorScheme
    val filled = done || current
    Box(
        Modifier.size(32.dp)
            .background(if (filled) c.primary else c.surface, CircleShape)
            .border(2.dp, if (filled) c.primary else c.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(Icons.Default.Check, contentDescription = tr("Done", "完成"), Modifier.size(18.dp), tint = c.onPrimary)
        else Text("$n", style = MaterialTheme.typography.labelLarge, color = if (current) c.onPrimary else c.outline)
    }
}

/** "Can't find your TV?": the fixes that used to sit under the guide as small print. */
@Composable
fun HelpDialog(brand: TvBrand, onAddByIp: () -> Unit, onDismiss: () -> Unit) {
    val tips = buildList {
        add(tr(
            "Keep the phone and the TV on the same Wi-Fi. Guest networks often keep devices apart.",
            "手机和电视要连同一个 Wi-Fi。访客网络常常会把设备隔开。",
        ))
        add(tr(
            "Debugging switched itself off? Some TVs reset it after a restart. Turn it back on and this screen finds the TV again.",
            "调试自己关掉了？有的电视重启后会重置。重新打开后，这个页面会再次找到电视。",
        ))
        if (brand.name == "Fire TV") add(tr(
            "No Developer options after 7 presses? Newer Fire TVs run Vega OS, which isn't Android, and Tellyscope can't reach them.",
            "按了 7 次还是没有开发者选项？较新的 Fire TV 运行的是 Vega OS，它不是 Android，Tellyscope 连不上。",
        ))
        add(tr(
            "Still nothing? Add the TV by its IP address, from the TV's network settings.",
            "还是找不到？用电视网络设置里的 IP 地址手动添加。",
        ))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Can't find your TV?", "找不到你的电视？")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tips.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        },
        confirmButton = { Button(onClick = { onDismiss(); onAddByIp() }) { Text(tr("Add by IP address", "用 IP 地址添加")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Close", "关闭")) } },
    )
}

/** Turns `**words**` into medium-weight spans: the menu names to look for on the TV. */
@Composable
fun marked(text: String): AnnotatedString {
    val strong = SpanStyle(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    return buildAnnotatedString {
        text.split("**").forEachIndexed { i, part -> if (i % 2 == 1) withStyle(strong) { append(part) } else append(part) }
    }
}
