package com.gamereminder;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ConfigManager {
    private static final Path CONFIG_PATH = Paths.get(
        System.getProperty("user.home"), ".game_reminder_config.json"
    );

    private List<String> gameProcesses;
    private String learningUrl;
    private Set<String> alertedProcesses;

    public ConfigManager() {
        this.gameProcesses = new ArrayList<>();
        this.learningUrl = "";
        this.alertedProcesses = new HashSet<>();
        load();
    }

    public List<String> getGameProcesses() {
        return gameProcesses;
    }

    public void addGameProcess(String processName) {
        String name = normalizeProcessName(processName);
        if (!name.isEmpty() && !gameProcesses.contains(name)) {
            gameProcesses.add(name);
        }
    }

    public void removeGameProcess(String processName) {
        gameProcesses.remove(processName);
    }

    public String getLearningUrl() {
        return learningUrl;
    }

    public void setLearningUrl(String url) {
        this.learningUrl = url == null ? "" : url.trim();
    }

    public boolean hasAlerted(String processName) {
        return alertedProcesses.contains(processName.toLowerCase());
    }

    public void markAlerted(String processName) {
        alertedProcesses.add(processName.toLowerCase());
    }

    public void clearAlerted() {
        alertedProcesses.clear();
    }

    public static String normalizeProcessName(String name) {
        String n = name.trim().toLowerCase();
        if (!n.endsWith(".exe")) {
            n += ".exe";
        }
        return n;
    }

    public void save() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"gameProcesses\": [\n");
            for (int i = 0; i < gameProcesses.size(); i++) {
                sb.append("    \"").append(escapeJson(gameProcesses.get(i))).append("\"");
                if (i < gameProcesses.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");
            sb.append("  \"learningUrl\": \"").append(escapeJson(learningUrl)).append("\"\n");
            sb.append("}\n");
            Files.writeString(CONFIG_PATH, sb.toString());
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(CONFIG_PATH)) {
            return;
        }
        try {
            String content = Files.readString(CONFIG_PATH);
            parseJson(content);
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    private void parseJson(String json) {
        // Parse gameProcesses array
        int arrStart = json.indexOf("[");
        int arrEnd = json.lastIndexOf("]");
        if (arrStart >= 0 && arrEnd > arrStart) {
            String arrContent = json.substring(arrStart + 1, arrEnd);
            String[] items = arrContent.split(",");
            for (String item : items) {
                String cleaned = item.trim().replaceAll("^\"|\"$", "").trim();
                if (!cleaned.isEmpty()) {
                    gameProcesses.add(cleaned);
                }
            }
        }

        // Parse learningUrl
        int urlKey = json.indexOf("\"learningUrl\"");
        if (urlKey >= 0) {
            int colon = json.indexOf(":", urlKey);
            if (colon >= 0) {
                int valStart = json.indexOf("\"", colon);
                int valEnd = json.indexOf("\"", valStart + 1);
                if (valStart >= 0 && valEnd > valStart) {
                    learningUrl = json.substring(valStart + 1, valEnd);
                }
            }
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}