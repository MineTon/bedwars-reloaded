package io.github.yannici.bedwarsreloaded;

import io.github.yannici.bedwarsreloaded.Commands.*;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.GameManager;
import io.github.yannici.bedwarsreloaded.Game.GameState;
import io.github.yannici.bedwarsreloaded.Game.RessourceSpawner;
import io.github.yannici.bedwarsreloaded.Game.Team;
import io.github.yannici.bedwarsreloaded.Listener.BlockListener;
import io.github.yannici.bedwarsreloaded.Listener.EntityListener;
import io.github.yannici.bedwarsreloaded.Listener.PlayerListener;
import io.github.yannici.bedwarsreloaded.Listener.ServerListener;
import io.github.yannici.bedwarsreloaded.Listener.WeatherListener;
import io.github.yannici.bedwarsreloaded.Localization.LocalizationConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import com.google.common.collect.ImmutableMap;

public class Main extends JavaPlugin {

    private static Main instance = null;

    private ArrayList<BaseCommand> commands = new ArrayList<BaseCommand>();
    private BukkitTask timeTask = null;
    private Package craftbukkit = null;
    private Package minecraft = null;
    private String version = null;
    private LocalizationConfig localization = null;

    private ScoreboardManager scoreboardManager = null;
    private GameManager gameManager = null;

    public Main() {
        this.registerConfigurationClasses();
    }

    @Override
    public void onEnable() {
        Main.instance = this;

        this.craftbukkit = this.getCraftBukkit();
        this.minecraft = this.getMinecraftPackage();
        this.version = this.loadVersion();
        
        /*if(Utils.materialIsColorable(Material.LEATHER_CHESTPLATE)) {
            this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage("COLORABLE!!!!"));
        }*/
        
        /*for(Constructor m : this.getMinecraftServerClass("EntityVillager").getDeclaredConstructors()) {
            StringBuilder sb = new StringBuilder();
            sb.append(m.getName() + ": ");
            for(java.lang.reflect.Parameter p : m.getParameters()) {
                sb.append(p.getType() + " " + p.getName() + ", ");
            }
            
            this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(sb.toString()));
        }*/
        
        /*for(Method m : this.getMinecraftServerClass("EntityVillager").getMethods()) {
            if(m.getName().equals("a_")) {
                StringBuilder sb = new StringBuilder(m.getName() + ": " + m.getReturnType().getName());
                for(Parameter p : m.getParameters()) {
                    sb.append(" >> " + p.getType().getName() + " " + p.getName());
                }
                this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(sb.toString()));
            }
        }*/
        
        this.registerCommands();
        this.registerListener();
        
        this.gameManager = new GameManager(this);
        this.saveDefaultConfig();
        this.localization = this.loadLocalization();
        
        // Loading
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.gameManager.loadGames();
        this.startTimeListener();
    }

    @Override
    public void onDisable() {
        this.stopTimeListener();
        this.gameManager.unloadGames();
    }
    
    private LocalizationConfig loadLocalization() {
    	LocalizationConfig config = new LocalizationConfig();
    	config.saveLocales();
    	
    	config.loadLocale(this.getConfig().getString("locale"), false);
    	return config;
    }
    
    public LocalizationConfig getLocalization() {
    	return this.localization;
    }
    
    private String loadVersion() {
        String packName = Bukkit.getServer().getClass().getPackage().getName();
        return packName.substring(packName.lastIndexOf('.') + 1);
    }
    
    public String getCurrentVersion() {
        return this.version;
    }
    
    public boolean isBungee() {
    	return this.getConfig().getBoolean("bungeecord.enabled");
    }
    
    public String getBungeeHub() {
    	if(this.getConfig().contains("bungeecord.hubserver")) {
    		return this.getConfig().getString("bungeecord.hubserver");
    	}
    	
    	return null;
    }

