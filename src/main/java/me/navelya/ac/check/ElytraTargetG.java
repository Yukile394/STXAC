package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraTargetG {
    public static final String NAME = "ElytraTargetG";

    private final NavelyaAC plugin;
    private final boolean enabled;
    private final double chaseMs, maxMoveAngle, maxAimAngle, minTargetTurn, maxDistance;

    public ElytraTargetG(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        chaseMs = c.getDouble("chase-seconds", 10) * 1000.0;
        maxMoveAngle = c.getDouble("max-move-angle", 4);
        maxAimAngle = c.getDouble("max-aim-angle", 3);
        minTargetTurn = c.getDouble("min-target-turn", 180);
        maxDistance = c.getDouble("max-distance", 40);
    }

    public void onMove(Player p, PlayerData d, Location from, Location to, Player t, long elapsed) {
        if (!enabled) return;
        double dt = Math.min(Math.max(elapsed, 0), 100);

        if (t != null && !d.recentCombat(t.getUniqueId(), System.currentTimeMillis(), plugin.getConfig().getLong("general.combat-seconds", 120) * 1000L)) {
            decay(d, dt);
            return;
        }

        Vector dir = t == null ? null : Util.toTarget(p, to, t);
        if (t == null || dir.length() > maxDistance || dir.length() < 2.5) {
            decay(d, dt);
            return;
        }
        if (!t.getUniqueId().equals(d.chaseTarget)) {
            d.resetChase();
            d.chaseTarget = t.getUniqueId();
        }

        Vector move = to.toVector().subtract(from.toVector());
        boolean ok = false;
        double moveAngle = 0, aim = 0;
        if (move.length() >= 0.5 && d.targetSpeed > 0.3) {
            moveAngle = Util.angle(move, dir);
            aim = Util.angle(Util.look(to.getYaw(), to.getPitch()), dir);
            ok = moveAngle <= maxMoveAngle && aim <= maxAimAngle;
        }

        if (!ok) {
            decay(d, dt);
            return;
        }

        d.chaseMs += dt;
        d.angleSum += moveAngle + aim;
        d.angleSamples++;

        if (d.targetMove.lengthSquared() > 1.0E-4) {
            double heading = Math.toDegrees(Math.atan2(-d.targetMove.getX(), d.targetMove.getZ()));
            if (!Double.isNaN(d.lastTargetHeading)) d.targetTurn += Math.abs(Util.wrap(heading - d.lastTargetHeading));
            d.lastTargetHeading = heading;
        }

        if (d.chaseMs >= chaseMs && d.targetTurn >= minTargetTurn) {
            double avg = d.angleSamples == 0 ? 0 : d.angleSum / (2.0 * d.angleSamples);
            double secs = d.chaseMs / 1000.0, turn = d.targetTurn;
            d.resetChase();
            plugin.flagger().flag(p, NAME, String.format("%.1fs kovalama ort.sapma=%.2f hedef donus=%.0f", secs, avg, turn));
        } else if (d.chaseMs > chaseMs * 2) {
            d.chaseMs = chaseMs;
        }
    }

    private void decay(PlayerData d, double dt) {
        d.chaseMs = Math.max(0, d.chaseMs - dt * 2);
        if (d.chaseMs == 0) d.resetChase();
    }
}
