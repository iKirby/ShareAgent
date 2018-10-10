package me.ikirby.shareagent

import android.content.ComponentName
import android.content.pm.PackageManager

fun PackageManager.setComponentEnabled(component: ComponentName, enabled: Boolean) {
    val state = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    this.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
}

fun PackageManager.isComponentEnabled(component: ComponentName): Boolean {
    return this.getComponentEnabledSetting(component) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
}