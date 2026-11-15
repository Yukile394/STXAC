package me.navelya.ac;

import me.navelya.ac.check.ElytraTargetA;
import me.navelya.ac.check.ElytraTargetB;
import me.navelya.ac.check.ElytraTargetC;
import me.navelya.ac.check.ElytraTargetD;
import me.navelya.ac.check.ElytraTargetE;
import me.navelya.ac.check.ElytraTargetF;
import me.navelya.ac.check.ElytraTargetG;
import me.navelya.ac.check.ElytraTargetK;
import me.navelya.ac.check.ElytraTargetZ;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NavelyaAC extends JavaPlugin {

    private final Map<UUID, PlayerData> data = new ConcurrentHashMap<>();
    private Flagger flagger;
    private ElytraTargetA checkA;
    private ElytraTargetB checkB;
    private ElytraTargetC checkC;
    private ElytraTargetD checkD;
    private ElytraTargetE checkE;
    private ElytraTargetF checkF;
    private ElytraTargetG checkG;
    private ElytraTargetK checkK;
    private ElytraTargetZ checkZ;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load();
        getServer().getPluginManager().registerEvents(new ACListener(this), this);
        getLogger().info("NavelyaAC aktif.");
    }

    private void load() {
        reloadConfig();
        flagger = new Flagger(this);
        checkA = new ElytraTargetA(this);
        checkB = new ElytraTargetB(this);
        checkC = new ElytraTargetC(this);
        checkD = new ElytraTargetD(this);
        checkE = new ElytraTargetE(this);
        checkF = new ElytraTargetF(this);
        checkG = new ElytraTargetG(this);
        checkK = new ElytraTargetK(this);
        checkZ = new ElytraTargetZ(this);
    }

    public PlayerData data(Player p) {
        return data.computeIfAbsent(p.getUniqueId(), u -> new PlayerData());
    }

    public void remove(Player p) {
        data.remove(p.getUniqueId());
    }

    public String msg(String s) {
        return Util.color(getConfig().getString("general.prefix", "NavelyaAC") + " &8» " + s);
    }

    public Flagger flagger() { return flagger; }
    public ElytraTargetA a() { return checkA; }
    public ElytraTargetB b() { return checkB; }
    public ElytraTargetC c() { return checkC; }
    public ElytraTargetD d() { return checkD; }
    public ElytraTargetE e() { return checkE; }
    public ElytraTargetF f() { return checkF; }
    public ElytraTargetG g() { return checkG; }
    public ElytraTargetK k() { return checkK; }
    public ElytraTargetZ z() { return checkZ; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 1 && a[0].equalsIgnoreCase("reload")) {
            load();
            s.sendMessage(msg("&7Config yenilendi."));
            return true;
        }
        if (a.length == 1 && a[0].equalsIgnoreCase("alerts") && s instanceof Player p) {
            PlayerData d = data(p);
            d.alertsOff = !d.alertsOff;
            s.sendMessage(msg("&7Alerts: " + (d.alertsOff ? "&ckapali" : "&aacik")));
            return true;
        }
        s.sendMessage(msg("&7/navelyaac <reload|alerts>"));
        return true;
    }
}
