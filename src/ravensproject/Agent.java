package ravensproject;

// Uncomment these lines to access image processing.
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * 
 * You may also create and submit new files in addition to modifying this file.
 * 
 * Make sure your file retains methods with the signatures:
 * public Agent()
 * public char Solve(RavensProblem problem)
 * 
 * These methods will be necessary for the project's main method to run.
 * 
 */
public class Agent {

    private Generator generator;
    private Random random;
    private ImageUtilities imageUtilities;

    /**
     * The default constructor for your Agent. Make sure to execute any
     * processing necessary before your Agent starts solving problems here.
     * 
     * Do not add any variables to this signature; they will not be used by
     * main().
     * 
     */
    public Agent() {
        generator = new Generator();
        random = new Random();
        imageUtilities = new ImageUtilities();
    }
    /**
     * The primary method for solving incoming Raven's Progressive Matrices.
     * For each problem, your Agent's Solve() method will be called. At the
     * conclusion of Solve(), your Agent should return a String representing its
     * answer to the question: "1", "2", "3", "4", "5", or "6". These Strings
     * are also the Names of the individual RavensFigures, obtained through
     * RavensFigure.getName().
     * 
     * In addition to returning your answer at the end of the method, your Agent
     * may also call problem.checkAnswer(String givenAnswer). The parameter
     * passed to checkAnswer should be your Agent's current guess for the
     * problem; checkAnswer will return the correct answer to the problem. This
     * allows your Agent to check its answer. Note, however, that after your
     * agent has called checkAnswer, it will *not* be able to change its answer.
     * checkAnswer is used to allow your Agent to learn from its incorrect
     * answers; however, your Agent cannot change the answer to a question it
     * has already answered.
     * 
     * If your Agent calls checkAnswer during execution of Solve, the answer it
     * returns will be ignored; otherwise, the answer returned at the end of
     * Solve will be taken as your Agent's answer to this problem.
     * 
     * @param problem the RavensProblem your agent should solve
     * @return your Agent's answer to this problem
     */
    public int Solve(RavensProblem problem) {
        System.out.println("Solving " + problem.getName());

        // Retrieve figures from problem
        Map<String, RavensFigure> figureMap = problem.getFigures();

        // Get list of figure names for solutions
        List<String> solutionKeyList = createKeyList(figureMap, "[0-9]");

        Map<String, BufferedImage> figureImageMap = new HashMap<>();
        for (String figureKey : figureMap.keySet()) {
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(figureMap.get(figureKey).getVisual()));
            } catch (IOException e) {
                throw new RuntimeException("Could not open specified image.", e);
            }

