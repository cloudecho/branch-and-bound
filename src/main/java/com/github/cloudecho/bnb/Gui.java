package com.github.cloudecho.bnb;

import javax.swing.*;

public class Gui extends JFrame {
    private static final Log LOG = LogFactory.getLog(Gui.class);

    Gui() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            LOG.warn(e);
        }

        Gui gui = new Gui();
        gui.pack();
        gui.setVisible(true);
    }
}
