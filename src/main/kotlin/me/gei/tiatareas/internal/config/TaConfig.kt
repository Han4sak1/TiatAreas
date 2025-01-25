package me.gei.tiatareas.internal.config

import me.gei.tiatareas.internal.bo.Area
import org.bukkit.Sound
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

object TaConfig {
    @Config("config.yml", autoReload = true)
    lateinit var conf: ConfigFile
        private set

    lateinit var prefix: String

    var areas: ArrayList<Area> = ArrayList()
        private set

    fun load() {
        prefix = conf.getString("Prefix")!!.colored()

        val file = File(getDataFolder(), "Areas")
        if (!file.exists()) {
            releaseResourceFile("Areas/example.yml", true)
        }

        file.walk()
            .filter { it.isFile }
            .filter { it.extension == "yml" }
            .forEach { loadAreas(it) }

        info("&a${areas.size} 个区域配置加载完成!".colored())
    }

    private fun loadAreas(file: File) {
        val areaConf = Configuration.loadFromFile(file, Type.YAML)

        areaConf.getKeys(false).forEach {
            val key = it
            val world = areaConf.getString("$it.world")!!
            val name = areaConf.getString("$it.name")!!.colored()
            val description = areaConf.getStringList("$it.description").colored()
            val command = areaConf.getStringList("$it.command").colored()
            val sound = areaConf.getString("$it.sound").let { s ->
                if(s.isNullOrEmpty()) {
                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP
                } else {
                    Sound.valueOf(s)
                }
            }
            val time = areaConf.getLong("$it.time")

            val regionID: String? = areaConf.getString("$it.region")?.lowercase()
            val isCSArea: Boolean = areaConf.getBoolean("$it.isCSArea", false)

            areas.add(Area(key, regionID, isCSArea, world, name, description, command, sound, time))
        }
    }

    fun reloadAreas() {
        areas.clear()
        load()
    }
}