package me.gei.tiatareas.internal.managers

import me.gei.tiatareas.api.DiscoverAPI
import me.gei.tiatareas.internal.config.TaConfig
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.platform.compat.PlaceholderExpansion

object PlaceHolderManager : PlaceholderExpansion {
    override val identifier: String = "TiatAreas"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if(player == null) return TaConfig.conf.getString("Settings.PlaceholderAPI.nullArea")!!.colored()

        when {
            args.lowercase() == "area" -> {
                return DiscoverAPI.getAreas(player.location)
                    .map { it.name }
                    .joinToString(separator = ", ")
            }

            args.lowercase().startsWith("area_") -> {
                return DiscoverAPI.getArea(player.world, args.substringAfter("area_"))?.name ?: ""
            }

            args.lowercase() == "description" -> {
                val description: StringBuilder = StringBuilder()
                DiscoverAPI.getAreas(player.location)
                    .forEach { description.append(it.description?.joinToString(separator = "\n", postfix = "\n\n")) }
                return description.toString()
            }

            args.lowercase().startsWith("description_") -> {
                return DiscoverAPI.getArea(player.world, args.substringAfter("description_"))?.description
                    ?.joinToString(separator = "\n") ?: TaConfig.conf.getString("Settings.PlaceholderAPI.nullArea")!!.colored()
            }

            args.lowercase() == "progress" -> {
                val progress: StringBuilder = StringBuilder()
                val areas = DiscoverAPI.getAreas(player.location)
                areas.forEachIndexed { index, area ->
                        progress.append(area.getDiscoverProgress(player))
                        if(index < areas.lastIndex)
                            progress.append(", ")
                    }
                return progress.toString()
            }

            args.lowercase().startsWith("progress_") -> {
                return DiscoverAPI.getArea(player.world, args.substringAfter("progress_"))
                    ?.getDiscoverProgress(player) ?: TaConfig.conf.getString("Settings.PlaceholderAPI.nullArea")!!.colored()
            }
        }

        return TaConfig.conf.getString("Settings.PlaceholderAPI.nullArea")!!.colored()
    }
}