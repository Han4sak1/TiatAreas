package me.gei.tiatareas.api

import com.sk89q.worldguard.protection.regions.ProtectedRegion
import ink.ptms.uw.common.worldguard.WorldGuard
import me.gei.tiatareas.internal.bo.Area
import me.gei.tiatareas.internal.config.TaConfig
import me.gei.tiatareas.internal.managers.DiscoverManager.hasDiscovered
import me.gei.tiatareas.internal.managers.DiscoverManager.setDiscovered
import me.gei.tiatareas.internal.managers.DiscoverManager.setUndiscovered
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

object DiscoverAPI {

    /**
     * 获取区域
     */
    fun getArea(world: World, id: String): Area? {
        return TaConfig.areas
            .filter { Bukkit.getWorld(it.world) == world }
            .firstOrNull { it.key == id }
    }

    /**
     * 玩家是否已经发现过此区域
     * @param area 区域名
     *
     * @return 是否发现过此区域或null(区域不存在)
     */
    fun hasDiscover(player: Player, area: Area): Boolean {
        return player.hasDiscovered(area)
    }

    /**
     * 为该玩家解锁此区域
     * @param area 区域名
     */
    fun setDiscovered(player: Player, area: Area) {
        player.setDiscovered(area)
    }

    /**
     * 清除玩家对此区域的发现状态
     * @param area 区域名
     */
    fun setUndiscovered(player: Player, area: Area) {
        player.setUndiscovered(area)
    }

    /**
     * 获取一个位置的WorldGuard区域
     */
    fun getRegions(location: Location): Set<ProtectedRegion> {
        return WorldGuard.API.getRegionManager(location.world).getApplicableRegions(location).regions
    }

    /**
     * 获取一个位置所在的TiatAreas区域
     */
    fun getAreas(location: Location): List<Area> {
        val areas: ArrayList<Area> = ArrayList()
        getRegions(location).forEach { region ->
            TaConfig.areas
                .firstOrNull { it.regionID?.lowercase() == region.id }
                ?.let {
                    areas.add(it)
                }
        }

        return areas
    }

    fun getWorldAreas(world: World): List<Area> {
        return TaConfig.areas
            .filter { Bukkit.getWorld(it.world) == world }
    }
}