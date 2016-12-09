import com.ub.cse601.project3.util.CrossValidation;

import javax.swing.tree.TreeNode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class DecisionTree {

    private int numOfFolds;
    private String fileName;
    public double[][] dataMatrix;
    private int dataSampleCount;
    private int colCount;
    public static final String CLASS_LABEL_NO = "CLASS_0";
    public static final String CLASS_LABEL_YES = "CLASS_1";
    public static final String NODE_LABEL = "ATTRIBUTE";

    public DecisionTree(int numOfFolds, String fileName) {

        this.numOfFolds = numOfFolds;
        this.fileName = fileName;

    }


    public double[][] readDataSet(String path) {
        Path filePath = null;
        try {
            filePath = Paths.get(path, fileName);
            List<String> dataSamples = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            int rows = dataSamples.size();
            this.dataSampleCount = rows;
            int columns = dataSamples.get(0).trim().split("\\s+").length;
            this.colCount = columns;
            dataMatrix = new double[rows][columns];
            for (int i = 0; i < rows; i++) {
                String[] singleDataSampleValue = dataSamples.get(i).trim().split("\\s+");
                for (int j = 0; j < columns; j++) {
                    //TODO: give decent numerical values to string data
                    if (singleDataSampleValue[j].equals("Absent")) {
                        dataMatrix[i][j] = 0.00;

                    } else if (singleDataSampleValue[j].equals("Present")) {
                        dataMatrix[i][j] = 1.00;
                    } else {
                        dataMatrix[i][j] = Double.parseDouble(singleDataSampleValue[j]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataMatrix;
    }


    public void runTreeInductionAlgo(double[][] dataMatrix, int kFold) {
        System.out.println("Running Tree Induction Algo...");
        CrossValidation cv = new CrossValidation(dataMatrix, kFold);
        List<Object[]> kFoldSplits = cv.generateKFoldSplit(dataMatrix, kFold);
        List<double[][]> validatedSplits = new ArrayList<>();
        double avgPrecision = 0, avgRecall = 0, avgFmeasure = 0, avgAccuracy = 0;
        for (int i = 0; i < kFoldSplits.size(); i++) {
            Object[] split = kFoldSplits.get(i);
            AtomicInteger truePositive = new AtomicInteger(0), trueNegative =
                    new AtomicInteger(0), falsePositive = new AtomicInteger(0), falseNegtive = new AtomicInteger(0);
            List<Integer> remainingAttributes = new ArrayList<>();
            double[][] trainData = (double[][]) split[0];
            double[][] testData = (double[][]) split[1];
            IntStream.range(0, colCount).forEach(x -> remainingAttributes.add(x));
            System.out.println("Creating decision tree for split:" + i + 1);
            DecisionNode decisionTree = createDecisionTree(trainData, remainingAttributes);
            System.out.println("Validting decision tree on split:" + i + 1);
            validateTestData(decisionTree, testData);
            Arrays.stream(testData).forEach(x -> {
                if (x[colCount - 1] == 1 && x[colCount] == 1) truePositive.getAndIncrement();
                else if (x[colCount - 1] == 1 && x[colCount] == 0) falseNegtive.getAndIncrement();
                else if (x[colCount - 1] == 0 && x[colCount] == 1) falsePositive.getAndIncrement();
                else if (x[colCount - 1] == 0 && x[colCount] == 0) trueNegative.getAndIncrement();
            });
            double dataSize = truePositive.intValue() + trueNegative.intValue() + falseNegtive.intValue() + falsePositive.intValue();
            avgAccuracy += (truePositive.intValue() + trueNegative.intValue()) / dataSize;
            avgPrecision += truePositive.intValue() / (truePositive.intValue() + falsePositive.intValue());
            avgRecall += truePositive.intValue() / (truePositive.intValue() + falseNegtive.intValue());
            avgFmeasure += 2 * avgRecall * avgPrecision / (avgRecall + avgPrecision);
        }
        avgAccuracy = avgAccuracy / kFold;
        avgPrecision = avgPrecision / kFold;
        avgRecall = avgRecall / kFold;
        avgFmeasure = avgFmeasure / kFold;
        System.out.println("Printing Evalauation Metrics...");
        System.out.println("Decision Tree, Avg, Accuracy: " + avgAccuracy);
        System.out.println("Decision Tree, Avg, Precision: " + avgPrecision);
        System.out.println("Decision Tree, Avg, Recall: " + avgRecall);
        System.out.println("Decision Tree, Avg, Fmeasure: " + avgFmeasure);
    }

    public void validateTestData(DecisionNode root, double[][] testData) {
        for (int i = 0; i < testData.length; i++) {
            while (root != null && !root.isLeaf) {
                int attributeIndex = root.attributeIndex;
                String edgeLabel = root.featureLabel;
                if (testData[i][attributeIndex] <= root.splitAttributeCutValue) {
                    edgeLabel = " < " + root.splitAttributeCutValue;
                } else {
                    edgeLabel = " > " + root.splitAttributeCutValue;
                }
                root = root.children.get(edgeLabel);
            }
            if (root.isLeaf) {
                if (root.featureLabel.split("_")[1].equals("1")) testData[i][colCount] = 1;
                else testData[i][colCount] = 0;
            }
        }
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
        double candidateCut = 0;
        for (int i = 0; i < trainData.length - 1; i++) {
            if (trainData[i][colCount - 1] == 1) {
                candidateOneCount += 1;
            } else {
                candidateZeroCount += 1;
            }
            int leftNodeCount = i + 1;
            int rightNodeCount = trainData.length - (i + 1);
            double giniLeftNode = 1 - Math.pow((double) candidateZeroCount / leftNodeCount, 2) - Math.pow((double) candidateOneCount / leftNodeCount, 2);
            double giniRightNode = 1 - Math.pow((double) (classZeroCount - candidateZeroCount) / rightNodeCount, 2) - Math.pow((double) (classOneCount - candidateOneCount) / rightNodeCount, 2);
            double giniSplit = (leftNodeCount / trainData.length) * giniLeftNode + (rightNodeCount / trainData.length) * giniRightNode;
            if (giniSplit < minGiniValue) {
                minGiniValue = giniSplit;
                candidateCut = i;
            }

        }
        attributeGiniValue[0] = candidateCut;
        attributeGiniValue[1] = minGiniValue;
        return attributeGiniValue;
    }

    public double[] findCandidateAttribute(double[][] trainData, List<Integer> remainingAttributes) {
        double minGiniValue = Double.MAX_VALUE;
        double[] candidateAttribute = new double[3];
        int candidateAttributeIndex = 0;
        int cutIndex = 0;
        for (Integer attributeIndex : remainingAttributes) {
            double[] candidateCurr = findAttributeGiniValue(trainData, attributeIndex);
            double candidateGiniValue = candidateCurr[1];
            if (candidateGiniValue < minGiniValue) {
                candidateAttributeIndex = attributeIndex;
                minGiniValue = candidateGiniValue;
                cutIndex = new Double(candidateCurr[0]).intValue();
            }
        }
        candidateAttribute[0] = candidateAttributeIndex;
        candidateAttribute[1] = minGiniValue;
        candidateAttribute[2] = cutIndex;
        return candidateAttribute;
    }


    public DecisionNode createDecisionTree(double[][] trainData, List<Integer> remainingAttributes) {
        DecisionNode node = new DecisionNode();
        if (checkStopCondition(trainData, remainingAttributes)) {
            int attributeIndex = findMajorityClassLabel(trainData);
            String classLabel = String.valueOf(attributeIndex);
            node.setFeatureLabel(classLabel.equals("1") ? CLASS_LABEL_YES : CLASS_LABEL_NO);
            node.setLeaf(true);
            node.attributeIndex = attributeIndex;
            return node;
        }
        double[] splitAttribute = findCandidateAttribute(trainData, remainingAttributes);
        Double splitAttributeIdx = splitAttribute[0];
        int splitAttributeIndex = splitAttributeIdx.intValue();
        double splitAttributeCutValue = splitAttribute[1];
        int cutIndex = new Double(splitAttribute[2]).intValue();
        node.setLeaf(false);
        node.setFeatureLabel(NODE_LABEL + splitAttributeIndex + 1);
        node.attributeIndex = splitAttributeIndex;
        node.splitAttributeCutValue = splitAttributeCutValue;
        remainingAttributes.remove(splitAttributeIndex);
        Object[] partitions = CrossValidation.generatePartitionsForSplit(trainData, cutIndex);
        boolean left = true;
        for (Object partition : partitions) {
            double[][] partitionData = (double[][]) partition;
            node.addNode(createDecisionTree(partitionData, remainingAttributes), node.featureLabel + (left ? " < " : " > " + splitAttributeCutValue));
            left = false;
        }
        return node;
    }

    public boolean checkStopCondition(double[][] trainData, List<Integer> remainingAttributes) {
        if (remainingAttributes.size() == 0) return true;
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        if (classOneCount == 0 || classZeroCount == 0) return true;
        return false;
    }

    public int findMajorityClassLabel(double[][] trainData) {
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        return classOneCount >= classZeroCount ? 1 : 0;
    }
}
