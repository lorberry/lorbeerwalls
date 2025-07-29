package de.laurel.lorbeerwalls.ui;

import de.laurel.lorbeerwalls.hacks.WallhackWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * main user interface window for the tool
 * @author lorberry+chatgpt
 */
public class MainGUI extends JFrame {

    /** checkbox to toggle the hack */
    private final JCheckBox wallhackCheckBox;
    /** textarea for logging */
    private final JTextArea logArea;
    /** background worker instance */
    private WallhackWorker wallhackWorker;
    /** drawing overlay instance */
    private WallhackOverlay overlay;

    /**
     * creates the main gui
     */
    public MainGUI() {
        setTitle("LorbeerWalls");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        wallhackCheckBox = new JCheckBox("load walls");
        controlPanel.add(wallhackCheckBox);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        printToLog("gui initialized");

        wallhackCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                startWallhack();
            } else {
                stopWallhack();
            }
        });
    }

    /**
     * starts the wallhack feature
     */
    private void startWallhack() {
        printToLog("starting wallhack");
        wallhackCheckBox.setEnabled(false);
        overlay = new WallhackOverlay();
        overlay.setVisible(true);

        wallhackWorker = new WallhackWorker(this, overlay);
        wallhackWorker.execute();
    }

    /**
     * stops the wallhack feature
     */
    private void stopWallhack() {
        printToLog("stopping wallhack");
        if (wallhackWorker != null && !wallhackWorker.isDone()) {
            wallhackWorker.cancel(true);
        }
    }

    /**
     * callback for when the wallhack stops
     */
    public void onWallhackStopped() {
        if (overlay != null) {
            overlay.setVisible(false);
            overlay.dispose();
            overlay = null;
        }
        wallhackCheckBox.setEnabled(true);
        if (wallhackCheckBox.isSelected()) {
            wallhackCheckBox.setSelected(false);
        }
    }

    /**
     * prints a message to the log area
     * @param message message to print
     */
    public void printToLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}
