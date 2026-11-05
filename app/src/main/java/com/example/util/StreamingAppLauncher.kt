package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

object StreamingAppLauncher {

    private const val TAG = "StreamingAppLauncher"

    fun getCandidatePackages(providerName: String): List<String> {
        val lower = providerName.lowercase()
        return when {
            lower.contains("prime") || lower.contains("amazon") -> listOf(
                "com.amazon.amazonvideo.livingroom",
                "com.amazon.avod.thirdpartyclient",
                "com.amazon.amazonvideo.livingroom.nvidia",
                "com.amazon.aiv.eu",
                "com.amazon.avod",
                "com.amazon.mp3"
            )
            lower.contains("netflix") -> listOf(
                "com.netflix.ninja",
                "com.netflix.mediaclient"
            )
            lower.contains("disney") -> listOf(
                "com.disney.disneyplus"
            )
            lower.contains("apple") -> listOf(
                "com.apple.atve.androidtv.appletv",
                "com.apple.atve.sony.appletv"
            )
            lower.contains("wow") || lower.contains("sky") -> listOf(
                "de.sky.online",
                "com.bskyb.nowtv.beta"
            )
            lower.contains("rtl") || lower.contains("tvnow") -> listOf(
                "de.rtli.tvnow",
                "de.rtli.tvnow.firetv"
            )
            lower.contains("joyn") -> listOf(
                "de.joyn.android.tv",
                "de.prosiebensat1digital.seventv"
            )
            lower.contains("paramount") -> listOf(
                "com.paramountplus.androidtv",
                "com.cbs.app",
                "com.cbs.ott"
            )
            lower.contains("youtube") -> listOf(
                "com.google.android.youtube.tv",
                "com.google.android.youtube"
            )
            lower.contains("crunchyroll") -> listOf(
                "com.crunchyroll.crunchyroid"
            )
            else -> emptyList()
        }
    }

    fun launch(
        context: Context,
        providerName: String,
        title: String,
        explicitCandidates: List<String>,
        fallbackUrl: String?
    ) {
        val pm = context.packageManager
        val candidates = (explicitCandidates + getCandidatePackages(providerName)).distinct()

        Log.d(TAG, "Attempting to launch app for provider: '$providerName' (candidates: $candidates)")

        // 1. Try direct package launch intents from candidate list
        for (pkg in candidates) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "Öffne $providerName App...", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Successfully launched package: $pkg")
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed launching candidate package $pkg: ${e.message}")
            }
        }

        // 2. Fuzzy search installed packages on this TV device
        val lowerProvider = providerName.lowercase()
        try {
            val installedPackages = pm.getInstalledPackages(0)
            val matchedPackage = installedPackages.firstOrNull { pkgInfo ->
                val p = pkgInfo.packageName.lowercase()
                when {
                    lowerProvider.contains("prime") || lowerProvider.contains("amazon") ->
                        p.contains("amazonvideo") || p.contains("primevideo") || p.contains("avod")
                    lowerProvider.contains("netflix") -> p.contains("netflix")
                    lowerProvider.contains("disney") -> p.contains("disneyplus")
                    lowerProvider.contains("apple") -> p.contains("atve") || (p.contains("apple") && p.contains("tv"))
                    lowerProvider.contains("wow") || lowerProvider.contains("sky") -> p.contains("sky.online") || p.contains("nowtv")
                    lowerProvider.contains("rtl") || lowerProvider.contains("tvnow") -> p.contains("tvnow") || p.contains("rtli")
                    lowerProvider.contains("joyn") -> p.contains("joyn") || p.contains("seventv")
                    lowerProvider.contains("paramount") -> p.contains("paramount") || p.contains("cbs.app")
                    else -> false
                }
            }

            if (matchedPackage != null) {
                val launchIntent = pm.getLaunchIntentForPackage(matchedPackage.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "Öffne $providerName App...", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Successfully launched fuzzy matched package: ${matchedPackage.packageName}")
                    return
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fuzzy package search error: ${e.message}")
        }

        // 3. Try custom scheme intents (e.g. primevideo://, netflix://)
        val schemeUri = when {
            lowerProvider.contains("prime") || lowerProvider.contains("amazon") -> Uri.parse("primevideo://")
            lowerProvider.contains("netflix") -> Uri.parse("netflix://")
            lowerProvider.contains("disney") -> Uri.parse("disneyplus://")
            else -> null
        }
        if (schemeUri != null) {
            try {
                val schemeIntent = Intent(Intent.ACTION_VIEW, schemeUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (schemeIntent.resolveActivity(pm) != null) {
                    context.startActivity(schemeIntent)
                    Toast.makeText(context, "Öffne $providerName...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Scheme launch failed: ${e.message}")
            }
        }

        // 4. If the app is genuinely not installed on the TV, try Google Play Store on TV
        val primaryPackage = candidates.firstOrNull()
        if (primaryPackage != null) {
            try {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$primaryPackage")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (storeIntent.resolveActivity(pm) != null) {
                    Toast.makeText(context, "$providerName App nicht installiert. Öffne Play Store...", Toast.LENGTH_LONG).show()
                    context.startActivity(storeIntent)
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "Play Store launch failed: ${e.message}")
            }
        }

        // 5. Last resort: web fallback
        if (!fallbackUrl.isNullOrBlank()) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "$providerName App konnte nicht gefunden werden.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