            figureImageMap.put(figureKey, image);
        }

        String strategy = determineStrategy(figureImageMap);

        // Todo - change all to if statements only so skip is not done
        if (strategy.equals("row_equals")) {
            for (String solutionKey : solutionKeyList) {
                if (areEqual(figureImageMap.get("H"), figureImageMap.get(solutionKey))) {
                    return Integer.parseInt(solutionKey);
                }
            }
        } else if (strategy.equals("one_of_each") && applyOneOfEachStrategy(figureImageMap, solutionKeyList) != -1) { //Todo - check if returns -1 and skip if so
            return applyOneOfEachStrategy(figureImageMap, solutionKeyList);
        } else if (strategy.equals("one_cancels") && applyOneCancelsStrategy(figureImageMap, solutionKeyList) != -1) {
            return applyOneCancelsStrategy(figureImageMap, solutionKeyList);
        } else if (strategy.equals("cancel_out") && applyCancelOutStrategy(figureImageMap, solutionKeyList) != -1) {
            return applyCancelOutStrategy(figureImageMap, solutionKeyList);
        } else if (strategy.equals("common_perms") && applyCommonPermsStrategy(figureImageMap, solutionKeyList) != -1) {
            return applyCommonPermsStrategy(figureImageMap, solutionKeyList);
        } else if (strategy.equals("productAB") && applyProductABStrategy(figureImageMap, solutionKeyList) != -1) {
            return applyProductABStrategy(figureImageMap, solutionKeyList);
        } else if (strategy.equals("productAC") && applyProductACStrategy(figureImageMap, solutionKeyList) != -1) {
            return applyProductACStrategy(figureImageMap, solutionKeyList);
        } else if (strategy.equals("diffAB") && applyDiffABStrategy(figureImageMap, solutionKeyList) != -1) {
            return applyDiffABStrategy(figureImageMap, solutionKeyList);
        } else if (strategy.equals("shared") && applySharedStrategy(figureImageMap, solutionKeyList) != -1) {
            return applySharedStrategy(figureImageMap, solutionKeyList);
        } else {
            return pickTheOneNotSeen(figureImageMap, solutionKeyList);
        }

        // never reaches here currently
        return -1;
    }

    public boolean areEqual(BufferedImage image1, BufferedImage image2) {
        int width = Math.max(image1.getWidth(), image2.getWidth());
        int height = Math.max(image1.getHeight(), image2.getHeight());
        int diff = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int img1Col = image1.getRGB(x, y) != 0 ? 0 : 255;
                int img2Col = image1.getRGB(x, y) != 0 ? 0 : 255;
                diff += Math.abs(img1Col - img2Col);
            }
        }

        int components = width * height * 3;
        double dist = (diff / 255.0 * 100) / components;
        Map<String, Integer> image1Colors = imageUtilities.getColors(image1);
        Map<String, Integer> image2Colors = imageUtilities.getColors(image2);
        int[] image2Black = {10000, 0};

        // Todo - might be able to trim this down and only work with Map
        int[] image1Black = new int[2];
        int[] image1White = new int [2];
        if (image1Colors.keySet().size() > 1) {
            image1Black[0] = image1Colors.get("black");
            image1Black[1] = 0;
            image1White[0] = image1Colors.get("white");
            image1White[1] = 255;
        } else {
            if (image1Colors.keySet().contains("white")) {
                image1Black[0] = image1Colors.get("black");
                image1Black[1] = 0;
                image1White[0] = 0;
                image1White[1] = 255;
            } else {
                image1Black[0] = 0;
                image1Black[1] = 0;
                image1White[0] = image1Colors.get("white");
                image1White[1] = 255;
            }
        }

//        int[] image2Black = new int[2];
        int[] image2White = new int [2];
        if (image2Colors.keySet().size() > 1) {
            image2Black[0] = image2Colors.get("black");
            image2Black[1] = 0;
            image2White[0] = image2Colors.get("white");
            image2White[1] = 255;
        } else {
            if (image2Colors.keySet().contains("white")) {
                image2Black[0] = image2Colors.get("black");
                image2Black[1] = 0;
                image2White[0] = 0;
                image2White[1] = 255;
            } else {
                image2Black[0] = 0;
                image2Black[1] = 0;
                image2White[0] = image2Colors.get("white");
                image2White[1] = 255;
            }
        }

