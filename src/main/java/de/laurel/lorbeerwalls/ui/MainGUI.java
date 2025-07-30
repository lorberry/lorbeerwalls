package de.laurel.lorbeerwalls.ui;

import de.laurel.lorbeerwalls.hacks.WallhackWorker;

import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {

    private final JButton toggleHackButton;
    private final JCheckBox teamCheckCheckBox;
    private final JCheckBox healthBarCheckBox;
    private final JCheckBox skeletonEspCheckBox;
    private final JSlider maxFpsSlider;
    private final JLabel maxFpsLabel;
    private final JTextArea logArea;
    private WallhackWorker wallhackWorker;
    private WallhackOverlay overlay;

    public MainGUI() {
        setTitle("LorbeerWalls");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        toggleHackButton = new JButton("Load");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        controlPanel.add(toggleHackButton, gbc);

        gbc.gridwidth = 1;

        maxFpsLabel = new JLabel("max fps: 144");
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(maxFpsLabel, gbc);

        maxFpsSlider = new JSlider(30, 300, 144);
        gbc.gridx = 1;
        gbc.gridy = 1;
        controlPanel.add(maxFpsSlider, gbc);

        teamCheckCheckBox = new JCheckBox("team check", true);
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlPanel.add(teamCheckCheckBox, gbc);

        skeletonEspCheckBox = new JCheckBox("skeleton esp", true);
        gbc.gridx = 1;
        gbc.gridy = 2;
        controlPanel.add(skeletonEspCheckBox, gbc);

        healthBarCheckBox = new JCheckBox("healthbar", true);
        gbc.gridx = 0;
        gbc.gridy = 3;
        controlPanel.add(healthBarCheckBox, gbc);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        printToLog("gui initialized");

        toggleHackButton.addActionListener(e -> {
            if (wallhackWorker == null || wallhackWorker.isDone()) {
                startWallhack();
            } else {
                stopWallhack();
            }
        });

        maxFpsSlider.addChangeListener(e -> maxFpsLabel.setText("max fps: " + maxFpsSlider.getValue()));
    }

    private void startWallhack() {
        printToLog("starting wallhack");
        toggleHackButton.setText("Unload");
        overlay = new WallhackOverlay(this);
        overlay.setVisible(true);
        wallhackWorker = new WallhackWorker(this, overlay);
        wallhackWorker.execute();
    }

    private void stopWallhack() {
        printToLog("stopping wallhack");
        toggleHackButton.setEnabled(false);
        if (wallhackWorker != null && !wallhackWorker.isDone()) {
            wallhackWorker.cancel(true);
        }
    }

    public void onWallhackStopped() {
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.dispose();
            overlay = null;
        }
        toggleHackButton.setText("Load");
        toggleHackButton.setEnabled(true);
        wallhackWorker = null;
    }

    public boolean isTeamCheckEnabled() {
        return teamCheckCheckBox.isSelected();
    }

    public boolean isHealthBarEnabled() {
        return healthBarCheckBox.isSelected();
    }

    public boolean isSkeletonEspEnabled() {
        return skeletonEspCheckBox.isSelected();
    }

    public int getMaxFps() {
        return maxFpsSlider.getValue();
    }

    public void printToLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}
