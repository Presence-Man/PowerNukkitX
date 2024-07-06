/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.PowerNukkitX;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import xxAROX.PresenceMan.PowerNukkitX.utils.Utils;

public final class EventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerLoginEvent(PlayerLoginEvent event){
        Utils.savePlayerData(event.getPlayer());
        if (Utils.isFromSameHost(Utils.retrievePlayerData_ip(event.getPlayer()))) return;
        if (!PresenceMan.enable_default) return;
        if (event.getPlayer().getSkin() != null && PresenceMan.update_skin) PresenceMan.save_skin(event.getPlayer(), event.getPlayer().getSkin());
        PresenceMan.setActivity(event.getPlayer(), PresenceMan.default_activity);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerChangeSkinEvent(PlayerChangeSkinEvent event){
        if (Utils.isFromSameHost(Utils.retrievePlayerData_ip(event.getPlayer()))) return;
        if (!event.isCancelled()) PresenceMan.save_skin(event.getPlayer(), event.getSkin());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerQuitEvent(PlayerQuitEvent event){
        if (Utils.isFromSameHost(Utils.retrievePlayerData_ip(event.getPlayer()))) return;
        PresenceMan.presences.remove(event.getPlayer().getLoginChainData().getXUID());
        PresenceMan.offline(event.getPlayer());
    }
}
