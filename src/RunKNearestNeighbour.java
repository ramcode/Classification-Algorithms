import java.util.Arrays;
import java.util.Scanner;

public class RunKNearestNeighbour {

    public static void main ( String args[] ) {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter 1 for crossvalidation and 2 for demo");
        int checker = Integer.valueOf(sc.nextLine());
        if(checker==1)
        {
            System.out.println("Enter File name of data set: ");
            String fileName = sc.nextLine();

            if (fileName == null || fileName.length() == 0) {

                fileName = "project3_dataset1.txt";
                //fileName = "Test.txt";

            }

            String path = "data/";
            System.out.println("Enter Cross Validation Fold Number: ");
            int foldNumber = Integer.valueOf(sc.nextLine());
            KNearestNeighbour KNN = new KNearestNeighbour(foldNumber, fileName);

            double[][] matrix = KNN.readFeatureValues(path, fileName);

            double[][] distanceMatrix = KNN.calculateDistanceMatrix();

            KNN.startCrossValidation(matrix);
        }
        else
        {
            System.out.println("Enter Training File name of data set: ");
            String trainingFileName = sc.nextLine();

            if (trainingFileName == null || trainingFileName.length() == 0) {

                trainingFileName = "project3_dataset3_train.txt";

            }

            System.out.println("Enter Testing File name of data set: ");
            String testingFileName = sc.nextLine();

            if (testingFileName == null || testingFileName.length() == 0) {

                testingFileName = "project3_dataset3_test.txt";

            }

            String path = "data/";
            KNearestNeighbour KNN = new KNearestNeighbour(trainingFileName, testingFileName);

            double[][] trainingmatrix = KNN.readFeatureValues(path, trainingFileName);
            double[][] testingmatrix = KNN.readFeatureValues(path, testingFileName);

            KNN.demoKNNalgorithm(trainingmatrix, testingmatrix);
        }


        /*System.out.println("DataMatrix");
        Arrays.stream(matrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/

        /*
        System.out.println("DistanceMatrix");
        Arrays.stream(distanceMatrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/

        //double[][] distanceMatrix = KNN.calculateDistanceMatrix();


    }
}
