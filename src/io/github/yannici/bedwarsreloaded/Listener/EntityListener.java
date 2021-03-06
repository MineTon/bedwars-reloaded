package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.GameState;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityListener extends BaseListener {

    public EntityListener() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent ede) {
        if(ede.getEntityType() != EntityType.VILLAGER) {
            return;
        }
        
        Game game = Main.getInstance().getGameManager().getGameByWorld(ede.getEntity().getWorld());
        if(game == null) {
            return;
        }
        
        if(game.getState() != GameState.WAITING && game.getState() != GameState.RUNNING) {
            return;
        }
        
        ede.setCancelled(true);
    }

}
