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

        // Get row and col size
        int row = Character.getNumericValue(problem.getProblemType().charAt(0));
        int col = Character.getNumericValue(problem.getProblemType().charAt(2));

        // Retrieve figures from problem
        Map<String, RavensFigure> figureMap = problem.getFigures();

        // Get list of figure names for problem
        List<String> figureKeyList = createKeyList(figureMap, "[A-Z]");

        // Get list of figure names for solutions
        List<String> solutionKeyList = createKeyList(figureMap, "[0-9]");

//        // Create list-matrix resembling RPM with null for placeholder on last entry
////        List<List<RavensFigure>> ravensFiguresListLR =
////                new ArrayList<>(getRavensMatrix(figureMap, figureKeyList, row, col));
////        List<List<RavensFigure>> ravensFiguresListUD =
////                new ArrayList<>(generateUpDownMatrix(ravensFiguresListLR));
//
//
//        // Create a matrix representation for the figures
//        List<List<BufferedImage>> figureImageMatrix = getMatrixRepresentation(figureMap, figureKeyList, row, col);
//
//        // Create a map of the figure images
//        Map<String, BufferedImage> solutionImageMap = new HashMap<>();
//
//        for (String figureKey : solutionKeyList) {
//            RavensFigure solutionFigure = figureMap.get(figureKey);
//            BufferedImage image = null;
//            try {
//                image = ImageIO.read(new File(solutionFigure.getVisual()));
//            } catch (IOException e) {
//                throw new RuntimeException("Could not open specified image.", e);
//            }
//
//            solutionImageMap.put(figureKey, image);
//        }
//
//        String strategy = determineStrategy(figureImageMatrix, solutionImageMap, row, col);

        Map<String, BufferedImage> figureImageMap = new HashMap<>();
        for (String figureKey : problem.getFigures().keySet()) {
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(problem.getFigures().get(figureKey).getVisual()));
            } catch (IOException e) {
                throw new RuntimeException("Could not open specified image.", e);
            }

            figureImageMap.put(figureKey, image);
        }

        String strategy = determineStrategy(figureImageMap);


        return -1;
    }

    public List<List<BufferedImage>> getMatrixRepresentation(Map<String, RavensFigure> figureMap,
                                                             List<String> figureKeyList,
                                                             int row,
                                                             int col) {
        figureKeyList.add(figureKeyList.size(), null); //add null object as placeholder for solution
        List<List<BufferedImage>> figureImageMatrix = new ArrayList<>();
        int ind = 0;
        List<BufferedImage> figureImageRow = new ArrayList<>();
        while (ind <= (row * col) - 2) {

            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(figureMap.get(figureKeyList.get(ind)).getVisual()));
            } catch (IOException e) {
                throw new RuntimeException("Could not open specified image.", e);
            }
            figureImageRow.add(image);

            int realInd = ind + 1;
            if (realInd % col == 0) {
                figureImageMatrix.add(figureImageRow);
                figureImageRow = new ArrayList<>();
            }
            ind++;
        }
        return figureImageMatrix;
    }



    public boolean areEqual(BufferedImage image1, BufferedImage image2) {
        int width = Math.max(image1.getWidth(), image2.getWidth());
        int height = Math.max(image1.getHeight(), image2.getHeight());
        int diff = 0;
        // might not work because of negative values for black
        // 0 = black
        // 255 = white
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

        Map<String, Double> stats = new HashMap<>();
        stats.put("dist", dist);
        stats.put("blk", (double) Math.abs(image1Black[0] - image2Black[0]));


        return false;
    }

    public String determineStrategy(Map<String, BufferedImage> figureImageMap) {
        String strategy = null;

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











        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(difAB)));
        frame.pack();
        frame.setVisible(true);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        return strategy;
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

    /**
     * This method creates a List of Lists resembling a Raven Progressive Matrix.
     *
     * @param figureMap
     * @param figureKeyList
     * @param row
     * @param col
     * @return The Raven Progressive Matrix
     */
    public List<List<RavensFigure>> getRavensMatrix(Map<String, RavensFigure> figureMap,
                                                    List<String> figureKeyList,
                                                    int row,
                                                    int col) {

        figureKeyList.add(figureKeyList.size(), null); //add null object as placeholder
        List<List<RavensFigure>> ravensFiguresList = new ArrayList<>();
        int ind = 0;
        List<RavensFigure> ravensFigureList = new ArrayList<>();
        while (ind <= (row * col) - 1) {
            ravensFigureList.add(figureMap.get(figureKeyList.get(ind)));
            int realInd = ind + 1;
            if (realInd % col == 0) {
                ravensFiguresList.add(ravensFigureList);
                ravensFigureList = new ArrayList<>();
            }
            ind++;
        }

        return ravensFiguresList;
    }

    /**
     * This method generates a Raven Progressive Matrix reading from the top of a column
     * to the bottom of a column. This gives the agent a different approach when trying
     * to find solutions.
     *
     * @param ravensFiguresList
     * @return The Raven Progressive Matrix
     */
    public List<List<RavensFigure>> generateUpDownMatrix(List<List<RavensFigure>> ravensFiguresList) {
        List<List<RavensFigure>> ravensFiguresListUD = new ArrayList<>();

        for (int i = 0; i < ravensFiguresList.size(); i++) {
            List<RavensFigure> tempList = new ArrayList<>();
            for (int j = 0; j < ravensFiguresList.size(); j++) {

                if (i == j)
                    tempList.add(j, ravensFiguresList.get(i).get(j));
                else
                    tempList.add(j, ravensFiguresList.get(j).get(i));
            }
            ravensFiguresListUD.add(i, tempList);
        }

        return ravensFiguresListUD;
    }

}
