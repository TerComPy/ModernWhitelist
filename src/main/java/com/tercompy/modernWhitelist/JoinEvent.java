package com.tercompy.modernWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class JoinEvent implements Listener {

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent event){
        String IP = event.getAddress().getHostAddress();
        Configuration config = JavaPlugin.getPlugin(MainClass.class).getConfig();
        if(MainClass.Activated){
            ConfigurationSection section = config.getConfigurationSection("players");
            assert section != null;
            if(section.getKeys(false).contains(IP.replace('.', '_'))){
                if(!section.getString(IP.replace('.', '_')).equalsIgnoreCase(event.getName().toLowerCase())){
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("denied-name"))));
                } else {
                    event.allow();
                }
            }else{
                if(MainClass.isVault){
                    if(MainClass.hasofflinepermis(Bukkit.getOfflinePlayer(event.getUniqueId()), "mw.admin")){
                        section.set(IP.replace('.', '_'), event.getName());
                        config.set("players", section);
                        JavaPlugin.getPlugin(MainClass.class).saveConfig();
                        JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                        Bukkit.getLogger().info(String.format("Администратору %s присвоен IP %s", event.getName(), event.getAddress().getHostAddress()));
                    }   event.allow();
                }
                else {
                    if(Bukkit.getOperators().contains(Bukkit.getOfflinePlayer(event.getUniqueId()))){
                        section.set(IP.replace('.', '_'), event.getName());
                        config.set("players", section);
                        JavaPlugin.getPlugin(MainClass.class).saveConfig();
                        JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                        Bukkit.getLogger().info(String.format("Администратору %s присвоен IP %s", event.getName(), event.getAddress().getHostAddress()));
                        event.allow();
                    } else {
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("denied-ip"))));
                    }
                }
            }
        }
    }

}
