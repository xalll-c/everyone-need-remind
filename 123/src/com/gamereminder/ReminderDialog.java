package com.gamereminder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ReminderDialog extends JDialog {

    public ReminderDialog(Frame owner, ConfigManager config) {
        super(owner, "学习提醒", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel("检测到游戏启动！");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        titleLabel.setForeground(new Color(200, 60, 60));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Message
        JLabel msgLabel = new JLabel("请先完成今天的学习任务再玩游戏");
        msgLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Icon (using unicode)
        JLabel iconLabel = new JLabel("📚");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton learnBtn = new JButton("去学习");
        learnBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        learnBtn.setBackground(new Color(70, 130, 220));
        learnBtn.setForeground(Color.WHITE);
        learnBtn.setFocusPainted(false);
        learnBtn.setPreferredSize(new Dimension(120, 38));
        learnBtn.addActionListener(e -> {
            String url = config.getLearningUrl();
            if (!url.isEmpty()) {
                openUrl(url);
            }
            dispose();
        });

        JButton skipBtn = new JButton("跳过");
        skipBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        skipBtn.setPreferredSize(new Dimension(120, 38));
        skipBtn.addActionListener(e -> dispose());

        btnPanel.add(learnBtn);
        btnPanel.add(skipBtn);

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(msgLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnPanel);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    private void openUrl(String url) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                Desktop.getDesktop().browse(java.net.URI.create(url));
            } else {
                File file = new File(url);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "路径不存在: " + url,
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "无法打开: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}