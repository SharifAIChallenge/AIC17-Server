package debugUI.paintIt;

import Swarm.models.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import debugUI.paintIt.EditorUtilSet;

/**
 * Created by miladink on 2/6/17.
 */
public class MapFrame extends JFrame {
    private MapPanel3 mapPanel;

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
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        JButton button1 = new JButton("pause/play(or space)");
        button1.addActionListener(e -> mapPanel.setLive(!mapPanel.isLive()));
        JButton button2 = new JButton("next step");
        button2.addActionListener(e -> mapPanel.increaseNeedle());
        JButton button3 = new JButton("previous step");
        button3.addActionListener(e -> mapPanel.subtractNeedle());
        JButton button4 = new JButton("save Images");
        button4.addActionListener(e -> mapPanel.saveImages());
        button4.setEnabled(false);
        mapPanel.saveButton = button4;
        //TODO:save the game procedure
        EditorUtilSet.addComponentX(0, 1, button1, buttonPanel);
        EditorUtilSet.addComponentX(1, 1, button2, buttonPanel);
        EditorUtilSet.addComponentX(2, 1, button3, buttonPanel);
        EditorUtilSet.addComponentX(3, 1, button4, buttonPanel);

        EditorUtilSet.addComponentY(0, 1, mapPanel, motherPanel);
        EditorUtilSet.addComponentY(1, 1, buttonPanel, motherPanel);

        //the end
        this.setContentPane(motherPanel);
        this.pack();

    }

    public void setMap(Map map){
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
