package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ElytraTargetZ {
    public static final String NAME = "ElytraTargetZ";

    private final NavelyaAC plugin;
    private final boolean enabled;
    private final double minDelta;
    private final int constNeeded, roundNeeded;

    public ElytraTargetZ(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        minDelta = c.getDouble("min-delta", 2.0);
        constNeeded = c.getInt("constant-needed", 5);
        roundNeeded = c.getInt("rounded-needed", 6);
    }

    public void onMove(Player p, PlayerData d, Location from, Location to, boolean hasTarget) {
        if (!enabled) return;
        float yaw = to.getYaw(), pitch = to.getPitch();

        if (Float.isNaN(yaw) || Float.isNaN(pitch) || Float.isInfinite(yaw) || Float.isInfinite(pitch)
                || Math.abs(pitch) > 90.0f) {
            plugin.flagger().flag(p, NAME, "gecersiz rotasyon yaw=" + yaw + " pitch=" + pitch);
            return;
        }

        double dy = Util.wrap(yaw - from.getYaw());
        double dp = pitch - from.getPitch();
        if (!hasTarget || Util.bedrock(p) || (Math.abs(dy) < 0.001 && Math.abs(dp) < 0.001)) {
            if (!hasTarget) { d.zConst = 0; d.zRound = 0; }
            return;
        }

        boolean same = Math.abs(dy) >= minDelta && Math.abs(dy - d.zLastYaw) < 1.0E-4
                || Math.abs(dp) >= minDelta && Math.abs(dp - d.zLastPitch) < 1.0E-4;
        if (same) {
            if (++d.zConst >= constNeeded) {
                d.zConst = 0;
                plugin.flagger().flag(p, NAME, String.format("sabit donus yaw=%.3f pitch=%.3f", dy, dp));
            }
        } else {
            d.zConst = 0;
        }

        boolean rounded = Math.abs(dy) >= 2.0 && Math.abs(dy - Math.rint(dy)) < 1.0E-4
                || Math.abs(dp) >= 2.0 && Math.abs(dp - Math.rint(dp)) < 1.0E-4;
        if (rounded) {
            if (++d.zRound >= roundNeeded) {
                d.zRound = 0;
                plugin.flagger().flag(p, NAME, String.format("yuvarlak donus yaw=%.3f pitch=%.3f", dy, dp));
            }
        } else if (d.zRound > 0) {
            d.zRound--;
        }

        d.zLastYaw = dy;
        d.zLastPitch = dp;
    }
}
