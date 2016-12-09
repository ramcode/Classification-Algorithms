import java.util.Arrays;
import java.util.Scanner;

public class RunKNearestNeighbour {

    public static void main ( String args[] ) {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter File name of data set: ");
        String fileName = sc.nextLine();

        if (fileName == null || fileName.length() == 0) {

            fileName = "project3_dataset1.txt";
            //fileName = "Test.txt";

        }

        String path = "Classification-Algorithms/data/";
        System.out.println("Enter Cross Validation Fold Number: ");
        int foldNumber = Integer.valueOf(sc.nextLine());
        KNearestNeighbour KNN = new KNearestNeighbour(foldNumber, fileName);

        double[][] matrix = KNN.readFeatureValues(path);

        double[][] distanceMatrix = KNN.calculateDistanceMatrix();

        /*System.out.println("DataMatrix");
        Arrays.stream(matrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/

        /*
        System.out.println("DistanceMatrix");
        Arrays.stream(distanceMatrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/

        KNN.startCrossValidation(matrix);
        //double[][] distanceMatrix = KNN.calculateDistanceMatrix();


    }
}
