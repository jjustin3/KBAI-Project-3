package ravensproject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.List;

/**
 * Created by justinjackson on 11/18/15.
 */
public class ImageUtilities {

    public BufferedImage invertImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = newImage.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (image.getRGB(x, y) == -16777216)
                    newImage.setRGB(x, y, 0);

        return newImage;
    }

    public Map<String, Integer> getColors(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Map<String, Integer> colorsMap = new HashMap<>();
        colorsMap.put("black", 0);
        colorsMap.put("white", 0);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (image.getRGB(x, y) == -16777216) {
                    int black = colorsMap.get("black") + 1;
                    colorsMap.put("black", black);
                } else if (image.getRGB(x, y) == 0) {
                    int white = colorsMap.get("white") + 1;
                    colorsMap.put("white", white);
                }
            }
        }

        return colorsMap;
    }



    // Operator methods
    //what they have in common!
    public BufferedImage add(BufferedImage image1, BufferedImage image2) {
        int width = Math.max(image1.getWidth(), image2.getWidth());
        int height = Math.max(image1.getHeight(), image2.getHeight());
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = newImage.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (image1.getRGB(x, y) == image2.getRGB(x, y) && image1.getRGB(x, y) == -16777216) // if image1 and image 2 are black
                    newImage.setRGB(x, y, -16777216);

        return newImage;
    }

    //combine images!
    public BufferedImage multiply(BufferedImage image1, BufferedImage image2) {
        int width = Math.max(image1.getWidth(), image2.getWidth());
        int height = Math.max(image1.getHeight(), image2.getHeight());
        BufferedImage newImage = new BufferedImage(width, height, image1.getType());

        Graphics graphics = newImage.getGraphics();
        graphics.drawImage(image1, 0, 0, null);
        graphics.dispose();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (image1.getRGB(x, y) == -16777216 || image2.getRGB(x, y) == -16777216)
                    newImage.setRGB(x, y, -16777216);

        return newImage;
    }

    //take multiplied and subtract add (what they don't have in common)!
    public BufferedImage difference(BufferedImage image1, BufferedImage image2) {
        int width = Math.max(image1.getWidth(), image2.getWidth());
        int height = Math.max(image1.getHeight(), image2.getHeight());
        BufferedImage newImage = new BufferedImage(width, height, image1.getType());

        Graphics graphics = newImage.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.drawImage(image1, 0, 0, null);
        graphics.dispose();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (image1.getRGB(x, y) == image2.getRGB(x, y))
                    newImage.setRGB(x, y, 0);

        return newImage;
    }


}
