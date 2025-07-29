package de.laurel.lorbeerwalls.main;

import de.laurel.lorbeerwalls.ui.MainGUI;

import javax.swing.*;

/**
 * main entry point for the application
 * @author lorberry+chatgpt
 */
public class LorbeerWalls {
    /**
     * starts the application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("starting LorbeerWalls");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("error setting look and feel: " + e.getMessage());
            }
            new MainGUI().setVisible(true);
        });
    }
}
