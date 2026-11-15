package me.navelya.ac;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;

public class Flagger {
    private final NavelyaAC plugin;
    private final FileConfiguration cfg;
    private final String prefix;

    public Flagger(NavelyaAC plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
        this.prefix = cfg.getString("general.prefix", "NavelyaAC");
    }

    public void flag(Player p, String check, String info) {
        PlayerData d = plugin.data(p);
        if (d.banned) return;
        long now = System.currentTimeMillis();
        long decay = cfg.getLong("general.log-decay-seconds", 900) * 1000L;
        ArrayDeque<Long> q = d.logs.computeIfAbsent(check, k -> new ArrayDeque<>());
        while (!q.isEmpty() && now - q.peekFirst() > decay) q.pollFirst();
        q.addLast(now);

        int max = Math.max(1, cfg.getInt("checks." + check + ".logs-to-ban", 3));
        int count = q.size();

        String alert = fmt(cfg.getString("general.alert-format", "{prefix} {player} {check} {info}"), p, check, info)
                .replace("{logs}", String.valueOf(count)).replace("{max}", String.valueOf(max));
        String msg = Util.color(alert);
        for (Player s : Bukkit.getOnlinePlayers()) {
            if (s.hasPermission("navelyaac.alerts") && !plugin.data(s).alertsOff) s.sendMessage(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
        write("[" + check + "] " + p.getName() + " (" + info + ") " + count + "/" + max);

        if (count >= max) ban(p, check, d);
    }

    private void ban(Player p, String check, PlayerData d) {
        d.banned = true;
        String reason = Util.color(fmt(cfg.getString("general.ban-reason", "{check}"), p, check, ""));
        String cmd = cfg.getString("general.ban-command", "ban {player} {reason}")
                .replace("{player}", p.getName()).replace("{reason}", reason);
        write("BAN " + p.getName() + " [" + check + "]");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        String bc = cfg.getString("general.ban-broadcast", "");
        if (!bc.isEmpty()) Bukkit.broadcastMessage(Util.color(fmt(bc, p, check, "")));
        if (p.isOnline()) p.kickPlayer(reason);
    }

    private String fmt(String s, Player p, String check, String info) {
        return s.replace("{prefix}", prefix).replace("{player}", p.getName())
                .replace("{check}", check).replace("{info}", info);
    }

    private void write(String line) {
        plugin.getDataFolder().mkdirs();
        File f = new File(plugin.getDataFolder(), "logs.txt");
        try (PrintWriter w = new PrintWriter(new FileWriter(f, true))) {
            w.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + line);
        } catch (IOException ignored) {
        }
    }
}
