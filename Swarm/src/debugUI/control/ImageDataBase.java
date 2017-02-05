package debugUI.control;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by milad on 7/6/16.
 */
public class ImageDataBase {
    HashMap<String,Image> images = new HashMap<String, Image>();
    private static ImageDataBase imageDataBase = new ImageDataBase();
    private ImageDataBase(){}
    public static Image getImage(String imageFilePath){
        Image image = imageDataBase.images.get(imageFilePath);
        if(image==null)
            return addImage(imageFilePath);
        else
            return  image;
    }
    public static Image  getImageScaled(String imageFilePath,int width,int height){
            Image image = imageDataBase.images.get(imageFilePath);
            if(image==null)
                return addImageScaled(imageFilePath,width,height);
            else {
                if(image.getWidth(null)!=width || image.getHeight(null)!=height) {
                    image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    imageDataBase.images.put(imageFilePath,image);
                }
                return image;
            }

    }
    public static Image addImage(String imageFilePath) {
        imageFilePath = alterAddress(imageFilePath);
        Image image = null;
        try {
            image = ImageIO.read(new File(imageFilePath));
            imageDataBase.images.put(imageFilePath,image);
        } catch (IOException ie) {
            System.err.println(imageFilePath);
            ie.printStackTrace();
        }
        return image;
    }
    private static String alterAddress(String imageFilePath){
        String result = "Swarm/src/debugUI/images/" + imageFilePath;
        return result;
    }
    public static Image addImageScaled(String imageFilePath, int width, int height){
        Image image = addImage(imageFilePath);
        if(width>0 && height>0) {
            image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageDataBase.images.put(imageFilePath,image);
        }
        return image;
    }//will add the image and scale it smooth
    public static  void savePicture(BufferedImage image, String address) {
        File outputfile = new File(address);
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ImageDataBase.getImageScaled("floor.png", 10,10);
    }
}

