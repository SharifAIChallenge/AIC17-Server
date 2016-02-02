import javax.swing.*;
import java.awt.*;

/**
 * Copyright (C) 2016 Hadi
 */
public class UITest {
    public static void main(String[] args) {
        int[][] adj = {
                {1, 2, 3, 4}, // 0
                {0, 2, 5, 6}, // 1
                {0, 1, 7, 8}, // 2
                {0, 9, 10}, // 3
                {0, 5, 7}, // 4
                {1, 4, 8}, // 5
                {1, 10}, // 6
                {2, 4, 8, 9}, // 7
                {2, 5, 7}, // 8
                {3, 7, 10}, // 9
                {3, 6, 9}, // 10
        };
        int[] owners = {
                -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 1,
        };
        int[] armyCounts = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
        };
        int[] xs = {
                1, 2, 3, 4, 5, 6, 10, 100, 1000, 10000, 50,
        };
        int[] ys = {
                1, 2, 3, 4, 5, 6, 10, 100, 1000, 10000, 50,
        };
        UITest uit = new UITest(11, adj, xs, ys);
        uit.update(owners, armyCounts);
    }

    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel();
    private final int width = 1000, height = 700;
    private final int radius = 9, border = 1, edge = 2;
    private int nNodes;
    private Node[] nodes;
    private int[][] adj;
    private int owners[];
    private int armyCounts[];
    private final Object lock = new Object();
    private static final Color[] colors = {
            Color.WHITE, // back ground
            Color.GRAY, // edges
            Color.BLACK, // nodes border
            Color.DARK_GRAY, // free
            Color.BLUE, // player 1
            Color.RED, // player 2
            Color.WHITE, // text
    };

    public UITest(int nodesCount, int[][] adjList, int xs[], int ys[]) {
        int minx = Integer.MAX_VALUE, maxx = Integer.MIN_VALUE;
        for (int x : xs) {
            minx = Math.min(x, minx);
            maxx = Math.max(x, maxx);
        }
        int miny = Integer.MAX_VALUE, maxy = Integer.MIN_VALUE;
        for (int y : ys) {
            miny = Math.min(y, miny);
            maxy = Math.max(y, maxy);
        }
        double xc = (double) (width - 2*radius) / (maxx - minx + 1);
        double yc = (double) (height - 2*radius) / (maxy - miny + 1);
        nNodes = nodesCount;
        nodes = new Node[nodesCount];
        for (int i = 0; i < nodesCount; i++) {
            nodes[i] = new Node(i, (int) ((xs[i]-minx)*xc + radius), (int) ((ys[i]-miny)*yc + radius));
        }
        this.adj = adjList;
        initUI();
    }

    private void initUI() {
        JFrame frame = new JFrame("Slippy Toad");
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        synchronized (lock) {
            g.setColor(colors[0]);
            g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
            g.setColor(colors[1]);
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(edge));
            for (int i = 0; i < nNodes; i++) {
                for (int j = 0; j < adj[i].length; j++) {
                    if (adj[i][j] > i)
                        g.drawLine(nodes[i].x, nodes[i].y, nodes[adj[i][j]].x, nodes[adj[i][j]].y);
                }
            }
            g.setStroke(oldStroke);
            for (int i = 0; i < nNodes; i++) {
                int total = border + radius;
                g.setColor(colors[2]);
                g.fillOval(nodes[i].x - total, nodes[i].y - total, 2 * total, 2 * total);
                g.setColor(colors[owners[i] + 4]);
                g.fillOval(nodes[i].x - radius, nodes[i].y - radius, 2 * radius, 2 * radius);
//                g.setColor(colors[0]);
//                g.fillRect(nodes[i].x + radius, nodes[i].y + radius, 2 * radius, 2 * radius);
                g.setColor(colors[6]);
                g.setFont(new Font("Calibri", Font.PLAIN, (int) (radius * 1.5)));
                String str = String.valueOf(armyCounts[i]);
                g.drawString(str, nodes[i].x - g.getFontMetrics().stringWidth(str)/2, nodes[i].y + radius/2);
            }
        }
    }

    public void update(int owners[], int armyCounts[]) {
        synchronized (lock) {
//            this.adj = adj;
            this.owners = owners;
            this.armyCounts = armyCounts;
        }
        panel.repaint();
    }


    private class Node {
        private int index, x, y;

        public Node(int index, int x, int y) {
            this.index = index;
            this.x = x;
            this.y = y;
        }
    }
}