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
    private Strategy strategy;

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
        strategy = new Strategy(imageUtilities);
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

        String chosenStrategy = determineStrategy(figureImageMap);
//        System.out.println(chosenStrategy);

        // Todo - change all to if statements only so skip is not done
        if (chosenStrategy.equals("row_equals") && strategy.applyRowEqualsStrategy(figureImageMap, solutionKeyList) != -1) {
            return strategy.applyRowEqualsStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("one_of_each") && strategy.applyOneOfEachStrategy(figureImageMap, solutionKeyList) != -1) { //Todo - check if returns -1 and skip if so
            System.out.println("picking one_of_each");
            return strategy.applyOneOfEachStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("one_cancels") && strategy.applyOneCancelsStrategy(figureImageMap, solutionKeyList) != -1) {
            System.out.println("picking one_cancels");
            return strategy.applyOneCancelsStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("cancel_out") && strategy.applyCancelOutStrategy(figureImageMap, solutionKeyList) != -1) {
            System.out.println("picking cancel_out");
            return strategy.applyCancelOutStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("common_perms") && strategy.applyCommonPermsStrategy(figureImageMap, solutionKeyList) != -1) {
            System.out.println("picking common_perms");
            return strategy.applyCommonPermsStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("productAB") && strategy.applyProductABStrategy(figureImageMap, solutionKeyList) != -1) {
            System.out.println("picking productAB");
            return strategy.applyProductABStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("productAC") && strategy.applyProductACStrategy(figureImageMap, solutionKeyList) != -1) {
            System.out.println("picking productAC");
            return strategy.applyProductACStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("diffAB") && strategy.applyDiffABStrategy(figureImageMap, solutionKeyList) != -1) {
            System.out.println("picking diffAB");
            return strategy.applyDiffABStrategy(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("shared") && strategy.applySharedStrategy(figureImageMap, solutionKeyList) != -1) {
            System.out.println("picking shared");
            return strategy.applySharedStrategy(figureImageMap, solutionKeyList);
        } else {
            System.out.println("picking one not seen");
            return strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        }

        // never reaches here currently
//        return -1;
    }

    public String determineStrategy(Map<String, BufferedImage> figureImageMap) {

        // get the individual images
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

//        System.out.println("FIGURE AB, BC =============");
        if (strategy.areEqual(figureA, figureB) && strategy.areEqual(figureB, figureC))
            if (strategy.areEqual(figureD, figureE) && strategy.areEqual(figureE, figureF))
                return "row_equals";
        else if ((strategy.areEqual(figureA, figureD) || strategy.areEqual(figureA, figureE) || strategy.areEqual(figureA, figureF))
                    && (strategy.areEqual(figureB, figureD) || strategy.areEqual(figureB, figureE) || strategy.areEqual(figureB, figureF))
                    && (strategy.areEqual(figureC, figureD) || strategy.areEqual(figureC, figureE) || strategy.areEqual(figureC, figureF)))
                return "one_of_each";
        else if (strategy.areEqual(rowAB, rowBC) && strategy.areEqual(rowDE, rowEF))
                return "one cancels";
        else if (strategy.areEqual(colADG, colBEH))
                return "cancel_out";
        else if (strategy.areEqual(AB, figureC) && strategy.areEqual(DE, figureF))
                return "productAB";
        else if (strategy.areEqual(AC, figureB) && strategy.areEqual(DF, figureE))
                return "productAC";
        else if (strategy.areEqual(difAB, figureC) && strategy.areEqual(difDE, figureF))
                return "diffAB";
        else if (strategy.isShared(figureImageMap))
                return "shared";
        else if (strategy.areEqual(ABC, DEF))
                return "common_perms";

        return "guess";
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
 * check current image methods vs imageChops methods
 */
