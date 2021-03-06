package debugUI.paintIt;

import Swarm.models.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import debugUI.paintIt.EditorUtilSet;

/**
 * Created by miladink on 2/6/17.
 */
public class MapFrame extends JFrame {
    private MapPanel3 mapPanel;
    private Map map;
    public MapFrame(Map map) throws HeadlessException {
        //prepare the frame
        this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        //making the motherPanel for JFrame
        JPanel motherPanel = new JPanel();
        motherPanel.setLayout(new GridBagLayout());

        mapPanel = new MapPanel3(map);//it is the frame to set
        this.map = map;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        JButton button1 = new JButton("play/pause(or space)");
        button1.addActionListener(e -> {
            mapPanel.setLive(!mapPanel.isLive());
            mapPanel.requestFocus();
        });
        JButton button2 = new JButton("next step");
        button2.addActionListener(e -> {
            mapPanel.increaseNeedle();
            mapPanel.requestFocus();
        });
        JButton button3 = new JButton("previous step");
        button3.addActionListener(e -> {
            mapPanel.subtractNeedle();
            mapPanel.requestFocus();
        });
        JButton button4 = new JButton("save Images");
        button4.addActionListener(e -> {
            mapPanel.saveImages();
            mapPanel.requestFocus();
        });
        button4.setEnabled(false);
        JButton button5 = new JButton("change theme");
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        JPanel bPanel1 = new JPanel();

        JPanel bPanel2 = new JPanel();
        button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.changeTheme();
                bPanel1.setBackground(mapPanel.getThemeBackGround());
                bPanel2.setBackground(mapPanel.getThemeBackGround());
                buttonPanel.setBackground(mapPanel.getThemeBackGround());
                CellPainter.changeTheme();
                mapPanel.requestFocus();
            }
        });
        mapPanel.saveButton = button4;

        JPanel textPanel = new JPanel();
        JLabel label_team1 = new JLabel("team1:0");
        JLabel label_turn = new JLabel("turn:0");
        JLabel label_team2 = new JLabel("team2:0");
        //set the timers to update these three values
        Timer timer1 = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s1 = Integer.toString(mapPanel.getGameMap().getScore()[0]);
                label_team1.setText("team1:"+s1);
                String s2 = Integer.toString(mapPanel.getGameMap().getScore()[1]);
                label_team2.setText("team2:"+s2);
                String turnStr = Integer.toString(mapPanel.getNeedle().get());
                label_turn.setText("turn:"+turnStr);
            }
        });
        timer1.start();
        textPanel.setLayout(new GridBagLayout());
        EditorUtilSet.addComponentX(0, 1, label_team1, textPanel);
        EditorUtilSet.addComponentX(1, 1, label_turn, textPanel);
        EditorUtilSet.addComponentX(2, 1, label_team2, textPanel);
        //TODO:save the game procedure
        EditorUtilSet.addComponentX(0, 1, button1, buttonPanel);
        EditorUtilSet.addComponentX(1, 1, button2, buttonPanel);
        EditorUtilSet.addComponentX(2, 1, button3, buttonPanel);
        EditorUtilSet.addComponentX(3, 1, button4, buttonPanel);
        EditorUtilSet.addComponentX(4, 1, button5, buttonPanel);

        //EditorUtilSet.addComponentY(0, 1, textPanel, motherPanel);
        //EditorUtilSet.addComponentY(1, 1, mapPanel, motherPanel);
        //EditorUtilSet.addComponentY(2, 1, buttonPanel, motherPanel);
        buttonPanel.setBackground(mapPanel.getThemeBackGround());
        bPanel1.setBackground(mapPanel.getThemeBackGround());
        bPanel2.setBackground(mapPanel.getThemeBackGround());
        EditorUtilSet.addComponentX(0, 50, bPanel1, centerPanel);
        EditorUtilSet.addComponentX(1, 1, mapPanel, centerPanel);
        EditorUtilSet.addComponentX(2, 50, bPanel2, centerPanel);
        motherPanel.setLayout(new BorderLayout());
        motherPanel.add(textPanel, BorderLayout.NORTH);
        motherPanel.add(centerPanel, BorderLayout.CENTER);
        motherPanel.add(buttonPanel, BorderLayout.SOUTH);
        //the end
        this.setContentPane(motherPanel);
        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.pack();

    }

    public void setMap(Map map){
        this.map = map;
        mapPanel.setMap(map);
    }

    public void gameOver(){
        mapPanel.gameOver();
    }

    public MapPanel3 getMapPanel() {
        return mapPanel;
    }

    public void setMapPanel(MapPanel3 mapPanel) {
        this.mapPanel = mapPanel;
    }


}
