// SPDX-License-Identifier: GPL-3.0-only

package app.tellyscope.ui

import app.tellyscope.adb.InstallKind
import app.tellyscope.adb.InstallRefusal
import app.tellyscope.adb.Peripheral
import app.tellyscope.adb.TvFolder
import app.tellyscope.adb.simplifiedChinese
import app.tellyscope.adb.tr
import app.tellyscope.data.Action
import app.tellyscope.data.AppNames
import app.tellyscope.data.DeviceState
import app.tellyscope.data.ResetPart
import app.tellyscope.data.ResetProgress
import app.tellyscope.data.Snapshot
import app.tellyscope.data.TvSettings
import app.tellyscope.data.sourceName
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

/**
 * Every user-facing sentence about actions and time lives here so wording is
 * reviewed in one place: every button says what it does before it runs.
 */
object Copy {
    fun button(action: Action) = when (action) {
        Action.Stop -> tr("Stop", "停止")
        Action.Disable -> tr("Disable", "停用")
        Action.Enable -> tr("Turn on", "启用")
        Action.Open -> tr("Open", "打开")
        Action.Uninstall -> tr("Uninstall", "卸载")
        Action.Change, Action.ClearCaches, Action.Home, Action.HomeBack, Action.Reset -> error("$action has its own button")
    }

    fun confirm(action: Action) = when (action) {
        Action.Stop -> tr("Yes, stop it", "确定停止")
        Action.Disable -> tr("Yes, disable it", "确定停用")
        Action.Enable -> tr("Yes, turn it on", "确定启用")
        Action.Open -> tr("Yes, open it", "确定打开")
        Action.Uninstall -> tr("Yes, uninstall it", "确定卸载")
        Action.Home -> tr("Yes, switch home screen", "确定切换主屏幕")
        Action.HomeBack -> tr("Yes, switch back", "确定切回去")
        Action.Change, Action.ClearCaches, Action.Reset -> error("$action isn't an app action")
    }

    /**
     * Names the package next to the label: the label comes from the app itself, and any app can
     * call itself "Settings". The package id is what actually gets changed.
     */
    fun explain(action: Action, app: String, pkg: String) = "$app ($pkg)".let { who ->
        when (action) {
            Action.Stop -> tr("Stops $who until you open it again.", "停止 $who，直到你再次打开它。")
            Action.Disable -> tr(
                "Hides $who and stops it running. Nothing is deleted; you can turn it back on here.",
                "隐藏 $who 并停止运行。不会删除任何内容，可以在这里重新启用。",
            )
            Action.Enable -> tr("Turns $who back on.", "重新启用 $who。")
            Action.Open -> tr("Opens $who on the TV.", "在电视上打开 $who。")
            Action.Uninstall -> tr(
                "Removes $who and everything saved in it. This can't be undone; to get it back, install it again.",
                "移除 $who 及其保存的全部内容。此操作无法撤销；要恢复，需要重新安装。",
            )
            Action.Change, Action.ClearCaches, Action.Home, Action.HomeBack, Action.Reset -> error("$action isn't a one-app action")
        }
    }

    fun done(action: Action, app: String) = when (action) {
        Action.Stop -> tr("Stopped $app", "已停止 $app")
        Action.Disable -> tr("Disabled $app", "已停用 $app")
        Action.Enable -> tr("Turned on $app", "已启用 $app")
        Action.Open -> tr("Opened $app", "已打开 $app")
        Action.Uninstall -> tr("Uninstalled $app", "已卸载 $app")
        // A setting's label is the whole sentence: "Private DNS set to …", as is a reset's.
        Action.Change, Action.Reset -> app
        Action.ClearCaches -> tr("Cleared $app", "已清除 $app")
        Action.Home -> tr("Made $app Home", "已将 $app 设为主屏幕")
        Action.HomeBack -> tr("Switched home back to $app", "已将主屏幕切回 $app")
    }

    /** Said in the confirm before Stop, Disable or Uninstall of an app the TV's list keeps. */
    val KEEP_WARNING get() = tr("This TV's list says to keep this.", "这台电视的清单建议保留此项。")

    /** Where the lists come from, for the Debloat screen and the snackbar after a change. */
    fun listSource(custom: String?) =
        if (custom == null) tr("Lists from tellyscope-devices, built from cleanyourtv", "清单来自 tellyscope-devices，基于 cleanyourtv")
        else tr("Lists from ${sourceName(custom)}", "清单来自 ${sourceName(custom)}")

    /** The Home screen card at the top of Tweaks: every launcher on the TV, and which one Home opens. */
    val HOME_SCREEN get() = tr("Home screen", "主屏幕")

    fun opensOnHome(app: String) = tr("$app opens when you press Home.", "按主页键时打开 $app。")

    val NO_DEFAULT get() = tr("No default: pick one below.", "没有默认项：请在下面选一个。")
    val ADDED get() = tr("Added", "已添加")

    fun builtIn(off: Boolean) = tr("Built into the TV", "电视自带") + if (off) tr(" · turned off", " · 已关闭") else ""

    val CURRENT get() = tr("Current", "当前")
    val KEEPS_HOME get() = tr("Keeps Home with its accessibility service", "用无障碍服务保持主页键")
    val MAKE_HOME get() = tr("Make Home", "设为主屏幕")
    val GET_LAUNCHER get() = tr("Get another launcher", "换一个桌面")
    val GET_LAUNCHER_TEXT get() = tr(
        "Install one from the Play Store, like Projectivy or FLauncher. Switch back any time.",
        "从 Play Store 安装一个，例如 Projectivy 或 FLauncher。随时可以切回去。",
    )
    val GET_PROJECTIVY_TEXT get() = tr("Install Projectivy. Switch back any time.", "安装 Projectivy。随时可以切回去。")

    /** The Apps tab's launcher rows: switching lives in the card, this opens it. */
    val HOME_SETTINGS get() = tr("Home screen settings", "主屏幕设置")

    fun makeHomeTitle(app: String) = tr("Make $app the home screen?", "将 $app 设为主屏幕？")

    /** A launcher the user added in place of the TV's own, which is turned off: otherwise many TVs switch back by themselves. */
    fun explainReplace(app: String, current: String, currentPkg: String, service: Boolean) = if (simplifiedChinese()) buildString {
        append("主页键将打开 $app。为避免电视自己切回去，Tellyscope 会关闭电视自带的桌面 $current。")
        append("\n\n它只会关闭这一个桌面。你自己安装的桌面保持启用，随时可以互相切换。")
        append("\n\n在历史记录里撤销，会重新启用 $current。")
        if (currentPkg in GOOGLE_HOMES) append("\n\n部分 Google TV 功能（例如应用行和直播标签）属于 $current，会一起消失。")
        if (service) append("\n\n接下来，你可以选择是否打开它的无障碍服务。")
    } else buildString {
        append("Home opens $app. To stop the TV switching back by itself, Tellyscope turns off $current, the TV's built-in launcher.")
        append("\n\nThat's the only launcher it ever turns off. Launchers you added stay on, so you can switch between them any time.")
        append("\n\nUndo in History turns $current back on.")
        if (currentPkg in GOOGLE_HOMES) append("\n\nSome Google TV features, like the Apps row and the Live tab, are part of $current and go away with it.")
        if (service) append("\n\nNext, you choose whether to turn on its accessibility service.")
    }

    /**
     * Away from [current], a launcher the user added: it stays on, only its accessibility service
     * goes off when it has one on, since that would take Home straight back. [off]: [app] is the
     * TV's own launcher, turned off by an earlier switch.
     */
    fun explainLeave(app: String, off: Boolean, current: String, currentService: Boolean, service: Boolean) =
        if (simplifiedChinese()) buildString {
            append(if (off) "Tellyscope 会重新启用 $app，并让按主页键时打开它。" else "Tellyscope 会让按主页键时打开 $app。")
            append("\n\n")
            if (currentService) append("同时会关闭 $current 的无障碍服务，否则主页键会立刻切回去。")
            append("$current 仍会安装并保持启用，你可以随时再把它设为主屏幕。")
            append("\n\n在历史记录里撤销，会把 $current 重新设为主屏幕")
            append(if (currentService) "，并重新打开它的服务。" else "。")
            if (service) append("\n\n接下来，你可以选择是否打开它的无障碍服务。")
        } else buildString {
            append(if (off) "Tellyscope turns $app back on and makes it open when you press Home." else "Tellyscope makes $app open when you press Home.")
            append("\n\n")
            if (currentService) append("It also turns off $current's accessibility service, which would otherwise take Home straight back. ")
            append("$current stays installed and on, so you can make it Home again any time.")
            append("\n\nUndo in History makes $current Home again")
            append(if (currentService) " and turns its service back on." else ".")
            if (service) append("\n\nNext, you choose whether to turn on its accessibility service.")
        }

    /**
     * The home switch touches two apps, so it names both. The old launcher is disabled, not just
     * replaced: otherwise many TVs switch back to it by themselves.
     */
    fun explainHome(app: String, pkg: String, current: String, currentPkg: String, service: Boolean = false) =
        if (simplifiedChinese()) buildString {
            append("按主页键时打开 $app（$pkg），然后停用 $current（$currentPkg），这样电视不会自己切回去。")
            append("在历史记录里撤销，会重新启用 $current 并把它设回主屏幕。")
            if (currentPkg in GOOGLE_HOMES) append("\n\n部分 Google TV 功能（例如应用行和直播标签）属于 $current，会一起消失。")
            if (service) append("\n\n接下来，你可以选择是否打开它的无障碍服务。")
        } else buildString {
            append("Makes $app ($pkg) open when you press Home, then disables $current ($currentPkg) so the TV can't switch back by itself. ")
            append("Undo in History turns $current back on and makes it Home again.")
            if (currentPkg in GOOGLE_HOMES) append("\n\nSome Google TV features, like the Apps row and the Live tab, are part of $current and go away with it.")
            if (service) append("\n\nNext, you choose whether to turn on its accessibility service.")
        }

