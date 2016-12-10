import com.ub.cse601.project3.util.CrossValidation;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.MathUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NaiveBayes {

    private int numOfFolds;
    private String fileName;
    public double[][] dataMatrix;
    private int dataSampleCount;
    private List<Integer> categoricalIndexStorer;
    private Map<String,Double> map;



    public NaiveBayes ( String fileName, int numOfFolds ) {

        this.fileName = fileName;
        this.numOfFolds = numOfFolds;
        this.categoricalIndexStorer = new ArrayList<>();
        this.map = new HashMap<>();

    }

    public double[][] readFeatureValues ( String path ) {

        Path filePath = null;

        try {

            filePath = Paths.get(path, fileName);
            List<String> dataSamples = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            int rows = dataSamples.size();
            this.dataSampleCount = rows;
            int columns = dataSamples.get(0).trim().split("\\s+").length;
            dataMatrix = new double[rows][columns + 1];
            double count = 0;

            String[] singleRecord = dataSamples.get(0).trim().split("\\s+");
            for ( int k = 0; k < columns; k++ ) {

                try {

                    Double.parseDouble(singleRecord[k]);

                } catch ( Exception e ) {

                    categoricalIndexStorer.add(k);

                }

            }

            for ( int i = 0; i < rows; i++ ) {

                String[] singleDataSampleValue = dataSamples.get(i).trim().split("\\s+");

                dataMatrix[i][columns] = -1;

                for (int j = 0; j < columns; j++) {

                    try {

                        dataMatrix[i][j] = Double.parseDouble(singleDataSampleValue[j]);

                    } catch (Exception e) {

                        StringBuilder string = new StringBuilder();
                        string.append(singleDataSampleValue[j]).append(String.valueOf(j));


                        if ( map.containsKey(string.toString()) ) {

                            dataMatrix[i][j] = map.get(string.toString());

                        } else {

                            map.put(string.toString(),count);
                            dataMatrix[i][j] = count;
                            count++;

                        }

                    }

                }
            }


        } catch ( Exception e ) {

            e.printStackTrace();

        }

        return dataMatrix;
    }

    public void startNaiveBayes ( double[][] featureMatrix ) {

        CrossValidation crossValidationObj = new CrossValidation( featureMatrix, numOfFolds );
        List<Object[]> getSplitSetsList = crossValidationObj.generateKFoldSplit( featureMatrix, numOfFolds );

        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        double totalAccuracy = 0;
        double totalPrecision = 0;
        double totalRecall = 0;
        double totalF1measure = 0;


        for ( Object singleObject[] : getSplitSetsList ) {

            double[][] trainingSet = (double[][])singleObject[0];
            double[][] testingSet = (double[][])singleObject[1];

            double[][] result = performNaiveBayes( trainingSet, testingSet );

            int[] validationData = performCrossValidation ( result );
            //Arrays.stream(result).forEach( x -> System.out.println(Arrays.toString(x)));

            truePositive += validationData[0];
            trueNegative += validationData[1];
            falsePositive += validationData[2];
            falseNegative += validationData[3];

        }

        totalAccuracy += ((double)(truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative));
        totalPrecision += ((double)(truePositive) / (truePositive + falsePositive));
        totalRecall += ((double)(truePositive) / (truePositive + falseNegative));
        totalF1measure += (((double)2*truePositive) / ((2*truePositive) + trueNegative + falsePositive));


        //totalAccuracy = totalAccuracy/numOfFolds;
        //totalPrecision = totalPrecision/numOfFolds;
        //totalRecall = totalRecall/numOfFolds;
        //totalF1measure = totalF1measure/numOfFolds;

        System.out.println("Accuracy = " + totalAccuracy);
        System.out.println("Precision = " + totalPrecision);
        System.out.println("Recall = " + totalRecall);
        System.out.println("F1Measure = " + totalF1measure);


    }


    public int[] performCrossValidation ( double[][] testingData ) {

        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        int[] returnArray = new int[4];

        RealMatrix rm = MatrixUtils.createRealMatrix(testingData);
        double[] trueLabel = rm.getColumn(testingData[0].length - 2);
        double[] predictedLabel = rm.getColumn(testingData[0].length - 1);

        for ( int i = 0; i < trueLabel.length; i++ ) {

            if ( predictedLabel[i] == trueLabel[i] && trueLabel[i] == 1.0 ) {

                truePositive++;

            } else if ( predictedLabel[i] == trueLabel[i] && trueLabel[i] == 0.0 ) {

                trueNegative++;

            } else if ( predictedLabel[i] != trueLabel[i] && trueLabel[i] == 1.0 ) {

                falseNegative++;

            } else if ( predictedLabel[i] != trueLabel[i] && trueLabel[i] == 0.0 ) {

                falsePositive++;

            }

        }

        returnArray[0] = truePositive;
        returnArray[1] = trueNegative;
        returnArray[2] = falsePositive;
        returnArray[3] = falseNegative;

        return returnArray;


    }

    public double[][] performNaiveBayes ( double[][] trainingSet, double[][] testSet ) {


        //***************For Training**********************************
        double prior0Count = 0.0;
        double prior1Count = 0.0;

        for ( int i = 0; i < trainingSet.length; i++ ) {

            if ( trainingSet[i][trainingSet[0].length - 2] == 0.0 ) {

                prior0Count++;

            } else {

                prior1Count++;

            }

        }

        double[][] posteriorMatrix = new double[map.size()][2];

        for ( int i = 0; i < posteriorMatrix.length; i++ ) {

            for ( int j = 0; j < posteriorMatrix[0].length; j++ ) {

                posteriorMatrix[i][j] = 0.0;

            }

        }

        //create realMatrix
        RealMatrix rm = MatrixUtils.createRealMatrix(trainingSet);

        for ( int i = 0; i < categoricalIndexStorer.size(); i++ ) {

            int index = categoricalIndexStorer.get(i);

            double[] columnValues = rm.getColumn(index);
            double[] trueLabelColumn = rm.getColumn(trainingSet[0].length - 2);

            for ( int k = 0; k < columnValues.length; k++) {

                if ( trueLabelColumn[k] == 0.0 ) {

                    posteriorMatrix[(int)columnValues[k]][0] +=  1;

                } else {

                    posteriorMatrix[(int)columnValues[k]][1] +=  1;

                }


            }

        }

        for ( int i = 0; i < posteriorMatrix.length; i++ ) {

            posteriorMatrix[i][0] /= prior0Count;
            posteriorMatrix[i][1] /= prior1Count;

        }

        double prior0Prob = prior0Count/trainingSet.length;
        double prior1Prob = prior1Count/trainingSet.length;


        //*******************For Testing*****************************

        RealMatrix testingSetRM = MatrixUtils.createRealMatrix(testSet);



        for ( int i = 0; i < testSet.length; i++ ) {

            double[] rowValues = testingSetRM.getRow(i);

            //-----------For Probability 0------------------------------------------
            double classPosteriorProbaility0 = prior0Prob; // get this checked

            for ( int j = 0; j < rowValues.length - 2; j++ ) {


                if ( categoricalIndexStorer.contains(j) ) {

                    //this is a categorical value
                    double catValuePosteriorProb = posteriorMatrix[(int)rowValues[j]][0];
                    classPosteriorProbaility0 *= catValuePosteriorProb;

                } else {

                    //fetch column for this index
                    //calculate mean for this index for value 0
                    //calculate variance for this index for value 0
                    //call pdf function with value
                    //multiply with classPosterirorProb0

                    double[] columnValue = rm.getColumn(j);
                    double[] trueLabelColumn = rm.getColumn(trainingSet[0].length - 2);
                    List<Double> zeroValueColumnList = new ArrayList<>();

                    for ( int k = 0; k < columnValue.length; k++) {

                        if ( trueLabelColumn[k] == 0 ) {

                            zeroValueColumnList.add(columnValue[k]);

                        }

                    }

                    double[] zeroColumnValue = new double[zeroValueColumnList.size()];

                    for ( int m = 0; m < zeroColumnValue.length; m++ ) {

                        zeroColumnValue[m] = zeroValueColumnList.get(i);


                    }

                    double meanColumn = StatUtils.mean(zeroColumnValue);
                    double varianceColumn = StatUtils.variance(zeroColumnValue);

                    double observationPDF = calculatePDF(meanColumn, varianceColumn, rowValues[j]);
                    classPosteriorProbaility0 *= observationPDF;

                }

            }

            //-----------For Probability 1-----------------------------------------------------------

            double classPosteriorProbaility1 = prior1Prob; // get this checked

            for ( int j = 0; j < rowValues.length - 2; j++ ) {


                if ( categoricalIndexStorer.contains(j) ) {

                    //this is a categorical value
                    double catValuePosteriorProb = posteriorMatrix[(int)rowValues[j]][1];
                    classPosteriorProbaility1 *= catValuePosteriorProb;

                } else {

                    //fetch column for this index
                    //calculate mean for this index for value 1
                    //calculate variance for this index for value 1
                    //call pdf function with value
                    //multiply with classPosterirorProb1

                    double[] columnValue = rm.getColumn(j);
                    double[] trueLabelColumn = rm.getColumn(trainingSet[0].length - 2);
                    List<Double> oneValueColumnList = new ArrayList<>();

                    for ( int k = 0; k < columnValue.length; k++) {

                        if ( trueLabelColumn[k] == 1 ) {

                            oneValueColumnList.add(columnValue[k]);

                        }

                    }

                    double[] oneColumnValue = new double[oneValueColumnList.size()];

                    for ( int m = 0; m < oneColumnValue.length; m++ ) {

                        oneColumnValue[m] = oneValueColumnList.get(i);


                    }

                    double meanColumn = StatUtils.mean(oneColumnValue);
                    double varianceColumn = StatUtils.variance(oneColumnValue);

                    double observationPDF = calculatePDF(meanColumn, varianceColumn, rowValues[j]);
                    classPosteriorProbaility1 *= observationPDF;

                }

            }

            System.out.println("*************Iteration : " + i );
            System.out.println(Arrays.toString(rowValues));
            System.out.println("Probability of 0 : " + classPosteriorProbaility0 );
            System.out.println("Probability of 1 : " + classPosteriorProbaility1 );
            System.out.println("*************End Iteration******************");

            if ( classPosteriorProbaility0 > classPosteriorProbaility1 ) {

                testSet[i][testSet[0].length - 1] = 0.0;

            } else {

                testSet[i][testSet[0].length - 1] = 1.0;

            }


        }

        return testSet;

    }


    public double calculatePDF ( double mean, double variance, double value ) {

        double denominator = 2*Math.PI*mean;
        denominator = Math.pow(denominator, 0.5);
        double EPow = (-Math.pow((value-mean),2)) / (2*variance);
        double pdf = (Math.pow(Math.E, EPow))/denominator;
        return pdf;
    }




}
