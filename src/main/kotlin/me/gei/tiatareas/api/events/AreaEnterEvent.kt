package me.gei.tiatareas.api.events

import me.gei.tiatareas.internal.bo.Area
import me.gei.tiatareas.internal.pojo.PlayerDiscoverInfo
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class AreaEnterEvent(val player: Player,val area: Area,val playerDiscoverInfo: PlayerDiscoverInfo): BukkitProxyEvent()