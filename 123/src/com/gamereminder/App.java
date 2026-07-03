package com.gamereminder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class App {
    private final ConfigManager config;
    private ProcessMonitor monitor;
    private TrayIcon trayIcon;
    private SystemTray tray;
    private JWindow trayMenuWindow;

    public App() {
        config = new ConfigManager();
    }

    public void start() {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null,
                "系统托盘不支持，程序无法运行。",
                "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if (config.getGameProcesses().isEmpty() || config.getLearningUrl().isEmpty()) {
            showConfigDialog(null);
        }

        if (config.getGameProcesses().isEmpty() || config.getLearningUrl().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "请先设置游戏进程和学习链接后再启动。",
                "提示", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        setupTray();
        startMonitor();
    }

    private void setupTray() {
        tray = SystemTray.getSystemTray();
        Image icon = createTrayImage();
        trayIcon = new TrayIcon(icon, "游戏学习提醒", createPopupMenu());
        trayIcon.setImageAutoSize(true);

        // Double-click also shows the Swing-based tray menu
        trayIcon.addActionListener(e -> showSwingTrayMenu());

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(null,
                "无法添加系统托盘图标: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // AWT popup menu as fallback — items trigger the Swing menu
    private PopupMenu createPopupMenu() {
        PopupMenu menu = new PopupMenu();
        Font menuFont = new Font("Microsoft YaHei", Font.PLAIN, 13);

        MenuItem settingsItem = new MenuItem("设置");
        settingsItem.setFont(menuFont);
        settingsItem.addActionListener(e -> showConfigDialog(null));

        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setFont(menuFont);
        aboutItem.addActionListener(e -> showAbout());

        menu.add(settingsItem);
        menu.addSeparator();
        menu.add(aboutItem);
        menu.addSeparator();

        MenuItem exitItem = new MenuItem("退出");
        exitItem.setFont(menuFont);
        exitItem.addActionListener(e -> exit());
        menu.add(exitItem);

        return menu;
    }

    private void showSwingTrayMenu() {
        if (trayMenuWindow != null && trayMenuWindow.isVisible()) {
            trayMenuWindow.dispose();
        }

        trayMenuWindow = new JWindow();
        trayMenuWindow.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160)));
        panel.setBackground(new Color(250, 250, 250));

        Font itemFont = new Font("Microsoft YaHei", Font.PLAIN, 14);

        panel.add(createMenuItem("设置", itemFont, e -> {
            closeTrayMenu();
            showConfigDialog(null);
        }));
        panel.add(createMenuSeparator());
        panel.add(createMenuItem("关于", itemFont, e -> {
            closeTrayMenu();
            showAbout();
        }));
        panel.add(createMenuSeparator());
        panel.add(createMenuItem("退出", itemFont, e -> {
            closeTrayMenu();
            exit();
        }));

        trayMenuWindow.add(panel);
        trayMenuWindow.pack();

        // Position near the mouse cursor
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        trayMenuWindow.setLocation(mouseLoc.x - 10, mouseLoc.y - 10);

        // Close when focus lost
        trayMenuWindow.addWindowFocusListener(new WindowFocusListener() {
            @Override public void windowGainedFocus(WindowEvent e) {}
            @Override public void windowLostFocus(WindowEvent e) {
                closeTrayMenu();
            }
        });

        trayMenuWindow.setVisible(true);
        trayMenuWindow.requestFocus();
    }

    private JPanel createMenuItem(String text, Font font, ActionListener action) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(new Color(250, 250, 250));
        item.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 40));

        JLabel label = new JLabel(text);
        label.setFont(font);
        item.add(label, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(70, 130, 220));
                label.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(new Color(250, 250, 250));
                label.setForeground(Color.BLACK);
            }
            @Override public void mouseClicked(MouseEvent e) {
                action.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, ""));
            }
        });

        return item;
    }

    private JPanel createMenuSeparator() {
        JPanel sep = new JPanel();
        sep.setBackground(new Color(250, 250, 250));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));
        return sep;
    }

    private void closeTrayMenu() {
        if (trayMenuWindow != null) {
            trayMenuWindow.dispose();
            trayMenuWindow = null;
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(null,
            "游戏学习提醒 v1.0\n\n" +
            "功能: 在打开游戏时提醒你先完成学习任务。\n" +
            "使用方法: 右键托盘图标或双击图标 → 设置，添加游戏进程和学习链接。\n\n" +
            "提示: 每天首次检测到游戏才会弹窗提醒。",
            "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startMonitor() {
        monitor = new ProcessMonitor(config, () -> {
            SwingUtilities.invokeLater(() -> showReminder());
        });
        monitor.start();
        trayIcon.displayMessage("游戏学习提醒",
            "监控已启动，正在守护你的学习时间。",
            TrayIcon.MessageType.INFO);
    }

    private void showReminder() {
        ReminderDialog dialog = new ReminderDialog(null, config);
        dialog.setVisible(true);
    }

    private void showConfigDialog(Frame owner) {
        ConfigDialog dialog = new ConfigDialog(owner, config);
        dialog.setVisible(true);
        if (monitor != null) {
            monitor.stop();
            config.clearAlerted();
            startMonitor();
        }
    }

    private Image createTrayImage() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(70, 130, 220));
        g2d.fillRect(2, 3, 10, 12);
        g2d.setColor(new Color(40, 90, 180));
        g2d.drawRect(2, 3, 10, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawLine(7, 5, 7, 13);
        g2d.drawLine(4, 7, 6, 7);
        g2d.drawLine(4, 9, 6, 9);
        g2d.drawLine(4, 11, 6, 11);

        g2d.dispose();
        return img;
    }

    private void exit() {
        closeTrayMenu();
        if (monitor != null) {
            monitor.stop();
        }
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        Font defaultFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        UIManager.put("OptionPane.messageFont", defaultFont);
        UIManager.put("OptionPane.buttonFont", defaultFont);

        SwingUtilities.invokeLater(() -> new App().start());
    }
}