    /** Fire OS: Amazon's launcher can't be turned off or replaced, so [app]'s accessibility service holds Home instead. */
    fun explainFireHome(app: String, pkg: String, current: String) = tr(
        "Makes Home open $app ($pkg) through its accessibility service. Fire OS won't let $current be turned off or replaced, " +
            "so it stays on and nothing else changes. Undo in History turns the service off.\n\nNext, you choose whether to turn on its accessibility service.",
        "通过无障碍服务让主页键打开 $app（$pkg）。Fire OS 不允许关闭或替换 $current，" +
            "所以它保持启用，其他内容不变。在历史记录里撤销会关掉该服务。\n\n接下来，你可以选择是否打开它的无障碍服务。",
    )

    /** No default home and no launcher of the TV's own on: nothing is turned off, so Undo can only turn its service off. */
    fun explainFirstHome(app: String, pkg: String, service: Boolean = false) = if (simplifiedChinese()) buildString {
        append("按主页键时打开 $app（$pkg）。这台电视目前没有默认主屏幕，也不会关闭其他应用。")
        if (service) append("\n\n接下来，你可以选择是否打开它的无障碍服务。")
    } else buildString {
        append("Makes $app ($pkg) open when you press Home. The TV has no default home screen now, and nothing else is turned off.")
        if (service) append("\n\nNext, you choose whether to turn on its accessibility service.")
    }

    /** The separate yes for a launcher's accessibility service: what it does, what it can see, how it goes. */
    fun serviceTitle(app: String) = tr("Turn on $app's accessibility service?", "打开 $app 的无障碍服务？")

    fun explainService(app: String, other: String) = tr(
        "It keeps the Home button opening $app. Without it, this TV's Home button may still open $other.\n\n" +
            "An accessibility service can see what's on screen. Turn it on only if you trust $app.\n\n" +
            "Undo in History, or making another launcher Home, turns it off.",
        "它让主页键一直打开 $app。不打开的话，这台电视的主页键仍可能打开 $other。\n\n" +
            "无障碍服务能看到屏幕上的内容。只有在你信任 $app 时才打开。\n\n" +
            "在历史记录里撤销，或把另一个桌面设为主屏幕，都会关掉它。",
    )

    val TURN_IT_ON get() = tr("Turn it on", "打开它")
    val SWITCH_WITHOUT get() = tr("Switch without it", "不打开也切换")
    val NOT_NOW get() = tr("Not now", "暂时不要")
    val TURN_ON get() = tr("Turn on", "打开")
    val GOT_IT get() = tr("Got it", "知道了")
    val SERVICE_OFF get() = tr("Accessibility service off", "无障碍服务已关闭")

    /** The TV page's card while the launcher on Home has its service off; [other] is the launcher Home may still open. */
    fun serviceOffTitle(other: String?) = tr(
        "Home may open ${other ?: TV_OWN_LAUNCHER}",
        "主页键仍可能打开 ${other ?: TV_OWN_LAUNCHER}",
    )

    val TV_OWN_LAUNCHER get() = tr("the TV's own launcher", "电视自带的桌面")

    /** Install a home screen: the Apps tab's card, the picker, the wait for the Store, and its dead ends. */
    val BETTER_HOME get() = tr("Get a better home screen", "换一个更好的主屏幕")

    /** [fire]: Fire OS, where there's no Play Store and Tellyscope installs it. */
    fun betterHomeText(current: String?, fire: Boolean = false) = tr(
        "Swap ${current ?: "the TV's home screen"} for a launcher without ads" +
            if (fire) "; switch back any time." else ". Free on the Play Store; switch back any time.",
        "把 ${current ?: "电视的主屏幕"} 换成没有广告的桌面" + if (fire) "，随时可以切回去。" else "。Play Store 上免费，随时可以切回去。",
    )

    val CHOOSE_ONE get() = tr("Choose one", "选一个")
    val INSTALL_ONE get() = tr("Install it", "安装")
    val CHOOSE_HOME get() = tr("Choose a home screen", "选择主屏幕")
    val CHOOSE_HOME_TEXT get() = tr(
        "Each one replaces the TV's home screen. Tellyscope opens it in the Play Store on the TV; you install it with the remote.",
        "每一个都会替换电视的主屏幕。Tellyscope 会在电视的 Play Store 里打开它，你用遥控器安装。",
    )
    /** After [CHOOSE_HOME_TEXT] when Download directly is offered. */
    val CAN_DOWNLOAD get() = tr("Some can also be downloaded directly.", "有些也可以直接下载。")
    val NO_LAUNCHER_LIST get() = tr(
        "Couldn't load the list of home screens. Check the phone's internet and try again.",
        "无法加载主屏幕列表。请检查手机网络后重试。",
    )
    val RECOMMENDED get() = tr("Recommended", "推荐")
    /** The picker's section for launchers on the TV that Recommended doesn't list. */
    val ON_THIS_TV get() = tr("On this TV", "这台电视上")
    val INSTALLED get() = tr("Installed", "已安装")
    val HOME_NOW get() = tr("Home now", "当前主屏幕")
    /** Shows a launcher on the TV, from the picker. */
    val OPEN get() = tr("Open", "打开")
    val USES_SERVICE get() = tr("Uses an accessibility service", "使用无障碍服务")
    val NO_SERVICE get() = tr("Open source · no accessibility service", "开源 · 不使用无障碍服务")
    /** Under the picker's list: any other launcher works too, and comes back as On this TV. */
    val OTHER_LAUNCHER get() = tr(
        "Prefer another launcher? Install it from the Play Store on your TV, then come back here. It shows up under $ON_THIS_TV.",
        "想用别的桌面？在电视的 Play Store 里安装，再回到这里。它会出现在「$ON_THIS_TV」下面。",
    )
    val NOT_AFFILIATED get() = tr(
        "Tellyscope isn't affiliated with any of these launchers or their developers.",
        "Tellyscope 与这些桌面及其开发者均无关联。",
    )
    val GET_FROM_STORE get() = tr("Get it from Play Store", "从 Play Store 获取")
    val STORE_RECOMMENDED get() = tr("Play Store recommended: it updates itself", "建议用 Play Store：它可以自己更新")
    val DOWNLOAD_DIRECTLY get() = tr("Download directly", "直接下载")
    val INSTALL_HOME get() = tr("Install a home screen", "安装主屏幕")
    val INSTALL_IT get() = tr("Install it on your TV", "安装到电视上")

    fun installItText(name: String) = tr(
        "The Play Store is open on the TV, on $name's page. Use the remote to select Install. Tellyscope moves on by itself once it's installed.",
        "电视上的 Play Store 已打开 $name 的页面。用遥控器选择安装。安装完成后 Tellyscope 会自动继续。",
    )

    fun storeOn(tv: String) = tr("Play Store on $tv", "$tv 上的 Play Store")

    val WAITING_INSTALL get() = tr("Waiting for install…", "正在等待安装…")
    val DOWNLOADS_TAKE get() = tr(
        "Downloads can take a minute. Tellyscope can't see the download, only when it's done.",
        "下载可能需要一点时间。Tellyscope 看不到下载进度，只能在完成后得知。",
    )
    val OPEN_STORE_AGAIN get() = tr("Open Play Store again", "再次打开 Play Store")
    val MAKE_IT_HOME get() = tr("Make it the home screen?", "把它设为主屏幕？")

    fun isInstalled(name: String) = tr("$name is installed.", "$name 已安装。")

    fun launcherLater(name: String) = tr(
        "$name is installed. To use it later, choose $MAKE_HOME on it in Tweaks, under $HOME_SCREEN.",
        "$name 已安装。以后要使用，请在「调整」的「$HOME_SCREEN」里对它选择「$MAKE_HOME」。",
    )

    fun launcherInstalled(name: String) = tr(
        "$name is installed. Press Home on the remote to pick it.",
        "$name 已安装。按遥控器上的主页键即可选用。",
    )

    fun launcherIsHome(name: String) = tr("$name is already your home screen.", "$name 已经是你的主屏幕。")

    val NO_STORE get() = tr("The Play Store didn't open", "Play Store 没有打开")

    fun noStoreText(tv: String, name: String) = tr(
        "$tv didn't open the Play Store for Tellyscope. On the TV, open the Play Store yourself and search for $name. " +
            "Tellyscope keeps watching and moves on once it's installed.",
        "$tv 没有为 Tellyscope 打开 Play Store。请在电视上自己打开 Play Store 并搜索 $name。" +
            "Tellyscope 会继续查看，安装完成后自动继续。",
    )

    val CHOOSE_ANOTHER get() = tr("Choose another", "换一个")

    fun notInstalled(name: String) = tr("$name isn't installed", "尚未安装 $name")
    /** The Store closed without an install: back on the picker, still chosen. */
    fun wasntInstalled(name: String) = tr("$name wasn't installed", "没有安装 $name")

    /** Install Projectivy on Fire OS: Tellyscope downloads it from its developer's GitHub and installs it over adb. */
    val INSTALL_PROJECTIVY get() = tr("Install Projectivy", "安装 Projectivy")
    val FIRE_INSTALL_TEXT get() = tr(
        "A home screen without ads. Tellyscope installs it on the TV for you; there's nothing to do on the TV.",
        "一个没有广告的主屏幕。Tellyscope 会替你装到电视上，电视上不用操作。",
    )

    fun downloadsFrom(name: String) = tr(
        "Tellyscope downloads $name from the developer's own release page:",
        "Tellyscope 从开发者自己的发布页下载 $name：",
    )

