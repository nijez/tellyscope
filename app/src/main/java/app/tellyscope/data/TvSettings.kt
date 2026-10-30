// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.data

import app.tellyscope.adb.Tweak
import app.tellyscope.adb.tr

/** What each TV tweak writes, and how History names it afterwards. */
object TvSettings {
    /** AdGuard's public ad-blocking resolver: what the ad-blocking switch offers first. */
    const val ADGUARD = "dns.adguard-dns.com"
    const val FAST = "0.5"
    /** History's name for a cache clear: "Cleared app caches". */
    const val CACHES = "app caches"
    /** Android 9, the first with Private DNS. */
    const val DNS_SDK = 28

    /** Off goes back to Automatic, Android's default, rather than turning encrypted DNS off. */
    fun dns(host: String?): Map<String, String?> =
        if (host == null) mapOf("private_dns_mode" to "opportunistic")
        else mapOf("private_dns_specifier" to host, "private_dns_mode" to "hostname")

    fun animations(fast: Boolean): Map<String, String?> = Tweak.ANIMATIONS.keys.associateWith { if (fast) FAST else "1.0" }

    fun tweaks(dns: Map<String, String?>, animations: Map<String, String?>, sdk: Int?) = TvTweaks(
        dnsHost = dns["private_dns_specifier"]?.takeIf { dns["private_dns_mode"] == "hostname" },
        animationScale = animations["window_animation_scale"],
        // A TV that doesn't say is let through: the lookup check after the change still guards it.
        dnsSupported = sdk == null || sdk >= DNS_SDK,
    )

    /** History's line for a change that left the TV at [values]. */
    fun label(tweak: Tweak, values: Map<String, String?>): String = when (tweak) {
        Tweak.PRIVATE_DNS -> when (values["private_dns_mode"]) {
            "hostname" -> tr(
                "Private DNS set to ${values["private_dns_specifier"]}",
                "私人 DNS 已设为 ${values["private_dns_specifier"]}",
            )
            "off" -> tr("Private DNS turned off", "私人 DNS 已关闭")
            else -> tr("Private DNS set to Automatic", "私人 DNS 已设为自动")
        }
        Tweak.ANIMATIONS -> when (val v = values["window_animation_scale"]?.toFloatOrNull()) {
            null, 1f -> tr("Animations at normal speed", "动画为正常速度")
            0f -> tr("Animations turned off", "动画已关闭")
            else -> tr(
                "Animations at ${v.toString().removeSuffix(".0")}×",
                "动画速度为 ${v.toString().removeSuffix(".0")}×",
            )
        }
    }

    val DNS_TOO_OLD get() = tr(
        "Private DNS needs Android 9 or newer, and this TV is older",
        "私人 DNS 需要 Android 9 或更高版本，这台电视更旧",
    )

    fun dnsUnreachable(host: String?) = tr(
        "the TV couldn't look anything up through $host, so Private DNS was put back. The network may block encrypted DNS (port 853)",
        "电视无法通过 $host 解析任何地址，因此已把私人 DNS 改回去。网络可能拦截了加密 DNS（端口 853）",
    )

    fun dnsStuck(host: String?) = tr(
        "the TV couldn't look anything up through $host, and putting the old Private DNS back failed. " +
            "The TV may be offline until you undo it in History",
        "电视无法通过 $host 解析任何地址，而且把原来的私人 DNS 改回去也失败了。" +
            "在你于历史记录里撤销之前，电视可能处于离线",
    )
}
