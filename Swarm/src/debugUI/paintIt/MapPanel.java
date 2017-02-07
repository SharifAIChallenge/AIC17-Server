package debugUI.paintIt;
import Swarm.map.Cell;
import Swarm.models.Map;
import Swarm.objects.Fish;
import Swarm.objects.Teleport;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
written by miladink
 */
public class MapPanel extends JPanel{

    private Map gameMap;
    private int cellSize;
    private BufferedImage shot;
    private int counter = 0;
    private ZipOutputStream out;

    public MapPanel(Map gameMap){
        this.gameMap = gameMap;
        int cellWidth = Math.min(80, 800/gameMap.getW());//to be sure that width will not violate 600
        int cellHeight = Math.min(80, 600/gameMap.getH());//to be sure that height will not violate 800
        cellSize = Math.min(cellWidth, cellHeight);
        int width = gameMap.getW() * cellSize;
        int height = gameMap.getH() * cellSize;
        this.setSize(new Dimension(width, height));
        this.setPreferredSize(new Dimension(width,height));


        //---open the zip file for storing the images
        File f = new File("history.zip");
        try {
            out = new ZipOutputStream(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //---enter the map and the first shot will be taken
        this.setMap(gameMap);

    }

    @Override
    protected synchronized void paintComponent(Graphics g){
        super.paintComponent(g);
        //---draw the image which sign is on, on the panel
        Graphics2D g2d = (Graphics2D)g;
        if(shot!= null) {
            g2d.drawImage(shot, 0, 0, null);
        }
        //---the resulted image is now drawn on the panel
    }
    public void setMap(Map gameMap){
        this.gameMap = gameMap;
        updatePaint();
        this.repaint();
        this.saveImage();
    }
    private void updatePaint(){
        //---we will draw on this image and then draw this image on the JPanel
        shot = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_3BYTE_BGR);//TODO:buggy maybe
        Graphics gTemp = shot.createGraphics();
        Graphics2D gTemp2d = (Graphics2D)gTemp;
        super.paintComponent(gTemp);
        //---set the settings for g2d
        gTemp2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        gTemp2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        //---settings set
        //---draw each cell individually
        Cell cells[][] = gameMap.getCells();
        for(int i = 0; i<cells.length;i++) {
            for (int j = 0; j < cells[0].length; j++) {
                gTemp2d.translate(j * cellSize, i * cellSize);
                //CellPainter.paint(cells[i][j], cellSize, gTemp2d);//TODO:correct this
                gTemp2d.translate(-j * cellSize, -i * cellSize);
            }
        }
        //---each cell is drawn
    }
    private void saveImage() {//TODO: call it when the game is over
        //---save the history in the 'history.zip'
        ZipEntry ze = new ZipEntry(Integer.toString(counter)+".png");
        try {
            out.putNextEntry(ze);
            ImageIO.write(shot, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        counter++;
    }
    public void gameOver(){//TODO:call this before you exit
        try {
            out.closeEntry();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Map getGameMap() {
        return gameMap;
    }

/*
    public static void main(String[] args) {
        Cell cells[][] = new Cell[10][10];
        for(int i = 0; i<10; i++)
            for(int j = 0; j<10; j++) {
                cells[i][j] = new Cell();
                int randomNum = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                if((randomNum%2)==0)
                    cells[i][j].setContent(new Fish(i,cells[i][j],0,0,true,0));
                cells[i][j].setRow(i);
                cells[i][j].setColumn(j);
            }
        cells[0][7].setTeleport(new Teleport(1, cells[0][7], cells[8][9]));
        cells[8][9].setTeleport(new Teleport(1, cells[8][9], cells[0][7]));
        Map map = new Map();
        map.setCells(cells);
        map.setW(cells.length);
        map.setH(cells[0].length);
        MapPanel mapPanel = new MapPanel(map);
        //open the JFrame frame
        MapFrame mapFrame = new MapFrame(map);
        mapFrame.gameOver();
    }
    */
}
