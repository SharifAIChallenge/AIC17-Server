package Swarm;

import network.data.Message;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Copyright (C) 2016 Hadi
 */
public class DebugUI {
    private final int width = 800, height = 600;
    private final int radius = 14, border = 3, edge = 3, arrSize = 5;
    private int nNodes, nTurns;
    private int[] x, y;

    private int[][] adj;
    private int owners[];
    private int armyCounts[];
    private model.Event[][] events;
    private Message status;
    private int[] movesDest;
    private int[] moveSize;

    private int theme = 0;
    private volatile boolean paused = false;
    private volatile boolean recording = false;
    private volatile boolean cont = false;

    private JFrame frame = new JFrame();
    private ArrayList<BufferedImage> screenshots = new ArrayList<>();
    private JPanel container = new JPanel();
    private JPanel panel = new JPanel();
    private Box buttonsPanel = Box.createHorizontalBox();
    private JButton[] buttons = new JButton[]{
            new JButton("Change Theme"),
            new JButton("Pause Game"),
            new JButton("Start Recording"),
            new JButton("Next Step"),
    };
    private ActionListener[] buttonActions = new ActionListener[]{
            e -> {
                theme = (theme + 1) % themes.length;
                panel.repaint();
            },
            e -> {
                if (!paused) {
                    paused = true;
                    buttons[1].setText("Resume Game");
                    buttons[3].setEnabled(true);
                } else {
                    paused = false;
                    buttons[1].setText("Pause Game");
                    buttons[3].setEnabled(false);
                }
            },
            e -> {
                if (!recording) {
                    recording = true;
                    buttons[2].setText("Stop Recording");
                } else {
                    recording = false;
                    buttons[2].setText("Start Recording");
                    saveImages();
                }
            },
            e -> {
                cont = true;
            }
    };

    private static final Color[][] themes = new Color[][]{
            {
                    new Color(247, 247, 247), // back ground
                    new Color(84, 82, 87), // edges
                    new Color(84, 82, 87), // nodes border
                    new Color(230, 219, 214), // free
                    new Color(242, 184, 133), // player 1
                    new Color(222, 230, 181), // player 2
                    new Color(84, 82, 87), // text
                    new Color(0, 0, 0, 0), // transparent
            },
            {
                    new Color(245, 237, 243), // back ground
                    new Color(89, 79, 83), // edges
                    new Color(89, 79, 83), // nodes border
                    new Color(245, 237, 243), // free
                    new Color(204, 126, 156), // player 1
                    new Color(164, 179, 156), // player 2
                    new Color(89, 79, 83), // text
                    new Color(0, 0, 0, 0), // transparent
            },
            {
                    new Color(97, 97, 115), // back ground
                    new Color(242, 242, 223), // edges
                    new Color(242, 242, 223), // nodes border
                    new Color(97, 97, 115), // free
                    new Color(156, 87, 106), // player 1
                    new Color(56, 59, 51), // player 2
                    new Color(242, 242, 223), // text
                    new Color(0, 0, 0, 0), // transparent
            },
            {
                    new Color(238, 223, 204), // back ground
                    new Color(72, 36, 10), // edges
                    new Color(72, 36, 10), // nodes border
                    new Color(238, 223, 204), // free
                    new Color(250, 128, 114), // player 1
                    new Color(179, 238, 251), // player 2
                    new Color(72, 36, 10), // text
                    new Color(0, 0, 0, 0), // transparent
            },
            {
                    new Color(239, 187, 255), // back ground
                    new Color(95, 0, 95), // edges
                    new Color(95, 0, 95), // nodes border
                    new Color(239, 187, 255), // free
                    new Color(216, 150, 255), // player 1
                    new Color(190, 41, 236), // player 2
                    new Color(95, 0, 95), // text
                    new Color(0, 0, 0, 0), // transparent
            },
            {
                    new Color(223, 169, 67), // back ground
                    new Color(139, 52, 14), // edges
                    new Color(77, 27, 0), // nodes border
                    new Color(223, 169, 67), // free
                    new Color(169, 94, 0), // player 1
                    new Color(255, 180, 0), // player 2
                    new Color(77, 27, 0), // text
                    new Color(0, 0, 0, 0), // transparent
            },
    };
    private String[] themeNames = {
            "Typical",
            "Bruise",
            "Dark",
            "Short",
            "Purpy",
            "Wood",
    };