    public Package getCraftBukkit() {
        try {
            if(this.craftbukkit == null) {
                return Package.getPackage("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
            } else {
                return this.craftbukkit;
            }
        } catch(Exception ex) {
            this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.packagenotfound", ImmutableMap.of("package", "craftbukkit"))));
            return null;
        }
    }

    public Package getMinecraftPackage() {
        try {
            if(this.minecraft == null) {
                return Package.getPackage("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
            } else {
                return this.minecraft;
            }
        } catch(Exception ex) {
            this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.packagenotfound", ImmutableMap.of("package", "minecraft server"))));
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public Class getCraftBukkitClass(String classname) {
        try {
            if(this.craftbukkit == null) {
                this.craftbukkit = this.getCraftBukkit();
            }

            return Class.forName(this.craftbukkit.getName() + "." + classname);
        } catch(Exception ex) {
            this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.classnotfound", ImmutableMap.of("package", "craftbukkit", "class", classname))));
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public Class getMinecraftServerClass(String classname) {
        try {
            if(this.minecraft == null) {
                this.minecraft = this.getMinecraftPackage();
            }

            return Class.forName(this.minecraft.getName() + "." + classname);
        } catch(Exception ex) {
            this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.classnotfound", ImmutableMap.of("package", "minecraft server", "class", classname))));
            return null;
        }
    }
    
    public String getFallbackLocale() {
    	return "en";
    }

    public static Main getInstance() {
        return Main.instance;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    private void registerListener() {
        new WeatherListener();
        new BlockListener();
        new PlayerListener();
        new EntityListener();
        new ServerListener();
    }

    private void registerConfigurationClasses() {
        ConfigurationSerialization.registerClass(RessourceSpawner.class, "RessourceSpawner");
        ConfigurationSerialization.registerClass(Team.class, "Team");
    }

    private void registerCommands() {
        this.commands.add(new HelpCommand(this));
        this.commands.add(new SetSpawnerCommand(this));
        this.commands.add(new AddGameCommand(this));
        this.commands.add(new StartGameCommand(this));
        this.commands.add(new StopGameCommand(this));
        this.commands.add(new SetRegionCommand(this));
        this.commands.add(new AddTeamCommand(this));
        this.commands.add(new SaveGameCommand(this));
        this.commands.add(new JoinGameCommand(this));
        this.commands.add(new SetSpawnCommand(this));
        this.commands.add(new SetLobbyCommand(this));
        this.commands.add(new LeaveGameCommand(this));
        this.commands.add(new SetBedCommand(this));

        this.getCommand("bw").setExecutor(new BedwarsCommandExecutor(this));
    }

    public ArrayList<BaseCommand> getCommands() {
        return this.commands;
    }
    
    private ArrayList<BaseCommand> filterCommandsByPermission(ArrayList<BaseCommand> commands, String permission) {
    	Iterator<BaseCommand> it = commands.iterator();
    	
    	while(it.hasNext()) {
        	BaseCommand command = it.next();
        	if(!command.getPermission().equals(permission)) {
        		it.remove();
        	}
        }
    	
    	return commands;
    }
    
    @SuppressWarnings("unchecked")
	public ArrayList<BaseCommand> getBaseCommands() {
        ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
        commands = this.filterCommandsByPermission(commands, "base");
        
        return commands;
    }
    
    @SuppressWarnings("unchecked")
	public ArrayList<BaseCommand> getSetupCommands() {
        ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
        commands = this.filterCommandsByPermission(commands, "setup");
        
        return commands;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    private void startTimeListener() {
        this.timeTask = this.getServer().getScheduler().runTaskTimer(this, new Runnable() {

            @Override
            public void run() {
                for(Game g : Main.getInstance().getGameManager().getGames()) {
                    if(g.getState() == GameState.RUNNING) {
                        g.getRegion().getWorld().setTime(1000);
                    }
                }
            }
        }, (long)10*20, (long)10*20);
    }
    
    public static String _l(String localeKey, Map<String, String> params) {
    	return (String)Main.getInstance().getLocalization().get(localeKey, params);
    }
    
    public static String _l(String localeKey) {
    	return (String)Main.getInstance().getLocalization().get(localeKey);
    }

    private void stopTimeListener() {
    	try {
    		this.timeTask.cancel();
    	} catch(Exception ex) {
    		// Timer isn't running. Just ignore.
    	}
    }

}
