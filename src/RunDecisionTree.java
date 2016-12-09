import com.sun.scenario.effect.Crop;
import com.ub.cse601.project3.util.CrossValidation;

import java.util.Scanner;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class RunDecisionTree {

    public static void main(String args[]) {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter File name of data set: ");
        String fileName = sc.nextLine();

        if (fileName == null || fileName.length() == 0) {

            fileName = "project3_dataset1.txt";

        }

        String path = "data/";
        System.out.println("Enter Cross Validation Fold Number: ");
        int foldNumber = Integer.valueOf(sc.nextLine());
        DecisionTree decisionTree = new DecisionTree(foldNumber, fileName);
        double[][] dataMatrix = decisionTree.readDataSet(path);
        decisionTree.runTreeInductionAlgo(dataMatrix, foldNumber);
        //double[][] distanceMatrix = KNN.calculateDistanceMatrix();

        /*Arrays.stream(matrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/

/*        Arrays.stream(distanceMatrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/


    }
}
