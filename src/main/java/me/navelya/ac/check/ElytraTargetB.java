package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import me.navelya.ac.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ElytraTargetB {
    public static final String NAME = "ElytraTargetB";

    private final NavelyaAC plugin;
    private final boolean enabled;
    private final long windowMs;
    private final double degrees, range;

    public ElytraTargetB(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        windowMs = c.getLong("window-ms", 1000);
        degrees = c.getDouble("degrees", 360);
        range = c.getDouble("target-range", 40);
    }

    public void onMove(Player p, PlayerData d, Location from, Location to, Player t, long now) {
        if (!enabled) return;
        double dYaw = Util.wrap(to.getYaw() - from.getYaw());
        if (Math.abs(dYaw) < 0.001) return;

        if (t == null || t.getLocation().distance(to) > range) {
            d.yawWindow.clear();
            return;
        }

        d.yawWindow.addLast(new double[]{now, dYaw});
        while (!d.yawWindow.isEmpty() && now - d.yawWindow.peekFirst()[0] > windowMs) d.yawWindow.pollFirst();

        double sum = 0;
        for (double[] e : d.yawWindow) sum += e[1];
        if (Math.abs(sum) >= degrees && d.yawWindow.size() >= 4) {
            d.yawWindow.clear();
            plugin.flagger().flag(p, NAME, String.format("%.0f derece / %dms", Math.abs(sum), windowMs));
        }
    }
}
