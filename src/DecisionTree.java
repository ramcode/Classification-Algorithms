import javax.swing.tree.TreeNode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class DecisionTree {

    private int numOfFolds;
    private String fileName;
    public double[][] dataMatrix;
    private int dataSampleCount;
    private int colCount;

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

    public double calculateGINI(double[][] dataSet) {
        long classOneCount = Arrays.stream(dataSet).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = dataSet.length - classOneCount;
        return 1 - Math.pow((double) classZeroCount / dataSet.length, 2) - Math.pow((double) classOneCount / dataSet.length, 2);
    }

    public void sortTrainData(double[][] trainData, int attributeIndex) {
        Arrays.sort(trainData, (r1, r2) -> Double.compare(r1[attributeIndex], r2[attributeIndex]));
    }

    public double findAttributeGiniValue(double[][] trainData, int attributeIndex) {
        Arrays.sort(trainData, (r1, r2) -> Double.compare(r1[attributeIndex], r2[attributeIndex]));
        double minGiniValue = 0;
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        int candidateZeroCount = 0;
        int candidateOneCount = 0;
        for (int i = 0; i < trainData.length - 1; i++) {
            double candidateCut = (trainData[i][attributeIndex] + trainData[i + 1][attributeIndex]) / 2.0;
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
            minGiniValue = Math.min(giniSplit, minGiniValue);
        }
        return minGiniValue;
    }

    public int findCandidateAttribute(double[][] trainData, List<Integer> remainingAttributes) {
        double minGiniValue = 0;
        int candidateAttributeIndex = 0;
        for (Integer attriButeIndex : remainingAttributes) {
            double candidateGiniValue = findAttributeGiniValue(trainData, attriButeIndex);
            if (candidateGiniValue < minGiniValue) {
                candidateAttributeIndex = attriButeIndex;
            }
        }
        return candidateAttributeIndex;
    }


    public void runTreeInductionAlgo(double[][] trainData, List<Integer> remainingAttributes) {
        if (checkStopCondition(trainData, remainingAttributes)) {
            String classLabel = findMajorityClassLabel(trainData);
            DecisionNode node = new DecisionNode(classLabel, true);
            return ;
        }

    }

    public boolean checkStopCondition(double[][] trainData, List<Integer> remainingAttributes) {
        if (remainingAttributes.size() == 0) return true;
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        if (classOneCount == 0 || classZeroCount == 0) return true;
        return false;
    }

    public String findMajorityClassLabel(double[][] trainData) {
        long classOneCount = Arrays.stream(trainData).filter(x -> x[colCount - 1] == 1).count();
        long classZeroCount = trainData.length - classOneCount;
        return classOneCount >= classZeroCount ? "1" : "0";
    }
}
