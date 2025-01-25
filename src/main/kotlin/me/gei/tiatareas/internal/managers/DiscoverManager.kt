package me.gei.tiatareas.internal.managers

import ink.ptms.uw.common.worldguard.events.RegionEnterEvent
import ink.ptms.uw.common.worldguard.events.RegionLeaveEvent
import me.gei.tiatareas.api.events.AreaDiscoverEvent
import me.gei.tiatareas.api.events.AreaEnterEvent
import me.gei.tiatareas.api.events.AreaLeaveEvent
import me.gei.tiatareas.internal.bo.Area
import me.gei.tiatareas.internal.config.TaConfig
import me.gei.tiatareas.internal.managers.DatabaseManager.getDataContainer
import me.gei.tiatareas.internal.managers.DatabaseManager.isDatabaseInitialized
import me.gei.tiatareas.internal.managers.DiscoverProgressManager.setupDiscoverTimer
import me.gei.tiatareas.internal.managers.DiscoverProgressManager.stopDiscoverTimer
import me.gei.tiatareas.internal.pojo.PlayerDiscoverInfo
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object DiscoverManager {

    val areaPlayers = ConcurrentHashMap<Area, CopyOnWriteArrayList<Player>>()

    @SubscribeEvent
    private fun onRegionEnter(e: RegionEnterEvent) {
        if(!e.player.isDatabaseInitialized()) return

        //处理CustomStructures区域
        val regionId = e.region.id.let {
            if(it.contains("CustomStructuresAutoGen"))
                it.split("_")[2]
            else it
        }

        TaConfig.areas
            .filter { it.world == e.player.world.name }
            .firstOrNull { it.regionID == regionId }
            ?.let {
               if(!e.player.hasDiscovered(it)) {
                   e.player.setupDiscoverTimer(it)
               } else {
                   val playerDiscoverInfo = PlayerDiscoverInfo(e.player.getDataContainer().discoveringAreas.toMap(), e.player.getDataContainer().discoveredAreas.toList())
                   AreaEnterEvent(e.player, it, playerDiscoverInfo).call()
               }
            }
    }

    @SubscribeEvent
    private fun onRegionLeave(e: RegionLeaveEvent) {
        if(!e.player.isDatabaseInitialized()) return

        //处理CustomStructures区域
        val regionId = e.region.id.let {
            if(it.contains("CustomStructuresAutoGen"))
                it.split("_")[2]
            else it
        }

        TaConfig.areas
            .filter { it.world == e.player.world.name }
            .firstOrNull { it.regionID == regionId }
            ?.let {
                if(!e.player.hasDiscovered(it)) {
                    e.player.stopDiscoverTimer(it)
                } else {
                    val playerDiscoverInfo = PlayerDiscoverInfo(e.player.getDataContainer().discoveringAreas.toMap(), e.player.getDataContainer().discoveredAreas.toList())
                    AreaLeaveEvent(e.player, it, playerDiscoverInfo).call()
                }
            }
    }

    @SubscribeEvent
    private fun onAreaDiscover(e: AreaDiscoverEvent) {
        e.player.sendLang(
            "Discover",
            e.area.name ?: "",
            e.area.description?.joinToString(separator = "\n") ?: ""
        )

        submit {
            e.area.rewardCommands?.forEach {
                console().performCommand(
                    it.replace("%player%", e.player.name)
                        .replace("%area%", e.area.name ?: "")
                )
            }
        }

        areaPlayers.computeIfAbsent(e.area) { CopyOnWriteArrayList() }.add(e.player)
    }

    @SubscribeEvent
    private fun onAreaEnter(e: AreaEnterEvent) {
        e.player.sendLang(
            "Enter",
            e.area.name ?: "",
            e.area.description?.joinToString(separator = "\n") ?: ""
        )

        areaPlayers.computeIfAbsent(e.area) { CopyOnWriteArrayList() }.add(e.player)
    }

    @SubscribeEvent
    private fun onAreaLeave(event: AreaLeaveEvent) {
        areaPlayers[event.area]?.remove(event.player)
    }

    fun Player.hasDiscovered(area: Area): Boolean {
        return this.getDataContainer().discoveredAreas.contains(area)
    }

    fun Player.setDiscovered(area: Area) {
        TaConfig.areas
            .firstOrNull { it == area }
            ?.let {
                val playerDiscoverInfo = PlayerDiscoverInfo(this.player.getDataContainer().discoveringAreas, this.player.getDataContainer().discoveredAreas)
                if(!this.hasDiscovered(it)) {
                    val discoverEvent = AreaDiscoverEvent(this, it, playerDiscoverInfo)
                    discoverEvent.call()

                    if(!discoverEvent.isCancelled) {
                        this.getDataContainer().discoveredAreas.add(it)
                    }
                }
            }
    }

    fun Player.setUndiscovered(area: Area) {
        if(this.hasDiscovered(area)) {
            this.getDataContainer().discoveredAreas.remove(area)
        }
    }

    fun Player.getDiscoverProgress(area: Area): String {
        return if(this.hasDiscovered(area)) {
            "100%"
        } else {
            if(this.getDataContainer().discoveringAreas.contains(area)) {
                this.getDataContainer().discoveringAreas[area]!!
            } else "0%"
        }
    }
}