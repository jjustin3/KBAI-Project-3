package ravensproject;

// Uncomment these lines to access image processing.
import java.io.File;
import javax.imageio.ImageIO;

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

        List<String> strategies = determineStrategy(figureImageMap);

        int solution = -1;
        if (strategies.contains("row_equals"))
            solution = strategy.rowEquals(figureImageMap, solutionKeyList);
        if (strategies.contains("one_of_each") && solution == -1)
            solution = strategy.oneOfEach(figureImageMap, solutionKeyList);
        if (strategies.contains("one_cancels") && solution == -1)
            solution = strategy.oneCancels(figureImageMap, solutionKeyList);
        if (strategies.contains("cancel_out") && solution == -1)
            solution = strategy.cancelOut(figureImageMap, solutionKeyList);
        if (strategies.contains("common_perms") && solution == -1)
            solution = strategy.commonPermutations(figureImageMap, solutionKeyList);
        if (strategies.contains("productAB") && solution == -1)
            solution = strategy.productAB(figureImageMap, solutionKeyList);
        if (strategies.contains("productAC") && solution == -1)
            solution = strategy.productAC(figureImageMap, solutionKeyList);
        if (strategies.contains("diffAB") && solution == -1)
            solution = strategy.differenceAB(figureImageMap, solutionKeyList);
        if (strategies.contains("shared") && solution == -1)
            solution = strategy.shared(figureImageMap, solutionKeyList);
        if (solution == -1 )
            solution = strategy.chooseLeastLikely(figureImageMap, solutionKeyList);

        return solution;
    }

    /**
     * This method determines strategies based off of the order of importance of those strategies.
     *
     * @param figureImageMap
     * @return The chosen strategies.
     */
    public List<String> determineStrategy(Map<String, BufferedImage> figureImageMap) {

        // get the individual images
        BufferedImage figureA = figureImageMap.get("A");
        BufferedImage figureB = figureImageMap.get("B");
        BufferedImage figureC = figureImageMap.get("C");
        BufferedImage figureD = figureImageMap.get("D");
        BufferedImage figureE = figureImageMap.get("E");
        BufferedImage figureF = figureImageMap.get("F");
        BufferedImage figureG = figureImageMap.get("G");
        BufferedImage figureH = figureImageMap.get("H");

        // similarities
        BufferedImage rowAB = imageUtilities.add(figureA, figureB);
        BufferedImage rowBC = imageUtilities.add(figureB, figureC);
        BufferedImage rowDE = imageUtilities.add(figureD, figureE);
        BufferedImage rowEF = imageUtilities.add(figureE, figureF);

        // combinations
        BufferedImage colAD = imageUtilities.multiply(figureA, figureD);
        BufferedImage colADG = imageUtilities.multiply(colAD, figureG);

        BufferedImage colBE = imageUtilities.multiply(figureB, figureE);
        BufferedImage colBEH = imageUtilities.multiply(colBE, figureH);

        BufferedImage AB = imageUtilities.multiply(figureA, figureB);
        BufferedImage AC = imageUtilities.multiply(figureA, figureC);
        BufferedImage ABC = imageUtilities.multiply(AB, figureC);
        BufferedImage DE = imageUtilities.multiply(figureD, figureE);
        BufferedImage DF = imageUtilities.multiply(figureD, figureF);
        BufferedImage DEF = imageUtilities.multiply(DE, figureF);

        // differences
        BufferedImage diffAB = imageUtilities.difference(AB, rowAB);
        BufferedImage diffDE = imageUtilities.difference(DE, rowDE);

        // run the chosen strategies
        List<String> strategies = new ArrayList<>();
        if (strategy.areEqual(figureA, figureB) && strategy.areEqual(figureB, figureC))
            if (strategy.areEqual(figureD, figureE) && strategy.areEqual(figureE, figureF))
                strategies.add("row_equals");
        if ((strategy.areEqual(figureA, figureD) || strategy.areEqual(figureA, figureE) || strategy.areEqual(figureA, figureF))
                    && (strategy.areEqual(figureB, figureD) || strategy.areEqual(figureB, figureE) || strategy.areEqual(figureB, figureF))
                    && (strategy.areEqual(figureC, figureD) || strategy.areEqual(figureC, figureE) || strategy.areEqual(figureC, figureF)))
            strategies.add("one_of_each");
        if (strategy.areEqual(rowAB, rowBC) && strategy.areEqual(rowDE, rowEF))
            strategies.add("one_cancels");
        if (strategy.areEqual(colADG, colBEH))
            strategies.add("cancel_out");
        if (strategy.areEqual(AB, figureC) && strategy.areEqual(DE, figureF))
            strategies.add("productAB");
        if (strategy.areEqual(AC, figureB) && strategy.areEqual(DF, figureE))
            strategies.add("productAC");
        if (strategy.areEqual(diffAB, figureC) && strategy.areEqual(diffDE, figureF))
            strategies.add("diffAB");
        if (strategy.isShared(figureImageMap))
            strategies.add("shared");
        if (strategy.areEqual(ABC, DEF))
            strategies.add("common_perms");

        return strategies;
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
