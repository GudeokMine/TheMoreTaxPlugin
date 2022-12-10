package lepitar.lepitar

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.BrewingStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.event.inventory.SmithItemEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class SmithingTaxListener : Listener {

    private val economy = TheMoreTaxPlugin.econ
    private val config = TheMoreTaxPlugin.instance.config

    @EventHandler
    fun onPlayerJoin(event: InventoryClickEvent) {
        val type = event.inventory.type
        when (type) {
            InventoryType.SMITHING -> {
                smithing(event)
            }
            InventoryType.BREWING -> {
                BrewTaxListener().brewing(event)
            }
            else -> {
                event.isCancelled = false
            }
        }
    }

    @EventHandler
    private fun brewingResult(e: PrepareSmithingEvent) {
        val item = e.result
        if (item != null) {
            val meta = item.itemMeta
            item.itemMeta = meta.apply {
                meta.lore = listOf(
                    "${ChatColor.RESET}${ChatColor.WHITE}${ChatColor.BOLD}강화에 ${ChatColor.GOLD}${ChatColor.BOLD}${
                        getSmithPrice().toInt()
                    }${ChatColor.WHITE}${ChatColor.BOLD}원이 소모됩니다."
                )
            }
        }
    }

    private fun smithing(e: InventoryClickEvent) {
        val player = e.whoClicked
        val balance = economy.getBalance(player as OfflinePlayer)
        val item = e.currentItem
        if (item != null && item.type != Material.AIR && e.slot == 2) {
            val meta = item.itemMeta
            if (meta.lore == null)
                return

            if (balance >= getSmithPrice()) {
                val response = economy.withdrawPlayer(player as OfflinePlayer, getSmithPrice())
                if (response.transactionSuccess()) {
                    player.sendMessage("출금완료")
                    item.itemMeta = meta.apply {
                        lore = null
                    }
                } else {
                    e.isCancelled = true
                }
            } else {
                player.sendMessage("잔액이 부족합니다. 현재 잔액 ${balance}원")
                e.isCancelled = true
            }
        }
    }

    private fun getSmithPrice(): Double {
        val normal = config.getString("smithPrice.upgradePrice")
        return normal!!.toDouble()
    }

}
