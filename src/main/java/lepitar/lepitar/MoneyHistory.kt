package lepitar.lepitar

import encryptsl.cekuj.net.api.enums.OperationType
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.events.AccountEconomyManageEvent
import encryptsl.cekuj.net.api.events.PlayerEconomyPayEvent
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MoneyHistory : Listener {

    private val economy = TheMoreTaxPlugin.econ
    private val config = TheMoreTaxPlugin.instance.config
    private val instance = TheMoreTaxPlugin.instance

    @EventHandler
    fun withdraw(e: PlayerEconomyPayEvent) {
        val sender: Player = e.sender
        val target: OfflinePlayer = e.target
        val money: Double = e.money
        val balance = economy.getBalance(sender as OfflinePlayer)

        if (money > balance) {
            e.isCancelled = true
            sender.sendMessage("§c돈이 부족합니다.")
            return
        }

        if (e.transactionType == TransactionType.PAY) {
            config.set("richRate.${target.uniqueId}.name", target.name)
            config.set("richRate.${target.uniqueId}.money", config.getInt("richRate.${target.uniqueId}.money") + money.toInt())
            instance.saveConfig()
        }

    }
}
