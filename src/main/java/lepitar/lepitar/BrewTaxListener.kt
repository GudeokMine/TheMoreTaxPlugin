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
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

class BrewTaxListener : Listener {

    private val economy = TheMoreTaxPlugin.econ
    private val config = TheMoreTaxPlugin.instance.config

    val allowPotion = arrayOf(
        PotionType.WATER,
        PotionType.MUNDANE,
        PotionType.THICK,
        PotionType.AWKWARD
    )

    val potionType = arrayOf(
        Material.POTION,
        Material.SPLASH_POTION,
        Material.LINGERING_POTION
    )

//    @EventHandler
//    fun onPlayerClick(event: InventoryClickEvent) {
//        val type = event.inventory.type
//        event.isCancelled = true
//        when (type) {
//            else -> {
//                event.isCancelled = false
//            }
//        }
//    }

    @EventHandler
    fun onBrewBreak(e: BlockBreakEvent) {
        if (e.block.type == Material.BREWING_STAND) {
            val brewingStand = e.block.state as BrewingStand
            brewingStand.inventory.storageContents.forEach {
                if (it?.type in potionType) {
                    val potionMeta = it?.itemMeta as PotionMeta
                    potionMeta.basePotionData = PotionData(PotionType.WATER)
                    it.itemMeta = potionMeta

                }
            }
        }
    }

    @EventHandler
    fun onBrewExplosion(e: BlockExplodeEvent) {
        if (e.block.type == Material.BREWING_STAND) {
            val brewingStand = e.block.state as BrewingStand
            brewingStand.inventory.storageContents.forEach {
                if (it?.type in potionType) {
                    val potionMeta = it?.itemMeta as PotionMeta
                    potionMeta.basePotionData = PotionData(PotionType.WATER)
                    potionMeta.lore = null
                    it.itemMeta = potionMeta
                }
            }
        }
    }

    @EventHandler
    fun onBrewExplosionByEntity(e: EntityExplodeEvent) {
        e.blockList().forEach { it ->
            if (it.type == Material.BREWING_STAND) {
                val brewingStand = it.state as BrewingStand
                brewingStand.inventory.storageContents.forEach { it ->
                    if (it?.type in potionType) {
                        val potionMeta = it?.itemMeta as PotionMeta
                        potionMeta.basePotionData = PotionData(PotionType.WATER)
                        potionMeta.lore = null
                        it.itemMeta = potionMeta
                    }
                }
            }
        }
    }

    @EventHandler
    fun hopperPrevent(e: InventoryMoveItemEvent) {
        val type = e.destination.type
        when (type) {
            InventoryType.HOPPER -> {
                if (e.source.type == InventoryType.BREWING)
                    e.isCancelled = true
            }
            else -> {
                e.isCancelled = false
            }
        }
    }

    @EventHandler
    private fun brewingResult(e: BrewEvent) {
        for (result in e.results) {
            if (result.type in potionType) {
                if ((result.itemMeta as PotionMeta).basePotionData.type in allowPotion)
                    return

                result.itemMeta = result.itemMeta?.apply {
                    lore = listOf("${ChatColor.RESET}${ChatColor.WHITE}${ChatColor.BOLD}제작에 ${ChatColor.GOLD}${ChatColor.BOLD}${getPotionPrice(result.itemMeta as PotionMeta).toInt()}${ChatColor.WHITE}${ChatColor.BOLD}원이 소모됩니다.")
                }
            }
        }
    }

    fun brewing(e: InventoryClickEvent) {
        val player = e.whoClicked
        val balance = economy.getBalance(player as OfflinePlayer)
        val item = e.currentItem
        if (item != null && item.type != Material.AIR && item.type in potionType) {
            val meta = item.itemMeta
            val basePotionData = (meta as PotionMeta).basePotionData
            if (basePotionData.type in allowPotion || meta.lore == null)
                return

            when (e.slot) {
                0, 1, 2 -> {
                    if (balance >= getPotionPrice(meta)) {
                        val response = economy.withdrawPlayer(player as OfflinePlayer, getPotionPrice(meta))
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
                else -> {
                    e.isCancelled = false
                }
            }
        }
    }

    private fun getPotionPrice(potionData: PotionMeta): Double {
        val normal = config.getString("brewPrice.normalPotions")
        val extendRate = config.getString("brewPrice.extendPotions")
        val upgradeRate = config.getString("brewPrice.upgradePotions")

        return if (potionData.basePotionData.isUpgraded)
            normal!!.toDouble() * upgradeRate!!.toDouble() / 100
        else if (potionData.basePotionData.isExtended)
            normal!!.toDouble() * extendRate!!.toDouble() / 100
        else
            normal!!.toDouble()
    }

}