    fun release(version: String, size: Long) = tr("Release $version · ${bytes(size)}", "版本 $version · ${bytes(size)}")

    val WHERE_FROM get() = tr("Where it comes from", "来源")
    val SIGNED_CHECK get() = tr(
        "Tellyscope checks it's signed by Projectivy's developer before installing it on the TV. Tellyscope isn't affiliated with Projectivy.",
        "安装到电视之前，Tellyscope 会核对它是否由 Projectivy 的开发者签名。Tellyscope 与 Projectivy 无关联。",
    )
    val DOWNLOAD_AND_INSTALL get() = tr("Download and install", "下载并安装")

    /** Download directly on a TV with the Play Store: what Tellyscope checks, what it doesn't, and the safer way. */
    fun downloadDirectly(name: String) = tr("Download $name directly?", "直接下载 $name？")

    val DIRECT_CHECK get() = tr(
        "Tellyscope checks the app carries the developer's signature. It does not scan the app for malicious code, and the app won't update on its own.",
        "Tellyscope 会核对应用是否带有开发者签名。它不会扫描恶意代码，应用也不会自己更新。",
    )
    val STORE_SAFER get() = tr(
        "Getting it from Play Store is safer and keeps it updated.",
        "从 Play Store 获取更稳妥，也能保持更新。",
    )
    val DOWNLOAD_ANYWAY get() = tr("Download anyway", "仍然下载")
    val STORE_INSTEAD get() = tr("Get it from the Play Store on the TV instead.", "改为在电视的 Play Store 上获取。")

    /** Install Projectivy's steps, one panel: download (or the picked file), the check, the install. */
    fun installing(name: String) = tr("Installing $name", "正在安装 $name")
    val STEP_DOWNLOAD get() = tr("Download from GitHub", "从 GitHub 下载")
    val STEP_DOWNLOADED get() = tr("Downloaded", "已下载")
    fun downloading(percent: Int, done: Long, total: Long) = tr(
        "Downloading · $percent% · ${bytes(done)} of ${bytes(total)}",
        "正在下载 · $percent% · ${bytes(done)} / ${bytes(total)}",
    )
    val STEP_PICKED get() = tr("Picked a file", "已选择文件")
    val STEP_CHECK get() = tr("Check it's Projectivy", "核对是否为 Projectivy")
    val STEP_CHECKING get() = tr("Checking it's Projectivy…", "正在核对是否为 Projectivy…")
    val STEP_CHECKED get() = tr("It's Projectivy", "确认为 Projectivy")
    val STEP_CHECKED_TEXT get() = tr("Signed by its developer", "由开发者签名")
    val STEP_FAILED get() = tr("Not Projectivy", "不是 Projectivy")
    val STEP_FAILED_TEXT get() = tr("Package name or signature didn't match", "包名或签名不匹配")
    fun installOn(tv: String) = tr("Install on $tv", "安装到 $tv")

    fun installingOn(tv: String) = tr("Installing on $tv…", "正在安装到 $tv…")

    /** X while it downloads: nothing is on the TV yet. */
    val STOP_TITLE get() = tr("Stop the download?", "停止下载？")
    val STOP_TEXT get() = tr("Nothing is on the TV yet. You can start again any time.", "电视上还没有任何文件。你可以随时重新开始。")
    val STOP get() = tr("Stop", "停止")
    val KEEP_DOWNLOADING get() = tr("Keep downloading", "继续下载")

    val NOT_PROJECTIVY get() = tr("That download isn't Projectivy", "这次下载的不是 Projectivy")
    val NOT_PROJECTIVY_TEXT get() = tr(
        "The file from GitHub didn't match Projectivy's package name or its developer's signature, so Tellyscope installed nothing on the TV.",
        "从 GitHub 取得的文件与 Projectivy 的包名或开发者签名不符，因此 Tellyscope 没有在电视上安装任何内容。",
    )
    val NOT_PROJECTIVY_FILE get() = tr("That file isn't Projectivy", "这个文件不是 Projectivy")
    val NOT_PROJECTIVY_FILE_TEXT get() = tr(
        "It didn't match Projectivy's package name or its developer's signature, so Tellyscope installed nothing on the TV.",
        "它与 Projectivy 的包名或开发者签名不符，因此 Tellyscope 没有在电视上安装任何内容。",
    )
    val CANT_READ_FILE get() = tr(
        "Tellyscope couldn't read that file. Pick it again, or download it again.",
        "Tellyscope 无法读取该文件。请重新选择，或重新下载。",
    )
    fun cantInstall(name: String) = tr("Couldn't install $name", "无法安装 $name")

    /** Couldn't download: GitHub's limit for the network ([until] is a clock time), or no way to reach it. */
    val NO_DOWNLOAD get() = tr("Couldn't download Projectivy", "无法下载 Projectivy")
    val NO_DOWNLOAD_OFFLINE get() = tr(
        "The phone can't reach GitHub. Check its internet, then try again.",
        "手机无法连接 GitHub。请检查网络后重试。",
    )
    fun noDownloadLimited(until: String?) = tr(
        "GitHub is limiting downloads from your network ${if (until != null) "until $until" else "for a while"}. Try again then, or download it yourself.",
        "GitHub 正在限制你所在网络的下载${if (until != null) "，直到 $until" else "，请稍后再试"}。到时重试，或自己下载。",
    )
    val OR_YOURSELF get() = tr("Or download it yourself", "或者自己下载")
    val YOURSELF_PAGE get() = tr(
        "Download the app file (.apk) from the release page in your browser.",
        "在浏览器中从发布页下载应用文件（.apk）。",
    )
    fun yourselfPick(file: String?) = tr(
        "Come back and pick ${file ?: "it"}. Tellyscope checks it's Projectivy before installing it.",
        "回来后选择${file ?: "它"}。Tellyscope 会在安装前核对它是不是 Projectivy。",
    )
    val LATEST_RELEASE get() = tr("Latest release", "最新版本")
    val PICK_DOWNLOADED get() = tr("Pick the downloaded file", "选择已下载的文件")
    val PICK_ANOTHER get() = tr("Pick another file", "另选一个文件")
    val TRY_DOWNLOAD_AGAIN get() = tr("Try download again", "重新下载")

    val NOT_INSTALLED_TEXT get() = tr(
        "The Play Store closed on the TV before it was installed. If the Store said it isn't available for this TV, choose another launcher.",
        "电视上的 Play Store 在安装完成前关闭了。如果商店显示这台电视无法安装，请换一个桌面。",
    )

    /** The Tweaks row that reopens it after Not now on the card. */

    fun serviceOffText(app: String) = tr(
        "$app's accessibility service is off, so the TV can take Home back.",
        "$app 的无障碍服务已关闭，电视可能会把主页键收回去。",
    )

    private val GOOGLE_HOMES = setOf("com.google.android.tvlauncher", "com.google.android.apps.tv.launcherx")

    /**
     * Force-stop has no true undo, and undoing a Turn on disables the app; the button says what it
     * actually does.
     */
    fun undo(undo: Action) = when (undo) {
        Action.Open -> tr("Open again", "再次打开")
        Action.Disable -> button(Action.Disable)
        else -> tr("Undo", "撤销")
    }

    fun ago(at: Long, now: Long): String {
        val s = ((now - at) / 1000).coerceAtLeast(0)
        return when {
            s < 10 -> tr("just now", "刚刚")
            s < 60 -> tr("$s s ago", "$s 秒前")
            s < 3600 -> tr("${s / 60} min ago", "${s / 60} 分钟前")
            s < 86_400 -> tr("${s / 3600} h ago", "${s / 3600} 小时前")
            else -> tr("${s / 86_400} d ago", "${s / 86_400} 天前")
        }
    }

    fun uptime(s: Long): String {
        val d = s / 86_400
        val h = (s % 86_400) / 3600
        val m = (s % 3600) / 60
        return if (simplifiedChinese()) when {
            d > 0 -> "${d}天${h}小时"
            h > 0 -> "${h}小时${m}分"
            else -> "${m}分"
        } else when {
            d > 0 -> "${d}d ${h}h"
            h > 0 -> "${h}h ${m}m"
            else -> "${m}m"
        }
    }

    /** Uptime for people: the largest unit or two, spelled out. */
    fun upFor(s: Long): String {
        val d = s / 86_400
        val h = (s % 86_400) / 3600
        val m = (s % 3600) / 60
        if (simplifiedChinese()) return when {
            d > 0 && h > 0 -> "$d 天 $h 小时"
            d > 0 -> "$d 天"
            h > 0 -> "$h 小时"
            else -> "$m 分钟"
        }
        fun n(v: Long, unit: String) = "$v $unit${if (v == 1L) "" else "s"}"
        return when {
            d > 0 && h > 0 -> "${n(d, "day")}, ${n(h, "hour")}"
            d > 0 -> n(d, "day")
            h > 0 -> n(h, "hour")
            else -> n(m, "minute")
        }
    }

