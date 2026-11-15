package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraTargetD {
    public static final String NAME = "ElytraTargetD";

    private final NavelyaAC plugin;
    private final boolean enabled;
    private final double minDelta, maxDelta, maxAim, range;
    private final int flipsNeeded;

    public ElytraTargetD(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        minDelta = c.getDouble("min-delta", 2.5);
        maxDelta = c.getDouble("max-delta", 40);
        maxAim = c.getDouble("max-aim-error", 15);
        flipsNeeded = c.getInt("flips-needed", 10);
        range = plugin.getConfig().getDouble("general.target-range", 60);
    }

    public void onMove(Player p, PlayerData d, Location from, Location to, Player t) {
        if (!enabled) return;
        double dy = Util.wrap(to.getYaw() - from.getYaw());
        double dp = to.getPitch() - from.getPitch();
        if (Math.abs(dy) < 0.001 && Math.abs(dp) < 0.001) return;

        Vector dir = Util.toTarget(p, to, t);
        double dist = dir.length();
        double aim = Util.angle(Util.look(to.getYaw(), to.getPitch()), dir);
        if (dist < 3 || dist > range || aim > maxAim) {
            d.jitBuffer = 0;
            d.jitLastYaw = dy;
            d.jitLastPitch = dp;
            return;
        }

        boolean flipYaw = flip(dy, d.jitLastYaw);
        boolean flipPitch = flip(dp, d.jitLastPitch);
        if (flipYaw || flipPitch) {
            d.jitBuffer++;
        } else if (Math.abs(dy) > 0.001 || Math.abs(dp) > 0.001) {
            d.jitBuffer = Math.max(0, d.jitBuffer - 2);
        }
        d.jitLastYaw = dy;
        d.jitLastPitch = dp;

        if (d.jitBuffer >= flipsNeeded) {
            d.jitBuffer = 0;
            plugin.flagger().flag(p, NAME, String.format("jitter yaw=%.1f pitch=%.1f hata=%.1f", dy, dp, aim));
        }
    }

    private boolean flip(double cur, double last) {
        double a = Math.abs(cur), b = Math.abs(last);
        return a >= minDelta && b >= minDelta && a <= maxDelta && b <= maxDelta && Math.signum(cur) != Math.signum(last)
                && a / b >= 0.5 && a / b <= 2.0;
    }
}
