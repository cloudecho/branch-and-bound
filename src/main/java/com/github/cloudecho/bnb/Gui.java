package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.ByteArrayLogHandler;
import com.github.cloudecho.bnb.util.Log;
import com.github.cloudecho.bnb.util.LogFactory;
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
    JToggleButton btnSimplexLog = new JToggleButton("SIMPLEX LOG");
    JTextArea editor = new JTextArea(12, 70);
    JTextArea info = new JTextArea(24, 70);
    final JSplitPane splitPane;

    Gui() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnSolve.setFont(BUTTON_FONT);
        btnClear.setFont(BUTTON_FONT);
        btnHide.setFont(BUTTON_FONT);
        btnSimplexLog.setFont(BUTTON_FONT);

        // north
        Box north = Box.createHorizontalBox();
        north.add(btnHide);
        north.add(btnClear);
        north.add(Box.createGlue());
        north.add(btnSimplexLog);
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

    private void addListeners() {
        btnHide.addActionListener(actionEvent ->
                splitPane.setDividerLocation(btnHide.isSelected() ? 0 : splitPane.getLastDividerLocation())
        );

        btnSolve.addActionListener((actionEvent) -> {
            if (info.getText().length() > 0) {
                info.append("\n----\n\n");
            }
            Model model = Model.valueOf(editor.getText());
            Solver solver = model.newSolver();
            solver.solve();
            showInfo(model, solver);
        });

        btnClear.addActionListener((actionEvent) -> {
            info.setText("");
        });

        btnSimplexLog.addActionListener(actionEvent ->
                Simplex.LOG.setLevel(btnSimplexLog.isSelected() ? Level.ALL : Level.INFO)
        );
    }

    static final ByteArrayLogHandler LOG_HANDLER = new ByteArrayLogHandler();

    static {
        BnB.LOG.setLevel(Level.ALL);

        Simplex.LOG.addHandler(LOG_HANDLER);
        BnB.LOG.addHandler(LOG_HANDLER);
    }

    private void showInfo(Model model, Solver solver) {
        info.append(LOG_HANDLER.getString());
        info.append(String.format("%s\n%s: %.6f\niterations: %d\n",
                solver.getState(),
                solver.getObjectiveType(),
                solver.getObjective(),
                solver.getIterations()));
        int j = 0;
        for (String var : model.variables) {
            double[] x = solver.getX();
            info.append(String.format("var%3d:%9s = %.6f\n", j + 1, var, x[j]));
            j++;
        }
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
