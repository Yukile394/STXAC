package me.navelya.ac;

import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    public boolean alertsOff;
    public final Map<UUID, Long> combat = new HashMap<>();

    public boolean recentCombat(UUID id, long now, long ms) {
        Long t = combat.get(id);
        return t != null && now - t <= ms;
    }

    public boolean banned;
    public long lastTeleport;
    public long lastMove;

    public final Map<String, ArrayDeque<Long>> logs = new HashMap<>();

    public UUID targetId;
    public Vector targetPos;
    public Vector targetMove = new Vector();
    public double targetSpeed;
    public long targetSampleMs;

    public int snapCount;
    public long lastSnapMs;
    public int lockTicks;
    public int gcdBuffer;
    public float lastDPitch;

    public double jitLastYaw, jitLastPitch;
    public int jitBuffer;
    public int cPred, cOver;
    public double fRot1, fRot2, fAim1;
    public int fCount;
    public long kLastUse, kSwitchMs, kSwapLast, lagUntil;
    public int kStreak, kSwapCount;
    public long fLastFlick, fLastEvent;

    public double zLastYaw, zLastPitch;
    public int zConst, zRound;

    public final ArrayDeque<double[]> yawWindow = new ArrayDeque<>();

    public int perfectBoosts;
    public long lastPerfectBoost;

    public UUID chaseTarget;
    public double chaseMs;
    public double targetTurn;
    public double lastTargetHeading = Double.NaN;
    public double angleSum;
    public int angleSamples;

    public void resetChase() {
        chaseTarget = null;
        chaseMs = 0;
        targetTurn = 0;
        lastTargetHeading = Double.NaN;
        angleSum = 0;
        angleSamples = 0;
    }

    public void resetAll() {
        snapCount = 0;
        lockTicks = 0;
        gcdBuffer = 0;
        lastDPitch = 0;
        jitBuffer = 0; jitLastYaw = 0; jitLastPitch = 0;
        cPred = 0; cOver = 0;
        fRot1 = 0; fRot2 = 0; fAim1 = 999; fCount = 0; fLastEvent = 0;
        kStreak = 0; kSwitchMs = 0; kSwapCount = 0;
        zLastYaw = 0; zLastPitch = 0; zConst = 0; zRound = 0;
        yawWindow.clear();
        resetChase();
        targetId = null;
        targetPos = null;
        targetSpeed = 0;
    }
}
