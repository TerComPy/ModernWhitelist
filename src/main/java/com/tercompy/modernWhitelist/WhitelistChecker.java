package com.tercompy.modernWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.logging.Level;

public class WhitelistChecker extends PluginsCommand {


    protected WhitelistChecker(String name) {
        super(name);
        this.setPermission("mw.admin");
        this.setAliases(new ArrayList<>());
        this.setUsage("/mw [args]");
        if(name.equals("mw")) {
            this.setLabel("mw");
        }
    }

    @Override
    @NonNull
    public List<String> tabComplete(@NonNull CommandSender commandSender, @NonNull String alias, @NonNull String[] strings){
        ArrayList<String> listcommand = new ArrayList<>();
        if(strings.length == 1){
            if(commandSender.hasPermission("mw.admin")){
                listcommand.add("reload");
                listcommand.add("enable");
                listcommand.add("disable");
                listcommand.add("add");
                listcommand.add("remove");
                if(strings[0].isEmpty()){
                    return listcommand;
                }
                ArrayList<String> userplatye = new ArrayList<>();
                for(String cm: listcommand){
                        if(cm.toLowerCase().startsWith(strings[0].toLowerCase())){
                            userplatye.add(cm);
                        }
                }
                return userplatye;
            }
        }
        else if (strings.length == 2){
            if(strings[0].equals("add")){
                if(commandSender.hasPermission("mw.admin")){
                    for(Player player: commandSender.getServer().getOnlinePlayers()){
                        listcommand.add(player.getName());
                    }
                }
            }
            else if(strings[0].equals("remove")){
                ConfigurationSection section = JavaPlugin.getPlugin(MainClass.class).getConfig().getConfigurationSection("players");
                if(section != null){
                    for(String os: section.getKeys(false)){
                        listcommand.add(section.getString(os));
                    }
                }
                else {
                    JavaPlugin.getPlugin(MainClass.class).getLogger().log(Level.WARNING, "Конфиг настроен некорректно!");
                }

            }
        }
        return listcommand;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(commandSender instanceof Player && !commandSender.hasPermission("mw.admin")){
            String msg = JavaPlugin.getPlugin(MainClass.class).getConfig().getString("denied-message");
            assert msg != null;
            String mst = ChatColor.translateAlternateColorCodes('&', msg);
            commandSender.sendMessage(mst);
            return true;
        }
        else {
            if(!(strings.length > 0)){
                commandSender.sendMessage(ChatColor.DARK_RED + "Не указаны аргументы команды! Доступные: add,remove,reload,enable,disable!");
                return true;
            }
            if(Objects.equals(strings[0], "add")){
                if(!(strings.length > 1)){
                    commandSender.sendMessage(ChatColor.DARK_RED + "Не указано имя игрока!");
                    return true;
                }
                Player player = commandSender.getServer().getPlayer(strings[1]);
                if(player == null){
                    commandSender.sendMessage(ChatColor.DARK_RED + "Ошибка в никнейме игрока, либо данный игрок вышел с сервера");
                    return true;
                }
                Configuration configuration = JavaPlugin.getPlugin(MainClass.class).getConfig();
                ConfigurationSection section = configuration.getConfigurationSection("players");
                if(section.getKeys(false).contains(Bukkit.getPlayer(strings[1]).getAddress().getAddress().getHostAddress().replace('.', '_')) && Objects.equals(section.getString(Bukkit.getPlayer(strings[1]).getAddress().getAddress().getHostAddress().replace('.', '_')), player.getName())){
                    commandSender.sendMessage(ChatColor.DARK_RED + "Данный игрок уже добавлен в белый список");
                    return true;
                }
                section.set(Bukkit.getPlayer(strings[1]).getAddress().getAddress().getHostAddress().replace('.', '_'), Bukkit.getPlayer(strings[1]).getName());
                configuration.set("players", section);
                JavaPlugin.getPlugin(MainClass.class).saveConfig();
                JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                commandSender.sendMessage(ChatColor.GREEN + "Успешно добавлен новый игрок!");
                return true;
            } else if (Objects.equals(strings[0], "remove")){
                if(!(strings.length > 1)){
                    commandSender.sendMessage(ChatColor.DARK_RED + "Не указано имя игрока!");
                    return true;
                }
                else{
                    Configuration configuration = JavaPlugin.getPlugin(MainClass.class).getConfig();
                    ConfigurationSection section = configuration.getConfigurationSection("players");
                    assert section != null;
                    if(MainClass.isVault){
                        if(MainClass.hasofflinepermis(Bukkit.getOfflinePlayer(strings[1]), "mw.admin")){
                            commandSender.sendMessage(ChatColor.DARK_RED + "Администратор не может быть удален!!");
                            return true;
                        }
                    } else {
                        if(Bukkit.getOperators().contains(Bukkit.getOfflinePlayer(strings[1]))){
                            commandSender.sendMessage(ChatColor.DARK_RED + "Администратор не может быть удален!!");
                            return true;
                        }
                    }
                    int index = 0;
                    for(String obj: section.getKeys(false)){
                        index++;
                        if(Objects.equals(section.getString(obj), strings[1])){
                            section.set(obj, null);
                            commandSender.sendMessage(ChatColor.GREEN + "Успешное удаление!");
                            configuration.set("players", section);
                            JavaPlugin.getPlugin(MainClass.class).saveConfig();
                            JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                            if(configuration.getBoolean("enabled")) {
                                for (Player pl : JavaPlugin.getPlugin(MainClass.class).getServer().getOnlinePlayers()) {
                                    if (pl.getName().equals(strings[1])) {
                                        pl.kickPlayer(ChatColor.DARK_RED + "Вас исключили из вайт-листа!");
                                    }
                                }
                                return true;
                            }
                        } else {
                            if(index == section.getKeys(false).size()){
                                commandSender.sendMessage(ChatColor.GREEN + "Не нашли данного игрока в списке!");
                                return true;
                            }
                        }
                    }

                }
            }
            else if(Objects.equals(strings[0], "reload")){
                JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                Configuration configuration = JavaPlugin.getPlugin(MainClass.class).getConfig();
                ConfigurationSection section = configuration.getConfigurationSection("players");
                assert section != null;
                boolean conf = configuration.getBoolean("enabled");
                MainClass.Activated = conf;
                if (conf) {
                    for(Player pl: JavaPlugin.getPlugin(MainClass.class).getServer().getOnlinePlayers()){
                        if(!section.getKeys(false).contains(pl.getAddress().getAddress().getHostAddress().replace('.', '_'))){
                            if(pl.hasPermission("mw.admin")){
                                section.set(pl.getAddress().getAddress().getHostAddress().replace('.', '_'), pl.getName());
                                configuration.set("players", section);
                                JavaPlugin.getPlugin(MainClass.class).saveConfig();
                                JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                                Bukkit.getLogger().info(String.format("Администратору %s назначен новый IP", pl.getName()));
                                commandSender.sendMessage(ChatColor.GREEN + "Конфиг успешно перезагружен!");
                                return true;
                            }
                            pl.kickPlayer(ChatColor.DARK_RED + "Вас исключили из вайт-листа!");
                        }
                    }
                }
                commandSender.sendMessage(ChatColor.GREEN + "Конфиг успешно перезагружен!");
                return true;
            }
            else if (Objects.equals(strings[0], "enable")){
                Configuration configuration = JavaPlugin.getPlugin(MainClass.class).getConfig();
                boolean enb = configuration.getBoolean("enabled");
                if(enb){
                    commandSender.sendMessage(ChatColor.DARK_RED + "Вайт-лист уже активирован");
                    return true;
                }
                else{
                    enb = true;
                    configuration.set("enabled", enb);
                    JavaPlugin.getPlugin(MainClass.class).saveConfig();
                    JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                    MainClass.Activated = true;
                    commandSender.sendMessage(ChatColor.GREEN + "Вайт-лист активирован!");
                    ConfigurationSection section = configuration.getConfigurationSection("players");
                    assert section != null;
                    for(Player pl: JavaPlugin.getPlugin(MainClass.class).getServer().getOnlinePlayers()){
                        if(!section.getKeys(false).contains(pl.getAddress().getAddress().getHostAddress().replace('.', '_'))){
                            pl.kickPlayer(ChatColor.DARK_RED + "Вайт-лист был активирован, однако вы не включены в него!");
                        }
                    }
                    return true;
                }
            }
            else if (Objects.equals(strings[0], "disable")){
                Configuration configuration = JavaPlugin.getPlugin(MainClass.class).getConfig();
                boolean enb = configuration.getBoolean("enabled");
                if(!enb){
                    commandSender.sendMessage(ChatColor.DARK_RED + "Вайт-лист уже деактивирован");
                    return true;
                }
                else{
                    enb = false;
                    configuration.set("enabled", enb);
                    JavaPlugin.getPlugin(MainClass.class).saveConfig();
                    MainClass.Activated = false;
                    JavaPlugin.getPlugin(MainClass.class).reloadConfig();
                    commandSender.sendMessage(ChatColor.GREEN + "Вайт-лист деактивирован!");
                    return true;
                }
            }

        }
        return false;
    }
}
