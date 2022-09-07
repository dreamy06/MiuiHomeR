package com.yuk.miuiHomeR.hook

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.init.InitFields.appContext
import com.github.kyuubiran.ezxhelper.utils.hookAllConstructorAfter
import com.yuk.miuiHomeR.mPrefsMap
import com.yuk.miuiHomeR.utils.ktx.*
import com.zhenxiang.blur.BlurFrameLayout
import com.zhenxiang.blur.model.CornersRadius
import de.robv.android.xposed.XposedHelpers

@RequiresApi(Build.VERSION_CODES.S)
object Dock : BaseHook() {
    override fun init() {

        if (!mPrefsMap.getBoolean("home_dock_blur")) return
        var isShowEditPanel = false
        var isFolderShowing = false
        val launcherClass = "com.miui.home.launcher.Launcher".findClass()
        val launcherStateClass = "com.miui.home.launcher.LauncherState".findClass()
        val folderInfo = "com.miui.home.launcher.FolderInfo".findClass()
        val blurClass = "com.miui.home.launcher.common.BlurUtils".findClass()
        val mDockHeight = dp2px(mPrefsMap.getInt("home_dock_height", 98).toFloat())
        val mDockMargin = dp2px(mPrefsMap.getInt("home_dock_margin", 10).toFloat())
        val mDockBottomMargin = dp2px(mPrefsMap.getInt("home_dock_bottom_margin", 13).toFloat())
        val mDockCorner = dp2px(mPrefsMap.getInt("home_dock_corner", 25).toFloat())
        hookAllConstructorAfter("com.miui.home.launcher.Launcher") {
            var mDockBlur = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mDockBlur")
            if (mDockBlur != null) return@hookAllConstructorAfter
            mDockBlur = BlurFrameLayout(appContext)
            XposedHelpers.setAdditionalInstanceField(it.thisObject, "mDockBlur", mDockBlur)
        }
        launcherClass.hookAfterMethod("onCreate", Bundle::class.java) {
            val mSearchBarContainer = it.thisObject.callMethod("getSearchBarContainer") as FrameLayout
            val mSearchEdgeLayout = mSearchBarContainer.parent as FrameLayout
            val mDockBlur = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mDockBlur") as BlurFrameLayout
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mDockHeight)
            lp.gravity = Gravity.BOTTOM
            lp.setMargins(mDockMargin, 0, mDockMargin, mDockBottomMargin)
            mDockBlur.blurController.apply {
                backgroundColour = Color.parseColor("#44FFFFFF")
                cornerRadius = CornersRadius.all(mDockCorner.toFloat())
                blurRadius = mPrefsMap.getInt("home_blur_radius", 100)
            }
            mDockBlur.layoutParams = lp
            mSearchEdgeLayout.addView(mDockBlur, 0)
            launcherClass.hookAfterMethod("isFolderShowing") { hookParam ->
                isFolderShowing = hookParam.result as Boolean
            }
            launcherClass.hookAfterMethod("showEditPanel", Boolean::class.java) { hookParam ->
                isShowEditPanel = hookParam.args[0] as Boolean
                if (isShowEditPanel) mDockBlur.visibility = View.GONE
                else mDockBlur.visibility = View.VISIBLE
            }
            launcherClass.hookAfterMethod("openFolder", folderInfo, View::class.java) {
                mDockBlur.visibility = View.GONE
            }
            launcherClass.hookAfterMethod("closeFolder", Boolean::class.java) {
                if (!isShowEditPanel) mDockBlur.visibility = View.VISIBLE
            }
            blurClass.hookAfterMethod(
                "fastBlurWhenEnterRecents", launcherClass, launcherStateClass, Boolean::class.java
            ) {
                mDockBlur.visibility = View.GONE
            }
        }
        launcherClass.hookAfterMethod("onStateSetStart", launcherStateClass) {
            val mDockBlur = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mDockBlur") as BlurFrameLayout
            if ("LauncherState" == it.args[0].javaClass.simpleName && !isFolderShowing && !isShowEditPanel) {
                mDockBlur.visibility = View.VISIBLE
            } else mDockBlur.visibility = View.GONE
        }
        "com.miui.home.launcher.DeviceConfig".hookBeforeMethod(
            "calcHotSeatsMarginTop", Context::class.java, Boolean::class.javaPrimitiveType
        ) {
            it.result = dp2px(mPrefsMap.getInt("home_dock_top_margin", 30).toFloat())
        }
        "com.miui.home.launcher.DeviceConfig".hookBeforeMethod(
            "calcHotSeatsMarginBottom",
            Context::class.java,
            Boolean::class.java,
            Boolean::class.java
        ) {
            it.result = dp2px(mPrefsMap.getInt("home_dock_icon_bottom_margin", -8).toFloat())
        }
    }

}