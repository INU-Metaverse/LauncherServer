package kr.goldenmine.inuminecraftlauncher.launcher.models

data class MinecraftAccount(
    val userName: String,
    val uuid: String,
    val accessToken: String,
    val userType: String,
) {
}