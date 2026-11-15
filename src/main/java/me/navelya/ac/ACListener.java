package me.navelya.ac;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class ACListener implements Listener {
    private final NavelyaAC plugin;

    public ACListener(NavelyaAC plugin) {
        this.plugin = plugin;
    }

    private boolean skip(Player p) {
        return p.hasPermission("navelyaac.bypass");
    }

    private double range() {
        return plugin.getConfig().getDouble("general.target-range", 60);
    }

    private void updateTarget(PlayerData d, Player t, long now) {
        if (t == null) {
            d.targetId = null;
            d.targetPos = null;
            d.targetSpeed = 0;
            d.targetMove = new Vector();
            return;
        }
        Vector pos = t.getLocation().toVector();
        if (!t.getUniqueId().equals(d.targetId) || d.targetPos == null) {
            d.targetId = t.getUniqueId();
            d.targetPos = pos;
            d.targetSampleMs = now;
            d.targetSpeed = 0;
            d.targetMove = new Vector();
            return;
        }
        long dt = now - d.targetSampleMs;
        if (dt >= 50) {
            Vector mv = pos.clone().subtract(d.targetPos);
            d.targetMove = mv;
            d.targetSpeed = mv.length() * 50.0 / dt;
            d.targetPos = pos;
            d.targetSampleMs = now;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location from = e.getFrom(), to = e.getTo();
        if (to == null || !p.isGliding() || skip(p)) return;

        PlayerData d = plugin.data(p);
        long now = System.currentTimeMillis();
        long elapsed = d.lastMove == 0 ? 50 : now - d.lastMove;
        d.lastMove = now;
        if (elapsed > 120) d.lagUntil = now + 1500;
        if (now - d.lastTeleport < 1500) return;

        Player t = Util.nearestTarget(p, range());
        updateTarget(d, t, now);

        plugin.z().onMove(p, d, from, to, t != null);
        plugin.b().onMove(p, d, from, to, t, now);
        if (t == null) {
            plugin.g().onMove(p, d, from, to, null, elapsed);
            return;
        }
        plugin.a().onMove(p, d, from, to, t, now);
        plugin.d().onMove(p, d, from, to, t);
        plugin.c().onMove(p, d, from, to, t);
        plugin.f().onMove(p, d, from, to, t, now);
        plugin.g().onMove(p, d, from, to, t, elapsed);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null || e.getItem().getType() != Material.FIREWORK_ROCKET) return;
        Player p = e.getPlayer();
        if (!p.isGliding() || skip(p)) return;
        PlayerData d = plugin.data(p);
        long now = System.currentTimeMillis();
        Player t = Util.nearestTarget(p, range());
        updateTarget(d, t, now);
        plugin.e().onBoost(p, d, t, now);
        plugin.k().onUse(p, d, e.getHand(), now);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        d.lastTeleport = System.currentTimeMillis();
        d.resetAll();
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent e) {
        if (e.getEntity() instanceof Player p) plugin.data(p).resetAll();
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        var inv = p.getInventory();
        plugin.k().onSwitch(plugin.data(p), inv.getItem(e.getPreviousSlot()), inv.getItem(e.getNewSlot()), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player a && e.getDamager() instanceof Player b) {
            long now = System.currentTimeMillis();
            plugin.data(a).combat.put(b.getUniqueId(), now);
            plugin.data(b).combat.put(a.getUniqueId(), now);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.remove(e.getPlayer());
    }
}
