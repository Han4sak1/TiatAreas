package me.gei.tiatareas.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
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
import java.util.concurrent.CopyOnWriteArrayList

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
object AreaListSerializer: KSerializer<CopyOnWriteArrayList<Area>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TAAreaList") {
        element<List<String>>("list")
    }

    override fun serialize(encoder: Encoder, value: CopyOnWriteArrayList<Area>) {
        val listSerializer = ListSerializer(String.serializer())
        val listToSerialize = value.map { it.key }
        encoder.encodeSerializableValue(listSerializer, listToSerialize)
    }

    override fun deserialize(decoder: Decoder): CopyOnWriteArrayList<Area> {
        val listSerializer = ListSerializer(String.serializer())
        val list = decoder.decodeSerializableValue(listSerializer).map {
            TaConfig.areas.find { area -> area.key == it  }!!
        }
        return CopyOnWriteArrayList(list)
    }
}