import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by VenkataRamesh on 12/10/2016.
 */
public class RunRandomForestBoosting {


    public static void main(String args[]) {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter File name of data set: ");
        String fileName = sc.nextLine();

        if (fileName == null || fileName.length() == 0) {

            fileName = "project3_dataset1.txt";

        }

        String path = "data/";
        Random rand = new Random(10);
        int trees = rand.nextInt(100);
        int randAttrVal = 25;
        System.out.println("Enter File name of testing data set: ");
        String testFileName = sc.nextLine();

        if (testFileName == null || testFileName.length() == 0) {

            testFileName = fileName;
        }

        RandomForestBoosting randomForest = new RandomForestBoosting(trees, fileName, randAttrVal);
        double[][] dataMatrix = randomForest.readDataSet(path, fileName);
        double[][] testMatrix = randomForest.readDataSet(path, testFileName);
        List<DecisionNode> randomTrees = randomForest.runTreeInductionAlgo(dataMatrix, trees, randAttrVal);
        if (testFileName != null && testFileName.length() > 0) {
            randomForest.validateRandomForest(testMatrix, randomTrees);
        } else {
            randomForest.validateRandomForest(dataMatrix, randomTrees);
        }
        //double[][] distanceMatrix = KNN.calculateDistanceMatrix();

        /*Arrays.stream(matrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/

/*        Arrays.stream(distanceMatrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/


    }
}
