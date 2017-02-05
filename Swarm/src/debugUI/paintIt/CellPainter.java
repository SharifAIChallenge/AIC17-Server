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

    static void paint(Cell cell, int cellSize, Graphics2D g2d) {
        ArrayList<ImageToDraw> drawList = getImage(cell, cellSize);
        for (ImageToDraw image : drawList) {
            g2d.transform(image.getSt());
            g2d.drawImage(image.getImage(), 0, 0, cellSize, cellSize, null);
            g2d.transform(image.getEn());
        }

        //---drawing the color linking the input and output teleport to each other
        /*Color color  = new Color(100, 255, 100);
        if(cell.isTeleportIn() || cell.isTeleportOut()) {//TODO:uncomment this and translate this
            int R = (hash(cell.getColumn() + cell.getTeleported().getRow())) % 256;
            int G = (hash(cell.getRow() + cell.getTeleported().getRow())) % 256;
            int B = (hash(R + G)) % 256;
            color = new Color(R, G, B);
        }
        Color temp_color = g2d.getColor();//store the color before
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawOval(cellSize/3, cellSize/3, cellSize/6, cellSize/6);
        g2d.setColor(temp_color);//restore the color before to the g2d*/
        //---circles for the input output teleport ended
    }
    private static ArrayList<ImageToDraw> getImage(Cell cell, int cellSize){
        ArrayList<ImageToDraw> ret = new ArrayList<>();
        //---load the images if they are not loaded
        if(floor == null) {
            floor = ImageDataBase.getImageScaled("floor.png", cellSize, cellSize);
            slipper = ImageDataBase.getImageScaled("slipper.png", cellSize, cellSize);
            teleport_in = ImageDataBase.getImageScaled("teleport_in.png", cellSize, cellSize);
            for(int i = 0; i<4; i++)//we assume we have 4 trash images
                trashImages.add(ImageDataBase.getImageScaled("trash"+Integer.toString(i)+".png", cellSize, cellSize));
            for(int i = 0; i<4; i++)//we assume we have 4 food images
                foodImages.add(ImageDataBase.getImageScaled("food"+Integer.toString(i)+".png", cellSize, cellSize));
            for(int i = 0; i<16; i++)//we assume we have 4 food images
                fishImages.add(ImageDataBase.getImageScaled(Integer.toString(i)+".png", cellSize, cellSize));
        }
        //--needed images are loaded now
        ret.add(new ImageToDraw(floor));//we always have floor
        if(cell.getNet()!= null)
            ret.add(new ImageToDraw(slipper));
        //TODO:uncomment this and translate this
        if(cell.getTeleport()!=null) {
            ret.add(new ImageToDraw(teleport_in));
        }



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
            ans += (num%10)*power;
            num = num/10;
            power =power * 2;
        }
        return ans;
    }
}
