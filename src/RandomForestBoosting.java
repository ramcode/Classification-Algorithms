import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class RandomForestBoosting {

    private int numOfFolds;
    private String fileName;
    public double[][] dataMatrix;
    private int dataSampleCount;
    private int colCount;
    public static final String CLASS_LABEL_NO = "CLASS_0";
    public static final String CLASS_LABEL_YES = "CLASS_1";
    public static final String NODE_LABEL = "ATTRIBUTE";
    private int randAttrVal;
    Map<Double, String> reverseMap = null;
    List<Integer> ignoreList = null;
    Map<String, Double> map = null;

    public RandomForestBoosting(int numOfFolds, String fileName, int randAttrVal) {

        this.numOfFolds = numOfFolds;
        this.fileName = fileName;
        this.randAttrVal = randAttrVal;
    }

    public double[][] readDataSet(String path, String fileName) {
        Path filePath = null;
        ignoreList = new ArrayList<Integer>();
        try {
            filePath = Paths.get(path, fileName);
            List<String> dataSamples = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            int rows = dataSamples.size();
            this.dataSampleCount = rows;
            int columns = dataSamples.get(0).trim().split("\\s+").length;
            this.colCount = columns;
            dataMatrix = new double[rows][columns + 1];
            double count = 0;
            map = new HashMap<String, Double>();
            reverseMap = new HashMap<Double, String>();
            String[] singleRecord = dataSamples.get(0).trim().split("\\s+");
            for (int k = 0; k < columns; k++) {

                try {

                    Double.parseDouble(singleRecord[k]);

                } catch (Exception e) {

                    ignoreList.add(k);

                }

            }
            for (int i = 0; i < rows; i++) {
                String[] singleDataSampleValue = dataSamples.get(i).trim().split("\\s+");
                for (int j = 0; j < columns; j++) {
                    //TODO: give decent numerical values to string data
                    try {
                        dataMatrix[i][j] = Double.parseDouble(singleDataSampleValue[j]);
                    } catch (Exception ex) {
                        String key = String.valueOf(j) + "_" + singleDataSampleValue[j];
                        if (map.containsKey(key)) {
                            dataMatrix[i][j] = map.get(key);
                        } else {
                            map.put(key, count);
                            dataMatrix[i][j] = count;
                            reverseMap.put(count, singleDataSampleValue[j]);
                            count++;
                        }
                    }
                }
                dataMatrix[i][colCount] = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CrossValidation cv = new CrossValidation();
        if (ignoreList.size() != colCount - 1) {
            dataMatrix = cv.getNormalizedMatrix(dataMatrix, ignoreList, 0);
        }
        return dataMatrix;
    }


    public List<DecisionNode> runTreeInductionAlgo(double[][] dataMatrix, int kFold, int randAttrToSelect) {
        System.out.println("Running Tree Induction Algo...");
        CrossValidation cv = new CrossValidation(dataMatrix, kFold);
        List<Object[]> kFoldSplits = cv.generateRandomSamples(dataMatrix, kFold);
        List<double[][]> validatedSplits = new ArrayList<>();
        double avgPrecision = 0, avgRecall = 0, avgFmeasure = 0, avgAccuracy = 0;
        //TODO change
        List<DecisionNode> randomTrees = new ArrayList<>();
        for (int i = 0; i < kFoldSplits.size(); i++) {
            Object[] split = kFoldSplits.get(i);
            double truePositive = 0, trueNegative = 0, falsePositive = 0, falseNegtive = 0;
            List<Integer> remainingAttributes = new ArrayList<>();
            int m = 0;
            while (m < randAttrToSelect) {
                Random rand = new Random();
                int attrIndex = rand.nextInt(colCount);
                if (!remainingAttributes.contains(attrIndex)) {
                    remainingAttributes.add(attrIndex);
                    m++;
                }
            }
            double[][] trainData = (double[][]) split[0];
            //double[][] testData = (double[][]) split[1];
            System.out.println("Creating Random tree, iteration:" + (i + 1) + "");
            DecisionNode decisionTree = createDecisionTree(trainData, remainingAttributes);
            randomTrees.add(decisionTree);

            //validateTestData(decisionTree, testData);
            /*for (int j = 0; j < testData.length; j++) {
                if (testData[j][colCount - 1] == 1 && testData[j][colCount] == 1) truePositive++;
                else if (testData[j][colCount - 1] == 1 && testData[j][colCount] == 0) falseNegtive++;
                else if (testData[j][colCount - 1] == 0 && testData[j][colCount] == 1) falsePositive++;
                else if (testData[j][colCount - 1] == 0 && testData[j][colCount] == 0) trueNegative++;
            }
            double dataSize = truePositive + trueNegative + falseNegtive + falsePositive;
            double currAccuracy = (truePositive + trueNegative) / dataSize;
            avgAccuracy += !Double.isNaN(currAccuracy) ? currAccuracy : 0;
            double currPrecision = truePositive / (truePositive + falsePositive);
            avgPrecision += !Double.isNaN(currPrecision) ? currPrecision : 0;
            double currRecall = truePositive / (truePositive + falseNegtive);
            avgRecall += !Double.isNaN(currRecall) ? currRecall : 0;
            double currFmeasure = (2 * currRecall * currPrecision) / (currRecall + currPrecision);
            avgFmeasure += !Double.isNaN(currFmeasure) ? currFmeasure : 0;*/
        }
        /*avgAccuracy = avgAccuracy / kFold;
        avgPrecision = avgPrecision / kFold;
        avgRecall = avgRecall / kFold;
        avgFmeasure = avgFmeasure / kFold;
        System.out.println("Printing Evalauation Metrics...");
        System.out.println("Decision Tree, Avg, Accuracy: " + avgAccuracy);
        System.out.println("Decision Tree, Avg, Precision: " + avgPrecision);
        System.out.println("Decision Tree, Avg, Recall: " + avgRecall);
        System.out.println("Decision Tree, Avg, Fmeasure: " + avgFmeasure);*/
        return randomTrees;
    }

    public void validateRandomForest(double[][] testData, List<DecisionNode> randomTrees) {
        //String tab = "\t";
        //TODO change later
        for (int i = 0; i < testData.length; i++) {
            int count1 = 0, count0 = 0;
            for (int m = 0; m < randomTrees.size(); m++) {
                DecisionNode root = randomTrees.get(m);
                while (root != null && !root.isLeaf) {
                    int attributeIndex = root.attributeIndex;
                    String edgeLabel = root.featureLabel;
                    if (ignoreList.contains(attributeIndex)) {
                        if (reverseMap.get(testData[i][attributeIndex]).equals(root.splitCatValue)) {
                            edgeLabel += " = " + root.splitCatValue;
                        } else {
                            edgeLabel += " != " + root.splitCatValue;
                        }
                    } else {
                        if (testData[i][attributeIndex] <= root.splitAttributeCutValue) {
                            edgeLabel += " < " + root.splitAttributeCutValue;
                        } else {
                            edgeLabel += " > " + root.splitAttributeCutValue;
                        }
                    }
                    root = root.children.get(edgeLabel);
                }
                if (root.isLeaf) {
                    if (root.featureLabel.split("_")[1].equals("1")) {
                        //System.out.println(tab+"\t"+root.featureLabel);
                        //testData[i][colCount] = 1;
                        count1++;
                    } else {
                        //testData[i][colCount] = 0;
                        count0++;
                        //System.out.println(tab+"\t"+root.featureLabel);
                    }
                }
            }
            if (count1 >= count0) {
                testData[i][colCount] = 1;
            } else testData[i][colCount] = 0;
        }
        double truePositive = 0, trueNegative = 0, falsePositive = 0, falseNegtive = 0;
        for (int j = 0; j < testData.length; j++) {
            if (testData[j][colCount - 1] == 1 && testData[j][colCount] == 1) truePositive++;
            else if (testData[j][colCount - 1] == 1 && testData[j][colCount] == 0) falseNegtive++;
            else if (testData[j][colCount - 1] == 0 && testData[j][colCount] == 1) falsePositive++;
            else if (testData[j][colCount - 1] == 0 && testData[j][colCount] == 0) trueNegative++;
        }

        double currAccuracy = (truePositive + trueNegative) / testData.length;
        currAccuracy = !Double.isNaN(currAccuracy) ? currAccuracy : 0;
        double currPrecision = truePositive / (truePositive + falsePositive);
        currPrecision = !Double.isNaN(currPrecision) ? currPrecision : 0;
        double currRecall = truePositive / (truePositive + falseNegtive);
        currRecall = !Double.isNaN(currRecall) ? currRecall : 0;
        double currFmeasure = (2 * currRecall * currPrecision) / (currRecall + currPrecision);
        currFmeasure = !Double.isNaN(currFmeasure) ? currFmeasure : 0;
        System.out.println("Printing Evalauation Metrics...");
        System.out.println("Random Forest Tree, Avg, Accuracy: " + currAccuracy);
        System.out.println("Random Forest, Avg, Precision: " + currPrecision);
        System.out.println("Random Forest, Avg, Recall: " + currRecall);
        System.out.println("Random Forest, Avg, Fmeasure: " + currFmeasure);
    }

    public double calculateGINI(double[][] dataSet) {
        long classOneCount = Arrays.stream(dataSet).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = dataSet.length - classOneCount;
        return 1 - Math.pow((double) classZeroCount / dataSet.length, 2) - Math.pow((double) classOneCount / dataSet.length, 2);
    }

    public void sortTrainData(double[][] trainData, int attributeIndex) {
        Arrays.sort(trainData, (r1, r2) -> Double.compare(r1[attributeIndex], r2[attributeIndex]));
    }

    public double[] findAttributeGiniValue(double[][] trainData, int attributeIndex) {
        double[] attributeGiniValue = new double[2];
        Arrays.sort(trainData, (r1, r2) -> Double.compare(r1[attributeIndex], r2[attributeIndex]));
        double minGiniValue = Double.MAX_VALUE;
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        int candidateZeroCount = 0;
        int candidateOneCount = 0;
        double candidateCutValue = 0;
        for (int i = 0; i < trainData.length - 1; i++) {
            if (trainData[i][colCount - 1] == 1) {
                candidateOneCount += 1;
            } else {
                candidateZeroCount += 1;
            }
            double leftNodeCount = i + 1;
            double rightNodeCount = trainData.length - (i + 1);
            double giniLeftNode = 1 - Math.pow((candidateZeroCount / leftNodeCount), 2) - Math.pow((candidateOneCount / leftNodeCount), 2);
            double giniRightNode = 1 - Math.pow(((classZeroCount - candidateZeroCount) / rightNodeCount), 2) - Math.pow(((classOneCount - candidateOneCount) / rightNodeCount), 2);
            double giniSplit = (leftNodeCount / trainData.length) * giniLeftNode + (rightNodeCount / trainData.length) * giniRightNode;
            if (giniSplit < minGiniValue) {
                minGiniValue = giniSplit;
                candidateCutValue = trainData[i][attributeIndex];
            }

        }
        attributeGiniValue[0] = candidateCutValue;
        attributeGiniValue[1] = minGiniValue;
        return attributeGiniValue;
    }

    public double[] findCandidateAttribute(double[][] trainData, List<Integer> remainingAttributes) {
        double minGiniValue = Double.MAX_VALUE;
        double[] candidateAttribute = new double[3];
        int candidateAttributeIndex = 0;
        double cutIndexValue = 0;
        for (Integer attributeIndex : remainingAttributes) {
            double[] candidateCurr = findAttributeGiniValue(trainData, attributeIndex);
            double candidateGiniValue = candidateCurr[1];
            if (candidateGiniValue < minGiniValue) {
                candidateAttributeIndex = attributeIndex;
                minGiniValue = candidateGiniValue;
                cutIndexValue = candidateCurr[0];
            }
        }
        candidateAttribute[0] = candidateAttributeIndex;
        candidateAttribute[1] = minGiniValue;
        candidateAttribute[2] = cutIndexValue;
        return candidateAttribute;
    }


    public DecisionNode createDecisionTree(double[][] trainData, List<Integer> remainingAttributes) {
        DecisionNode node = new DecisionNode();
        double[] candidateAttribute = findCandidateAttribute(trainData, remainingAttributes);
        if (checkStopCondition(trainData, remainingAttributes, candidateAttribute)) {
            int classValue = findMajorityClassLabel(trainData);
            String classLabel = String.valueOf(classValue);
            node.setFeatureLabel(classLabel.equals("1") ? CLASS_LABEL_YES : CLASS_LABEL_NO);
            node.setLeaf(true);
            return node;
        }
        Double splitAttributeIdx = candidateAttribute[0];
        Integer splitAttributeIndex = splitAttributeIdx.intValue();
        double splitAttributeCutValue = candidateAttribute[1];
        double cutIndexValue = candidateAttribute[2];
        node.setLeaf(false);
        if (ignoreList.contains(splitAttributeIndex)) {
            node.setFeatureLabel(NODE_LABEL + "_" + splitAttributeIndex);
            node.attributeIndex = splitAttributeIndex;
            node.splitCatValue = reverseMap.get(cutIndexValue);
        } else {
            node.setFeatureLabel(NODE_LABEL + "_" + splitAttributeIndex);
            node.attributeIndex = splitAttributeIndex;
            node.splitAttributeCutValue = cutIndexValue;
        }
        remainingAttributes.remove(splitAttributeIndex);
        Object[] partitions = CrossValidation.generatePartitionsForSplit(trainData, cutIndexValue, splitAttributeIndex);
        boolean left = true;
        for (Object partition : partitions) {
            double[][] partitionData = (double[][]) partition;
            if (ignoreList.contains(node.attributeIndex)) {
                node.addNode(createDecisionTree(partitionData, remainingAttributes), node.featureLabel + (left ? " = " : " != ") + node.splitCatValue);
            } else {
                node.addNode(createDecisionTree(partitionData, remainingAttributes), node.featureLabel + (left ? " < " : " > ") + node.splitAttributeCutValue);
            }
            left = false;
        }
        return node;
    }

    public boolean checkStopCondition(double[][] trainData, List<Integer> remainingAttributes, double[] candidateAttribute) {
        if (remainingAttributes.size() == 0) return true;
        int attributeIndex = new Double(candidateAttribute[0]).intValue();
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        if (classOneCount == 0 || classZeroCount == 0) return true;
        long attrCount = Arrays.stream(trainData).map(x -> x[attributeIndex]).distinct().count();
        if (attrCount == 1) return true;
        return false;
    }

    public int findMajorityClassLabel(double[][] trainData) {
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        return classOneCount >= classZeroCount ? 1 : 0;
    }


    public void printTree(DecisionNode root, String tab) {
        if (root.isLeaf) {
            System.out.println(tab + root.featureLabel);
            tab = tab + "\t";
            return;
        }
        System.out.println(tab + root.featureLabel);
        tab = tab + "\t";
        boolean left = true;
        for (Map.Entry<String, DecisionNode> entry : root.children.entrySet()) {
            printTree(entry.getValue(), left ? "" : tab);
            left = false;
        }
    }

    private void print(DecisionNode node, String prefix, boolean isTail, String edgeLabel) {
        System.out.println(prefix + (isTail ? edgeLabel + "└── " : edgeLabel + "├── ") + node.featureLabel);
        for (Map.Entry<String, DecisionNode> entry : node.children.entrySet()) {
            print(entry.getValue(), prefix + (isTail ? "    " : "│   "), false, entry.getKey());
        }
    }
}
