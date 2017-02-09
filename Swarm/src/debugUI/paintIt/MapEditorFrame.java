package debugUI.paintIt;

import Swarm.map.Cell;
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
public class MapEditorFrame extends JFrame {
    private MapEditorPanel mapPanel;
    private Map map;
    private ButtonGroup buttonGroup = new ButtonGroup();
    ArrayList<JRadioButton> jRadioButtons = new ArrayList<>();
    public MapEditorFrame(){
        String message = JOptionPane.showInputDialog(null, "width,height");
        message = message.replaceAll("\n ", "");
        int commaPlace = message.indexOf(',');
        String str1 = message.substring(0, commaPlace);
        String str2 = message.substring(commaPlace+1);
        //make a map for the frame to be edited
        setMap(Integer.valueOf(str1),Integer.valueOf(str2));

        //prepare the frame
        this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        //making the radio buttons for the editor
        JPanel optionPanel = new JPanel(new GridBagLayout());
        final JRadioButton roach = new JRadioButton("roach", true);
        final JRadioButton trash = new JRadioButton("trash", false);
        final JRadioButton food = new JRadioButton("food", false);
        final JRadioButton slipper = new JRadioButton("slipper", false);
        final JRadioButton teleport = new JRadioButton("teleport", false);
        final JRadioButton eraser = new JRadioButton("eraser", false);

        EditorUtilSet.addComponentX(0, 1, roach, optionPanel);
        EditorUtilSet.addComponentX(1, 1, trash, optionPanel);
        EditorUtilSet.addComponentX(2, 1, food, optionPanel);
        EditorUtilSet.addComponentX(3, 1, slipper, optionPanel);
        EditorUtilSet.addComponentX(4, 1, teleport, optionPanel);
        EditorUtilSet.addComponentX(5, 1, eraser, optionPanel);
        buttonGroup.add(roach);
        jRadioButtons.add(roach);
        buttonGroup.add(trash);
        jRadioButtons.add(trash);
        buttonGroup.add(food);
        jRadioButtons.add(food);
        buttonGroup.add(slipper);
        jRadioButtons.add(slipper);
        buttonGroup.add(teleport);
        jRadioButtons.add(teleport);
        buttonGroup.add(eraser);
        jRadioButtons.add(eraser);

        //making the motherPanel for JFrame
        JPanel motherPanel = new JPanel();
        motherPanel.setLayout(new GridBagLayout());

        mapPanel = new MapEditorPanel(this, map);//it is the frame to set
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        JButton button4 = new JButton("save map");
        button4.addActionListener(e -> {
            mapPanel.saveMap();
            mapPanel.requestFocus();
        });
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
        motherPanel.add(optionPanel, BorderLayout.NORTH);
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
        System.out.println(getSelected());

    }

    public void setMap(int w, int h){
        if(w<1 || h<1)
            return;
        map = new Map();
        Cell cells[][] = new Cell[h][w];
        for(int i = 0; i<h; i++)
            for(int j = 0; j<w; j++){
                cells[i][j] = new Cell(i, j, null, null, null);
            }
        map.setCells(cells);
        map.setW(w);
        map.setH(h);
        this.mapPanel = new MapEditorPanel(this, map);
    }

    public String getSelected(){
        for(JRadioButton jRadioButton: jRadioButtons)
            if(jRadioButton.isSelected())
                return jRadioButton.getText();
        return "it is a bug";
    }
}
