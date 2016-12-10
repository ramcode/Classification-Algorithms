import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NaiveBayes {

    private int numOfFolds;
    private String fileName;
    public double[][] dataMatrix;
    public double[][] trainDataMatrix;
    public double[][] testDataMatrix;
    private int dataSampleCount;
    private List<Integer> categoricalIndexStorer;
    private Map<String,Double> map;



    public NaiveBayes ( String fileName, int numOfFolds ) {

        this.fileName = fileName;
        this.numOfFolds = numOfFolds;
        this.categoricalIndexStorer = new ArrayList<>();
        this.map = new HashMap<>();

    }

    public double[][] readFeatureValues ( String path, String fileName ) {

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

        //CrossValidation cvObject = new CrossValidation(dataMatrix, numOfFolds) ;
        //dataMatrix = cvObject.getNormalizedMatrix(dataMatrix, categoricalIndexStorer, 0);
        //Arrays.stream(dataMatrix).forEach(x->System.out.println(Arrays.toString(x)));
        return dataMatrix;
    }

    public void startNaiveBayes ( double[][] featureMatrix, double[][] testDataMatrix ) {


        CrossValidation crossValidationObj = new CrossValidation( featureMatrix, numOfFolds );
        List<Object[]> getSplitSetsList = new ArrayList<Object[]>();
        if(numOfFolds>0)
        {
            getSplitSetsList = crossValidationObj.generateKFoldSplit( featureMatrix, numOfFolds );
        }
        else
        {
            Object[] traintestSplit = new Object[2];
            traintestSplit[0] = featureMatrix;
            traintestSplit[1] = testDataMatrix;
            getSplitSetsList.add(traintestSplit);

        }

        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        double totalAccuracy = 0;
        double totalPrecision = 0;
        double totalRecall = 0;
        double totalF1measure = 0;

        /*System.out.println("*************MAP************************");

        Iterator it = map.entrySet().iterator();

        while ( it.hasNext() ) {

            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());

        }

        System.out.println("*************MAP END************************");
*/

        for ( Object singleObject[] : getSplitSetsList ) {

            truePositive = 0;
            trueNegative = 0;
            falsePositive =0;
            falseNegative =0;

            double[][] trainingSet = (double[][])singleObject[0];
            double[][] testingSet = (double[][])singleObject[1];

            double[][] result = performNaiveBayes( trainingSet, testingSet );

            int[] validationData = performCrossValidation ( result );
            //Arrays.stream(result).forEach( x -> System.out.println(Arrays.toString(x)));
            //System.out.println("******Validation values per iteration***************");
            //System.out.println("TP:" + validationData[0]);
            //System.out.println("TN:" + validationData[1]);
            //System.out.println("FP:" + validationData[2]);
            //System.out.println("FN:" + validationData[3]);
            //System.out.println("******************************************************");

            truePositive += validationData[0];
            trueNegative += validationData[1];
            falsePositive += validationData[2];
            falseNegative += validationData[3];


            totalAccuracy += !Double.isNaN(((double)(truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative))) ? ((double)(truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative)) : 0;
            //totalAccuracy += ((double)(truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative));
            //System.out.println("total accuracy ="+totalAccuracy);
            totalPrecision += !Double.isNaN(((double)(truePositive) / (truePositive + falsePositive))) ? ((double)(truePositive) / (truePositive + falsePositive)) : 0;
            //totalPrecision += ((double)(truePositive) / (truePositive + falsePositive));
            totalRecall += !Double.isNaN(((double)(truePositive) / (truePositive + falseNegative))) ? ((double)(truePositive) / (truePositive + falseNegative)) : 0;
            //totalRecall += ((double)(truePositive) / (truePositive + falseNegative));
            totalF1measure += !Double.isNaN((((double)2*truePositive) / ((2*truePositive) + falseNegative + falsePositive))) ? (((double)2*truePositive) / ((2*truePositive) + falseNegative + falsePositive)) : 0;
            //totalF1measure += (((double)2*truePositive) / ((2*truePositive) + falseNegative + falsePositive));

        }





        if(numOfFolds>0)
        {
            totalAccuracy = totalAccuracy/numOfFolds;
            totalPrecision = totalPrecision/numOfFolds;
            totalRecall = totalRecall/numOfFolds;
            totalF1measure = totalF1measure/numOfFolds;
        }

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




        //without smoothing
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
        //System.out.println("Prior 0 " + prior0Count);
        //System.out.println("Prior 1 " + prior1Count);

        //smoothing - Laplace Prior

/*
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

            posteriorMatrix[i][0] += 1;
            posteriorMatrix[i][1] += 1;

        }


        for ( int i = 0; i < posteriorMatrix.length; i++ ) {

            posteriorMatrix[i][0] /= (prior0Count + map.size());
            posteriorMatrix[i][1] /= (prior1Count + map.size());

        }


        double prior0Prob = (prior0Count + 1)/(trainingSet.length + 2);
        double prior1Prob = (prior1Count + 1)/(trainingSet.length+ 2);
        System.out.println("Prior 0 " + prior0Prob);
        System.out.println("Prior 1 " + prior1Prob);
*/




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
                    //System.out.println("Multiplication result for zeroProsteriorProb: " + classPosteriorProbaility0);

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

                        zeroColumnValue[m] = zeroValueColumnList.get(m);


                    }
                    //System.out.println("Zero Col Value - " + Arrays.toString(zeroColumnValue));
                    double meanColumn = StatUtils.mean(zeroColumnValue);
                    //System.out.println("Mean column:" + meanColumn);
                    double varianceColumn = StatUtils.variance(zeroColumnValue);
                    //System.out.println("variance col: " + varianceColumn);
                    double observationPDF = calculatePDF(meanColumn, varianceColumn, rowValues[j]);
                    //System.out.println("Observation PDF0: " + observationPDF);
                    classPosteriorProbaility0 *= observationPDF;
                    //System.out.println("Multiplication result for zeroProsteriorProb: " + classPosteriorProbaility0);

                }

            }

            //-----------For Probability 1-----------------------------------------------------------

            double classPosteriorProbaility1 = prior1Prob; // get this checked

            for ( int j = 0; j < rowValues.length - 2; j++ ) {


                if ( categoricalIndexStorer.contains(j) ) {

                    //this is a categorical value
                    double catValuePosteriorProb = posteriorMatrix[(int)rowValues[j]][1];
                    classPosteriorProbaility1 *= catValuePosteriorProb;
                    //System.out.println("Multiplication result for oneProsteriorProb: " + classPosteriorProbaility1);

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

                        oneColumnValue[m] = oneValueColumnList.get(m);

                    }
                    //System.out.println("One Col Value - " + Arrays.toString(oneColumnValue));
                    double meanColumn = StatUtils.mean(oneColumnValue);
                    //System.out.println("Mean column:" + meanColumn);
                    double varianceColumn = StatUtils.variance(oneColumnValue);
                    //System.out.println("variance col: " + varianceColumn);

                    double observationPDF = calculatePDF(meanColumn, varianceColumn, rowValues[j]);
                    //System.out.println("ObservationPDF1:" + observationPDF);
                    classPosteriorProbaility1 *= observationPDF;
                    //System.out.println("Multiplication result for onePosteriorProb: " + classPosteriorProbaility1);
                }

            }

            //System.out.println("*************Iteration : " + i );
            //System.out.println(Arrays.toString(rowValues));
            System.out.println("Probability of 0 : " + classPosteriorProbaility0 );
            System.out.println("Probability of 1 : " + classPosteriorProbaility1 );
            //System.out.println("*************End Iteration******************");

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
