import ui.AppContext;
import ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * CyberShield – Personal Cyber Safety Management System
 * Application entry point.
 *
 * Run this class to launch the application.
 */
public class Main {

    public static void main(String[] args) {
        // Apply FlatLaf-style dark appearance hints (pure Swing, no extra library)
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Force Nimbus as the base LAF for cleaner rendering on all platforms
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Fall back to system default
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored2) {}
        }

        // Override Nimbus defaults with our dark theme colours
        UIManager.put("control",              new Color(22, 27, 34));
        UIManager.put("info",                 new Color(22, 27, 34));
        UIManager.put("nimbusBase",           new Color(13, 17, 23));
        UIManager.put("nimbusAlertYellow",    new Color(248, 187, 0));
        UIManager.put("nimbusDisabledText",   new Color(100, 100, 100));
        UIManager.put("nimbusFocus",          new Color(32, 201, 151));
        UIManager.put("nimbusGreen",          new Color(63, 185, 80));
        UIManager.put("nimbusInfoBlue",       new Color(56, 139, 253));
        UIManager.put("nimbusLightBackground",new Color(30, 37, 46));
        UIManager.put("nimbusOrange",         new Color(210, 153, 34));
        UIManager.put("nimbusRed",            new Color(248, 81, 73));
        UIManager.put("nimbusSelectedText",   new Color(230, 237, 243));
        UIManager.put("nimbusSelectionBackground", new Color(48, 54, 61));
        UIManager.put("text",                 new Color(230, 237, 243));
        UIManager.put("OptionPane.background",     new Color(22, 27, 34));
        UIManager.put("Panel.background",          new Color(22, 27, 34));

        // Launch on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AppContext ctx = AppContext.getInstance();
            LoginFrame login = ctx.createLoginFrame();
            login.setVisible(true);
        });
    }
}
