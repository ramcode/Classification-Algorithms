import com.ub.cse601.project3.util.CrossValidation;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class KNearestNeighbour {

    private int numOfFolds;
    private int kValue = 3;
    private String fileName;
    private String trainingFileName;
    private String testingFileName;
    public double[][] trainingMatrix;
    public  double[][] testingMatrix;
    public double[][] dataMatrix;
    private int dataSampleCount;
    private List<Integer> categoricalIndexStorer;
    private Map<String, Double> map;

    //constructor for KNN
    public KNearestNeighbour ( int numOfFolds, String fileName ) {

        this.numOfFolds = numOfFolds;
        this.fileName = fileName;
        this.categoricalIndexStorer = new ArrayList<>();
        this.map = new HashMap<String, Double>();

    }

    public KNearestNeighbour(String trainingFileName, String testingFileName)
    {
        this.trainingFileName = trainingFileName;
        this.testingFileName = testingFileName;
        this.categoricalIndexStorer = new ArrayList<>();
        this.map = new HashMap<String, Double>();
    }


    public double[][] readFeatureValues(String path, String fileName) {

        Path filePath = null;

        try {

            filePath = Paths.get(path, fileName);
            List<String> dataSamples = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            int rows = dataSamples.size();
            this.dataSampleCount = rows;
            int columns = dataSamples.get(0).trim().split("\\s+").length;
            dataMatrix = new double[rows][columns + 2];
            double count = 0;

            String[] singleRecord = dataSamples.get(0).trim().split("\\s+");
            for (int k = 0; k < columns; k++) {

                try {

                    Double.parseDouble(singleRecord[k]);

                } catch (Exception e) {

                    categoricalIndexStorer.add(k);

                }

            }

            for (int i = 0; i < rows; i++) {

                String[] singleDataSampleValue = dataSamples.get(i).trim().split("\\s+");

                dataMatrix[i][columns] = -1;
                dataMatrix[i][columns+1]= -1;

                for (int j = 0; j < columns; j++) {

                    try {

                        dataMatrix[i][j] = Double.parseDouble(singleDataSampleValue[j]);

                    } catch (Exception e) {

                        StringBuilder string = new StringBuilder();
                        string.append(singleDataSampleValue[j]).append(String.valueOf(j));


                        if (map.containsKey(string)) {

                            dataMatrix[i][j] = map.get(singleDataSampleValue[j]);

                        } else {

                            map.put(string.toString(), count);
                            dataMatrix[i][j] = count;
                            count++;

                        }

                    }

                }
            }


        } catch (Exception e) {

            e.printStackTrace();

        }

        List<Integer> ignoreList = categoricalIndexStorer;
        ignoreList.add(dataMatrix[0].length-3);

        int flag = 1;
        CrossValidation CV = new CrossValidation(dataMatrix,numOfFolds);
        dataMatrix = CV.getNormalizedMatrix(dataMatrix,ignoreList, flag);

        return dataMatrix;

    }

    //calculation of the distance matrix for easy access later.
    public double[][] calculateDistanceMatrix () {

        double[][] distanceMatrix = null;

        try {

            distanceMatrix = new double[dataMatrix.length][dataMatrix.length];

            for ( int i = 0; i < dataMatrix.length - 1; i++ ) {

                for ( int j = i + 1; j < dataMatrix.length; j++ ) {

                    double[] object1 = dataMatrix[i];
                    double[] object2 = dataMatrix[j];
                    double squaredSum = 0;

                    for ( int dim = 0; dim < object1.length - 3; dim++ ) {

                        squaredSum += Math.pow(object1[dim] - object2[dim], 2);

                    }

                    double eucDistance = Math.sqrt(squaredSum);
                    distanceMatrix[i][j] = eucDistance;
                    distanceMatrix[j][i] = eucDistance;

                }

            }

        } catch ( Exception e ) {

            e.printStackTrace();

        }

        return distanceMatrix;

    }

    //for the demo files
    public void demoKNNalgorithm(double[][] trainingMatrix, double[][] testingMatrix)
    {
        double truePositive = 0;
        double trueNegative = 0;
        double falsePositive = 0;
        double falseNegative = 0;

        double truePositiveWeight = 0;
        double trueNegativeWeight = 0;
        double falsePositiveWeight = 0;
        double falseNegativeWeight = 0;

        double totalAccuracy = 0;
        double totalPrecision = 0;
        double totalRecall = 0;
        double totalF1measure = 0;

        double totalAccuracyWeight = 0;
        double totalPrecisionWeight = 0;
        double totalRecallWeight = 0;
        double totalF1measureWeight = 0;

        for(int i=0; i< testingMatrix.length;i++)
        {

            //KnnAlgorithm(distMatrix,j,foldSize, testSetStart);
            //demoKNNSingleTestCasePrediction(i);

            //for every test case
            int predValue = -1;
            double predWeight = -1;

            List<Integer> nearestIndices = new ArrayList<Integer>();
            List<Double> nearestDist = new ArrayList<Double>();

            List<Double> tempList = new ArrayList<Double>();

            //creating a templist of the testcase observation

            for(int x = 0;x<trainingMatrix.length;x++)
            {
                double distDiff = 0.0;

                for(int y = 0;y<trainingMatrix[0].length-3;y++)
                {
                    distDiff = distDiff + Math.pow((trainingMatrix[x][y]-testingMatrix[i][y]),2);
                }

                double euclidDist = Math.sqrt(distDiff);
                tempList.add(euclidDist);
            }


            //creating a list of k nearest indices and their distances
            for(int k = 0;k<kValue;k++)
            {
                int minIndex = -1;
                double minDist = Double.MAX_VALUE;

                for(int x=0;x<tempList.size();x++)
                {
                    if(tempList.get(x)<=minDist)
                    {
                        minDist = tempList.get(x);
                        minIndex = x;
                    }
                }

                nearestIndices.add(minIndex);
                nearestDist.add(minDist);
                //System.out.println("closest indices and dist ="+ (k+1)+" -- "+minIndex + " -- "+minDist);

                //setting the closest distance to max value
                tempList.set(minIndex,Double.MAX_VALUE);
            }

            //checking the count of k nearest true labels
            int class0 = 0;
            int class1 = 0;
            double weight0 = -1;
            double weight1 = -1;

            for(int k =0; k<kValue; k++)
            {
                class0 = 0;
                class1 = 0;

                if(trainingMatrix[nearestIndices.get(k)][(trainingMatrix[0].length)-3]==0)
                {
                    class0++;
                    if(weight0==-1)
                    {
                        weight0=0;
                    }
                    weight0 += ((double)1/nearestDist.get(k));
                }
                else
                {
                    class1++;
                    if(weight1==-1)
                    {
                        weight1=0;
                    }
                    weight1 += ((double)1/nearestDist.get(k));
                }
            }

            //predicting test case label based on majority k votes
            if(class0>class1)
            {
                predValue = 0;
            }
            else
            {
                predValue = 1;
            }

            if(weight0>weight1)
            {
                predWeight = 0;
            }
            else
            {
                predWeight = 1;
            }

            //System.out.println("pred value =" + predValue);
            testingMatrix[i][testingMatrix[0].length-2] = predValue;
            testingMatrix[i][testingMatrix[0].length-1] = predWeight;


            //for count values
            if(testingMatrix[i][testingMatrix[0].length-3] == testingMatrix[i][testingMatrix[0].length-2])
            {
                if(testingMatrix[i][testingMatrix[0].length-3]==0)
                {
                    trueNegative++;
                }
                else
                {
                    truePositive++;
                }
            }
            else
            {
                if(testingMatrix[i][testingMatrix[0].length-3]==0)
                {
                    falsePositive++;
                }
                else
                {
                    falseNegative++;
                }
            }

            //for weight values
            if(testingMatrix[i][testingMatrix[0].length-3] == testingMatrix[i][testingMatrix[0].length-1])
            {
                if(testingMatrix[i][testingMatrix[0].length-3]==0)
                {
                    trueNegativeWeight++;
                }
                else
                {
                    truePositiveWeight++;
                }
            }
            else
            {
                if(testingMatrix[i][testingMatrix[0].length-3]==0)
                {
                    falsePositiveWeight++;
                }
                else
                {
                    falseNegativeWeight++;
                }
            }

        }

        /*System.out.println("TP = " + truePositive);
        System.out.println("TN = " + trueNegative);
        System.out.println("FP = " + falsePositive);
        System.out.println("FN = " + falseNegative);*/

        totalAccuracy = ((truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative));
        totalPrecision = ((truePositive) / (truePositive + falsePositive));
        totalRecall = ((truePositive) / (truePositive + falseNegative));
        totalF1measure = (((double)2*truePositive) / ((2*truePositive) + falseNegative + falsePositive));

        totalAccuracyWeight = ((truePositiveWeight + trueNegativeWeight)/(truePositiveWeight + trueNegativeWeight + falsePositiveWeight + falseNegativeWeight));
        totalPrecisionWeight = (truePositiveWeight / (truePositiveWeight + falsePositiveWeight));
        totalRecallWeight = (truePositiveWeight / (truePositiveWeight + falseNegativeWeight));
        totalF1measureWeight = ((double)2*truePositiveWeight / (2*truePositiveWeight + falseNegativeWeight + falsePositiveWeight));

        System.out.println("Accuracy = " + totalAccuracy);
        System.out.println("Precision = " + totalPrecision);
        System.out.println("Recall = " + totalRecall);
        System.out.println("F1Measure = " + totalF1measure);

        /*System.out.println("AccuracyWeight = " + totalAccuracyWeight);
        System.out.println("PrecisionWeight = " + totalPrecisionWeight);
        System.out.println("RecallWeight = " + totalRecallWeight);
        System.out.println("F1MeasureWeight = " + totalF1measureWeight);*/
    }

    /*//demo KNN algorithm for every test case prediction
    public  void demoKNNSingleTestCasePrediction(double[][] trainingMatrix, double[][] testingMatrix)
    {

    }*/

    //Cross validation starts
    public void startCrossValidation(double[][] dataMatrix)
    {

        double truePositive = 0;
        double trueNegative = 0;
        double falsePositive = 0;
        double falseNegative = 0;

        double truePositiveWeight = 0;
        double trueNegativeWeight = 0;
        double falsePositiveWeight = 0;
        double falseNegativeWeight = 0;

        double totalAccuracy = 0;
        double totalPrecision = 0;
        double totalRecall = 0;
        double totalF1measure = 0;

        double totalAccuracyWeight = 0;
        double totalPrecisionWeight = 0;
        double totalRecallWeight = 0;
        double totalF1measureWeight = 0;

        CrossValidation CVclass = new CrossValidation(dataMatrix,numOfFolds);
        List<Object[]> splitSetsList = CVclass.generateKFoldSplit(dataMatrix,numOfFolds);

        int foldSize = dataMatrix.length/numOfFolds;
        int testSetStart = 0;

        for(int i=0;i<numOfFolds;i++)
        {
            truePositive = 0;
            trueNegative = 0;
            falsePositive = 0;
            falseNegative = 0;

            truePositiveWeight = 0;
            trueNegativeWeight = 0;
            falsePositiveWeight = 0;
            falseNegativeWeight = 0;

            Object[] splitObject = splitSetsList.get(i);

            double[][] trainSet = (double[][])splitObject[0];
            double[][] testSet = (double[][])splitObject[1];


            double[][] distMatrix = calculateDistanceMatrix();


            for(int j=testSetStart; j< (testSetStart+foldSize);j++)
            {
                //System.out.println("StartIndex =" + testSetStart);
                KnnAlgorithm(distMatrix,j,foldSize, testSetStart);

                //for count values
                if(dataMatrix[j][dataMatrix[0].length-3] == dataMatrix[j][dataMatrix[0].length-2])
                {
                    if(dataMatrix[j][dataMatrix[0].length-3]==0)
                    {
                        trueNegative++;
                    }
                    else
                    {
                        truePositive++;
                    }
                }
                else
                {
                    if(dataMatrix[j][dataMatrix[0].length-3]==0)
                    {
                        falsePositive++;
                    }
                    else
                    {
                        falseNegative++;
                    }
                }

                //for weight values
                if(dataMatrix[j][dataMatrix[0].length-3] == dataMatrix[j][dataMatrix[0].length-1])
                {
                    if(dataMatrix[j][dataMatrix[0].length-3]==0)
                    {
                        trueNegativeWeight++;
                    }
                    else
                    {
                        truePositiveWeight++;
                    }
                }
                else
                {
                    if(dataMatrix[j][dataMatrix[0].length-3]==0)
                    {
                        falsePositiveWeight++;
                    }
                    else
                    {
                        falseNegativeWeight++;
                    }
                }

            }

            testSetStart = testSetStart+foldSize;

            /*System.out.println("fold number = " + (i+1));
            System.out.println("TP = " + truePositive);
            System.out.println("TN = " + trueNegative);
            System.out.println("FP = " + falsePositive);
            System.out.println("FN = " + falseNegative);*/

            totalAccuracy += ((truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative));
            totalPrecision += ((truePositive) / (truePositive + falsePositive));
            totalRecall += ((truePositive) / (truePositive + falseNegative));
            totalF1measure += (((double)2*truePositive) / ((2*truePositive) + falseNegative + falsePositive));

            totalAccuracyWeight += ((truePositiveWeight + trueNegativeWeight)/(truePositiveWeight + trueNegativeWeight + falsePositiveWeight + falseNegativeWeight));
            totalPrecisionWeight += (truePositiveWeight / (truePositiveWeight + falsePositiveWeight));
            totalRecallWeight += (truePositiveWeight / (truePositiveWeight + falseNegativeWeight));
            totalF1measureWeight += ((double)2*truePositiveWeight / (2*truePositiveWeight + falseNegativeWeight + falsePositiveWeight));
        }

        totalAccuracy = totalAccuracy/numOfFolds;
        totalPrecision = totalPrecision/numOfFolds;
        totalRecall = totalRecall/numOfFolds;
        totalF1measure = totalF1measure/numOfFolds;

        totalAccuracyWeight = totalAccuracyWeight/numOfFolds;
        totalPrecisionWeight = totalPrecisionWeight/numOfFolds;
        totalRecallWeight = totalRecallWeight/numOfFolds;
        totalF1measureWeight = totalF1measureWeight/numOfFolds;

        System.out.println("Accuracy = " + totalAccuracy);
        System.out.println("Precision = " + totalPrecision);
        System.out.println("Recall = " + totalRecall);
        System.out.println("F1Measure = " + totalF1measure);

        /*System.out.println("AccuracyWeight = " + totalAccuracyWeight);
        System.out.println("PrecisionWeight = " + totalPrecisionWeight);
        System.out.println("RecallWeight = " + totalRecallWeight);
        System.out.println("F1MeasureWeight = " + totalF1measureWeight);*/

    }


    //algorithm for single test case in the test set from cross validation
    public void KnnAlgorithm(double[][] distMatrix, int testSetIndex, int foldSize, int testSetStart)
    {

        int predValue = -1;
        double predWeight = -1;

        List<Integer> nearestIndices = new ArrayList<Integer>();
        List<Double> nearestDist = new ArrayList<Double>();

        List<Double> tempList = new ArrayList<Double>();

        //creating a templist of the testcase observation
        for(int m=0;m<distMatrix.length;m++)//not considering true label and testing set elements
        {
            tempList.add(distMatrix[testSetIndex][m]);
        }


        //creating a list of k nearest indices and their distances
        for(int k = 0;k<kValue;k++)
        {
            int minIndex = -1;
            double minDist = Double.MAX_VALUE;

            for(int i=0;i<tempList.size();i++)
            {
                if (i<testSetStart || i>=(testSetStart+foldSize))
                {
                    if(tempList.get(i)<=minDist)
                    {
                        minDist = tempList.get(i);
                        minIndex = i;
                    }
                }
            }

            nearestIndices.add(minIndex);
            nearestDist.add(minDist);
            //System.out.println("closest indices and dist ="+ (k+1)+" -- "+minIndex + " -- "+minDist);

            //setting the closest distance to max value
            tempList.set(minIndex,Double.MAX_VALUE);
        }

        //checking the count of k nearest true labels
        int class0 = 0;
        int class1 = 0;
        double weight0 = -1;
        double weight1 = -1;

        for(int k =0; k<kValue; k++)
        {
            class0 = 0;
            class1 = 0;

            if(dataMatrix[nearestIndices.get(k)][(dataMatrix[0].length)-3]==0)
            {
                class0++;
                if(weight0==-1)
                {
                    weight0=0;
                }
                weight0 += ((double)1/nearestDist.get(k));
            }
            else
            {
                class1++;
                if(weight1==-1)
                {
                    weight1=0;
                }
                weight1 += ((double)1/nearestDist.get(k));
            }
        }

        //predicting test case label based on majority k votes
        if(class0>class1)
        {
            predValue = 0;
        }
        else
        {
            predValue = 1;
        }

        if(weight0>weight1)
        {
            predWeight = 0;
        }
        else
        {
            predWeight = 1;
        }

        //System.out.println("pred value =" + predValue);
        dataMatrix[testSetIndex][dataMatrix[0].length-2] = predValue;
        dataMatrix[testSetIndex][dataMatrix[0].length-1] = predWeight;
    }

}