//        Map<String, Double> stats = new HashMap<>();
//        stats.put("dist", dist);
//        stats.put("blk", (double) Math.abs(image1Black[0] - image2Black[0]));

        return dist < 1.1 && Math.abs(image1Black[0] - image2Black[0]) < 105;
    }

    public boolean isShared(Map<String, BufferedImage> figureImageMap) {
        // Todo - might not need delta image [1] ever!
        BufferedImage sharedAB = imageUtilities.compareImages(figureImageMap.get("A"), figureImageMap.get("B")).get(0);
        BufferedImage sharedDE = imageUtilities.compareImages(figureImageMap.get("D"), figureImageMap.get("E")).get(0);

        return areEqual(sharedAB, figureImageMap.get("C")) && areEqual(sharedDE, figureImageMap.get("F"));
    }

    public String determineStrategy(Map<String, BufferedImage> figureImageMap) {

        //get the individual images
        BufferedImage figureA = figureImageMap.get("A");
        BufferedImage figureB = figureImageMap.get("B");
        BufferedImage figureC = figureImageMap.get("C");
        BufferedImage figureD = figureImageMap.get("D");
        BufferedImage figureE = figureImageMap.get("E");
        BufferedImage figureF = figureImageMap.get("F");
        BufferedImage figureG = figureImageMap.get("G");
        BufferedImage figureH = figureImageMap.get("H");

        // overlays
        BufferedImage rowAB = imageUtilities.add(figureA, figureB);
        BufferedImage rowBC = imageUtilities.add(figureB, figureC);
        BufferedImage rowDE = imageUtilities.add(figureD, figureE);
        BufferedImage rowEF = imageUtilities.add(figureE, figureF);

        BufferedImage colAD = imageUtilities.multiply(figureA, figureD);
        BufferedImage colADG = imageUtilities.multiply(colAD, figureD);

        BufferedImage colBE = imageUtilities.multiply(figureB, figureE);
        BufferedImage colBEH = imageUtilities.multiply(colBE, figureD);

        // common permutations
        BufferedImage AB = imageUtilities.multiply(figureA, figureB);
        BufferedImage AC = imageUtilities.multiply(figureA, figureC);
        BufferedImage ABC = imageUtilities.multiply(AB, figureC);
        BufferedImage DE = imageUtilities.multiply(figureD, figureE);
        BufferedImage DF = imageUtilities.multiply(figureD, figureF);
        BufferedImage DEF = imageUtilities.multiply(DE, figureF);

        // diffs
        BufferedImage difAB = imageUtilities.difference(AB, rowAB);
        BufferedImage difDE = imageUtilities.difference(DE, rowDE);

//        JFrame frame = new JFrame();
//        frame.getContentPane().setLayout(new FlowLayout());
//        frame.getContentPane().add(new JLabel(new ImageIcon(difAB)));
//        frame.pack();
//        frame.setVisible(true);
//
//        try {
//            Thread.sleep(2000);
//        } catch(InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

        if (areEqual(figureA, figureB) && areEqual(figureB, figureC))
            if (areEqual(figureD, figureE) && areEqual(figureE, figureF))
                return "row_equals";
        else if ((areEqual(figureA, figureD) || areEqual(figureA, figureE) || areEqual(figureA, figureF))
                    && (areEqual(figureB, figureD) || areEqual(figureB, figureE) || areEqual(figureB, figureF))
                    && (areEqual(figureC, figureD) || areEqual(figureC, figureE) || areEqual(figureC, figureF)))
                return "one_of_each";
        else if (areEqual(rowAB, rowBC) && areEqual(rowDE, rowEF))
                return "one cancels";
        else if (areEqual(colADG, colBEH))
                return "cancel_out";
        else if (areEqual(AB, figureC) && areEqual(DE, figureF))
                return "productAB";
        else if (areEqual(AC, figureB) && areEqual(DF, figureE))
                return "productAC";
        else if (areEqual(difAB, figureC) && areEqual(difDE, figureF))
                return "diffAB";
        else if (isShared(figureImageMap))
                return "shared";
        else if (areEqual(ABC, DEF))
                return "common_perms";

        return "guess";
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
        BufferedImage rowHF = imageUtilities.add(figMap.get("H"), figMap.get("F"));
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
        } else if (answers.size() == 1) {
            return answers.get(0);
        }

        return -1;
    }

    public int applySharedStrategy(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage sharedGE = imageUtilities.compareImages(figMap.get("G"), figMap.get("H")).get(0);
        for (String solutionKey : solKeyList)
            if (areEqual(sharedGE, figMap.get(solutionKey)))
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

        if (answers.size() == 1)
            return Integer.parseInt(answers.get(0));
        // Todo - create a do not guess variable that can be analyzed here to determine if guess is returned
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
            if (areEqual(candidate, DEF))
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

    /**
     * This method creates a key list to be used for iteration when building out Raven
     * Progressive Matrices. The regex determines whether it will be a solution key
     * list or a problem figure key list.
     *
     * @param figureMap
     * @param regex
     * @return The list of keys
     */
    public List<String> createKeyList(Map<String, RavensFigure> figureMap, String regex) {
        List<String> keyList= new ArrayList<>();
        for (String name : figureMap.keySet())
            if (name.matches(regex))
                keyList.add(name);
        Collections.sort(keyList);

        return keyList;
    }

}

/* Todo:
 * get rid of image compareImages delta buffered image
 * 
 */
