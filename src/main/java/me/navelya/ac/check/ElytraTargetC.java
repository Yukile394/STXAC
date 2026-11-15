package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraTargetC {
    public static final String NAME = "ElytraTargetC";

    private final NavelyaAC plugin;
    private final boolean enabled;
    private final int maxPing, predTicks, overTicks;
    private final double minSpeed, leadMin, predError, overAhead, overError, range;

    public ElytraTargetC(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        maxPing = c.getInt("max-ping", 400);
        minSpeed = c.getDouble("min-target-speed", 0.6);
        leadMin = c.getDouble("lead-min", 3.0);
        predError = c.getDouble("pred-error", 1.0);
        predTicks = c.getInt("pred-ticks", 12);
        overAhead = c.getDouble("overshoot-ahead", 3.0);
        overError = c.getDouble("overshoot-error", 1.0);
        overTicks = c.getInt("overshoot-ticks", 14);
        range = Math.min(plugin.getConfig().getDouble("general.target-range", 60), 30);
    }

    public void onMove(Player p, PlayerData d, Location from, Location to, Player t) {
        if (!enabled) return;
        double rot = Math.hypot(Util.wrap(to.getYaw() - from.getYaw()), to.getPitch() - from.getPitch());
        Vector move = to.toVector().subtract(from.toVector());
        double speed = move.length();
        Vector dir = Util.toTarget(p, to, t);
        double dist = dir.length();

        if (p.getPing() > maxPing || t.getPing() > maxPing || dist < 3 || dist > range
                || speed < 0.5 || d.targetSpeed < minSpeed || d.targetMove.lengthSquared() < 1.0E-4) {
            d.cPred = 0;
            d.cOver = 0;
            return;
        }
        if (rot < 0.05) return;

        Vector look = Util.look(to.getYaw(), to.getPitch());
        double errCur = Util.angle(look, dir);

        double ticks = Math.max(1.0, Math.min(8.0, dist / speed));
        Vector predicted = dir.clone().add(d.targetMove.clone().multiply(ticks));
        double lead = Util.angle(predicted, dir);
        double errPred = Util.angle(look, predicted);

        if (lead >= leadMin && errPred <= predError && errCur - errPred >= leadMin) {
            if (++d.cPred >= predTicks) {
                d.cPred = 0;
                plugin.flagger().flag(p, NAME, String.format("predict hata=%.2f mevcut=%.2f onde=%.1f", errPred, errCur, lead));
                return;
            }
        } else if (errPred > 4.0) {
            d.cPred = 0;
        } else if (d.cPred > 0) {
            d.cPred--;
        }

        Vector tdir = d.targetMove.clone().normalize();
        double ahead = to.toVector().subtract(t.getLocation().toVector()).dot(tdir);
        double moveAngle = Util.angle(move, dir);

        if (ahead >= overAhead && moveAngle >= 60.0 && errCur <= overError) {
            if (++d.cOver >= overTicks) {
                d.cOver = 0;
                plugin.flagger().flag(p, NAME, String.format("overshoot onde=%.1f hata=%.2f", ahead, errCur));
            }
        } else if (ahead < overAhead || errCur > 5.0) {
            d.cOver = 0;
        } else if (d.cOver > 0) {
            d.cOver--;
        }
    }
}
