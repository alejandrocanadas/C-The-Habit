package com.example.cthehabit.utils

object SocialApps {

    val packages = setOf(
        "com.instagram.android",      // Instagram
        "com.zhiliaoapp.musically",   // TikTok
        "com.zhiliaoapp.musically.go",// TikTok Lite
        "com.reddit.frontpage",       // Reddit
        "com.twitter.android",        // X o / Twitter
        "com.google.android.youtube", // YouTube
        "com.vsco.cam",               // VSCO
        "com.facebook.katana"         // Facebook
    )


    fun getAppName(packageName: String): String {
        val pkg = packageName.lowercase()
        return when {
            pkg.contains("instagram") -> "Instagram"
            pkg.contains("facebook") -> "Facebook"
            pkg.contains("tiktok") || pkg.contains("musically") -> "TikTok"
            pkg.contains("twitter") || pkg.contains("x.") -> "X"
            pkg.contains("reddit") -> "Reddit"
            pkg.contains("youtube") -> "YouTube"
            pkg.contains("vsco") -> "VSCO"
            else -> packageName
        }
    }
}