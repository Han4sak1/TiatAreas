package me.gei.tiatareas.internal.managers

import me.gei.tiatareas.internal.config.TaConfig
import me.gei.tiatareas.internal.dto.DataContainer
import me.gei.tiatareas.internal.dto.Database
import me.gei.tiatareas.internal.dto.TypeSQL
import me.gei.tiatareas.internal.dto.TypeSQLite
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.io.newFile
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submitAsync
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.database.HostSQL
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object DatabaseManager {

    /**
     * 玩家数据库实例。
     *
     * 该变量用于存储玩家数据库的引用。初始值为 null，表示数据库尚未初始化。
     * 在设置数据库连接后，此变量将被赋予一个 [Database] 实例。
     */
    private var playerDatabase: Database? = null

    /**
     * 玩家数据容器。
     *
     * 该变量用于存储玩家的数据容器。它是一个线程安全的并发哈希映射，
     * 以玩家的 UUID 为键，对应的 [DataContainer] 为值。
     * 这允许快速、安全地访问和修改玩家的数据。
     */
    private val playerDataContainer = ConcurrentHashMap<UUID, DataContainer>()

    fun setup() {
        try {
            if (TaConfig.conf.getBoolean("Database.enable")) {
                setupDatabase(TaConfig.conf.getConfigurationSection("Database")!!)
            } else {
                setupDatabase(newFile(getDataFolder(), "data.db"))
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            disablePlugin()
            return
        }
    }

    private fun setupDatabase(
        conf: ConfigurationSection,
        table: String = conf.getString("table", "")!!,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
    ) {
        val hostSQL = HostSQL(conf)
        if (clearFlags) {
            hostSQL.flags.clear()
        }
        hostSQL.flags.addAll(flags)
        if (ssl != null) {
            hostSQL.flags -= "useSSL=false"
            hostSQL.flags += "sslMode=$ssl"
        }
        playerDatabase = Database(TypeSQL(hostSQL, table))
    }

    private fun setupDatabase(file: File = newFile(getDataFolder(), "data.db"), table: String? = null) {
        playerDatabase = Database(TypeSQLite(file, table))
    }

    /**
     * 为玩家设置数据容器。
     *
     * @param usernameMode 是否使用用户名模式，默认为 false
     */
    private fun Player.setupDataContainer(usernameMode: Boolean = false) {
        val user = if (usernameMode) name else uniqueId.toString()
        playerDataContainer[uniqueId] = DataContainer(user, playerDatabase!!)
    }

    /**
     * 释放玩家的数据容器。
     */
    private fun Player.releaseDataContainer() {
        //保存数据
        this.getDataContainer().updateAll()
        playerDataContainer.remove(uniqueId)
    }

    /**
     * 获取玩家的数据容器
     *
     * @return 数据容器
     * @throws IllegalStateException 如果数据容器不可用
     */
    fun Player.getDataContainer(): DataContainer {
        return playerDataContainer[uniqueId] ?: error("unavailable")
    }

    @SubscribeEvent(EventPriority.HIGHEST)
    private fun onPlayerLogin(e: PlayerLoginEvent) {
        submitAsync {
            e.player.setupDataContainer(usernameMode = true)
        }
    }

    @SubscribeEvent(EventPriority.LOWEST)
    private fun onPlayerQuit(e: PlayerQuitEvent) {
        submitAsync {
            e.player.releaseDataContainer()
        }
    }

    /**
     * 数据库是否已经初始化成功
     */
    fun Player.isDatabaseInitialized(): Boolean {
        //容器是否初始化
        return if (playerDataContainer[uniqueId] == null) false
        //数据是否初始化
        else playerDataContainer[uniqueId]!!.initialized
    }
}