package me.gei.tiatareas.internal.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import me.gei.tiatareas.internal.bo.Area
import me.gei.tiatareas.internal.serializers.AreaListSerializer
import me.gei.tiatareas.internal.serializers.AreaMapSerializer
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.function.submitAsync
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 缓存优先容器 Cache-First
 * 用于缓存数据并在一定时间后写入数据库
 * 数据库数据不同步给缓存
 *
 * @property user 用户标识
 * @property database 数据库实例
 */
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
class DataContainer(private val user: String, private val database: Database) {

    private val json = Json {
        serializersModule = SerializersModule {
            contextual(AreaListSerializer)
            contextual(AreaMapSerializer)
        }
    }

    /** 缓存 **/
    var discoveringAreas: DiscoveringAreasMap
        private set
    var discoveredAreas: DiscoveredAreasList
        private set

    var initialized = false
        private set

    init {
        discoveringAreas = getDiscoveringAreasMap()
        discoveredAreas = getDiscoveredAreasList()
        initialized = true
    }

    inner class DiscoveringAreasMap: ConcurrentHashMap<Area, String> {

        constructor(): super()
        constructor(map: ConcurrentHashMap<Area, String>): super(map)

        override fun put(key: Area, value: String): String? {
            val result = super.put(key, value)
            updateDiscoveringAreasMap()
            return result
        }

        override fun remove(key: Area): String? {
            val result = super.remove(key)
            updateDiscoveringAreasMap()
            return result
        }
    }

    inner class DiscoveredAreasList: CopyOnWriteArrayList<Area> {

        constructor(): super()
        constructor(list: CopyOnWriteArrayList<Area>): super(list)

        override fun add(element: Area): Boolean {
            val result = super.add(element)
            updateDiscoveredAreasList()
            return result
        }

        override fun remove(element: Area): Boolean {
            val result = super.remove(element)
            updateDiscoveredAreasList()
            return result
        }
    }

    /**
     * 反序列化从数据库获取
     */
    private fun getDiscoveringAreasMap(): DiscoveringAreasMap {
        val serializedMap = database.getDiscovering(user)
        return if (serializedMap == null) DiscoveringAreasMap()
        else DiscoveringAreasMap(json.decodeFromString<ConcurrentHashMap<Area, String>>(serializedMap))
    }

    /**
     * 反序列化从数据库获取
     */
    private fun getDiscoveredAreasList(): DiscoveredAreasList {
        val serializedList = database.getDiscovered(user)
        return if (serializedList == null) DiscoveredAreasList()
        else  DiscoveredAreasList(json.decodeFromString<CopyOnWriteArrayList<Area>>(serializedList))
    }

    /**
     * 更新指定键的值到数据库
     */
    private fun updateDiscoveringAreasMap() {
        submitAsync {
            val jsonString =
                if (discoveringAreas.isEmpty()) null
                else json.encodeToString(discoveringAreas as ConcurrentHashMap<Area, String>)
            database.setDiscovering(user, jsonString)
        }
    }

    /**
     * 更新指定键的值到数据库
     */
    private fun updateDiscoveredAreasList() {
        submitAsync {
            val jsonString =
                if (discoveredAreas.isEmpty()) null
                else json.encodeToString(discoveredAreas as CopyOnWriteArrayList<Area>)
            database.setDiscovered(user, jsonString)
        }
    }

    fun updateAll() {
        updateDiscoveredAreasList()
        updateDiscoveringAreasMap()
    }
}