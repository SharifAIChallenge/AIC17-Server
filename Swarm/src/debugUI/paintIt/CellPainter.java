package debugUI.paintIt;

import Swarm.map.Cell;
import Swarm.objects.Fish;
import Swarm.objects.Food;
import Swarm.objects.GameObject;
import Swarm.objects.Trash;
import debugUI.control.ImageDataBase;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
/*
written by miladink
 */
class CellPainter {//this will paint the cell  with top left at (0,0)

    static private ArrayList<Image> fishImages = new ArrayList<>();
    static private Image floor;
    static private Image slipper;
    static private Image teleport_in;
    static private ArrayList<Image> trashImages = new ArrayList<>();
    static private ArrayList<Image> foodImages = new ArrayList<>();

    static void paint(Cell cell, int cellSize, Graphics2D g2d, int theme) {
        ArrayList<ImageToDraw> drawList = getImage(cell, cellSize, theme);
        for (ImageToDraw image : drawList) {
            g2d.transform(image.getSt());
            g2d.drawImage(image.getImage(), 0, 0, cellSize, cellSize, null);
            g2d.transform(image.getEn());
        }

        //---drawing the color linking the input and output teleport to each other
        Color color  = new Color(100, 255, 100);
        if(cell.getTeleport()!=null) {
            int R = (hash(cell.getRow() + cell.getTeleport().getPair().getRow())) % 256;
            int G = (hash(cell.getColumn() + cell.getTeleport().getPair().getColumn())) % 256;
            int B = (hash(R + G)) % 256;
            System.out.println(B);
            color = new Color(R, G, B);
            Color temp_color = g2d.getColor();//store the color before
            g2d.setColor(color);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fillOval(0, 0, (int)(cellSize*0.3), (int)(cellSize*0.3));
            g2d.setColor(temp_color);//restore the color before to the g2d
            //---circles for the input output teleport ended
        }

        //writing the power of fish on it
        Font font = new Font("Consolas", Font.BOLD, 20);
        g2d.setFont(font);
        g2d.setColor(Color.MAGENTA);
        if(cell.getContent() instanceof Fish) {

            String str = Integer.toString(((Fish) cell.getContent()).getPower());
            g2d.drawString(str, (cellSize - g2d.getFontMetrics().stringWidth(str)) / 2, (cellSize - g2d.getFontMetrics().getHeight()));
            g2d.setColor(Color.YELLOW);
        }


    }

    private static ArrayList<ImageToDraw> getImage(Cell cell, int cellSize, int theme){
        ArrayList<ImageToDraw> ret = new ArrayList<>();
        //---load the images if they are not loaded
        if(floor == null) {
            String theme_str = Integer.toString(theme)+"/";
            floor = ImageDataBase.getImageScaled(theme_str+"floor.png", cellSize, cellSize);
            slipper = ImageDataBase.getImageScaled(theme_str+"slipper.png", 3*cellSize, 3*cellSize);
            teleport_in = ImageDataBase.getImageScaled(theme_str+"teleport_in.png", cellSize, cellSize);
            for(int i = 0; i<4; i++)//we assume we have 4 trash images
                trashImages.add(ImageDataBase.getImageScaled(theme_str+"trash"+Integer.toString(i)+".png", cellSize, cellSize));
            for(int i = 0; i<4; i++)//we assume we have 4 food images
                foodImages.add(ImageDataBase.getImageScaled(theme_str+"food"+Integer.toString(i)+".png", cellSize, cellSize));
            for(int i = 0; i<16; i++)//we assume we have 4 food images
                fishImages.add(ImageDataBase.getImageScaled(theme_str+Integer.toString(i)+".png", cellSize, cellSize));
        }
        //--needed images are loaded now
        ret.add(new ImageToDraw(floor));//we always have floor




        GameObject content = cell.getContent();
        //---adding the fish image
        Fish fish = null;
        if(content instanceof Fish)
            fish = (Fish)content;
        if(fish != null) {
            AffineTransform st = new AffineTransform();
            AffineTransform en = new AffineTransform();
            switch (fish.getDirection()) {
                case 1:
                    st.rotate(Math.PI / 2, cellSize/2, cellSize/2);
                    en.rotate(-1.0 * Math.PI / 2, cellSize/2, cellSize/2);
                    break;
                case 2:
                    st.rotate(Math.PI, cellSize/2, cellSize/2);
                    en.rotate(-1.0*Math.PI, cellSize/2, cellSize/2);
                    break;
                case 3:
                    st.rotate(3*Math.PI/2, cellSize/2, cellSize/2);
                    en.rotate(-3.0*Math.PI/2, cellSize/2, cellSize/2);
                    break;
            }
            ret.add(new ImageToDraw(getFishImage(fish, cellSize), st, en));
        }

        //if(cell.getNet()!= null)//TODO:maybe you should return this
        //    ret.add(new ImageToDraw(slipper));
        if(cell.getTeleport()!=null) {
            ret.add(new ImageToDraw(teleport_in));
        }
        //---adding the trash image
        Trash trash = null;
        if(content instanceof  Trash)
            trash = (Trash)content;
        if(trash!= null){
            int trash_i = hash(cell.getColumn() + cellSize + cell.getRow())%4;
            ret.add(new ImageToDraw(trashImages.get(trash_i)));
        }
        //---trash image is added now if the cell has trash
        //---add the food image
        Food food = null;
        if(content instanceof Food)
            food = (Food)content;
        if(food!= null){
            int food_i = hash(cell.getColumn() + cellSize + cell.getRow())%4;
            ret.add(new ImageToDraw(foodImages.get(food_i)));
        }
        //---food image is added if the cell has food in it
        return ret;
    }
    private static Image getFishImage(Fish fish, int cellSize){
        int fish_number = getFishNumber(fish);
        if(fishImages.get(fish_number) == null){
            fishImages.set(fish_number, ImageDataBase.getImageScaled(Integer.toString(fish_number)+".png", cellSize/2, cellSize/2));
        }
        return fishImages.get(fish_number);
    }
    private static int getFishNumber(Fish fish){
        int number = 0;
        number+= fish.getTeamNumber();
        if(fish.isQueen()){
            number+=2;
        }
        if(fish.isSick()) {
            number += 8;
        }
        if(fish.getColorNumber()==0){
            number+= 4;
        }
        return number;
    }
    private static int hash(int num){
        int power = 1;
        int ans = 0;
        while(num > 0){
            ans += (num%2)*power;
            num = num/2;
            power =power * 397;
        }
        return Math.abs(ans);


    }
    static void drawNet(Cell cell, int cellSize, Graphics2D g2d, int theme){
        //draw the net
        if(cell.getNet()!= null) {
            g2d.setColor(new Color(0, 0, 0, 135));
            g2d.translate(-cellSize, -cellSize);
            Composite composite = g2d.getComposite();
            int rule = AlphaComposite.SRC_OVER;
            Composite comp = AlphaComposite.getInstance(rule , 0.8f );
            g2d.setComposite(comp);
            g2d.drawImage(slipper, 0, 0, null);
            g2d.setComposite(composite);
            g2d.translate(cellSize, cellSize);
        }
    }

    public static void changeTheme(){
        floor = null;
    }
}
