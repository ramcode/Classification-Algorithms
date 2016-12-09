import com.ub.cse601.project3.util.CrossValidation;
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
    private int dataSampleCount;
    private List<Integer> categoricalIndexStorer;
    private Map<String,Double> map;



    public NaiveBayes ( String fileName, int numOfFolds ) {

        this.fileName = fileName;
        this.numOfFolds = numOfFolds;
        this.categoricalIndexStorer = new ArrayList<>();
        this.map = new HashMap<String,Double>();

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


                        if ( map.containsKey(string) ) {

                            dataMatrix[i][j] = map.get(singleDataSampleValue[j]);

                        } else {

                            count++;
                            map.put(string.toString(),count);
                            dataMatrix[i][j] = count;

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

        for ( Object singleObject[] : getSplitSetsList ) {

            double[][] trainingSet = (double[][])singleObject[0];
            double[][] testingSet = (double[][])singleObject[1];

            performNaiveBayes( trainingSet, testingSet );

        }

    }

    public void performNaiveBayes ( double[][] trainingSet, double[][] testSet ) {

        double accuracy;
        double precision;
        double recall;
        double f1Measure;

        double[][] MLEMatrix 


    }

    public double[][] prepareFeatureMatrix ( double[][] dataMatrix ) {

        RealMatrix rm = MatrixUtils.createRealMatrix(dataMatrix);
        double[][] normalizedData = new double[dataMatrix.length][dataMatrix[0].length];
        RealMatrix normalizedMatrix = MatrixUtils.createRealMatrix(normalizedData);
        //exclude last 2 columns
        //double[][] tempMatrix = rm.getSubMatrix(0, dataMatrix.length - 1, 0, dataMatrix[0].length - 3).getData();

        for (int i = 0; i < dataMatrix[0].length - 2; i++) {

            if ( categoricalIndexStorer.contains(i) ) {

                normalizedMatrix.setColumn(i, rm.getColumn(i));

            } else {
                System.out.println(Arrays.toString(rm.getColumn(i)));
                normalizedMatrix.setColumn(i, StatUtils.normalize(rm.getColumn(i)));
                System.out.println(Arrays.toString(StatUtils.normalize(rm.getColumn(i))));

            }

        }

        normalizedMatrix.setColumn(dataMatrix[0].length - 2, rm.getColumn(dataMatrix[0].length - 2));
        normalizedMatrix.setColumn(dataMatrix[0].length - 1, rm.getColumn(dataMatrix[0].length - 1));


        return normalizedMatrix.getData();
    }

    /*public List<MeanVariance> getMeanAndVariance ( double[][] normalizedMatrix ) {

        List<MeanVariance> resultList = new ArrayList<>();

        for ( int i = 0; i < normalizedMatrix[0].length - 2; i++ ) {


            //MeanVariance obj = new MeanVariance();

        }

    }
*/
    class MeanVariance {

        double mean;
        double variance;

        public MeanVariance( double mean, double variance ) {

            this.mean = mean;
            this.variance = variance;

        }

    }


}
