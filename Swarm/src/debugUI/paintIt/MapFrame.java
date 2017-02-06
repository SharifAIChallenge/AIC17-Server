package debugUI.paintIt;

import Swarm.models.Map;

import javax.swing.*;
import java.awt.*;

/**
 * Created by miladink on 2/6/17.
 */
public class MapFrame extends JFrame {
    private MapPanel2 mapPanel;

    public MapFrame(Map map) throws HeadlessException {
        mapPanel = new MapPanel2(map);
        this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(mapPanel);
        this.pack();
    }

    public void setMap(Map map){
        mapPanel.setMap(map);
    }

    public void gameOver(){
        mapPanel.gameOver();
    }

    public MapPanel2 getMapPanel() {
        return mapPanel;
    }

    public void setMapPanel(MapPanel2 mapPanel) {
        this.mapPanel = mapPanel;
    }
}
