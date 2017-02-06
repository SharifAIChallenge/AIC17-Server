package debugUI.images.paintIt;

import java.awt.*;
import java.awt.geom.AffineTransform;

/*
 * written by miladink
 */
 class ImageToDraw {
    private Image image;
    private AffineTransform st;
    private AffineTransform en;

    ImageToDraw(Image image) {
        this(image, new AffineTransform(), new AffineTransform());
    }
    ImageToDraw(Image image, AffineTransform st, AffineTransform en) {
        this.image = image;
        this.st = st;
        this.en = en;
    }

    Image getImage() {
        return image;
    }

    AffineTransform getSt() {
        return st;
    }

    AffineTransform getEn() {
        return en;
    }
}
