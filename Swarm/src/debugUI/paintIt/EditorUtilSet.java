package debugUI.paintIt;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by milad on 7/10/16.
 */
public class EditorUtilSet extends JPanel{

    public static void  addPanel(int y, int weightY, JPanel panel, Container container){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.7;
        gbc.weighty = weightY;
        //gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        //gbc.insets = new Insets(5,5,5,5);
        container.add(panel,gbc);
    }

    public static void addComponentY(int y,double weightY, JComponent component,Container container){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.weighty = weightY;
        gbc.gridwidth = 1;
        //gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        //gbc.insets = new Insets(10,100,20,100);
        container.add(component,gbc);
    }

    public static void  addComponentY(int y,double weightY, JComponent component,Container container, int ipady){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.weighty = weightY;
        gbc.gridwidth = 1;
        gbc.ipady = ipady;
        //gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        //gbc.insets = new Insets(,100,20,100);
        container.add(component,gbc);
    }

    public static void  addComponentY(int y,double weightY, JComponent component,Container container, int ipady, int a){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.weighty = weightY;
        gbc.gridwidth = 1;
        gbc.ipady = ipady;
        //gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,10,0,10);
        container.add(component,gbc);
    }

    public static void addComponentX(int x, double weightX, Component component, Container container){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = 0;
        gbc.weightx = weightX;
        gbc.weighty = 1;
        //gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        //gbc.insets = new Insets(5,5,5,5);
        container.add(component,gbc);
    }

    public static void addComponentXY(int x,int y, double weightX,double weightY, Component component, Container container){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightX;
        gbc.weighty = weightY;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        container.add(component,gbc);
    }

    public static void addComponentX(int x, double weightX, Component component, Container container,int ipadx){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = 0;
        gbc.weightx = weightX;
        gbc.weighty = 1;
        gbc.ipadx = ipadx;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        container.add(component,gbc);
    }

    public static void elan(final JTextField elanie, final String text){
        new Thread(){
            @Override
            public void run() {
                super.run();
                String text1 = elanie.getText();
                elanie.setText(text);
                try {
                    sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                elanie.setText(text1);
            }
        }.start();
    }


    public static Point getTransformed(Point point, AffineTransform transform){
        double[] theMatrix = new double[6];
        transform.getMatrix(theMatrix);
        //System.out.println("the input point is" + point.getX() +","+ point.getY());
        //System.out.println(theMatrix[0] +"," +theMatrix[1] +","+ theMatrix[2]+"," +theMatrix[3] +","+ theMatrix[4]+","+theMatrix[5]);
        int newX = (int)(point.getX()*theMatrix[0] + point.getY()*theMatrix[2] + theMatrix[4]);
        int newY = (int)(point.getY()*theMatrix[3] + point.getX()*theMatrix[1] + theMatrix[5]);
        return new Point(newX, newY);
    }
}
