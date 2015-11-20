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

        // Create list-matrix resembling RPM with null for placeholder on last entry
//        List<List<RavensFigure>> ravensFiguresListLR =
//                new ArrayList<>(getRavensMatrix(figureMap, figureKeyList, row, col));
//        List<List<RavensFigure>> ravensFiguresListUD =
//                new ArrayList<>(generateUpDownMatrix(ravensFiguresListLR));


        // Create a matrix representation for the figures
        List<List<BufferedImage>> figureImageMatrix = getMatrixRepresentation(figureMap, figureKeyList, row, col);

        // Create a map of the figure images
        Map<String, BufferedImage> solutionImageMap = new HashMap<>();

        for (String figureKey : solutionKeyList) {
            RavensFigure solutionFigure = figureMap.get(figureKey);
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(solutionFigure.getVisual()));
            } catch (IOException e) {
                throw new RuntimeException("Could not open specified image.", e);
            }

            solutionImageMap.put(figureKey, image);
        }

        String strategy = determineStrategy(figureImageMatrix, solutionImageMap, row, col);






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

    public String determineStrategy(List<List<BufferedImage>> figureImageMatrix,
                                    Map<String, BufferedImage> solutionImageMap,
                                    int row,
                                    int col) {
        String strategy = null;

        // overlays
        List<List<BufferedImage>> singleOverlayMatrixLR = new ArrayList<>();
        for (List<BufferedImage> figureImageRow : figureImageMatrix) {
            if (figureImageRow.size() == col) {
                List<BufferedImage> overlayRow = new ArrayList<>();
                for (int i = 0; i < figureImageRow.size() - 1; i++) {
                    if (figureImageRow.get(i + 1) != null) { //if last row
                        overlayRow.add(imageUtilities.add(figureImageRow.get(i), figureImageRow.get(i + 1)));
                    }
                }
                singleOverlayMatrixLR.add(overlayRow);
            }
        }

        List<BufferedImage> multiOverlayMatrixUD = new ArrayList<>();
        for (int i = 0; i < col - 1; i++) {
            BufferedImage image = null;
            for (int j = 0; j < row - 1; j++) {
                System.out.println("row:"+(j+1) +"  "+ "col:"+i);
                if (image == null) //initially
                    image = imageUtilities.multiply(figureImageMatrix.get(j).get(i), figureImageMatrix.get(j+1).get(i));
                else
                    image = imageUtilities.multiply(image, figureImageMatrix.get(j+1).get(i));
                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(image)));
                frame.pack();
                frame.setVisible(true);
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            multiOverlayMatrixUD.add(image);
        }

//        JFrame frame = new JFrame();
//        frame.getContentPane().setLayout(new FlowLayout());
//        frame.getContentPane().add(new JLabel(new ImageIcon(singleOverlayMatrixLR.get(0).get(0))));
//        frame.pack();
//        frame.setVisible(true);
//
//        try {
//            Thread.sleep(5000);
//        } catch(InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }




        // common permutations
//        List<List<BufferedImage>> permutationMatrix = generator.generatePermutations()

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
