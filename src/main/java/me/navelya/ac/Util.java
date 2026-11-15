package me.navelya.ac;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final double EXPANDER = 1.6777216E7;

    private Util() {}

    public static String color(String s) {
        Matcher m = HEX.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            StringBuilder r = new StringBuilder("§x");
            for (char ch : m.group(1).toCharArray()) r.append('§').append(ch);
            m.appendReplacement(sb, Matcher.quoteReplacement(r.toString()));
        }
        m.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static boolean bedrock(Player p) {
        return p.getUniqueId().getMostSignificantBits() == 0L;
    }

    public static double wrap(double a) {
        a %= 360.0;
        if (a >= 180.0) a -= 360.0;
        if (a < -180.0) a += 360.0;
        return a;
    }

    public static Vector look(float yaw, float pitch) {
        double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
        double xz = Math.cos(pr);
        return new Vector(-xz * Math.sin(yr), -Math.sin(pr), xz * Math.cos(yr));
    }

    public static double angle(Vector a, Vector b) {
        double la = a.length(), lb = b.length();
        if (la < 1.0E-9 || lb < 1.0E-9) return 0;
        double d = a.dot(b) / (la * lb);
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, d))));
    }

    public static Player nearestTarget(Player p, double range) {
        Player best = null;
        double bestD = range * range;
        for (Player o : p.getWorld().getPlayers()) {
            if (o == p || o.isDead() || o.getGameMode() == GameMode.SPECTATOR || !p.canSee(o)) continue;
            double d = o.getLocation().distanceSquared(p.getLocation());
            if (d < bestD) {
                bestD = d;
                best = o;
            }
        }
        return best;
    }

    public static Vector toTarget(Player p, Location at, Player t) {
        Vector eye = at.toVector().add(new Vector(0, p.getEyeHeight(), 0));
        return t.getBoundingBox().getCenter().subtract(eye);
    }

    public static long gcd(long a, long b) {
        if (a < b) {
            long t = a;
            a = b;
            b = t;
        }
        while (b > 0x4000) {
            long r = a % b;
            a = b;
            b = r;
        }
        return a;
    }

    public static long expand(double v) {
        return (long) (Math.abs(v) * EXPANDER);
    }
}