    public DebugUI(SwarmGameLogic logic) {
        this.nNodes = logic.getContext().getMap().getVertexNum();
        this.nTurns = logic.getContext().getMap().getGameConstants().getTurns();

        x = new int[nNodes];
        y = new int[nNodes];
        for (int i = 0; i < nNodes; i++) {
            int xs = logic.getContext().getMap().getNode(i).getX();
            int ys = logic.getContext().getMap().getNode(i).getY();
            x[i] = (int) ((width - radius * 3) * (double) xs / 1000 + 1.5 * radius);
            y[i] = (int) ((height - radius * 3) * (double) ys / 1000 + 1.5 * radius);
        }

        theme = (int) (Math.random() * themes.length);

        initUI();
    }

    private void initUI() {
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintGame((Graphics2D) g);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setMinimumSize(new Dimension(width, height));
        panel.setPreferredSize(new Dimension(width, height));
        panel.setMaximumSize(new Dimension(width, height));

        container.setLayout(new BorderLayout(0, 0));
        container.add(panel, BorderLayout.CENTER);
        container.add(buttonsPanel, BorderLayout.SOUTH);

        buttonsPanel.add(Box.createGlue());
        for (int i = 0; i < buttons.length; i++) {
            buttonsPanel.add(buttons[i]);
            buttons[i].addActionListener(buttonActions[i]);
            buttons[i].setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 5, 10, 5), buttons[i].getBorder()));
        }
        buttonsPanel.add(Box.createGlue());
        buttons[3].setEnabled(false);

        JFrame frame = new JFrame("Flows (Debugging UI)");
        frame.getContentPane().add(container);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void paintGame(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        container.setBackground(themes[theme][0]);
        buttonsPanel.setBackground(themes[theme][0]);
        for (Component c : buttonsPanel.getComponents()) {
            c.setBackground(themes[theme][0]);
            c.setForeground(themes[theme][6]);
        }
        buttons[0].setText("Theme: " + themeNames[theme]);

        // background
        g.setColor(themes[theme][0]);
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);

        if (adj == null)
            return;

        // draw edges
        g.setColor(themes[theme][1]);
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(edge));
        for (int i = 0; i < nNodes; i++) {
            for (int j = 0; j < adj[i].length; j++) {
                if (adj[i][j] > i)
                    g.drawLine(x[i], y[i], x[adj[i][j]], y[adj[i][j]]);
            }
        }

        // draw events
        g.setStroke(new BasicStroke(edge));
        if (events != null) {
            for (int i = 0; i < 2; i++) {
                for (model.Event event : events[i]) {
                    if (!"m".equals(event.getType()))
                        continue;
                    if (event.getArgs() == null || event.getArgs().length != 3)
                        continue;
                    int src = Integer.parseInt(event.getArgs()[0]);
                    int dst = Integer.parseInt(event.getArgs()[1]);
                    int arm = Integer.parseInt(event.getArgs()[2]);
                    g.setColor(themes[theme][1]);
                    drawArrow(g, x[src], y[src], (x[dst] + 2 * x[src]) / 3, (y[dst] + 2 * y[src]) / 3, arrSize + 2);
                    g.setColor(themes[theme][i + 4]);
                    drawArrow(g, x[src], y[src], (x[dst] + 2 * x[src]) / 3, (y[dst] + 2 * y[src]) / 3, arrSize);
                    int dx = x[dst] - x[src];
                    int dy = y[dst] - y[src];
                    if (dy < 0) {
                        dx = -dx;
                        dy = -dy;
                    }
                    int norm2 = dx * dx + dy * dy;
                    if (norm2 != 0) {
                        double norm = Math.sqrt(norm2);
                        int rad = (int) (radius * 0.75);
                        drawNode(g, String.valueOf(arm), (x[dst] + 2 * x[src]) / 3 - rad - (int) (radius * 1.1 * dy / norm), (y[dst] + 2 * y[src]) / 3 - rad + (int) (radius * 1.1 * dx / norm), rad, 0, 0, 0, 1.5);
//                        g.setColor(themes[theme][6]);
//                        g.setFont(new Font("Calibri", Font.PLAIN, radius));
//                        String str = String.valueOf(arm);
//                        g.drawString(str, (x[dst] + 2 * x[src]) / 3 - g.getFontMetrics().stringWidth(str) - (int) (radius * 0.8 * dy / norm), (y[dst] + 2 * y[src]) / 3 + radius / 3 + (int) (radius * 0.8 * dx / norm));
                    }
                }
            }
        }

        // draw nodes & accepted events
        g.setStroke(new BasicStroke(edge + 2));
        for (int i = 0; i < nNodes; i++) {
            if (movesDest != null && moveSize != null) {
                int src = i;
                int dst = movesDest[i];
                if (dst >= 0) {
//                    drawArrow(g, x[src], y[src], (2 * x[dst] + 3 * x[src]) / 5, (2 * y[dst] + 3 * y[src]) / 5);
                    String str = String.valueOf(moveSize[i]);
                    int dx = x[dst] - x[src];
                    int dy = y[dst] - y[src];
                    if (dy < 0) {
                        dx = -dx;
                        dy = -dy;
                    }
                    int norm2 = dx * dx + dy * dy;
                    if (norm2 != 0) {
                        double norm = Math.sqrt(norm2);
                        int rad = (int) (radius * 0.75);
                        drawNode(g, str, (x[dst] + 2 * x[src]) / 3 - rad - (int) (radius * 1.1 * dy / norm), (y[dst] + 2 * y[src]) / 3 - rad + (int) (radius * 1.1 * dx / norm), rad, 1, 0, 2, 1.5);
//                        int ddx = (int) (radius * dy / norm);
//                        int ddy = (int) (radius * dx / norm);
//                        g.setColor(themes[theme][2]);
//                        g.fillOval((x[dst] + 2 * x[src]) / 3 - radius - ddx, (y[dst] + 2 * y[src]) / 3 - radius * 3 / 4 + ddy, (int) (1.5 * radius), (int) (1.5 * radius));
//                        g.setColor(themes[theme][0]);
//                        g.fillOval((x[dst] + 2 * x[src]) / 3 - radius + border / 2 - ddx, (y[dst] + 2 * y[src]) / 3 - radius * 3 / 4 + border / 2 + ddy, (int) (1.5 * radius) - border, (int) (1.5 * radius) - border);
//                        g.setColor(themes[theme][2]);
//                        g.setFont(new Font("Calibri", Font.PLAIN, radius));
//                        g.drawString(str, (x[dst] + 2 * x[src]) / 3 - g.getFontMetrics().stringWidth(str) - ddx, (y[dst] + 2 * y[src]) / 3 + radius / 3 + ddy);
                    }
                }
            }
            drawNode(g, owners[i] != -1 ? String.valueOf(armyCounts[i]) : "", x[i] - (radius + border), y[i] - (radius + border), radius, border, owners[i] + 4, 2, 1.5);
        }
        g.setStroke(oldStroke);

        // draw status
        int turn = status.args.get(0).getAsInt();
        double score0 = status.args.get(1).getAsDouble();
        double score1 = status.args.get(2).getAsDouble();
        drawNode(g, String.format("%.1f", score0), 10, 10, 2 * radius, 2 * border, 4, 2, 1);
        drawNode(g, String.format("%.1f", score1), width - 4 * (radius + border) - 10, 10, 2 * radius, 2 * border, 5, 2, 1);
        drawNode(g, String.format("%d/%d", Math.max(turn, 0), nTurns), width / 2 - 3*radius, -10, 3*radius, 0, 7, 7, 0.6);
    }

    private void drawNode(Graphics2D g, String str, int x, int y, int radius, int border, int color, int borderColor, double scale) {
        int total = border + radius;
        g.setColor(themes[theme][borderColor]);
        g.fillOval(x, y, 2 * total, 2 * total);
        g.setColor(themes[theme][color]);
        g.fillOval(x + border, y + border, 2 * radius, 2 * radius);
        g.setColor(themes[theme][6]);
        int fontSize = (int) (scale * radius);
        do {
            g.setFont(new Font("Calibri", Font.PLAIN, fontSize));
            fontSize /= 1.1;
        } while (g.getFontMetrics().stringWidth(str) > 1.7 * radius);
        g.drawString(str, x + total - g.getFontMetrics().stringWidth(str) / 2, y + total + g.getFontMetrics().getHeight() / 4);
    }

    private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2, int arrSize) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(arrSize / 2));
        g.setStroke(old);
        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[]{len + arrSize / 2, len - arrSize / 2, len - arrSize / 2, len + arrSize / 2},
                new int[]{0, -arrSize, arrSize, 0}, 4);
    }

    public void update(int[][] adj, int[] owners, int[] armyCounts, model.Event[][] clientEvents, Message status, int[] movesDest, int[] moveSize) {
        cont = false;
        while (paused && !cont)
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        this.adj = copy(adj);
        this.owners = copy(owners);
        this.armyCounts = copy(armyCounts);
        this.events = clientEvents;
        this.status = status;
        this.movesDest = copy(movesDest);
        this.moveSize = copy(moveSize);
        panel.repaint();
        if (recording)
            screenshot();
    }

    private int[][] copy(int[][] arr) {
        if (arr == null)
            return null;
        int[][] cp = new int[arr.length][];
        for (int i = 0; i < arr.length; i++)
            cp[i] = copy(arr[i]);
        return cp;
    }

    private int[] copy(int[] arr) {
        if (arr == null)
            return null;
        int[] cp = new int[arr.length];
        System.arraycopy(arr, 0, cp, 0, arr.length);
        return cp;
    }

    private void screenshot() {
        BufferedImage bImg = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D cg = bImg.createGraphics();
        panel.paintAll(cg);
        screenshots.add(bImg);
    }

    private void saveImages() {
        BufferedImage[] images = screenshots.toArray(new BufferedImage[screenshots.size()]);
        screenshots.clear();
//        File file = null;
//        while (file == null) {
//            JOptionPane.showMessageDialog(null, "Choose a name to save the 'zip' file of recordings.", "AIC16", JOptionPane.INFORMATION_MESSAGE);
//            JFileChooser fileChooser = new JFileChooser((String) null);
//            int result = fileChooser.showOpenDialog(null);
//            if (result != JOptionPane.YES_OPTION)
//                return;
//            file = fileChooser.getSelectedFile();
//        }
        FileDialog fileDialog = new FileDialog((Frame) null, "Save Recorded Images", FileDialog.SAVE);
        fileDialog.setFilenameFilter((dir, name) -> name.matches(".*\\.zip"));
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("out.zip");
        fileDialog.setVisible(true);
        File[] files = fileDialog.getFiles();
        if (files.length != 1)
            return;
        File file = files[0];

        ZipOutputStream zout;
        try {
            zout = new ZipOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException ignore) {
            return;
        }
        for (int i = 0; i < images.length; i++) {
            try {
                ZipEntry entry = new ZipEntry(String.format("image%04d.png", i));
                zout.putNextEntry(entry);
                ImageIO.write(images[i], "png", zout);
            } catch (Exception ignore) {
            }
        }
        try {
            zout.close();
        } catch (IOException ignore) {
        }
    }
}