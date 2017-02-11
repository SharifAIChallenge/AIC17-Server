package debugUI.paintIt;
import Swarm.map.Cell;
import Swarm.models.Map;
import Swarm.objects.*;
import debugUI.DeepCopyMaker;
import network.Json;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.hash;

/*
written by miladink
 */
public class MapEditorPanel extends JPanel{

    MapEditorFrame frame = null;
    private Map gameMap;
    private int cellSize;
    private int theme = 0;
    private int themeNumbers = 4;
    private boolean teleport_Phase2 = false;
    private boolean sick = false;
    private boolean queen = false;
    private int team = 0;
    private int color = 0;
    private  int direction = 0;
    private int counter = 0;
    private GameObject gameObject;
    private Cell lastCell;


    MapEditorPanel(MapEditorFrame frame, Map gameMap){
        this.frame = frame;
        //---enter the map and the first shot will be taken
        this.setMap(gameMap);
        lastCell = gameMap.getCells()[0][0];
        setCellSize();
        this.setFocusable(true);
        this.requestFocus();
        MapEditorPanel thisMap = this;
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                //TODO:
                if(e.getKeyCode() == 39){
                    direction+=3;
                    direction%=4;
                }
                else if(e.getKeyCode() == 37){
                    direction+=1;
                    direction%=4;
                }else if(e.getKeyChar() == 's'){
                    sick = !sick;
                }else if(e.getKeyChar() == 'q'){
                    queen = !queen;
                }else if(e.getKeyChar() == 't'){
                    team = 1 - team;
                }else if(e.getKeyChar() == 'c'){
                    color = 1 - color;
                }
                //it is they are!
                if(lastCell.getContent() instanceof Fish){
                    Fish fish = new Fish(counter++,lastCell, team, direction, color, queen);
                    fish.setSick(sick);
                    lastCell.setContent(fish);
                }
                repaint();
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
                int y = e.getX();//it is true but look weird I know
                int x = e.getY();//it is true but look weird I know
                x = x/cellSize;
                y = y/cellSize;
                Cell cell = gameMap.getCells()[x][y];
                String selected = frame.getSelected();
                if(!selected.equals("teleport")) {
                    if(teleport_Phase2) {
                        teleport_Phase2 = false;
                        lastCell.setTeleport(null);
                    }
                }
                if(teleport_Phase2){
                    if(cell.getTeleport() == null) {
                        lastCell.getTeleport().setPair(cell);
                        cell.setTeleport(new Teleport(counter++, cell, lastCell));
                        teleport_Phase2 = false;
                    }
                }else
                {

                    if(selected.equals("roach")){
                        Fish fish = new Fish(counter++,lastCell, team, direction, color, queen);
                        fish.setSick(sick);
                        cell.setContent(fish);

                    }
                    else if(selected.equals("trash")){
                        cell.setContent(new Trash(counter++, cell));
                    }
                    else if(selected.equals("food")){
                        cell.setContent(new Food(counter++, cell));
                    }
                    else if(selected.equals("teleport")){
                        if(cell.getTeleport() == null) {
                            cell.setTeleport(new Teleport(counter++, cell, cell));
                            teleport_Phase2 = true;
                        }
                    }else if(selected.equals("slipper")){
                        cell.setNet(new Net(counter++, cell));
                    }else if(selected.equals("eraser")){
                        cell.setContent(null);
                        if(cell.getTeleport()!=null) {
                            Cell cell1 = cell.getTeleport().getPair();
                            cell1.setTeleport(null);
                            cell.setTeleport(null);
                        }
                        cell.setNet(null);
                    }
                }
                //TODO:calculate in which cell this click is happened
                //TODO:put the content in the cell if there is a content
                //TODO:if it is erase, deleteIt
                lastCell = cell;
                thisMap.repaint();
            }
        });
    }
    @Override
    protected synchronized void paintComponent(Graphics g){
        super.paintComponent(g);
        //---draw the image which sign is on, on the panel
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(draw(gameMap), null, null);
        //---the resulted image is now drawn on the panel
    }
    public void setMap(Map gameMap){
        this.gameMap = gameMap;

    }
    private BufferedImage draw(Map gameMap){
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
        ArrayList<Cell> cells_net = new ArrayList<>();
        for(int i = 0; i<cells.length;i++) {
            for (int j = 0; j < cells[0].length; j++) {
                gTemp2d.translate(j * cellSize, i * cellSize);
                CellPainter.paint(cells[i][j], cellSize, gTemp2d, theme);
                gTemp2d.translate(-j * cellSize, -i * cellSize);
                if(cells[i][j].getNet()!=null)
                    cells_net.add(cells[i][j]);
            }
        }

        for(Cell cell_temp: cells_net) {
            gTemp2d.translate(cell_temp.getColumn() * cellSize, cell_temp.getRow() * cellSize);
           // CellPainter.drawNet(cells[cell_temp.getRow()][cell_temp.getColumn()], cellSize, gTemp2d, theme);
            CellPainter.drawNet(hash(cell_temp.getColumn()+cell_temp.getRow())%4, cellSize,gameMap.getW(),gameMap.getH(), gTemp2d, theme);
            gTemp2d.translate(-cell_temp.getColumn() * cellSize, -cell_temp.getRow() * cellSize);
        }
        //---each cell is drawn
        return shot;
    }

    void saveMap(){
        FileDialog fileDialog = new FileDialog((Frame) null, "Save Recorded Images", FileDialog.SAVE);
        fileDialog.setFilenameFilter((dir, name) -> name.matches(".*\\.zip"));
        fileDialog.setMultipleMode(false);
        fileDialog.setFile("1.map");
        fileDialog.setVisible(true);
        File[] files = fileDialog.getFiles();
        if (files.length != 1)
            return;
        File f = files[0];
        MapJsonExtra jsonExtra = new MapJsonExtra(gameMap);
        String json = Json.GSON.toJson(jsonExtra);
        try {
            PrintWriter printWriter = new PrintWriter(f);
            printWriter.print(json);
            printWriter.close();
        } catch (FileNotFoundException e) {
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

    public Map getGameMap() {
        return gameMap;
    }

    public void setGameMap(Map gameMap) {
        this.gameMap = gameMap;
    }

    public void changeTheme(){
        theme+=1;
        theme = theme%themeNumbers;
        repaint();
    }

    public Color getThemeBackGround(){
        Color colors[] = new Color[Math.max(themeNumbers,5)];
        colors[0] = Color.decode("#606c68");
        colors[1] = Color.decode("#606c68");
        colors[2] = Color.decode("#e9cef3");
        colors[3] = Color.decode("#e9cef3");
        colors[4] = Color.decode("#757575");
        if(colors.length>theme)
            return colors[theme];
        else
            return Color.BLACK;
    }

    public GameObject makeGameObject(){
        //TODO:
        String selected = frame.getSelected();
        if(selected.equals("roach")){
           return new Fish(counter++,gameMap.getCells()[0][0], team, direction, color, queen);
        }
        else if(selected.equals("trash")){
            return new Trash(counter++, gameMap.getCells()[0][0]);
        }
        else if(selected.equals("food")){
            return new Food(counter++, gameMap.getCells()[0][0]);
        }
        else if(selected.equals("teleport")){
            return new Teleport(counter++, gameMap.getCells()[0][0], lastCell);
        }
        return null;
    }

    private void addToCell(GameObject gameObject, int i, int j){
        Cell cell = gameMap.getCells()[i][j];
        //if(gameObject instanceof Net)
    }
    public static void main(String[] args) {
        MapEditorFrame frame = new MapEditorFrame();
    }

}
