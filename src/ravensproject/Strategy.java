package ravensproject;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class contains the strategies for determining a solution
 */
public class Strategy {

    private ImageUtilities imageUtilities;
    private Random random;

    public Strategy(ImageUtilities imageUtilities) {
        this.imageUtilities = imageUtilities;
        random = new Random();
    }

    /**
     * This method determines if two images are "equal" or not (within a given threshold) based on
     * the number of black pixels between the images.
     *
     * @param image1
     * @param image2
     * @return Whether the two images are equal
     */
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

        return dist < 1.6 && Math.abs(image1Black[0] - image2Black[0]) < 205;
    }

    /**
     * This method determines, based on A->B and D->E, whether or not the transition between
     * AB->C and DE->F is equivalent.
     *
     * @param figureImageMap
     * @return Whether AB->C and DE->F share a common transition
     */
    public boolean isShared(Map<String, BufferedImage> figureImageMap) {
        BufferedImage sharedAB = imageUtilities.compareImages(figureImageMap.get("A"), figureImageMap.get("B")).get(0);
        BufferedImage sharedDE = imageUtilities.compareImages(figureImageMap.get("D"), figureImageMap.get("E")).get(0);

        return areEqual(sharedAB, figureImageMap.get("C")) && areEqual(sharedDE, figureImageMap.get("F"));
    }

    /**
     * This method determines a solution from H-># through straight-up comparison.
     *
     * @param figMap
     * @param solKeyList
     * @return A solution based on H->#
     */
    public int rowEquals(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        for (String solutionKey : solKeyList)
            if (areEqual(figMap.get("H"), figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);
        return -1;
    }

    /**
     * This method determines a solution from comparing the similarity between the first and last row.
     * If a figure in the first row does not have equivalence with G or H, then it is deemed the "missing figure"
     * and is a candidate for comparison against a solution. If they all share similarities with G and H, then
     * the strategy is deemed "skippable".
     * @param figMap
     * @param solKeyList
     *
     * @return A solution based on the missing (dissimilar) figure in the first row
     */
    public int oneOfEach(Map<String, BufferedImage> figMap, List<String> solKeyList) {
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

    /**
     * This method determines a solution by looking at CF's similarities and GH's similarities.
     * If a solution's similarity to each one is equivalent, then it is deemed the correct answer.
     *
     * @param figMap
     * @param solKeyList
     * @return The solution based on the equivalence to both CF and GH
     */
    public int oneCancels(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage colCF = imageUtilities.add(figMap.get("C"), figMap.get("F"));
        BufferedImage rowGH = imageUtilities.add(figMap.get("G"), figMap.get("H"));
        List<Integer> answers = new ArrayList<>();

        for (String solutionKey : solKeyList) {
            BufferedImage candidate1 = imageUtilities.add(colCF, figMap.get(solutionKey));
            BufferedImage candidate2 = imageUtilities.add(rowGH, figMap.get(solutionKey));

            if (areEqual(colCF, candidate1) && areEqual(rowGH, candidate2))
                answers.add(Integer.parseInt(solutionKey));
        }

        if (answers.size() != 1) {
            if (isShared(figMap))
                return shared(figMap, solKeyList);
            else
                return chooseLeastLikely(figMap, solKeyList);
        } else {
            return answers.get(0);
        }
    }

    /**
     * This method determines a solution based on the comparison with G's similarity to H. If the solution
     * is not equivalent to GH, the strategy is deemed "skippable".
     *
     * @param figMap
     * @param solKeyList
     * @return The solution based on its equivalence to the similarity between G and H
     */
    public int shared(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage sharedGH = imageUtilities.compareImages(figMap.get("G"), figMap.get("H")).get(0);

        for (String solutionKey : solKeyList)
            if (areEqual(sharedGH, figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);
        return -1;
    }

    /**
     * This method determines the solution if it is not a likely one (not equivalent to any figures in
     * the RPM. This is considered the "fallback" option for the agent, as it is more of a guess than
     * anything.
     *
     * @param figMap
     * @param solKeyList
     * @return A solution that is unlike any of the figures
     */
    public int chooseLeastLikely(Map<String, BufferedImage> figMap, List<String> solKeyList) {
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
        else if (answers.size() > 1 && answers.size() < 5)
            return Integer.parseInt(answers.get(random.nextInt(answers.size())));
        return -1;
    }

    /**
     * This method determines a solution based off of the column ADG. If a solution in the column CF# is
     * equivalent to ADG, that solution is deemed the correct one.
     *
     * @param figMap
     * @param solKeyList
     * @return The solution based on the similarity to ADG
     */
    public int cancelOut(Map<String, BufferedImage> figMap, List<String> solKeyList) {
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
            if (areEqual(colADG, candidate))
                return Integer.parseInt(solutionKey);
        }

        return -1;
    }

    /**
     * This method determines a solution based off of the comparison between DEF and GH#.
     *
     * @param figMap
     * @param solKeyList
     * @return The solution that matches most with DEF
     */
    public int commonPermutations(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage DE = imageUtilities.multiply(figMap.get("D"), figMap.get("E"));
        BufferedImage GH = imageUtilities.multiply(figMap.get("G"), figMap.get("H"));
        BufferedImage DEF = imageUtilities.multiply(DE, figMap.get("F"));

        for (String solutionKey : solKeyList) {
            BufferedImage candidate = imageUtilities.multiply(GH, figMap.get(solutionKey));
            if (areEqual(DEF, candidate))
                return Integer.parseInt(solutionKey);
        }

        return chooseLeastLikely(figMap, solKeyList);
    }

    /**
     * This method determines a solution based off of the similarity between itself and GH only
     * if GH is similar to AB and DE is equivalent to F.
     *
     * @param figMap
     * @param solKeyList
     * @return The solution based off of the equivalence with GH
     */
    public int productAB(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage GH = imageUtilities.multiply(figMap.get("G"), figMap.get("H"));

        for (String solutionKey : solKeyList)
            if (areEqual(GH, figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);

        return chooseLeastLikely(figMap, solKeyList);
    }

    /**
     * This method determines a solution based on the similarity between G# and H only if
     * AC is equivalent to B and DF is equivalent to E.
     *
     * @param figMap
     * @param solKeyList
     * @return The solution based off of G# and thats equivalence to H
     */
    public int productAC(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        for (String solutionKey : solKeyList) {
            BufferedImage candidate = imageUtilities.multiply(figMap.get("G"), figMap.get(solutionKey));
            if (areEqual(candidate, figMap.get("H")))
                return Integer.parseInt(solutionKey);
        }

        return chooseLeastLikely(figMap, solKeyList);
    }

    /**
     * This method looks at the difference between G and H and determines if the solution is equivalent
     * to that difference. If it is, it is deemed the correct solution.
     *
     * @param figMap
     * @param solKeyList
     * @return The solution based on the equivalence to the difference between G and H
     */
    public int diffAB(Map<String, BufferedImage> figMap, List<String> solKeyList) {
        BufferedImage diffGH = imageUtilities.difference(figMap.get("G"), figMap.get("H"));

        for (String solutionKey : solKeyList)
            if (areEqual(diffGH, figMap.get(solutionKey)))
                return Integer.parseInt(solutionKey);

        return chooseLeastLikely(figMap, solKeyList);
    }

}
