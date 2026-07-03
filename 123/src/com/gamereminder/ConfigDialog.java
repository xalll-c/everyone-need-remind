package com.gamereminder;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;

public class ConfigDialog extends JDialog {
    private final ConfigManager config;
    private DefaultListModel<String> listModel;
    private JList<String> gameList;
    private JTextField processInput;
    private JTextField urlInput;

    public ConfigDialog(Frame owner, ConfigManager config) {
        super(owner, "设置", true);
        this.config = config;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // ---- Game list section ----
        JPanel gameSection = new JPanel(new BorderLayout(5, 8));
        gameSection.setBorder(createTitledBorder("监控的游戏进程"));

        listModel = new DefaultListModel<>();
        for (String p : config.getGameProcesses()) {
            listModel.addElement(p);
        }
        gameList = new JList<>(listModel);
        gameList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(gameList);
        scrollPane.setPreferredSize(new Dimension(400, 150));

        // Add panel
        JPanel addPanel = new JPanel(new BorderLayout(5, 0));
        processInput = new JTextField();
        processInput.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        processInput.setToolTipText("输入进程名，如: VALORANT-Win64-Shipping");

        JButton addBtn = new JButton("添加");
        addBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        addBtn.addActionListener(e -> addProcess());

        JButton removeBtn = new JButton("删除");
        removeBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        removeBtn.addActionListener(e -> removeProcess());

        addPanel.add(processInput, BorderLayout.CENTER);
        JPanel btnGroup = new JPanel(new GridLayout(1, 2, 5, 0));
        btnGroup.add(addBtn);
        btnGroup.add(removeBtn);
        addPanel.add(btnGroup, BorderLayout.EAST);

        gameSection.add(scrollPane, BorderLayout.CENTER);
        gameSection.add(addPanel, BorderLayout.SOUTH);
        gameSection.add(new JLabel("提示: 进程名不需要区分大小写，可省略 .exe"), BorderLayout.NORTH);

        // ---- URL section ----
        JPanel urlSection = new JPanel(new BorderLayout(5, 5));
        urlSection.setBorder(createTitledBorder("学习链接 / 本地路径"));

        urlInput = new JTextField(config.getLearningUrl());
        urlInput.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        urlInput.setToolTipText("输入网址或本地文件/文件夹路径，如: https://www.bilibili.com/xxx 或 D:\\学习视频");

        JButton browseBtn = new JButton("浏览...");
        browseBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        browseBtn.addActionListener(e -> browseLocalPath());

        JPanel urlInputPanel = new JPanel(new BorderLayout(5, 0));
        urlInputPanel.add(urlInput, BorderLayout.CENTER);
        urlInputPanel.add(browseBtn, BorderLayout.EAST);
        urlSection.add(urlInputPanel, BorderLayout.CENTER);

        // ---- Bottom buttons ----
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton saveBtn = new JButton("保存");
        saveBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        saveBtn.setPreferredSize(new Dimension(90, 35));
        saveBtn.addActionListener(e -> saveAndClose());

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(90, 35));
        cancelBtn.addActionListener(e -> dispose());

        bottomPanel.add(saveBtn);
        bottomPanel.add(cancelBtn);

        mainPanel.add(gameSection, BorderLayout.CENTER);
        mainPanel.add(urlSection, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Re-pack to adjust layout
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outer.add(gameSection, BorderLayout.CENTER);

        JPanel southGroup = new JPanel(new BorderLayout(0, 10));
        southGroup.add(urlSection, BorderLayout.CENTER);
        southGroup.add(bottomPanel, BorderLayout.SOUTH);
        outer.add(southGroup, BorderLayout.SOUTH);

        setContentPane(outer);
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Microsoft YaHei", Font.BOLD, 13)
        );
        return border;
    }

    private void addProcess() {
        String name = processInput.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入进程名", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String normalized = ConfigManager.normalizeProcessName(name);
        if (listModel.contains(normalized)) {
            JOptionPane.showMessageDialog(this, "该进程已存在", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        listModel.addElement(normalized);
        processInput.setText("");
    }

    private void removeProcess() {
        int idx = gameList.getSelectedIndex();
        if (idx >= 0) {
            listModel.remove(idx);
        } else {
            JOptionPane.showMessageDialog(this, "请先选择要删除的进程", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void browseLocalPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle("选择学习文件或文件夹");
        // Start from the current text field value if it's a local path
        String current = urlInput.getText().trim();
        if (!current.isEmpty() && !current.startsWith("http")) {
            File f = new File(current);
            if (f.exists()) {
                chooser.setCurrentDirectory(f.isDirectory() ? f : f.getParentFile());
            }
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            urlInput.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void saveAndClose() {
        config.getGameProcesses().clear();
        for (int i = 0; i < listModel.size(); i++) {
            config.getGameProcesses().add(listModel.get(i));
        }
        config.setLearningUrl(urlInput.getText().trim());
        config.save();
        config.clearAlerted();
        JOptionPane.showMessageDialog(this, "设置已保存", "提示", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}