    /** "May 2023", in the phone's language. Simplified Chinese uses 2023年5月. */
    fun month(m: YearMonth): String =
        if (simplifiedChinese()) m.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA))
        else m.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    /** Network speed in the same units as [bytes], so a download's size and speed line up. */
    fun speed(bytesPerSec: Long) = bytes(bytesPerSec) + tr("/s", "/秒")

    fun mb(v: Int) = if (v >= 1024) "%.1f GB".format(v / 1024.0) else "$v MB"

    fun uninstallTitle(app: String) = tr("Uninstall $app?", "卸载 $app？")

    /** The confirm for the launcher on Home: Home goes back first, and a failed step stops it. */
    fun uninstallHome(app: String, back: String, service: Boolean) = if (simplifiedChinese()) listOfNotNull(
        "$app 是你的主屏幕。移除之前，Tellyscope 会：",
        "1. 把 $back 重新设为主屏幕，如果它已关闭就先启用。",
        "2. 关闭 $app 的无障碍服务。".takeIf { service },
        "${if (service) 3 else 2}. 卸载它，以及保存在里面的全部内容。此操作无法撤销。",
        "如果某一步失败，Tellyscope 会停在那里，说明是哪一步，并且不会卸载。",
    ).joinToString("\n\n") else listOfNotNull(
        "$app is your home screen. Before removing it, Tellyscope:",
        "1. Makes $back the home screen again, turning it back on if it's off.",
        "2. Turns off $app's accessibility service.".takeIf { service },
        "${if (service) 3 else 2}. Uninstalls it, with everything saved in it. This can't be undone.",
        "If a step fails, Tellyscope stops there, says which one, and doesn't uninstall.",
    ).joinToString("\n\n")

    /** The Apps tabs' header over the apps the TV needs, and why they're apart. */
    val SYSTEM get() = tr("System", "系统")
    val SYSTEM_WHY get() = tr("Needed by the TV · can't be changed", "电视需要 · 无法更改")

    /** Debloat with nothing left on the list: what's done, what's next, then Apps. */
    val GATE_TITLE get() = tr("Everything on the list is off", "清单上的应用都已关闭")
    val GATE_DONE get() = tr(
        "All the apps the cleanyourtv list suggests for this TV are already off.",
        "cleanyourtv 清单为这台电视建议的应用都已关闭。",
    )
    val GATE_LAUNCHER get() = tr(
        "To trim it further, switch to a minimal home screen. It replaces the TV's own, with its ads and suggestions.",
        "想再精简一点，可以换成简洁的主屏幕。它会替换电视自带的主屏幕，连同广告和推荐一起去掉。",
    )
    val GATE_APPS get() = tr(
        "You can still choose, app by app, what to keep or turn off in Apps.",
        "你仍可以在「应用」里逐个决定保留或关闭。",
    )
    val GO_TO_APPS get() = tr("Go to Apps", "前往应用")
    val DONT_SHOW_AGAIN get() = tr("Don't show again", "不再显示")

    /** Apps, while the TV's app list is being read. */
    val READING_APPS get() = tr("Reading the TV's apps…", "正在读取电视上的应用…")
    val READING_APPS_LINE get() = tr("This takes a few seconds.", "这需要几秒钟。")

    /** Running, with no word from the TV. */
    val TV_OFFLINE get() = tr("TV offline", "电视离线")
    val RUNNING_OFFLINE get() = tr("Running apps show here once the TV is connected.", "电视连上后，正在运行的应用会显示在这里。")

    val ANDROID_SYSTEM get() = tr(AppNames.SYSTEM, "Android 系统")

    /** The word next to the status dot, so the state never rests on colour alone. */
    fun state(s: DeviceState) = when (s) {
        DeviceState.Online -> tr("Connected", "已连接")
        DeviceState.Offline -> tr("Offline", "离线")
        DeviceState.NotAllowed -> tr("Not allowed", "未允许")
        DeviceState.Waiting -> tr("Connecting", "正在连接")
        DeviceState.NeedsPairing -> tr("Needs the pairing code", "需要配对码")
        DeviceState.Found -> tr("Not connected", "未连接")
    }

    /** Under 10 GB a whole number hides too much: a 1.5 GB TV isn't a 2 GB one. */
    fun gbOf(used: Double, total: Double) =
        if (simplifiedChinese()) {
            if (total < 10) "%.1f / %.1f GB".format(used, total) else "%.1f / %.0f GB".format(used, total)
        } else if (total < 10) "%.1f of %.1f GB".format(used, total) else "%.1f of %.0f GB".format(used, total)

    /** Free storage in plain words, rounded down so it never promises room that isn't there; null if unknown. */
    fun free(s: Snapshot): String? = free(s.storageUsedGb, s.storageTotalGb)

    /** [free] from the two numbers, for a TV's saved facts as well as a live reading. */
    fun free(used: Double?, total: Double?): String? {
        val gb = used?.let { u -> total?.let { t -> (t - u).coerceAtLeast(0.0) } } ?: return null
        return if (gb < 10) tr("%.1f GB free".format(Math.floor(gb * 10) / 10), "剩余 %.1f GB".format(Math.floor(gb * 10) / 10))
        else tr("${gb.toInt()} GB free", "剩余 ${gb.toInt()} GB")
    }

    /** An offline TV that has never sent stats; a refusing TV is on, it just hasn't allowed us. */
    fun noStats(s: DeviceState) =
        if (s == DeviceState.NotAllowed) tr("Stats show up once the TV allows this phone.", "电视允许这台手机后，状态就会显示。")
        else tr("No stats yet. They show up once the TV is back on.", "还没有状态。电视重新开机后就会显示。")

    val WRONG_CODE get() = tr("That code didn't work. Check the 6 digits on the TV and try again.", "配对码不正确。请核对电视上的 6 位数字后重试。")
    val UNREACHABLE get() = tr(
        "The TV didn't answer. Check it's on and on the same Wi-Fi as this phone, then try again.",
        "电视没有响应。请确认它已开机，并且和这台手机在同一个 Wi-Fi 下，然后重试。",
    )
    val PAIR_INSTEAD get() = tr(
        "The TV didn't let this phone in yet. Pair it once with the code from the TV.",
        "电视还没有允许这台手机。请用电视上的配对码配对一次。",
    )
    val LOGGED_OUT get() = tr("You were logged out", "你已被登出")
    val LOGGED_OUT_WHY get() = tr(
        "The TV removed this phone's access. Tellyscope disconnected and stopped asking, so nothing pops up on the TV until you connect again.",
        "电视撤销了这台手机的权限。Tellyscope 已断开并停止询问，在你再次连接之前，电视上不会弹出提示。",
    )
    val LOGGED_OUT_CAUSES get() = listOf(
        tr(
            "Revoke USB debugging authorisations was chosen on the TV.",
            "在电视上选择了「撤销 USB 调试授权」。",
        ),
        tr(
            "Remove all other access was used from another phone, or from another Tellyscope build on this phone.",
            "另一台手机，或这台手机上的另一个 Tellyscope 版本，使用了「移除所有其他权限」。",
        ),
    )
    fun loggedOutOf(name: String) = tr("Logged out of $name", "已从 $name 登出")

    /** When the TV took the access back, on the logged-out page. */
    fun loggedOutAt(at: Long, now: Long): String {
        // Calendar days, so 23:00 reads "yesterday" the next morning.
        val zone = ZoneId.systemDefault()
        val days = ChronoUnit.DAYS.between(Instant.ofEpochMilli(at).atZone(zone).toLocalDate(), Instant.ofEpochMilli(now).atZone(zone).toLocalDate())
        val clock = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(at))
        return when {
            now - at < 5 * 60_000 -> tr("Logged out just now", "刚刚被登出")
            days == 0L -> tr("Logged out today, $clock", "今天 $clock 被登出")
            days == 1L -> tr("Logged out yesterday", "昨天被登出")
            else -> tr("Logged out $days days ago", "$days 天前被登出")
        }
    }

    data class Health(val ok: Boolean, val text: String)

    /**
     * The one line at the top of a TV's page: the most pressing problem, or that all is well.
     * [heaviest] are app names by memory use, largest first; there is no per-app storage size yet.
     */
    fun health(s: Snapshot, heaviest: List<String>): Health {
        val storage = s.storageUsedGb?.let { u -> s.storageTotalGb?.takeIf { it > 0 }?.let { u / it } }
        val ram = if (s.ramTotalMb > 0) s.ramUsedMb.toDouble() / s.ramTotalMb else null
        val most = heaviest.take(2).takeIf { it.isNotEmpty() }?.let { names ->
            if (simplifiedChinese()) "：${names.joinToString("和")}占用最多" else ": ${names.joinToString(" and ")} use the most"
        }.orEmpty()
        return when {
            storage != null && storage >= FULL -> Health(false, tr("Storage almost full", "存储空间几乎已满"))
            ram != null && ram >= FULL -> Health(false, tr("Memory almost full", "内存几乎已满") + most)
            else -> Health(true, tr("Running fine", "运行正常"))
        }
    }

    const val FULL = 0.9

    fun bytes(v: Long): String = when {
        v >= 1L shl 30 -> "%.1f GB".format(v / (1L shl 30).toDouble())
        v >= 1L shl 20 -> "%.1f MB".format(v / (1L shl 20).toDouble())
        v >= 1L shl 10 -> "${v shr 10} KB"
        else -> tr("$v bytes", "$v 字节")
    }

    fun folder(f: TvFolder) = when (f) {
        TvFolder.DOWNLOAD -> tr("Download", "下载")
        TvFolder.MOVIES -> tr("Movies", "电影")
    }

    // Sideload

    fun installButton(kind: InstallKind) = when (kind) {
        InstallKind.NEW -> tr("Install", "安装")
        InstallKind.UPDATE -> tr("Update", "更新")
        InstallKind.REINSTALL -> tr("Reinstall", "重新安装")
        InstallKind.DOWNGRADE -> tr("Install older version", "安装旧版本")
    }

    fun installKind(kind: InstallKind, onTv: String?, picked: String?) = when (kind) {
        InstallKind.NEW -> tr("Not on this TV yet. This installs it.", "这台电视上还没有。这将安装它。")
        InstallKind.UPDATE -> tr(
            "Updates the TV's copy" + versions(onTv, picked) + ". Its settings stay.",
            "更新电视上的版本" + versions(onTv, picked) + "。设置会保留。",
        )
        InstallKind.REINSTALL -> tr(
            "The TV already has this version" + (onTv?.let { " ($it)" } ?: "") + ". This installs it again over itself.",
            "电视上已有这个版本" + (onTv?.let { "（$it）" } ?: "") + "。这将在原位再安装一次。",
        )
        InstallKind.DOWNGRADE -> tr(
            "This is older than the one on the TV" +
                (if (onTv != null && picked != null) " ($picked here, $onTv on the TV)" else "") +
                ". Android normally refuses that, and the app's saved data may not work with the older version.",
            "这比电视上的版本更旧" +
                (if (onTv != null && picked != null) "（这里是 $picked，电视上是 $onTv）" else "") +
                "。Android 通常会拒绝，应用已保存的数据也可能无法在旧版本上使用。",
        )
    }

    private fun versions(onTv: String?, picked: String?) =
        if (onTv != null && picked != null) tr(" from $onTv to $picked", "，从 $onTv 到 $picked") else ""

    val ALLOW_DOWNGRADE get() = tr("Install the older version anyway", "仍然安装旧版本")
    val INSTALL_KEEP_OPEN get() = tr("Keep Tellyscope open until it's done.", "完成前请保持 Tellyscope 打开。")

    fun installed(kind: InstallKind, app: String) = when (kind) {
        InstallKind.UPDATE -> tr("Updated $app.", "已更新 $app。")
        InstallKind.REINSTALL -> tr("Reinstalled $app.", "已重新安装 $app。")
        InstallKind.NEW, InstallKind.DOWNGRADE -> tr("Installed $app.", "已安装 $app。")
    }

    fun installRefused(reason: InstallRefusal, app: String) = when (reason) {
        InstallRefusal.SIGNATURE -> tr(
            "The $app on the TV comes from a different developer, so Android won't replace it. If you're sure, uninstall it first: from Apps here if you installed it, or on the TV.",
            "电视上的 $app 来自另一位开发者，Android 不会替换它。如果确定要换，请先卸载：你自己装的可以在这里的「应用」里卸，否则在电视上卸。",
        )
        InstallRefusal.DOWNGRADE -> tr("The TV has a newer $app and won't go back to an older one.", "电视上的 $app 更新，无法退回旧版本。")
        InstallRefusal.NO_SPACE -> tr(
            "The TV is out of space. Disable or uninstall something there, then try again.",
            "电视空间不足。请先停用或卸载一些内容，然后重试。",
        )
        InstallRefusal.TV_SAID_NO -> tr(
            "The TV blocked the install. Look in the TV's security settings for installing apps from unknown sources, or for app verification, then try again.",
            "电视阻止了安装。请到电视的安全设置里查看「安装未知来源应用」或应用验证，然后重试。",
        )
        InstallRefusal.TV_TOO_OLD -> tr("$app needs a newer version of Android than this TV has.", "$app 需要比这台电视更高的 Android 版本。")
        InstallRefusal.WRONG_CPU -> tr(
            "This $app is built for a different kind of processor. Look for a version made for this TV, often marked armeabi-v7a or arm64-v8a.",
            "这个 $app 是为另一种处理器构建的。请找适合这台电视的版本，通常标为 armeabi-v7a 或 arm64-v8a。",
        )
        InstallRefusal.APP_TOO_OLD -> tr(
            "$app was made for a very old version of Android, and this TV won't install it.",
            "$app 是为很旧的 Android 版本制作的，这台电视不会安装它。",
        )
        InstallRefusal.MISSING_FEATURE -> tr(
            "$app needs something this TV doesn't have, like a touchscreen or a camera.",
            "$app 需要这台电视没有的硬件，例如触摸屏或摄像头。",
        )
        InstallRefusal.MISSING_PARTS -> tr(
            "$app comes in several parts and one is missing. Pick all of its .apk files together, or its .apks bundle.",
            "$app 由多个部分组成，缺了一部分。请同时选择它的全部 .apk 文件，或选择 .apks 包。",
        )
        InstallRefusal.TEST_ONLY -> tr("This is a developer test build of $app, which the TV won't install.", "这是 $app 的开发测试版，电视不会安装。")
        InstallRefusal.CONFLICT -> tr(
            "Another app on the TV already uses something $app needs, so the TV refused it.",
            "电视上的另一个应用已经占用了 $app 需要的内容，因此电视拒绝了安装。",
        )
        InstallRefusal.BROKEN -> tr(
            "This file isn't a working app. It may be damaged or only half downloaded; try getting it again.",
            "这个文件不是可用的应用。它可能已损坏或只下载了一半，请重新获取。",
        )
        InstallRefusal.UNKNOWN -> tr("The TV didn't install $app.", "电视没有安装 $app。")
    }

    val NOT_AN_APP get() = tr(
        "That file isn't an Android app. Pick an .apk, several split .apk files, or an .apks or .xapk bundle.",
        "这个文件不是 Android 应用。请选择 .apk、多个拆分的 .apk，或 .apks / .xapk 包。",
    )
    val SIDELOAD_LOST get() = tr(
        "Lost the TV partway through. Check it's still on and connected, then try again.",
        "中途和电视断开了。请确认它仍开机并已连接，然后重试。",
    )

    // Files

    fun sent(name: String, savedAs: String, f: TvFolder) =
        if (savedAs == name) tr("Sent $name to ${folder(f)}", "已将 $name 发送到${folder(f)}")
        else tr(
            "Sent to ${folder(f)} as $savedAs, since $name was already there",
            "已发送到${folder(f)}，命名为 $savedAs，因为已有 $name",
        )

    fun saved(name: String) = tr("Saved $name to your phone", "已将 $name 保存到手机")
    fun sendFailed(name: String) = tr("Couldn't send $name. Check the TV is still connected and has space.", "无法发送 $name。请确认电视仍连接且有足够空间。")
    fun saveFailed(name: String) = tr("Couldn't save $name. Check the TV is still connected and the phone has space.", "无法保存 $name。请确认电视仍连接且手机有足够空间。")

    val CANCELLED_SEND get() = tr("Cancelled. Nothing was left on the TV.", "已取消。电视上没有留下任何文件。")
    val CANCELLED_SAVE get() = tr("Cancelled. Nothing was saved.", "已取消。没有保存任何内容。")
    val CANT_READ get() = tr("Couldn't read that file on your phone.", "无法读取手机上的这个文件。")
    val CANT_UNDO get() = tr("This version of Tellyscope can't undo that setting.", "这个版本的 Tellyscope 无法撤销该项设置。")
    val FILES_LOST get() = tr("Couldn't read this folder. Check the TV is still connected.", "无法读取这个文件夹。请确认电视仍连接。")

    // Remote

    val KEY_REFUSED get() = tr("The TV didn't take that key.", "电视没有接受这个按键。")
    val TYPE_REFUSED get() = tr(
        "The TV didn't take the text. Select a text box on the TV first, then send again.",
        "电视没有接受这段文字。请先在电视上选中一个文本框，再发送。",
    )
    val REMOTE_LOST get() = tr("Lost the TV. Check it's still on and connected.", "和电视断开了。请确认它仍开机并已连接。")
    val NO_UPDATE_SCREEN get() = tr(
        "This TV has no update screen Tellyscope can open. Look in its Settings, under System or About.",
        "这台电视没有 Tellyscope 能打开的更新页面。请到它的设置里，在「系统」或「关于」下查找。",
    )
    val UPDATES_OPENED get() = tr(
        "Opened the update screen on the TV. Look at the TV to check for updates.",
        "已在电视上打开更新页面。请看着电视检查更新。",
    )
    val REBOOT_REFUSED get() = tr(
        "The TV said no to restarting. Use its own remote or power button instead.",
        "电视拒绝重启。请改用它自己的遥控器或电源键。",
    )
    val REBOOT_IGNORED get() = tr(
        "The TV didn't restart. Use its own remote or power button instead.",
        "电视没有重启。请改用它自己的遥控器或电源键。",
    )
    val REMOTE_OFFLINE_WAKE get() = tr("works again once it reconnects", "重新连接后即可使用")
    val TYPE_HINT get() = tr("Select a text box on the TV, like a search box, then type here.", "先在电视上选中文本框（例如搜索框），再在这里输入。")
    val BACKSPACE get() = tr("Delete the last letter you typed on the TV", "删除你在电视上输入的最后一个字符")
    val SCREEN_OFF get() = tr("Screen off", "关闭屏幕")
    val SCREEN_ON get() = tr("Screen on", "打开屏幕")
    val SCREEN_OFF_WAKE get() = tr("tap any key to wake", "按任意键唤醒")
    val SCREEN_OFF_TIP get() = tr(
        "Turns off the TV's picture. The app stays connected; any key wakes it.",
        "关闭电视画面。应用保持连接；按任意键即可唤醒。",
    )
    val SCREEN_OFF_REFUSED get() = tr("This TV can't turn its picture off from Tellyscope.", "这台电视无法通过 Tellyscope 关闭画面。")
    val SCREEN_ON_REFUSED get() = tr("The TV's picture didn't come back on. Try again, or restart the TV.", "电视画面没有重新打开。请重试，或重启电视。")

    fun cantType(text: String): String {
        val odd = text.firstOrNull { it !in ' '..'~' }
        return when {
            text.isEmpty() -> tr("Type something first.", "请先输入内容。")
            text.length > app.tellyscope.adb.Remote.MAX_TEXT -> tr(
                "That's too long to send in one go. Send up to ${app.tellyscope.adb.Remote.MAX_TEXT} characters at a time.",
                "一次发送太长。每次最多 ${app.tellyscope.adb.Remote.MAX_TEXT} 个字符。",
            )
            odd == '\n' || odd == '\t' -> tr("The TV can only take one line at a time.", "电视一次只能接收一行。")
            odd != null -> tr(
                "The TV can only type plain letters, numbers and symbols, not \u201c$odd\u201d.",
                "电视只能输入普通字母、数字和符号，不能输入「$odd」。",
            )
            else -> tr(
                "The TV reads \u201c%s\u201d as a space, so it can't type that.",
                "电视会把「%s」当成空格，因此无法输入。",
            )
        }
    }

    val REMOTE_RESTART get() = tr("Restart TV", "重启电视")
    val REBOOT_TITLE get() = tr("Restart the TV?", "重启电视？")
    val REBOOT_TEXT get() = tr(
        "The TV turns off and on again. Tellyscope reconnects by itself when it's back, usually within a minute.",
        "电视会关机再开机。恢复后 Tellyscope 会自动重连，通常在一分钟内。",
    )
    val RESTARTING_TITLE get() = tr("Restarting…", "正在重启…")
    val RESTARTING_TEXT get() = tr("The TV is restarting. Tellyscope will reconnect by itself.", "电视正在重启。Tellyscope 会自动重连。")
    val RESTART_LATE_TITLE get() = tr("The TV hasn't come back yet", "电视还没有恢复")
    val RESTART_LATE_STATUS get() = tr("Not back yet", "尚未恢复")
    val RESTART_LATE_TEXT get() = tr(
        "It's been a minute. Some TVs take longer to start; Retry waits another minute.",
        "已经过了一分钟。有的电视启动更慢；重试会再等一分钟。",
    )
    val RESTART_LOST_TEXT get() = tr("Use the TV's own remote to turn it on, then tap Retry.", "请用电视自己的遥控器开机，然后点重试。")

    fun waitingFor(name: String, ms: Long): String {
        val s = (ms / 1000).coerceAtLeast(0)
        val clock = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
        return tr("Waiting for $name · $clock", "正在等待 $name · $clock")
    }

    fun restartLost(name: String) = tr("Still can't reach $name", "仍然无法连接 $name")
    fun backOnline(name: String) = tr("Back online. $name reconnected.", "已恢复在线。$name 已重新连接。")

    // Remote buttons

    val REMOTE_BUTTONS get() = tr("Remote buttons", "遥控按钮")
    val MORE get() = tr("More options", "更多选项")

    fun buttonsHint(cap: Int) = tr(
        "Pick up to $cap. Drag to set the order. To pick another, untick one first.",
        "最多选 $cap 个。拖动可调整顺序。要换一个，请先取消勾选。",
    )
    fun notOnTv(name: String) = tr(
        "$name isn't on this TV. Install it from the Play Store on the TV.",
        "这台电视上没有 $name。请在电视的 Play Store 里安装。",
    )
    fun cantOpen(label: String) = tr(
        "The TV couldn't open $label. If it's turned off, turn it on in Apps.",
        "电视无法打开 $label。如果它已关闭，请在「应用」里启用。",
    )

    fun leftDock(labels: List<String>) =
        if (labels.size == 1) tr(
            "${labels[0]} was uninstalled, so its button left the Remote.",
            "${labels[0]} 已卸载，它的按钮已从遥控器上移除。",
        )
        else tr(
            "${labels.size} apps were uninstalled, so their buttons left the Remote.",
            "已卸载 ${labels.size} 个应用，它们的按钮已从遥控器上移除。",
        )

    fun restarting(minutes: Long) =
        if (minutes < 3) tr(
            "Tellyscope reconnects on its own when the TV is back, usually within a couple of minutes.",
            "电视恢复后 Tellyscope 会自动重连，通常在一两分钟内。",
        )
        else tr(
            "Taking a while. Some TVs turn network debugging off when they restart. If it doesn't come back, turn it on again in the TV's Developer options.",
            "时间有点长。有的电视重启后会关掉网络调试。如果一直不回来，请到电视的开发者选项里重新打开。",
        )

    val TWEAKS get() = tr("TV tweaks", "电视调整")
    val DNS_TITLE get() = tr("Block ads with Private DNS", "用私人 DNS 拦截广告")
    fun dnsState(host: String?) = host?.let { tr("On: $it", "已打开：$it") }
        ?: tr("Off. Turn on to block ads and trackers in every app on the TV.", "已关闭。打开后可拦截电视上所有应用里的广告和跟踪。")
    val DNS_TOO_OLD get() = tr("Needs Android 9 or newer. This TV is older.", "需要 Android 9 或更高版本。这台电视更旧。")
    val DNS_DIALOG get() = tr("Block ads on the whole TV?", "在整台电视上拦截广告？")
    val DNS_TEXT get() = tr(
        "The TV will look up every address through this ad-blocking DNS provider. AdGuard is free and needs no account. " +
            "Tellyscope then checks the TV can still look addresses up, and puts it back if not. Undo is in History.",
        "电视会通过这个拦截广告的 DNS 解析每个地址。AdGuard 免费且不需要账号。" +
            "随后 Tellyscope 会检查电视是否仍能解析地址，不行就改回去。撤销在历史记录里。",
    )
    val DNS_FIELD get() = tr("DNS provider", "DNS 服务")
    val DNS_BAD get() = tr(
        "Enter a hostname like dns.adguard-dns.com. An IP address won't work here.",
        "请输入主机名，例如 dns.adguard-dns.com。这里不能填 IP 地址。",
    )
    val DNS_CONFIRM get() = tr("Turn on", "打开")
    val ANIMATIONS_TITLE get() = tr("Faster animations", "加快动画")
    fun animationsState(scale: String?) =
        if (scale == TvSettings.FAST) tr("On: menus move at twice the speed", "已打开：菜单以两倍速度移动")
        else tr("Off. Turn on to make menus feel snappier.", "已关闭。打开后菜单会更利落。")
    val CACHES_TITLE get() = tr("Clear all app caches", "清除所有应用缓存")
    val CACHES_TEXT get() = tr(
        "Frees space by clearing every app's temporary files. App data and sign-ins stay.",
        "清除每个应用的临时文件以腾出空间。应用数据和登录状态会保留。",
    )
    val CACHES_BUTTON get() = tr("Clear caches", "清除缓存")
    fun cachesCleared(freedKb: Long?) =
        if (freedKb == null || freedKb <= 0) tr("Cleared app caches", "已清除应用缓存")
        else tr("Cleared app caches, freed ${bytes(freedKb * 1024)}", "已清除应用缓存，释放 ${bytes(freedKb * 1024)}")
    val SCREENSHOT_TITLE get() = tr("Screenshot", "截图")
    val SCREENSHOT_TEXT get() = tr(
        "Saves what the TV shows to your phone. Protected video, like most streaming apps, shows as black.",
        "把电视画面保存到手机。受保护的视频（例如大多数流媒体应用）会显示为黑屏。",
    )
    val SCREENSHOT_BUTTON get() = tr("Take screenshot", "截取画面")
    fun screenshotName(at: Long) =
        tr("TV screenshot ", "电视截图 ") + SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.ROOT).format(Date(at)) + ".png"
    val SCREENSHOT_SAVED get() = tr("Screenshot saved to your phone", "截图已保存到手机")
    val SCREENSHOT_FAILED get() = tr("Couldn't take a screenshot. Check the TV is still connected.", "无法截图。请确认电视仍连接。")
    val REQUEST_TITLE get() = tr("Another device wants to control this TV", "另一台设备想控制这台电视")
    val REQUEST_TEXT get() = tr(
        "The TV is asking whether to allow debugging from a device with this key fingerprint. " +
            "Allow it only if you started this yourself and the fingerprint matches the one that device shows.",
        "电视正在询问是否允许这把密钥指纹对应的设备进行调试。" +
            "只有在你自己发起、并且指纹与那台设备显示的一致时才允许。",
    )
    val REQUEST_ALWAYS get() = tr("Always allow this device", "始终允许此设备")
    val REQUEST_ALLOW get() = tr("Allow", "允许")
    val REQUEST_DENY get() = tr("Deny", "拒绝")
    val REQUEST_ALLOWED get() = tr("Allowed. That device can control the TV now.", "已允许。该设备现在可以控制电视。")
    val REQUEST_DENIED get() = tr("Denied. That device can't connect.", "已拒绝。该设备无法连接。")
    val REQUEST_GONE get() = tr("The TV's prompt had closed or changed, so nothing was pressed.", "电视上的提示已关闭或改变，因此没有按下任何选项。")
    /** A prompt under Fire OS's remote alert: it can't be read until the alert closes. */
    val REQUEST_BEHIND_ALERT get() = tr(
        "The TV's prompt is behind an Amazon alert, “Unable to Detect Your Remote”. Press OK on your TV remote to close it.",
        "电视上的提示被亚马逊提示挡住了：「Unable to Detect Your Remote」。请用电视遥控器按确定关掉它。",
    )
    val REQUEST_THEN get() = tr("Then the prompt shows here, so you can check its fingerprint.", "然后提示会显示在这里，你可以核对指纹。")
    val REQUEST_WAITING get() = tr("Waiting for the TV's prompt", "正在等待电视上的提示")
    val PRESS_OK get() = tr("Press OK on the TV", "在电视上按确定")

    // Who has access
    val WARNING_TITLE get() = tr("Another computer is connected to this TV", "另一台电脑正连接着这台电视")
    val WARNING_TEXT get() = tr(
        "It can do everything Tellyscope can: install or remove apps, read files and control the screen.",
        "它能做 Tellyscope 能做的一切：安装或移除应用、读取文件、控制屏幕。",
    )
    val ITS_MINE get() = tr("It's mine", "这是我的")
    val REMOVE_ACCESS get() = tr("Remove all other access…", "移除所有其他权限…")
    val CONNECTED_NOW get() = tr("Connected now", "当前已连接")
    val WHO_HAS_ACCESS get() = tr("Who has access", "谁有权限")

    /** The TV menu's rows for the screens it opens. */
    val HISTORY get() = tr("History", "历史记录")
    val ADVANCED_MONITOR get() = tr("Advanced monitor", "高级监视")

    /** In the TV menu and in Settings: opens Android's share sheet with the repo link. */
    val SHARE get() = tr("Share Tellyscope", "分享 Tellyscope")

    /** Reset TV: its row in the TV menu, its screen, and what it says before, during and after. */
    val RESET_TV get() = tr("Reset TV to how it shipped", "把电视恢复到出厂时的样子")
    val RESET_KEEPS get() = tr("Keeps apps' data and sign-ins", "保留应用数据和登录状态")

    fun apps(n: Int) = if (n == 1) tr("1 app", "1 个应用") else tr("$n apps", "$n 个应用")

    fun resetFound(n: Int) = tr(
        "$n ${if (n == 1) "change" else "changes"} since this TV left the factory",
        "自这台电视出厂以来有 $n 项更改",
    )
    val RESET_FOUND_TEXT get() = tr("Your apps' data and sign-ins stay. Nothing has changed yet.", "你的应用数据和登录状态会保留。目前还没有改动。")
    fun turnOnApps(n: Int) = tr("Turn on ${apps(n)}", "启用 ${apps(n)}")
    val TURN_ON_TEXT get() = tr(
        "Preinstalled apps turned off by hand, from any phone or tool",
        "预装应用被手动关闭过，可能来自任何手机或工具",
    )
    fun bringBackApps(n: Int) = tr("Bring back ${apps(n)}", "恢复 ${apps(n)}")
    val BRING_BACK_TEXT get() = tr("Preinstalled apps that were uninstalled. They're still on the TV.", "被卸载过的预装应用。它们仍在电视上。")
    fun resetHome(to: String) = tr("Make $to the home screen", "将 $to 设为主屏幕")
    fun resetHomeText(from: String, service: Boolean) = tr(
        "Instead of $from" + (if (service) ", with its accessibility service turned off" else "") + ". $from stays installed.",
        "取代 $from" + (if (service) "，并关闭它的无障碍服务" else "") + "。$from 仍会保留安装。",
    )

    /** Under what Reset found: what it leaves alone. [maker] is the TV's brand, when known. */
    fun resetLeaves(shippedOff: Int, maker: String?) = tr(
        (if (shippedOff > 0) "${apps(shippedOff)} ${maker ?: "the maker"} shipped switched off stay off. " else "") +
            "Apps and launchers you installed stay installed.",
        (if (shippedOff > 0) "${maker ?: "厂商"}出厂时关闭的 ${apps(shippedOff)} 会保持关闭。" else "") +
            "你安装的应用和桌面会保留。",
    )

    val NOTHING_TO_RESET get() = tr("Nothing to reset", "没有需要恢复的内容")
    fun nothingToReset(ownHome: String?, maker: String?) = tr(
        "Every preinstalled app is on" + (ownHome?.let { ", and $it is the home screen" } ?: "") + ". " +
            "Apps ${maker ?: "the maker"} shipped switched off stay off.",
        "每个预装应用都已启用" + (ownHome?.let { "，并且 $it 是主屏幕" } ?: "") + "。" +
            "${maker ?: "厂商"}出厂时关闭的应用会保持关闭。",
    )

    fun resetting(p: ResetProgress, home: String?) = when (p.part) {
        ResetPart.TurnOn -> tr("Turning on apps: ${p.done} of ${p.of}.", "正在启用应用：${p.done} / ${p.of}。")
        ResetPart.BringBack -> tr("Bringing back apps: ${p.done} of ${p.of}.", "正在恢复应用：${p.done} / ${p.of}。")
        ResetPart.Home -> tr(
            "Making ${home ?: "the TV's own launcher"} the home screen.",
            "正在将 ${home ?: "电视自带的桌面"} 设为主屏幕。",
        )
    } + tr(" Keep the TV on and connected.", " 请保持电视开机并连接。")
    val RESETTING get() = tr("Resetting…", "正在恢复…")
    val RESET_APPS_STALE get() = tr(
        "Reset done, but the app list didn't refresh. Open the TV again to see it.",
        "恢复已完成，但应用列表没有刷新。请重新打开这台电视查看。",
    )
    val RESET_BUTTON get() = tr("Reset TV", "恢复电视")

    val RESET_DONE get() = tr("TV reset to how it shipped", "电视已恢复到出厂状态")
    val RESET_PARTLY get() = tr("Reset partly done", "恢复只完成了一部分")
    fun resetDoneText(apps: Int, home: String?) = if (simplifiedChinese()) listOfNotNull(
        when (apps) {
            0 -> null
            else -> "$apps 个预装应用已恢复"
        },
        home?.let { "$it 是主屏幕" },
    ).joinToString("，") + "。应用数据和登录状态都保留了。" else listOfNotNull(
        when (apps) {
            0 -> null
            1 -> "1 preinstalled app is back"
            else -> "$apps preinstalled apps are back"
        },
        home?.let { "$it is the home screen" },
    ).joinToString(", and ") + ". Apps' data and sign-ins stayed."
    fun resetPartlyText(failed: Int) = tr(
        "${if (failed == 1) "One step" else "$failed steps"} didn't work. Everything else is back to how it shipped. Nothing was lost.",
        "${if (failed == 1) "有一步" else "有 $failed 步"}没有成功。其余内容已恢复到出厂状态。没有丢失任何内容。",
    )
    fun turnedOn(done: Int, of: Int) =
        if (done == of) tr("Turned on ${apps(of)}", "已启用 ${apps(of)}") else tr("Turned on $done of ${apps(of)}", "已启用 $done / ${apps(of)}")
    fun broughtBack(done: Int, of: Int) =
        if (done == of) tr("Brought back ${apps(of)}", "已恢复 ${apps(of)}") else tr("Brought back $done of ${apps(of)}", "已恢复 $done / ${apps(of)}")
    fun isHome(app: String) = tr("$app is the home screen", "$app 是主屏幕")
    fun stillHome(app: String) = tr("$app is still the home screen", "$app 仍是主屏幕")
    fun tryAgain(app: String?) = if (app != null) tr("Try $app again", "重试 $app") else tr("Try again", "重试")

    /** Debloat while it turns the ticked apps off, then its result. */
    fun disablingApps(done: Int, of: Int) = tr(
        "Turning off apps: $done of $of. Keep the TV on and connected.",
        "正在关闭应用：$done / $of。请保持电视开机并连接。",
    )
    val DISABLING get() = tr("Disabling…", "正在停用…")
    val DISABLING_NOTE get() = tr("Nothing is deleted. Turn any back on from History.", "不会删除任何内容。可以在历史记录里重新启用。")
    val DEBLOAT_DONE get() = tr("Debloat done", "精简完成")
    val DEBLOAT_PARTLY get() = tr("Debloat partly done", "精简只完成了一部分")
    val DEBLOAT_RESULT_TEXT get() = tr("Nothing was deleted; undo any of them from History.", "没有删除任何内容；可以在历史记录里撤销。")
    fun turnedOff(done: Int, of: Int) =
        if (done == of) tr("Turned off ${apps(of)}", "已关闭 ${apps(of)}") else tr("Turned off $done of ${apps(of)}", "已关闭 $done / ${apps(of)}")
    fun stillOn(app: String) = tr("$app is still on", "$app 仍开着")

    /** Under History in the menu, and on top of the History screen: apps Restore everything turns back on. */
    fun stillOff(n: Int) = if (n == 1) tr("1 app still off", "仍有 1 个应用处于关闭") else tr("$n apps still off", "仍有 $n 个应用处于关闭")
    fun stillOffTitle(n: Int) =
        if (n == 1) tr("1 app Tellyscope turned off is still off", "Tellyscope 关闭的 1 个应用仍处于关闭")
        else tr("$n apps Tellyscope turned off are still off", "Tellyscope 关闭的 $n 个应用仍处于关闭")
    val STILL_OFF_TEXT get() = tr(
        "Restore everything turns them back on. Apps that were off before Tellyscope's first change stay off.",
        "「全部恢复」会重新启用它们。Tellyscope 第一次更改之前就已关闭的应用会保持关闭。",
    )
    fun othersConnected(n: Int) =
        if (n == 1) tr("1 other connected now", "另有 1 个正在连接") else tr("$n others connected now", "另有 $n 个正在连接")
    fun connections(n: Int) = if (n == 1) tr("1 connection", "1 个连接") else tr("$n connections", "$n 个连接")
    fun connectedFrom(address: String, n: Int) = tr("From $address · ${connections(n)}", "来自 $address · ${connections(n)}")
    val ON_TV get() = tr("An app on this TV", "这台电视上的一个应用")
    val ACCESS_INTRO get() = tr(
        "Each computer allowed to use ADB can install or remove apps, read files and control the screen.",
        "每台被允许使用 ADB 的电脑都可以安装或移除应用、读取文件并控制屏幕。",
    )
    val THIS_PHONE get() = tr("This phone", "这台手机")
    const val TELLYSCOPE = "Tellyscope"
    val NOT_THIS_PHONE get() = tr("Not this phone", "不是这台手机")
    val YOURS get() = tr("Yours", "你的")
    val WAITING_CHIP get() = tr("Waiting", "等待中")
    val WAITING_LINE get() = tr("Waiting for you to allow it on the TV", "正在等你在电视上允许它")
    val WAITING_TEXT get() = tr(
        "An Allow prompt for it is on the TV now. It can't do anything until you choose Allow there. Not yours? Choose Deny.",
        "电视上现在有它的「允许」提示。在你选择允许之前，它什么也做不了。不是你的？请选择拒绝。",
    )
    val ANSWER get() = tr("Answer", "去处理")
    val NEW_CHIP get() = tr("New", "新的")
    val NEW_KEY_TEXT get() = tr(
        "Allowed on the TV while Tellyscope wasn't watching. Not yours? Remove all other access.",
        "在 Tellyscope 没有查看时被电视允许了。不是你的？请移除所有其他权限。",
    )
    val ALLOWED_COMPUTERS get() = tr("Allowed computers", "已允许的电脑")
    val UNNAMED_COMPUTER get() = tr("Unnamed computer", "未命名的电脑")
    val CANT_CHECK_CONNECTED get() = tr("This TV doesn't say who is connected.", "这台电视不报告谁正在连接。")
    val CANT_CHECK_ALLOWED get() = tr("This TV doesn't list its allowed computers.", "这台电视不列出已允许的电脑。")
    val NOBODY_ALLOWED get() = tr(
        "No computer is allowed. The next one to connect has to be allowed on the TV.",
        "还没有允许任何电脑。下一台要连接的电脑必须在电视上被允许。",
    )
    val ACCESS_FOOT_LINK get() = tr(
        "The TV doesn't say which allowed computer is behind a connection. If you don't recognise an address, remove all other access.",
        "电视不会说明某个连接对应哪台已允许的电脑。如果不认识某个地址，请移除所有其他权限。",
    )
    val ACCESS_FOOT_ALL get() = tr(
        "Android can't remove one computer on its own: this clears every computer, then you allow Tellyscope again.",
        "Android 无法单独移除一台电脑：这会清除所有电脑，然后你再重新允许 Tellyscope。",
    )
    /** Rough on purpose: the TV stamps a key when any connection uses it, not per session. */
    fun lastSeen(at: Long?, now: Long): String {
        if (at == null) return tr("Never seen", "从未出现")
        val days = ((now - at) / 86_400_000).coerceAtLeast(0)
        val clock = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(at))
        return when {
            now - at < 5 * 60_000 -> tr("Last seen just now", "刚刚出现过")
            days == 0L -> tr("Last seen today, $clock", "今天 $clock 出现过")
            days == 1L -> tr("Last seen yesterday", "昨天出现过")
            days < 30 -> tr("Last seen $days days ago", "$days 天前出现过")
            else -> tr("Last seen over a month ago", "一个多月前出现过")
        }
    }
    /** Enough of a fingerprint to tell keys apart: the first three and last two pairs. */
    fun shortFingerprint(fp: String): String {
        val parts = fp.split(':')
        return if (parts.size <= 5) fp else (parts.take(3) + "…" + parts.takeLast(2)).joinToString(":")
    }

    // Remove all other access
    val REMOVE_TITLE get() = tr("Remove all other access", "移除所有其他权限")
    val CONFIRM_TITLE get() = tr("Remove all other access?", "移除所有其他权限？")
    val CONFIRM_TEXT get() = tr(
        "Every computer this TV has allowed loses access, your own computers too: they'll need allowing again.",
        "这台电视允许过的每台电脑都会失去权限，包括你自己的电脑：它们需要重新被允许。",
    )
    val CONFIRM_REMOTE get() = tr(
        "Tellyscope reconnects straight away. Keep the TV remote handy: the TV asks you to allow this phone.",
        "Tellyscope 会马上重连。请把电视遥控器放在手边：电视会请你允许这台手机。",
    )
    val CONFIRM_BUTTON get() = tr("Remove access", "移除权限")
    val CANCEL get() = tr("Cancel", "取消")
    val REVOKING get() = tr("Removing access…", "正在移除权限…")
    val ALLOW_TITLE get() = tr("Allow this phone on the TV", "在电视上允许这台手机")
    val ALLOW_TEXT_A get() = tr(
        "Other access is removed. The TV now asks “Allow USB debugging?”. Tick ",
        "其他权限已移除。电视现在会询问「允许 USB 调试吗？」。请勾选",
    )
    val ALLOW_ALWAYS get() = tr("Always allow from this computer", "一律允许使用这台计算机进行调试")
    val ALLOW_TEXT_B get() = tr(", then choose ", "，然后选择")
    val ALLOW_BUTTON get() = tr("Allow", "允许")
    val FINGERPRINT_LABEL get() = tr("The TV should show this fingerprint", "电视上应显示这个指纹")
    val FINGERPRINT_HINT get() = tr("If it shows a different one, choose Cancel on the TV.", "如果显示的不一样，请在电视上选择取消。")
    val STOP_WAITING get() = tr("Stop waiting", "停止等待")
    val REFUSED get() = tr(
        "The TV didn't allow this phone. It may have timed out, or Cancel was chosen.",
        "电视没有允许这台手机。可能已超时，或选择了取消。",
    )
    val ASK_AGAIN get() = tr("Ask the TV again", "再次询问电视")
    val CHECKING get() = tr("Checking the TV…", "正在检查电视…")
    val DONE_TITLE get() = tr("Only this phone has access", "只有这台手机有权限")
    val DONE_TEXT get() = tr(
        "Tellyscope checked the TV again: this phone is the only allowed computer, and nothing else is connected.",
        "Tellyscope 再次检查了电视：这台手机是唯一被允许的电脑，没有其他连接。",
    )
    val TRUST_WARNING get() = tr(
        "Only allow computers and apps you trust. Anything you allow can install apps, read files and control the TV.",
        "只允许你信任的电脑和应用。你允许的任何一方都可以安装应用、读取文件并控制电视。",
    )
    fun backTo(tv: String) = tr("Back to $tv", "返回 $tv")
    val STILL_TITLE get() = tr("Another computer is still connected", "另一台电脑仍在连接")
    fun stillText(who: String, tv: String) = tr(
        "Tellyscope still sees $who on $tv. It may have connected again straight away. Turn it off or unplug it from the network, then try again.",
        "Tellyscope 在 $tv 上仍能看到 $who。它可能立刻又连上了。请关掉它或断开网络，然后重试。",
    )
    fun stillOpenText(who: String, tv: String) = tr(
        "Tellyscope still sees $who on $tv. Its access is gone, but the TV keeps a connection open until it ends. Restart the TV to end it.",
        "Tellyscope 在 $tv 上仍能看到 $who。权限已经没有了，但电视会保持这条连接直到结束。重启电视即可断开。",
    )
    val CHECK_AGAIN get() = tr("Check again", "再检查一次")
    val RESTART_TV get() = tr("Restart the TV", "重启电视")
    val RESTARTING_TO_CHECK get() = tr("Restarting the TV…", "正在重启电视…")
    fun connectionFrom(address: String) = tr("a connection from $address", "来自 $address 的连接")
    val TRY_AGAIN get() = tr("Try again", "重试")
    val UNCONFIRMED_TITLE get() = tr("Revoked on the TV", "已在电视上撤销")
    val UNCONFIRMED_TEXT get() = tr(
        "Tellyscope can't check the list on this TV. Look under Developer options to confirm.",
        "Tellyscope 无法检查这台电视上的列表。请到开发者选项里确认。",
    )
    val DONE get() = tr("Done", "完成")
    val MANUAL_INTRO get() = tr(
        "This TV didn't take Tellyscope's request, so it's two steps on the TV. Afterwards only this phone can use ADB on it; your own computers will need allowing again.",
        "这台电视没有接受 Tellyscope 的请求，所以需要在电视上做两步。之后只有这台手机能在它上面使用 ADB；你自己的电脑需要重新被允许。",
    )
    val MANUAL_STEP_1 get() = tr("Open Developer options on the TV", "在电视上打开开发者选项")
    val MANUAL_STEP_1_TEXT get() = tr(
        "Tellyscope opens it for you. If nothing happens on the TV, go to Settings → Device Preferences → Developer options.",
        "Tellyscope 会替你打开。如果电视上没有反应，请前往 设置 → 设备偏好设置 → 开发者选项。",
    )
    val OPEN_ON_TV get() = tr("Open on TV", "在电视上打开")
    val MANUAL_STEP_2 get() = tr("Choose “Revoke USB debugging authorisations”", "选择「撤销 USB 调试授权」")
    val MANUAL_STEP_2_TEXT get() = tr(
        "Then come back here, and Tellyscope asks the TV to allow this phone again.",
        "然后回到这里，Tellyscope 会请电视再次允许这台手机。",
    )
    val MANUAL_DONE get() = tr("I've revoked it", "我已经撤销")
    val DEV_OPTIONS_FAILED get() = tr(
        "Couldn't open Developer options on the TV. Go to Settings → Device Preferences → Developer options.",
        "无法在电视上打开开发者选项。请前往 设置 → 设备偏好设置 → 开发者选项。",
    )

    // Connected devices
    val DEVICES_TITLE get() = tr("Connected devices", "已连接的设备")
    fun showAll(n: Int) = tr("Show all ($n)", "显示全部（$n）")
    val SHOW_LESS get() = tr("Show less", "收起")
    val NO_DEVICES get() = tr("Nothing plugged in or paired.", "没有插入或配对的设备。")
    fun group(g: Peripheral.Group) = when (g) {
        Peripheral.Group.Bluetooth -> tr("Bluetooth", "蓝牙")
        Peripheral.Group.Input -> tr("Input", "输入设备")
        Peripheral.Group.Storage -> tr("USB storage", "USB 存储")
        Peripheral.Group.Hdmi -> "HDMI-CEC"
        Peripheral.Group.Audio -> tr("Audio output", "音频输出")
    }
    /** The row's right-hand words. Collapsed rows have no group label, so Bluetooth says so there. */
    fun peripheralStatus(p: Peripheral, grouped: Boolean): String {
        val amount = p.free?.let { bytes(it).replace(".0 ", " ") }
        val status = amount?.let { tr("$it free", "剩余 $it") } ?: p.status
        return if (!grouped && p.group == Peripheral.Group.Bluetooth) {
            tr("Bluetooth · ${status.lowercase()}", "蓝牙 · $status")
        } else status
    }
}
