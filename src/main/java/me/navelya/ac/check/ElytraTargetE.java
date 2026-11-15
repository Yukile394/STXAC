package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraTargetE {
    public static final String NAME = "ElytraTargetE";

    private final NavelyaAC plugin;
    private final boolean enabled;
    private final double maxError, minDistance, range;
    private final int streakNeeded;
    private final long streakWindow;

    public ElytraTargetE(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        maxError = c.getDouble("max-error", 1.0);
        minDistance = c.getDouble("min-distance", 4.0);
        streakNeeded = c.getInt("streak-needed", 3);
        streakWindow = c.getLong("streak-window-ms", 10_000);
        range = plugin.getConfig().getDouble("general.target-range", 60);
    }

    public void onBoost(Player p, PlayerData d, Player t, long now) {
        if (!enabled || t == null) return;

        Vector dir = Util.toTarget(p, p.getLocation(), t);
        double dist = dir.length();
        if (dist < minDistance || dist > range || d.targetSpeed < 0.2) return;
        Vector n = dir.clone().multiply(1.0 / dist);
        Vector lateral = d.targetMove.clone().subtract(n.clone().multiply(d.targetMove.dot(n)));
        if (lateral.length() < 0.3) return;

        Location l = p.getLocation();
        double err = Util.angle(Util.look(l.getYaw(), l.getPitch()), dir);

        if (err > maxError) {
            if (now - d.lastPerfectBoost > streakWindow) d.perfectBoosts = 0;
            return;
        }

        if (now - d.lastPerfectBoost > streakWindow) d.perfectBoosts = 0;
        d.lastPerfectBoost = now;
        if (++d.perfectBoosts >= streakNeeded) {
            plugin.flagger().flag(p, NAME, String.format("fisek hata=%.2f mesafe=%.1f seri=%d", err, dist, d.perfectBoosts));
        }
    }
}
