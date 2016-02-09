import models.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Copyright (C) 2016 Hadi
 */
public class DebugUI {
    public static void main(String[] args) {
//        int[][] adj = {
//                {1, 2, 3, 4}, // 0
//                {0, 2, 5, 6}, // 1
//                {0, 1, 7, 8}, // 2
//                {0, 9, 10}, // 3
//                {0, 5, 7}, // 4
//                {1, 4, 8}, // 5
//                {1, 10}, // 6
//                {2, 4, 8, 9}, // 7
//                {2, 5, 7}, // 8
//                {3, 7, 10}, // 9
//                {3, 6, 9}, // 10
//        };
//        int[] owners = {
//                -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1,
//        };
//        int[] armyCounts = {
//                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
//        };
//        int[] xs = {
//                1, 2, 3, 4, 5, 6, 10, 100, 1000, 10000, 50,
//        };
//        int[] ys = {
//                1, 2, 3, 4, 5, 6, 10, 100, 1000, 10000, 50,
//        };
//        DebugUI uit = new DebugUI(11, adj, xs, ys);
//        uit.update(owners, armyCounts);
    }

    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel();
    private final int width = 800, height = 600;
    private final int radius = 9, border = 1, edge = 2;
    private int nNodes;
    private int[] x, y;
//    private Node[] nodes;
//    private int[][] adj;
//    private int owners[], armyCounts[];

    private FlowsGameLogic logic;

    private final Object lock = new Object();
    private static final Color[] colors = {
            Color.WHITE,        // back ground
            Color.GRAY,         // edges
            Color.BLACK,        // nodes border
            Color.DARK_GRAY,    // free
            Color.BLUE,         // player 1
            Color.RED,          // player 2
            Color.WHITE,        // text
    };

    public DebugUI(FlowsGameLogic logic) {
        this.logic = logic;
        this.nNodes = logic.getContext().getMap().getVertexNum();

        x = new int[nNodes];
        y = new int[nNodes];
        for (int i = 0; i < nNodes; i++) {
            int xs = logic.getContext().getMap().getNode(i).getX();
            int ys = logic.getContext().getMap().getNode(i).getY();
            x[i] = (int) ((width - radius * 3) * (double) xs / 1000 + 1.5 * radius);
            y[i] = (int) ((height - radius * 3) * (double) ys / 1000 + 1.5 * radius);
        }

        initUI();
        update();
    }

    private void initUI() {
        JFrame frame = new JFrame("Flows (Debugging UI)");
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
        frame.getContentPane().add(panel);
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

        Map map = logic.getContext().getMap();
        int[][] adj = map.getAdjacencyList();
        int owners[] = map.getOwnership();
        int armyCounts[] = map.getArmyCount();

        // draw edges
        g.setColor(colors[0]);
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
        g.setColor(colors[1]);
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(edge));
        for (int i = 0; i < nNodes; i++) {
            for (int j = 0; j < adj[i].length; j++) {
                if (adj[i][j] > i)
                    g.drawLine(x[i], y[i], x[adj[i][j]], y[adj[i][j]]);
            }
        }

        // draw nodes
        g.setStroke(oldStroke);
        for (int i = 0; i < nNodes; i++) {
            int total = border + radius;
            g.setColor(colors[2]);
            g.fillOval(x[i] - total, y[i] - total, 2 * total, 2 * total);
            g.setColor(colors[owners[i] + 4]);
            g.fillOval(x[i] - radius, y[i] - radius, 2 * radius, 2 * radius);
//                g.setColor(colors[0]);
//                g.fillRect(nodes[i].x + radius, nodes[i].y + radius, 2 * radius, 2 * radius);
            g.setColor(colors[6]);
            g.setFont(new Font("Calibri", Font.PLAIN, (int) (radius * 1.5)));
            String str = String.valueOf(armyCounts[i]);
            g.drawString(str, x[i] - g.getFontMetrics().stringWidth(str)/2, y[i] + radius/2);
        }

    }

    public void update() {
        panel.repaint();
    }

    private int step = 0;
    private void saveImage() {
        BufferedImage bImg = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D cg = bImg.createGraphics();
        panel.paintAll(cg);
        try {
            if (ImageIO.write(bImg, "png", new File(String.format("./screenshot/step%04d.png", step++)))) {
                System.out.println("-- saved");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}