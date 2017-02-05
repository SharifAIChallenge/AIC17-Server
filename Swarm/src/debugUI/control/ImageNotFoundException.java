package debugUI.control;

import java.io.IOException;

/**
 * Created by milad on 7/6/16.
 */
public class ImageNotFoundException extends IOException {
    private String ImageName;

    public ImageNotFoundException(String ImageName) {
        this.ImageName = ImageName;
    }

    public String getImageName() {
        return ImageName;
    }

}
