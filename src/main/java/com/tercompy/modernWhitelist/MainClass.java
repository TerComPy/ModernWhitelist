package com.tercompy.modernWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.logging.Logger;

public final class MainClass extends JavaPlugin {

    static boolean Activated = false;
    static boolean isVault = false;
    Logger logger = getLogger();
    static RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rpp = null;

    @Override
    public void onEnable() {

        logger.info("Plugin ModernWhitelist is starting up now!");

        saveDefaultConfig();

        Field commandregis = null;
        try {
            commandregis = getServer().getClass().getDeclaredField("commandMap");
            commandregis.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
        try {
            CommandMap mapcmd = (CommandMap) commandregis.get(Bukkit.getServer());
            mapcmd.register(getName(), new WhitelistChecker("mw"));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        logger.info("Command with Tab Completer Registered!");

        Bukkit.getServer().getPluginManager().addPermission(new Permission("mw.admin"));

        logger.info("Permission registered!");

        getServer().getPluginManager().registerEvents(new JoinEvent(), this);

        logger.info("Events registered!");

        Activated = getConfig().getBoolean("enabled");

        if(getServer().getPluginManager().getPlugin("Vault") == null){
            logger.info("Vault doesn't installed! Vault hook disabled!");
            isVault = false;
        }
        else {
            logger.info("Hooking with Vault!");
            rpp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            isVault = true;
            logger.info("Hooked Successfully!");
        }

        logger.info("Initialization Successfully! Welcome! Thanks for trust me! TerComPy (Developer of ModernWhitelist) ");
    }

    public static boolean hasofflinepermis(OfflinePlayer name, String permission){
        return rpp.getProvider().playerHas("world", name, permission);
    }

}
