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
    private String problem;

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
        this.problem = problem.getName();

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
        System.out.println(chosenStrategy);

        int solution = -1;

        // Todo - change all to if statements only so skip is not done
        if (chosenStrategy.equals("row_equals")) {
            solution = strategy.applyRowEqualsStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("one_of_each")) { //Todo - check if returns -1 and skip if so
            solution = strategy.applyOneOfEachStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("one_cancels")) {
            solution = strategy.applyOneCancelsStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("cancel_out")) {
            solution = strategy.applyCancelOutStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("common_perms")) {
            solution = strategy.applyCommonPermsStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("productAB")) {
            solution = strategy.applyProductABStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("productAC")) {
            solution = strategy.applyProductACStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("diffAB")) {
            solution = strategy.applyDiffABStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else if (chosenStrategy.equals("shared")) {
            solution = strategy.applySharedStrategy(figureImageMap, solutionKeyList);
            if (solution == -1)
                solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        } else {
            solution = strategy.pickTheOneNotSeen(figureImageMap, solutionKeyList);
        }

        return solution;
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
        BufferedImage colADG = imageUtilities.multiply(colAD, figureG);

        BufferedImage colBE = imageUtilities.multiply(figureB, figureE);
        BufferedImage colBEH = imageUtilities.multiply(colBE, figureH);

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

//        if (problem.equals("Basic Problem D-09")) {
//            JFrame frame = new JFrame();
//            frame.getContentPane().setLayout(new FlowLayout());
//            frame.getContentPane().add(new JLabel(new ImageIcon(ABC)));
//            frame.getContentPane().add(new JLabel(new ImageIcon(DEF)));
//            frame.pack();
//            frame.setVisible(true);
//            try {
//                Thread.sleep(11000);
//            } catch(InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }


//        System.out.println("areEqual = "+(strategy.areEqual(rowAB, rowBC) && strategy.areEqual(rowDE, rowEF)));
        if (strategy.areEqual(figureA, figureB) && strategy.areEqual(figureB, figureC))
            if (strategy.areEqual(figureD, figureE) && strategy.areEqual(figureE, figureF))
                return "row_equals";
        if ((strategy.areEqual(figureA, figureD) || strategy.areEqual(figureA, figureE) || strategy.areEqual(figureA, figureF))
                    && (strategy.areEqual(figureB, figureD) || strategy.areEqual(figureB, figureE) || strategy.areEqual(figureB, figureF))
                    && (strategy.areEqual(figureC, figureD) || strategy.areEqual(figureC, figureE) || strategy.areEqual(figureC, figureF)))
                return "one_of_each";
        if (strategy.areEqual(rowAB, rowBC) && strategy.areEqual(rowDE, rowEF))
                return "one_cancels";
        if (strategy.areEqual(colADG, colBEH))
                return "cancel_out";
        if (strategy.areEqual(AB, figureC) && strategy.areEqual(DE, figureF))
                return "productAB";
        if (strategy.areEqual(AC, figureB) && strategy.areEqual(DF, figureE))
                return "productAC";
        if (strategy.areEqual(difAB, figureC) && strategy.areEqual(difDE, figureF))
                return "diffAB";
        if (strategy.isShared(figureImageMap))
                return "shared";
        if (strategy.areEqual(ABC, DEF))
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
