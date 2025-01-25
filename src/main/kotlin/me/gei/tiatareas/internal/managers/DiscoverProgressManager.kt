package me.gei.tiatareas.internal.managers

import me.gei.tiatareas.internal.bo.Area
import me.gei.tiatareas.internal.config.TaConfig
import me.gei.tiatareas.internal.managers.DatabaseManager.getDataContainer
import me.gei.tiatareas.internal.managers.DiscoverManager.setDiscovered
import org.bukkit.entity.Player
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.service.PlatformExecutor
import taboolib.platform.util.sendLang
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

object DiscoverProgressManager {

    private val timers = ConcurrentHashMap<Player, HashMap<Area, PlatformExecutor.PlatformTask>>()

    fun Player.setupDiscoverTimer(area: Area) {
        val startTime = System.currentTimeMillis()
        val player = this

        //每1秒检查一次
        val discoverTimer =
            submitAsync(delay = 20L, period = 20L) {
                //防止玩家刚开始探索区域立即退出时timers.computeIfAbsent()执行不及时，导致stopDiscoverTimer没有即使析构任务的情况
                if(!player.isOnline) {
                    this.cancel()
                    timers[player]?.remove(area)
                }

                val percentage: String
                val currentTime = System.currentTimeMillis()
                //判断是否从未探索过
                if(!player.getDataContainer().discoveringAreas.containsKey(area)) {
                    val period = Math.round(((currentTime - startTime).toDouble() / (area.discoverTime * 1000).toDouble()) * 100.0)
                    percentage = min(period, 100L).toString() + "%"
                }
                //如果曾经探索过，则继续
                else {
                    val currentPercentage = player.getDataContainer().discoveringAreas[area]!!
                        .removeSuffix("%")
                        .toLong()

                    val period = Math.round(((currentTime - startTime).toDouble() / (area.discoverTime * 1000).toDouble()) * 100.0) + currentPercentage
                    percentage = min(period, 100L).toString() + "%"
                }

                player.getDataContainer().discoveringAreas[area] = percentage
                player.sendLang(
                    "Discovering",
                    TaConfig.prefix,
                    area.name ?: "",
                    percentage
                )


                // 如果达到100%则判断为成功发现
                if(percentage == "100%") {
                    player.setDiscovered(area)
                    player.getDataContainer().discoveringAreas.remove(area)

                    //清理
                    timers[player]!!.remove(area)
                    cancel()
                }
            }


        timers.computeIfAbsent(this) { HashMap() }[area] = discoverTimer
    }

    fun Player.stopDiscoverTimer(area: Area) {
        //如果玩家刚进入未发现区域时立刻退出，此时timers.computeIfAbsent()未执行完成，则直接返回
        if(timers[this] == null) return

        if(timers[this]!!.containsKey(area)) {

            this.sendLang(
                "DiscoverStop",
                TaConfig.prefix,
                area.name ?: ""
            )

            //停止更新并清理
            timers[this]!![area]!!.cancel()
            timers[this]!!.remove(area)
        }
    }
}