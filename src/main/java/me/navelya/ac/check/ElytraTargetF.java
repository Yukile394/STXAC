package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraTargetF {
    public static final String NAME = "ElytraTargetF";

    private final NavelyaAC plugin;
    private final boolean enabled;
    private final int maxPing, flicksNeeded;
    private final long maxGap, window;
    private final double spikeMin, calmMax, stopMax, aimMax, range;

    public ElytraTargetF(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        maxPing = c.getInt("max-ping", 400);
        maxGap = c.getLong("max-gap-ms", 150);
        spikeMin = c.getDouble("spike-min", 90);
        calmMax = c.getDouble("calm-max", 5);
        stopMax = c.getDouble("stop-max", 3);
        aimMax = c.getDouble("aim-max", 30);
        flicksNeeded = c.getInt("flicks-needed", 3);
        window = c.getLong("window-ms", 15000);
        range = plugin.getConfig().getDouble("general.target-range", 60);
    }

    public void onMove(Player p, PlayerData d, Location from, Location to, Player t, long now) {
        if (!enabled) return;
        long gap = d.fLastEvent == 0 ? 0 : now - d.fLastEvent;
        d.fLastEvent = now;
        if (gap > maxGap || p.getPing() > maxPing) {
            d.fRot1 = 0;
            d.fRot2 = 0;
            d.fAim1 = 999;
            return;
        }

        double rot0 = Math.hypot(Util.wrap(to.getYaw() - from.getYaw()), to.getPitch() - from.getPitch());

        if (d.fRot2 <= calmMax && d.fRot1 >= spikeMin && rot0 <= stopMax && d.fAim1 <= aimMax) {
            if (now - d.fLastFlick > window) d.fCount = 0;
            d.fLastFlick = now;
            if (++d.fCount >= flicksNeeded) {
                d.fCount = 0;
                plugin.flagger().flag(p, NAME, String.format("ani donus %.1f derece (oncesi %.1f sonrasi %.1f)", d.fRot1, d.fRot2, rot0));
            }
        }

        Vector dir = Util.toTarget(p, to, t);
        double dist = dir.length();
        d.fAim1 = dist >= 3 && dist <= range ? Util.angle(Util.look(to.getYaw(), to.getPitch()), dir) : 999;
        d.fRot2 = d.fRot1;
        d.fRot1 = rot0;
    }
}
