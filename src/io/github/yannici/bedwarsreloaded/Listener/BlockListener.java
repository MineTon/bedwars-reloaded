package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.GameState;
import io.github.yannici.bedwarsreloaded.Game.Team;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Bed;

import com.google.common.collect.ImmutableMap;

public class BlockListener extends BaseListener {

    public BlockListener() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();

        Game g = Game.getGameOfPlayer(p);
        if(g == null) {
            return;
        }

        if(g.getState() != GameState.RUNNING && g.getState() != GameState.WAITING) {
            return;
        }

        if(g.getState() == GameState.WAITING) {
            e.setCancelled(true);
            return;
        }

        if(e.getBlock().getType() == Material.BED_BLOCK) {
            e.setCancelled(true);
            
            Team team = Game.getPlayerTeam(p, g);
            if(team == null) {
                return;
            }
            
            Block bedBlock = team.getBed();
            Block breakBlock = e.getBlock();
            Bed breakBed = (Bed) e.getBlock().getState().getData();
            
            if(!breakBed.isHeadOfBed()) {
                breakBlock = breakBlock.getRelative(breakBed.getFacing());
                breakBed = (Bed)breakBlock.getState().getData();
            }
            
            if(bedBlock.equals(breakBlock)) {
                p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.blocks.ownbeddestroy")));
                return;
            }
            
            Team bedDestroyTeam = Game.getTeamOfBed(g, breakBlock);
            if(bedDestroyTeam == null) {
                return;
            }
            
            breakBlock.getDrops().clear();
            breakBlock.setType(Material.AIR);
            Block neighbor = breakBlock.getRelative(breakBed.getFacing().getOppositeFace());
            neighbor.getDrops().clear();
            neighbor.setType(Material.AIR);
            
            g.broadcast(ChatColor.RED + Main._l("ingame.blocks.beddestroyed", ImmutableMap.of("team", bedDestroyTeam.getChatColor() + bedDestroyTeam.getName() + ChatColor.RED)));
            g.broadcastSound(Sound.ENDERDRAGON_GROWL, 30.0F, 10.0F);
            g.setPlayersScoreboard();
            return;
        }

        if(g.getRegion().getBlocks(false).contains(e.getBlock())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlace(BlockPlaceEvent bpe) {
    	Player player = bpe.getPlayer();
    	Game game = Game.getGameOfPlayer(player);
    	
    	if(game == null) {
    		return;
    	}
    	
    	if(game.getState() == GameState.STOPPED) {
    		return;
    	}
    	
    	if(game.getState() == GameState.WAITING) {
    		bpe.setCancelled(true);
    		bpe.setBuild(false);
    		return;
    	}
    	
    	if(game.getState() == GameState.RUNNING) {
    		Block placeBlock = bpe.getBlockPlaced();
        	
        	if(placeBlock.getType() == Material.BED || placeBlock.getType() == Material.BED_BLOCK) {
        		bpe.setCancelled(true);
        		bpe.setBuild(false);
        	}
    	}
    }

}
