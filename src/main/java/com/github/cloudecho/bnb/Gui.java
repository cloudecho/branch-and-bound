package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.ui.TextLineNumber;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class Gui extends JFrame {
    private static final Log LOG = LogFactory.getLog(Gui.class);
    static final Font BUTTON_FONT = new Font(Font.DIALOG, Font.PLAIN, 14);
    static final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);

    JButton btnSolve = new JButton("SOLVE");
    JButton btnClear = new JButton("CLEAR OUT");
    JToggleButton btnHide = new JToggleButton("HIDE INPUT");
    JTextArea editor = new JTextArea(12, 70);
    JTextArea info = new JTextArea(24, 70);
    final JSplitPane splitPane;

    Gui() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnSolve.setFont(BUTTON_FONT);
        btnClear.setFont(BUTTON_FONT);
        btnHide.setFont(BUTTON_FONT);

        // north
        Box north = Box.createHorizontalBox();
        north.add(btnHide);
        north.add(btnClear);
        north.add(Box.createGlue());
        north.add(btnSolve);

        // splitPane/top
        editor.setFont(TEXT_FONT);
        editor.setText(Model.sample());
        JScrollPane editorPane = new JScrollPane(editor);
        TextLineNumber textLineNumber = new TextLineNumber(editor);
        textLineNumber.setCurrentLineForeground(Color.PINK);
        editorPane.setRowHeaderView(textLineNumber);

        // splitPane/bottom
        info.setEditable(false);
        info.setFont(TEXT_FONT);
        JScrollPane infoPane = new JScrollPane(info);

        // assembly
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPane, infoPane);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(north, BorderLayout.NORTH);

        addListeners();
    }

    static {
        BnB.LOG.setLevel(Level.ALL);
    }

    private void addListeners() {
        btnHide.addActionListener((actionEvent) -> {
            splitPane.setDividerLocation(btnHide.isSelected() ? 0 : splitPane.getLastDividerLocation());
        });

        btnSolve.addActionListener((actionEvent) -> {
            if (info.getText().length() > 0) {
                info.append("----\n\n");
            }
            Solver solver = Model.valueOf(editor.getText()).solver();
            // show result
            showInfo(solver);
            solver.solve();  // solve
            showInfo(solver);
        });

        btnClear.addActionListener((actionEvent) -> {
            info.setText("");
        });
    }

    private void showInfo(Solver solver) {
        info.append(solver.toString());
        info.append("\n\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                LOG.warn(e);
            }

            Gui gui = new Gui();
            gui.pack();
            gui.setVisible(true);
        });
    }
}
