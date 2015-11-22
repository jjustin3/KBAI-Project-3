package ravensproject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by justinjackson on 11/22/15.
 */
public class Strategy {

    private ImageUtilities imageUtilities;
    private Random random;

    public Strategy(ImageUtilities imageUtilities) {
        this.imageUtilities = imageUtilities;
        random = new Random();
    }

    public boolean areEqual(BufferedImage image1, BufferedImage image2) {
        int width = Math.max(image1.getWidth(), image2.getWidth());
        int height = Math.max(image1.getHeight(), image2.getHeight());
        int diff = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                diff += image1.getRGB(x, y) != image2.getRGB(x, y) ? 255 : 0;
            }
        }

        int components = width * height * 3;
        double dist = (diff / 255.0 * 100) / components;

        Map<String, Integer> image1Colors = imageUtilities.getColors(image1);
        Map<String, Integer> image2Colors = imageUtilities.getColors(image2);

        // Todo - might be able to trim this down and only work with Map
        int[] image1Black = new int[2];
        if (image1Colors.keySet().size() > 1) {
            image1Black[0] = image1Colors.get("black");
            image1Black[1] = 0;
        } else {
            if (image1Colors.keySet().contains("white")) {
                image1Black[0] = image1Colors.get("black");
                image1Black[1] = 0;
            } else {
                image1Black[0] = 0;
                image1Black[1] = 0;
            }
        }

        int[] image2Black = {10000, 0};
        if (image2Colors.keySet().size() > 1) {
            image2Black[0] = image2Colors.get("black");
            image2Black[1] = 0;
        } else {
            if (image2Colors.keySet().contains("white")) {
                image2Black[0] = image2Colors.get("black");
                image2Black[1] = 0;
            } else {
                image2Black[0] = 0;
                image2Black[1] = 0;
            }
        }

//        System.out.println("dist: "+dist+"     blk: "+Math.abs(image1Black[0] - image2Black[0]));

        return dist < 1.3 && Math.abs(image1Black[0] - image2Black[0]) < 205; // < 1.1 and < 105 // < 1.3 and < 205
    }

    public boolean isShared(Map<String, BufferedImage> figureImageMap) {
        // Todo - might not need delta image [1] ever!
        BufferedImage sharedAB = imageUtilities.compareImages(figureImageMap.get("A"), figureImageMap.get("B")).get(0);
        BufferedImage sharedDE = imageUtilities.compareImages(figureImageMap.get("D"), figureImageMap.get("E")).get(0);

        return areEqual(sharedAB, figureImageMap.get("C")) && areEqual(sharedDE, figureImageMap.get("F"));
    }

    public int applyRowEqualsStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        for (String solutionKey : solKeyList)
            if (areEqual(figMap.get("H"), figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);
        return -1;
    }

    public int applyOneOfEachStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        String missingFigure;
        if (areEqual(figMap.get("A"), figMap.get("G")) || areEqual(figMap.get("A"), figMap.get("H"))) {
            if (areEqual(figMap.get("B"), figMap.get("G")) || areEqual(figMap.get("B"), figMap.get("H"))) {
                if (areEqual(figMap.get("C"), figMap.get("G")) || areEqual(figMap.get("C"), figMap.get("H"))) {
                    return -1;
                } else {
                    missingFigure = "C";
                }
            } else {
                missingFigure = "B";
            }
        } else {
            missingFigure = "A";
        }

        for (String solutionKey : solKeyList)
            if (areEqual(figMap.get(missingFigure), figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);
        return -1;
    }

    public int applyOneCancelsStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage rowCF = imageUtilities.add(figMap.get("C"), figMap.get("F"));
        BufferedImage rowGH = imageUtilities.add(figMap.get("G"), figMap.get("H"));
        List<Integer> answers = new ArrayList<>();

        for (String solutionKey : solKeyList) {
            BufferedImage candidate1 = imageUtilities.add(rowCF, figMap.get(solutionKey));
            BufferedImage candidate2 = imageUtilities.add(rowGH, figMap.get(solutionKey));

            if (areEqual(rowCF, candidate1) && areEqual(rowGH, candidate2))
                answers.add(Integer.parseInt(solutionKey));
        }

        if (answers.size() != 1) {
            if (isShared(figMap))
                return applySharedStrategy(figMap, solKeyList);
            else
                return pickTheOneNotSeen(figMap, solKeyList);
        } else {
            return answers.get(0);
        }

//        return -1;
    }

    public int applySharedStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage sharedGH = imageUtilities.compareImages(figMap.get("G"), figMap.get("H")).get(0);

        for (String solutionKey : solKeyList)
            if (areEqual(sharedGH, figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);
        return -1;
    }

    public int pickTheOneNotSeen(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        List<String> figures = new ArrayList<>();
        List<String> answers = new ArrayList<>(solKeyList);
        for (String figureKey : figMap.keySet())
            if (!solKeyList.contains(figureKey))
                figures.add(figureKey);

        for (String figure : figures)
            for (String solution : solKeyList)
                if (areEqual(figMap.get(figure), figMap.get(solution)))
                    if (answers.contains(solution))
                        answers.remove(solution);

//        System.out.println(answers);

        if (answers.size() == 1)
            return Integer.parseInt(answers.get(0));
        else if (answers.size() > 1 && answers.size() < 3) // toggle after penalty determined
            return Integer.parseInt(answers.get(random.nextInt(answers.size())));
        return -1;
    }

    public int applyCancelOutStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage figureA = figMap.get("A");
        BufferedImage figureC = figMap.get("C");
        BufferedImage figureD = figMap.get("D");
        BufferedImage figureF = figMap.get("F");
        BufferedImage figureG = figMap.get("G");

        BufferedImage colAD = imageUtilities.multiply(figureA, figureD);
        BufferedImage colADG = imageUtilities.multiply(colAD, figureG);
        BufferedImage colCF = imageUtilities.multiply(figureC, figureF);

        for (String solutionKey : solKeyList) {
            BufferedImage candidate = imageUtilities.multiply(colCF, figMap.get(solutionKey));
            if (areEqual(candidate, colADG))
                return Integer.parseInt(solutionKey);
        }

        return -1;
    }

    public int applyCommonPermsStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage DE = imageUtilities.multiply(figMap.get("D"), figMap.get("E"));
        BufferedImage GH = imageUtilities.multiply(figMap.get("G"), figMap.get("H"));
        BufferedImage DEF = imageUtilities.multiply(DE, figMap.get("F"));

        for (String solutionKey : solKeyList) {
            BufferedImage candidate = imageUtilities.multiply(GH, figMap.get(solutionKey));
            if (areEqual(DEF, candidate))
                return Integer.parseInt(solutionKey);
        }

        return pickTheOneNotSeen(figMap, solKeyList);
    }

    public int applyProductABStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage GH = imageUtilities.multiply(figMap.get("G"), figMap.get("H"));

        for (String solutionKey : solKeyList)
            if (areEqual(GH, figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);

        return pickTheOneNotSeen(figMap, solKeyList);
    }

    public int applyProductACStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        for (String solutionKey : solKeyList) {
            BufferedImage candidate = imageUtilities.multiply(figMap.get("G"), figMap.get(solutionKey));
            if (areEqual(candidate, figMap.get("H")))
                return Integer.parseInt(solutionKey);
        }

        return pickTheOneNotSeen(figMap, solKeyList);
    }

    public int applyDiffABStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage difGH = imageUtilities.difference(figMap.get("G"), figMap.get("H"));

        for (String solutionKey : solKeyList)
            if (areEqual(difGH, figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);

        return pickTheOneNotSeen(figMap, solKeyList);
    }

}
