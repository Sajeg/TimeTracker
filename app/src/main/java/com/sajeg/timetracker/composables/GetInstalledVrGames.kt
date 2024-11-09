package com.sajeg.timetracker.composables

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

@SuppressLint("QueryPermissionsNeeded")
fun getInstalledVrGames(context: Context): List<ApplicationInfo> {
    val packageManager = context.packageManager
    val apps = mutableListOf<ApplicationInfo>()
    val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    for (packageInfo in packages) {
        if (isValidApp(packageInfo.packageName, context)) {
            apps.add(packageInfo)
        }
    }

    return apps
}

fun isValidApp(packageName: String, context: Context): Boolean {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    val excludedMetaSystemApps = listOf<String>(
        "com.oculus.accountscenter",
        "com.oculus.mobile_mrc_setup",
        "com.oculus.helpcenter",
        "com.meta.handseducationmodule",
        "com.oculus.vrprivacycheckup",
        "com.oculus.environment.prod.bubbles",
        "com.oculus.environment.prod.personaloffice01",
        "com.oculus.environment.prod.personaloffice02",
        "com.meta.environment.prod.stinson.launchpad",
        "com.oculus.environment.prod.adobe",
        "com.oculus.environment.prod.japan",
        "com.meta.environment.prod.storybook",
        "com.meta.environment.prod.rockquarry",
        "com.meta.environment.prod.abstraction",
        "com.oculus.environment.prod.spacestation",
        "com.meta.environment.prod.monolith",
        "com.meta.environment.prod.futurescape",
        "com.meta.environment.prod.lakesidepeak",
        "com.meta.environment.prod.underwater",
        "com.meta.environment.prod.polarvillage",
        "com.meta.environment.prod.vista",
        "com.oculus.environment.prod.cyberhome",
        "com.oculus.environment.prod.rifthome",
        "com.oculus.environment.prod.dome",
        "com.oculus.environment.prod.winterlodge",
        "com.meta.environment.prod.treehouse",
        "com.meta.environment.prod.nuxd",
        "com.meta.environment.prod.bluehillgoldmine",
        "com.sajeg.questrpc"
    )
    if ((packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
        (packageInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0 &&
        !excludedMetaSystemApps.contains(packageInfo.packageName)
    ) {
        return true
    }
    return false
}