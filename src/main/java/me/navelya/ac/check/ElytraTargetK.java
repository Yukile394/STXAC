package me.navelya.ac.check;

import me.navelya.ac.NavelyaAC;
import me.navelya.ac.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ElytraTargetK {
    public static final String NAME = "ElytraTargetK";

    private final NavelyaAC plugin;
    private final boolean enabled, offhandSword;
    private final long minDelay, swapMs, swapWindow;
    private final int streakNeeded, swapsNeeded;

    public ElytraTargetK(NavelyaAC plugin) {
        this.plugin = plugin;
        ConfigurationSection c = plugin.getConfig().getConfigurationSection("checks." + NAME);
        enabled = c.getBoolean("enabled", true);
        offhandSword = c.getBoolean("flag-offhand-with-sword", false);
        minDelay = c.getLong("min-delay-ms", 150);
        streakNeeded = c.getInt("streak-needed", 3);
        swapMs = c.getLong("swap-ms", 60);
        swapsNeeded = c.getInt("swaps-needed", 2);
        swapWindow = c.getLong("swap-window-ms", 10000);
    }

    private static boolean sword(ItemStack i) {
        return i != null && i.getType().name().endsWith("_SWORD");
    }

    public void onSwitch(PlayerData d, ItemStack prev, ItemStack now, long time) {
        d.kSwitchMs = sword(prev) && now != null && now.getType() == Material.FIREWORK_ROCKET ? time : 0;
    }

    public void onUse(Player p, PlayerData d, EquipmentSlot hand, long now) {
        if (!enabled) return;
        if (d.kLastUse != 0 && now - d.kLastUse < 5) return;

        long gap = d.kLastUse == 0 ? Long.MAX_VALUE : now - d.kLastUse;
        d.kLastUse = now;
        boolean lag = now < d.lagUntil || p.getPing() > 400 || Bukkit.getTPS()[0] < 18.5;

        if (lag) {
            d.kStreak = 0;
            d.kSwitchMs = 0;
        } else if (gap < minDelay) {
            if (++d.kStreak >= streakNeeded) {
                d.kStreak = 0;
                plugin.flagger().flag(p, NAME, "fisek gecikmesi " + gap + "ms (min " + minDelay + "ms)");
                return;
            }
        } else {
            d.kStreak = 0;
        }

        if (!lag && hand == EquipmentSlot.HAND && d.kSwitchMs != 0 && now - d.kSwitchMs <= swapMs) {
            long swap = now - d.kSwitchMs;
            d.kSwitchMs = 0;
            if (now - d.kSwapLast > swapWindow) d.kSwapCount = 0;
            d.kSwapLast = now;
            if (++d.kSwapCount >= swapsNeeded) {
                d.kSwapCount = 0;
                plugin.flagger().flag(p, NAME, "kilic -> fisek gecisi " + swap + "ms");
                return;
            }
        }

        if (offhandSword && hand == EquipmentSlot.OFF_HAND && sword(p.getInventory().getItemInMainHand())) {
            plugin.flagger().flag(p, NAME, "elinde kilic varken fisek");
        }
    }
}
