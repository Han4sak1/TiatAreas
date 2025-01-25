package me.gei.tiatareas.internal.pojo

import me.gei.tiatareas.internal.bo.Area

class PlayerDiscoverInfo(
    val discovering: Map<Area, String>,
    val discovered: List<Area>
)