// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope

import app.tellyscope.adb.simplifiedChinese
import app.tellyscope.ui.Copy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class CopyTest {
    private val saved = Locale.getDefault()

    @Before fun english() = Locale.setDefault(Locale.US)

    @After fun restore() = Locale.setDefault(saved)

    @Test fun upForSpellsOutTheLargestUnits() {
        assertEquals("3 days, 4 hours", Copy.upFor(3 * 86_400L + 4 * 3_600 + 59))
        assertEquals("1 day", Copy.upFor(86_400L + 120))
        assertEquals("1 hour", Copy.upFor(3_600L + 300))
        assertEquals("12 minutes", Copy.upFor(12 * 60L))
    }

    @Test fun speedUsesTheSameUnitsAsSizes() {
        assertEquals("2.3 MB/s", Copy.speed(2_400_000))
        assertEquals("12 KB/s", Copy.speed(12_500))
        assertEquals("300 bytes/s", Copy.speed(300))
    }

    @Test fun simplifiedChineseFollowsZhAndZhCnOnly() {
        assertTrue(simplifiedChinese(Locale.CHINESE))
        assertTrue(simplifiedChinese(Locale.SIMPLIFIED_CHINESE))
        assertTrue(simplifiedChinese(Locale.forLanguageTag("zh-Hans")))
        assertTrue(simplifiedChinese(Locale.forLanguageTag("zh-SG")))
        assertFalse(simplifiedChinese(Locale.TRADITIONAL_CHINESE))
        assertFalse(simplifiedChinese(Locale.forLanguageTag("zh-HK")))
        assertFalse(simplifiedChinese(Locale.forLanguageTag("zh-Hant")))
        assertFalse(simplifiedChinese(Locale.US))
    }

    @Test fun chineseCopyCoversButtonsTimeAndSafety() {
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE)
        assertEquals("3 天 4 小时", Copy.upFor(3 * 86_400L + 4 * 3_600 + 59))
        assertEquals("1 天", Copy.upFor(86_400L))
        assertEquals("12 分钟", Copy.upFor(12 * 60L))
        assertEquals("300 字节/秒", Copy.speed(300))
        assertEquals("2.3 MB/秒", Copy.speed(2_400_000))
        assertEquals("停止", Copy.button(app.tellyscope.data.Action.Stop))
        assertEquals("确定停用", Copy.confirm(app.tellyscope.data.Action.Disable))
        assertEquals("停用", Copy.button(app.tellyscope.data.Action.Disable))
        assertTrue(Copy.explain(app.tellyscope.data.Action.Uninstall, "Kodi", "org.xbmc.kodi").contains("无法撤销"))
        assertEquals(
            "存储空间几乎已满",
            Copy.health(
                app.tellyscope.data.Snapshot(
                    at = 0, cpuPercent = 0, ramUsedMb = 100, ramTotalMb = 1000,
                    storageUsedGb = 9.5, storageTotalGb = 10.0,
                    uptimeSeconds = 60, screenOn = true, procs = emptyList(),
                ),
                emptyList(),
            ).text,
        )
        Locale.setDefault(Locale.TRADITIONAL_CHINESE)
        assertEquals("Stop", Copy.button(app.tellyscope.data.Action.Stop))
        assertEquals("3 days, 4 hours", Copy.upFor(3 * 86_400L + 4 * 3_600 + 59))
    }
}
