package me.gei.tiatareas.commands

import me.gei.tiatareas.internal.config.TaConfig
import me.gei.tiatareas.internal.managers.DiscoverManager.setDiscovered
import me.gei.tiatareas.internal.managers.DiscoverManager.setUndiscovered
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang

@CommandHeader("areas", ["ta"])
object TaCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val discover = subCommand {
        player("player") {
            suggestPlayers()
            dynamic("area") {
                suggestion<CommandSender> { _, _ ->
                    TaConfig.areas.map { it.key }
                }

                execute<CommandSender> { sender, context, _ ->
                    val player = context.player("player").castSafely<Player>()
                    TaConfig.areas
                        .firstOrNull{ it.key == context["area"] }
                        ?.let {
                            player?.setDiscovered(it)

                            sender.sendLang(
                                "DiscoverSuccess",
                                TaConfig.prefix,
                                sender.name,
                                it.name ?: ""
                            )
                        }
                }
            }
        }
    }

    @CommandBody
    val unDiscover = subCommand {
        player("player") {
            suggestPlayers()
            dynamic("area") {
                suggestion<CommandSender> { _, _ ->
                    TaConfig.areas.map { it.key }
                }

                execute<CommandSender> { sender, context, _ ->
                    val player = context.player("player").castSafely<Player>()
                    TaConfig.areas
                        .firstOrNull{ it.key == context["area"] }
                        ?.let {
                            player?.setUndiscovered(it)

                            sender.sendLang(
                                "UnDiscoverSuccess",
                                TaConfig.prefix,
                                sender.name,
                                it.name ?: ""
                            )
                        }
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            TaConfig.reloadAreas()
            TaConfig.prefix = TaConfig.conf.getString("Prefix")!!.colored()
            sender.sendLang("ReloadSuccess", TaConfig.prefix)
        }
    }
}