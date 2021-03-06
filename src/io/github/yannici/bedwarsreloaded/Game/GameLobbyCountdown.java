package io.github.yannici.bedwarsreloaded.Game;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwarsreloaded.Main;

public class GameLobbyCountdown extends BukkitRunnable {

    private Game game = null;
    private int counter = 0;
    private int lobbytime;

    public GameLobbyCountdown(Game game) {
        this.game = game;
        this.counter = Main.getInstance().getConfig().getInt("lobbytime");
        this.lobbytime = this.counter;
    }

    @Override
    public void run() {
        ArrayList<Player> players = this.game.getPlayers();
        float xpPerLevel = 1.0F/this.lobbytime;
        
        for(Player p : players) {
            p.setLevel(this.counter);
            if(this.counter == this.lobbytime) { 
                p.setExp(1.0F);
            } else {
                p.setExp(p.getExp() - xpPerLevel);
            }
            
        }
        
        if(this.game.getState() != GameState.WAITING) {
            this.cancel();
        }

        if(this.counter == this.lobbytime) {
            this.game.broadcast(ChatColor.YELLOW + Main._l("lobby.countdown", ImmutableMap.of("sec", ChatColor.RED.toString() + this.counter + ChatColor.YELLOW)), players);
        }

        if(players.size() < this.game.getMinPlayers()) {
            this.game.broadcast(ChatColor.RED + Main._l("lobby.countdowncancel"), players);
            this.counter = this.lobbytime;
            for(Player p : players) {
                p.setLevel(this.counter);
                p.setExp(1.0F);
            }
            
            this.cancel();
        }

        if(this.counter <= 10 && this.counter > 0) {
            this.game.broadcast(ChatColor.YELLOW + Main._l("lobby.countdown", ImmutableMap.of("sec", ChatColor.RED.toString() + this.counter + ChatColor.YELLOW)), players);
            
            for(Player player : players) {
                player.playSound(player.getLocation(), Sound.CLICK, 20.0F, 20.0F);
            }
        }

        if(this.counter == 0) {
            this.cancel();
            for(Player player : players) {
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 20.0F, 20.0F);
                player.setLevel(0);
                player.setExp(0.0F);
            }
            
            this.game.start(Main.getInstance().getServer().getConsoleSender());
        }
        
        this.counter--;
    }

}
