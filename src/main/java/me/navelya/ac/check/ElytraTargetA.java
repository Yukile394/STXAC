package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraTargetA {
    public static final String NAME = "ElytraTargetA";

    private final NavelyaAC plugin;
    private final boolean enabled, gcdCheck;
    private final double range, snapMin, snapBefore, snapAfter, lockError;
    private final int snapsNeeded, lockTicks, gcdBuffer;

    public ElytraTargetA(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        gcdCheck = c.getBoolean("gcd-check", true);
        snapMin = c.getDouble("snap-min-rotation", 35);
        snapBefore = c.getDouble("snap-before-error", 20);
        snapAfter = c.getDouble("snap-after-error", 1);
        snapsNeeded = c.getInt("snaps-needed", 3);
        lockError = c.getDouble("lock-error", 0.8);
        lockTicks = c.getInt("lock-ticks", 12);
        gcdBuffer = c.getInt("gcd-buffer", 15);
        range = plugin.getConfig().getDouble("general.target-range", 60);
    }

    public void onMove(Player p, PlayerData d, Location from, Location to, Player t, long now) {
        if (!enabled) return;

        double dYaw = Util.wrap(to.getYaw() - from.getYaw());
        float dPitch = to.getPitch() - from.getPitch();
        double rot = Math.hypot(dYaw, dPitch);
        if (rot < 0.001) return;

        Vector dir = Util.toTarget(p, to, t);
        double dist = dir.length();
        if (dist < 3.0 || dist > range) {
            d.lockTicks = 0;
            d.gcdBuffer = 0;
            return;
        }

        double before = Util.angle(Util.look(from.getYaw(), from.getPitch()), dir);
        double after = Util.angle(Util.look(to.getYaw(), to.getPitch()), dir);

        if (rot >= snapMin && before >= snapBefore && after <= snapAfter) {
            if (now - d.lastSnapMs > 10_000) d.snapCount = 0;
            d.lastSnapMs = now;
            if (++d.snapCount >= snapsNeeded) {
                d.snapCount = 0;
                plugin.flagger().flag(p, NAME, String.format("snap rot=%.1f onceki=%.1f sonraki=%.2f", rot, before, after));
                return;
            }
        }

        if (after <= lockError && rot >= 0.3 && d.targetSpeed > 0.3) {
            if (++d.lockTicks >= lockTicks) {
                d.lockTicks = 0;
                plugin.flagger().flag(p, NAME, String.format("kilit hata=%.2f hiz=%.2f", after, d.targetSpeed));
                return;
            }
        } else if (after > 3.0) {
            d.lockTicks = 0;
        }

        if (gcdCheck && !Util.bedrock(p)) {
            float lastP = d.lastDPitch;
            d.lastDPitch = dPitch;
            if (Math.abs(dPitch) > 0.1 && Math.abs(lastP) > 0.1 && Math.abs(to.getPitch()) < 85
                    && Math.abs(dPitch) < 20 && Math.abs(lastP) < 20 && after <= 6.0) {
                long g = Util.gcd(Util.expand(dPitch), Util.expand(lastP));
                if (g < 131072L) {
                    if (++d.gcdBuffer >= gcdBuffer) {
                        d.gcdBuffer = 0;
                        plugin.flagger().flag(p, NAME, "gecersiz rotasyon adimi");
                    }
                } else if (d.gcdBuffer > 0) {
                    d.gcdBuffer--;
                }
            }
        }
    }
}
