package me.gei.tiatareas.internal.bo

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Polygonal2DRegion
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import ink.ptms.uw.common.worldguard.WorldGuard
import kotlinx.serialization.Serializable
import me.gei.tiatareas.internal.managers.DiscoverManager
import me.gei.tiatareas.internal.managers.DiscoverManager.getDiscoverProgress
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency

@RuntimeDependencies(
    RuntimeDependency(
        "!org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3",
        test = "!kotlinx.serialization.Serializer",
        relocate = ["!kotlin.", "!kotlin1924.", "!kotlinx.serialization.", "!me.gei.tiatareas.kotlinx.serialization163."],
        transitive = false
    ),
    RuntimeDependency(
        "!org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.6.3",
        test = "!kotlinx.serialization.json.Json",
        relocate = ["!kotlin.", "!kotlin1924.", "!kotlinx.serialization.", "!me.gei.tiatareas.kotlinx.serialization163."],
        transitive = false
    )
)
@Serializable
data class Area(
    var key: String,
    var regionID: String?,
    var isCSArea: Boolean,
    var world: String,
    var name: String?,
    var description: List<String>?,
    var rewardCommands: List<String>?,
    var discoverySound: Sound?,
    var discoverTime: Long
) {
    /**
     * 获取一个TiatAreas区域对应的WorldGuard区域
     *
     * @return 在isCSArea为true时返回所有区域组成的列表
     */
    fun getWorldGuardRegion(): List<ProtectedRegion> {
        val regionManager = WorldGuard.API.getRegionManager(Bukkit.getWorld(this.world))

        if(!isCSArea)
            return listOf(regionManager.getRegion(this.regionID)!!)

        val regions: ArrayList<ProtectedRegion> = ArrayList()
        regionManager.getRegions().entries.forEach {
            if(it.key.contains("CustomStructuresAutoGen_${this.world}_${this.regionID}")) {
                regions.add(it.value)
            }
        }

        return regions
    }

    /**
     * 获取一个TiatAreas区域内的所有玩家
     */
    fun getPlayersInArea(): List<Player> {
        return DiscoverManager.areaPlayers[this] ?: emptyList()
    }

    /**
     * 获取区域中心
     *
     * @return 在isCSArea为true时返回所有区域中心组成的列表
     */
    fun getAreaCenter(): List<Location> {
        val locations: ArrayList<Location> = ArrayList()
        this.getWorldGuardRegion().forEach {
            if(it is ProtectedCuboidRegion) {
                val worldEditRegionCenter = CuboidRegion(it.minimumPoint, it.maximumPoint).center
                locations.add(BukkitAdapter.adapt(Bukkit.getWorld(this.world), worldEditRegionCenter))
            }
            else if(it is ProtectedPolygonalRegion) {
                val worldEditRegionCenter = Polygonal2DRegion(BukkitAdapter.adapt(Bukkit.getWorld(this.world)), it.points, it.minimumPoint.y.toInt(), it.maximumPoint.y.toInt()).center
                locations.add(BukkitAdapter.adapt(Bukkit.getWorld(this.world), worldEditRegionCenter))
            }
        }

        return locations
    }

    /**
     * 获取玩家对该区域的解锁进度
     *
     * @return 解锁进度(e.g. 95%)
     */
    fun getDiscoverProgress(player: Player): String {
        return player.getDiscoverProgress(this)
    }
}