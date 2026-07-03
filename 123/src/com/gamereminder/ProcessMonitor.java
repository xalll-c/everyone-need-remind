package com.gamereminder;

import java.io.*;
import java.util.*;

public class ProcessMonitor {
    private static final int POLL_INTERVAL_MS = 5000;

    private final ConfigManager config;
    private final Runnable onGameDetected;
    private volatile boolean running = false;
    private Thread monitorThread;

    public ProcessMonitor(ConfigManager config, Runnable onGameDetected) {
        this.config = config;
        this.onGameDetected = onGameDetected;
    }

    public void start() {
        if (running) return;
        running = true;
        monitorThread = new Thread(this::monitorLoop, "ProcessMonitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public void stop() {
        running = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }

    private void monitorLoop() {
        while (running) {
            try {
                checkProcesses();
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void checkProcesses() {
        List<String> targets = config.getGameProcesses();
        if (targets.isEmpty()) return;

        Set<String> runningProcesses = getRunningProcesses();
        for (String target : targets) {
            if (runningProcesses.contains(target) && !config.hasAlerted(target)) {
                config.markAlerted(target);
                onGameDetected.run();
                break;
            }
        }
    }

    private Set<String> getRunningProcesses() {
        Set<String> processes = new HashSet<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("tasklist", "/fo", "csv", "/nh");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), "GBK"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Format: "process.exe","1234","Console","1","123,456 K"
                    int firstComma = line.indexOf(",");
                    if (firstComma > 0) {
                        String name = line.substring(0, firstComma)
                            .replace("\"", "").trim().toLowerCase();
                        processes.add(name);
                    }
                }
            }
            p.waitFor();
        } catch (Exception e) {
            // Silently ignore monitoring errors
        }
        return processes;
    }
}