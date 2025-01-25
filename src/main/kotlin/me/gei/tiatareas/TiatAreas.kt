package me.gei.tiatareas

import me.gei.tiatareas.internal.config.TaConfig
import me.gei.tiatareas.internal.managers.DatabaseManager
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.chat.colored
import taboolib.platform.BukkitPlugin

object TiatAreas : Plugin() {

    override fun onEnable() {
        info("&a  _____ _       _        _                       ".colored())
        info("&a |_   _(_) __ _| |_     / \\   _ __ ___  __ _ ___ ".colored())
        info("&a   | | | |/ _` | __|   / _ \\ | '__/ _ \\/ _` / __|".colored())
        info("&a   | | | | (_| | |_   / ___ \\| | |  __/ (_| \\__ \\".colored())
        info("&a   |_| |_|\\__,_|\\__| /_/   \\_\\_|  \\___|\\__,_|___/".colored())
        info("&a                                                 ".colored())

        TaConfig.load()
        DatabaseManager.setup()
    }

    fun getInstance() : BukkitPlugin { return BukkitPlugin.getInstance(); }
}