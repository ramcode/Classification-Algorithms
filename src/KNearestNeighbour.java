import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class KNearestNeighbour {

    private int numOfFolds;
    private String fileName;
    public double[][] dataMatrix;
    private int dataSampleCount;


    public KNearestNeighbour ( int numOfFolds, String fileName ) {

        this.numOfFolds = numOfFolds;
        this.fileName = fileName;

    }


    public double[][] readFeatureValues ( String path ) {

        Path filePath = null;

        try {

            filePath = Paths.get(path, fileName);
            List<String> dataSamples = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            int rows = dataSamples.size();
            this.dataSampleCount = rows;
            int columns = dataSamples.get(0).trim().split("\\s+").length;
            dataMatrix = new double[rows][columns];

            for ( int i = 0; i < rows; i++ ) {

                String[] singleDataSampleValue = dataSamples.get(i).trim().split("\\s+");

                for ( int j = 0; j < columns; j++ ) {

                    //TODO: give decent numerical values to string data
                    if ( singleDataSampleValue[j].equals("Absent") ) {

                        dataMatrix[i][j] = 0.00;

                    } else if ( singleDataSampleValue[j].equals("Present") ) {

                        dataMatrix[i][j] = 1.00;

                    } else {

                        dataMatrix[i][j] = Double.parseDouble(singleDataSampleValue[j]);

                    }

                }

            }


        } catch ( Exception e ) {

            e.printStackTrace();

        }

        return dataMatrix;
    }

    public double[][] calculateDistanceMatrix () {

        double[][] distanceMatrix = null;

        try {

            distanceMatrix = new double[dataMatrix.length][dataMatrix.length];

            for ( int i = 0; i < dataMatrix.length - 1; i++ ) {

                for ( int j = i + 1; j < dataMatrix.length; j++ ) {

                    double[] object1 = dataMatrix[i];
                    double[] object2 = dataMatrix[j];
                    double squaredSum = 0;

                    for ( int dim = 0; dim < object1.length - 1; dim++ ) {

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


}
