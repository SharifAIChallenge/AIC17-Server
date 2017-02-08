package debugUI.paintIt;
import Swarm.map.Cell;
import Swarm.models.Map;
import Swarm.objects.*;
import debugUI.DeepCopyMaker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
written by miladink
 */
public class MapPanel2 extends JPanel{

    private final Timer timer1;
    private Map gameMap;
    private int cellSize;
    private boolean isEnded = false;
    JButton saveButton;


    public boolean isEnded() {
        return isEnded;
    }

    public void setEnded(boolean ended) {
        isEnded = ended;
    }

    private boolean isLive = false;
    private boolean saveTried = false;
    private int timeInterval = 500;
    private ZipOutputStream out;
    private ArrayList<BufferedImage> shots = new ArrayList<>();
    private AtomicInteger needle = new AtomicInteger(0);

    MapPanel2(Map gameMap){
        this.gameMap = gameMap;
        setCellSize();
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
                    if(shots.size()>needle.get()+1)
                        increaseNeedle();
                    thisMap.repaint();
                }
                else if(e.getKeyCode() == 37){
                    if(needle.get()>0)
                        needle.decrementAndGet();
                    thisMap.repaint();
                }
                else if(e.getKeyCode()==32){
                    isLive = !(isLive);
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
        timer1 = new Timer(timeInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(thisMap.isLive) {
                    increaseNeedle();
                    thisMap.repaint();
                }
            }
        });
        timer1.start();
    }
    @Override
    protected synchronized void paintComponent(Graphics g){
        super.paintComponent(g);
        //---draw the image which sign is on, on the panel
        Graphics2D g2d = (Graphics2D)g;
        if(needle.get()!=-1 && shots.size()>0) {
            g2d.drawImage(shots.get(needle.get()), 0, 0, null);
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
                //CellPainter.paint(cells[i][j], cellSize, gTemp2d);//TODO:correct this
                gTemp2d.translate(-j * cellSize, -i * cellSize);
            }
        }
        //---each cell is drawn
        shots.add(shot);
    }
    void gameOver(){//TODO:call this before you exit
        //---open the zip file for storing the images
        isEnded = true;
        if(saveButton != null)
            saveButton.setEnabled(true);
    }
    public void saveImages(){
        saveTried = true;
        FileDialog fileDialog = new FileDialog((Frame) null, "Save Recorded Images", FileDialog.SAVE);
        fileDialog.setFilenameFilter((dir, name) -> name.matches(".*\\.zip"));
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("history.zip");
        fileDialog.setVisible(true);
        File[] files = fileDialog.getFiles();
        if (files.length != 1)
            return;
        File f = files[0];
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
    private void setCellSize(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int cellWidth = Math.min(80, (int)(screenSize.getWidth()*0.8)/gameMap.getW());//to be sure that width will not violate 600
        int cellHeight = Math.min(80, (int)(screenSize.getHeight()*0.8)/gameMap.getH());//to be sure that height will not violate 800
        cellSize = Math.min(cellWidth, cellHeight);
        int width = gameMap.getW() * cellSize;
        int height = gameMap.getH() * cellSize;
        this.setSize(new Dimension(width, height));
        this.setPreferredSize(new Dimension(width,height));
    }
    public static void main(String[] args) {

        Cell cells[][] = new Cell[10][10];
        for(int i = 0; i<10; i++)
            for(int j = 0; j<10; j++) {
                cells[i][j] = new Cell();
                int randomNum = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                if((randomNum%2)==0) {
                    int rand1 = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                    int rand2 = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                    int rand3 = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                    int rand4 = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                    int rand5 = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                    cells[i][j].setContent(new Fish(i, cells[i][j], rand1, rand2, rand3, rand4==0));
                    ((Fish)cells[i][j].getContent()).setSick(rand5 == 0);
                }
                cells[i][j].setRow(i);
                cells[i][j].setColumn(j);
            }
        cells[0][7].setTeleport(new Teleport(1, cells[0][7], cells[8][9]));
        cells[8][9].setTeleport(new Teleport(1, cells[8][9], cells[0][7]));
        cells[5][6].setNet(new Net(1, cells[5][6]));
        cells[7][6].setNet(new Net(1, cells[5][6]));
        Map map = new Map();
        map.setCells(cells);
        map.setW(cells.length);
        map.setH(cells[0].length);
        int grades[] = new int[2];
        grades[0] = 1;
        grades[1] = 10;
        map.setScore(grades);
        MapFrame mapFrame = new MapFrame(map);
        cells[5][5].setContent(new Food(1, cells[5][5]));
        mapFrame.setMap(map);
        for(int i = 0; i<10; i+=2)
            for(int j = 0; j<10; j+=3) {
                cells[i][j].setContent(new Trash(1, cells[i][j]));
                mapFrame.setMap(map);
            }
        for(int i = 0; i<600; i++){
            int rand5 = ThreadLocalRandom.current().nextInt(0, 10);
            int rand6 = ThreadLocalRandom.current().nextInt(0, 10);
            int rand7 = ThreadLocalRandom.current().nextInt(0, 10);
            int rand8 = ThreadLocalRandom.current().nextInt(0, 10);
            rand7 = (rand5+1)%10;
            rand8 = rand6;
            GameObject temp = cells[rand5][rand6].getContent();
            cells[rand5][rand6].setContent(cells[rand7][rand8].getContent());
            cells[rand7][rand8].setContent(temp);
            mapFrame.setMap(map);
        }
        mapFrame.gameOver();
    }
    public void increaseNeedle(){
        if(needle.get()+1<shots.size()){
            needle.incrementAndGet();
            repaint();
        }
        else {
            return;
        }
        if(needle.get() == shots.size()-1 && isEnded) {
            if(!saveTried)
                saveImages();
            timer1.stop();
        }
    }
    public void subtractNeedle(){
        if(needle.get()>1) {
            needle.getAndDecrement();
            repaint();
        }
    }
    public boolean isLive() {
        return isLive;
    }
    public void setLive(boolean live) {
        isLive = live;
    }

    public AtomicInteger getNeedle() {
        return needle;
    }

    public void setNeedle(AtomicInteger needle) {
        this.needle = needle;
    }
}
