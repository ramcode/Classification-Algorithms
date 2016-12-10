import com.sun.scenario.effect.Crop;
import com.ub.cse601.project3.util.CrossValidation;

import java.util.Scanner;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class RunDecisionTree {

    public static void main(String args[]) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Enter Cross Validation Fold Number: ");
        System.out.println("Enter a negative value for NO cross validation: ");
        int foldNumber = Integer.valueOf(sc.nextLine());

        System.out.println("Enter File name of data set: ");
        String fileName = sc.nextLine();

        if (fileName == null || fileName.length() == 0) {

            fileName = "project3_dataset1.txt";

        }

        System.out.println("Enter File name of testing data set: ");
        String testFileName = sc.nextLine();

        if (testFileName == null || testFileName.length() == 0) {

            testFileName = fileName;
        }

        String path = "data/";

        DecisionTree decisionTree = new DecisionTree(foldNumber, fileName);
        double[][] dataMatrix = decisionTree.readDataSet(path, fileName);
        double[][] testMatrix = decisionTree.readDataSet(path, testFileName);
        decisionTree.runTreeInductionAlgo(dataMatrix, testMatrix);
        //double[][] distanceMatrix = KNN.calculateDistanceMatrix();

        /*Arrays.stream(matrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/

/*        Arrays.stream(distanceMatrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/


    }
}
