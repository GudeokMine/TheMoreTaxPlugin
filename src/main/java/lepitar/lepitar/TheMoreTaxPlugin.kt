package lepitar.lepitar

import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin


class TheMoreTaxPlugin: JavaPlugin() {

    companion object {
        lateinit var econ: Economy
        lateinit var instance: TheMoreTaxPlugin
    }

    override fun onEnable() {
        if (!setupEconomy()) {
            server.pluginManager.disablePlugin(this)
            return
        }
        instance = this
        server.pluginManager.apply {
            registerEvents(BrewTaxListener(), this@TheMoreTaxPlugin)
            registerEvents(SmithingTaxListener(), this@TheMoreTaxPlugin)
            registerEvents(MoneyHistory(), this@TheMoreTaxPlugin)
        }
        config.options().copyDefaults(true)
        saveDefaultConfig()
        logger.info("세금을 땔 준비가되었습니다!")
    }

    override fun onDisable() {
        logger.info("세금이 부족해...!")
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(
            Economy::class.java
        ) ?: return false
        econ = rsp.provider
        return econ != null
    }
}