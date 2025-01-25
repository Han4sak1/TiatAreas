package me.gei.tiatareas.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.gei.tiatareas.internal.bo.Area
import me.gei.tiatareas.internal.config.TaConfig
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import java.util.concurrent.ConcurrentHashMap

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
object AreaMapSerializer: KSerializer<ConcurrentHashMap<Area, String>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TAAreaMap") {
        element<Map<Area, String>>("map")
    }

    override fun deserialize(decoder: Decoder): ConcurrentHashMap<Area, String> {
        val mapSerializer = MapSerializer(String.serializer(), String.serializer())
        val map = decoder.decodeSerializableValue(mapSerializer).entries.associate {
            TaConfig.areas.find { area -> area.key == it.key }!! to it.value
        }
        return ConcurrentHashMap<Area, String>().apply { putAll(map) }
    }

    override fun serialize(encoder: Encoder, value: ConcurrentHashMap<Area, String>) {
        val mapSerializer = MapSerializer(String.serializer(), String.serializer())
        val mapToSerialize = value.entries.associate { it.key.key to it.value }
        encoder.encodeSerializableValue(mapSerializer, mapToSerialize)
    }
}