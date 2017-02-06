package debugUI.paintIt;
import Swarm.map.Cell;
import Swarm.models.Map;
import Swarm.objects.Fish;
import Swarm.objects.Food;
import Swarm.objects.Teleport;
import Swarm.objects.Trash;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
written by miladink
 */
public class MapPanel2 extends JPanel{

    private Map gameMap;
    private int cellSize;

    private ZipOutputStream out;
    private ArrayList<BufferedImage> shots = new ArrayList<>();
    private int needle = 0;
    public MapPanel2(Map gameMap){
        this.gameMap = gameMap;
        int cellWidth = Math.min(80, 800/gameMap.getW());//to be sure that width will not violate 600
        int cellHeight = Math.min(80, 600/gameMap.getH());//to be sure that height will not violate 800
        cellSize = Math.min(cellWidth, cellHeight);
        int width = gameMap.getW() * cellSize;
        int height = gameMap.getH() * cellSize;
        this.setSize(new Dimension(width, height));
        this.setPreferredSize(new Dimension(width,height));

        //---enter the map and the first shot will be taken
        this.setMap(gameMap);
        this.setFocusable(true);
        this.requestFocus();
        MapPanel2 thisMap = this;
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==39) {
                    if(shots.size()>needle+1)
                        needle+=1;
                    thisMap.repaint();
                }
                else if(e.getKeyCode() == 37){
                    if(needle>0)
                        needle-=1;
                    thisMap.repaint();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                thisMap.requestFocus();
            }
        });
    }
    @Override
    protected synchronized void paintComponent(Graphics g){
        super.paintComponent(g);
        //---draw the image which sign is on, on the panel
        Graphics2D g2d = (Graphics2D)g;
        if(needle!=-1 && shots.size()>0) {
            g2d.drawImage(shots.get(needle), 0, 0, null);
        }
        //---the resulted image is now drawn on the panel
    }
    public void setMap(Map gameMap){
        this.gameMap = gameMap;
        updatePaint();

    }
    private void updatePaint(){
        //---we will draw on this image and then draw this image on the JPanel
        BufferedImage shot = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_3BYTE_BGR);//TODO:buggy maybe
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
                CellPainter.paint(cells[i][j], cellSize, gTemp2d);
                gTemp2d.translate(-j * cellSize, -i * cellSize);
            }
        }
        //---each cell is drawn
        shots.add(shot);
    }
    void gameOver(){//TODO:call this before you exit
        //---open the zip file for storing the images
        File f = new File("history.zip");
        try {
            out = new ZipOutputStream(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(int i = 0; i<shots.size(); i++) {
            ZipEntry ze = new ZipEntry(Integer.toString(i) + ".png");
            try {
                out.putNextEntry(ze);
                ImageIO.write(shots.get(i), "png", out);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            out.closeEntry();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


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

        MapFrame mapFrame = new MapFrame(map);
        cells[5][5].setContent(new Food(1, cells[5][5]));
        for(int i = 0; i<10; i+=2)
            for(int j = 0; j<10; j+=3) {
                cells[i][j].setContent(new Trash(1, cells[i][j]));
                mapFrame.setMap(map);
            }
        mapFrame.gameOver();

    }
}
