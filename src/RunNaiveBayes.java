import com.ub.cse601.project3.util.CrossValidation;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class RunNaiveBayes {

    public static void main ( String args[] ) {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter File name of data set: ");
        String fileName = sc.nextLine();

        if (fileName == null || fileName.length() == 0) {

            //fileName = "Test.txt";
            fileName = "project3_dataset4.txt";
            //fileName = "project3_dataset2.txt";
            //fileName = "project3_dataset1.txt";

        }

        String path = "data/";
        System.out.println("Enter Cross Validation Fold Number: ");
        int foldNumber = Integer.valueOf(sc.nextLine());

        NaiveBayes nbHandler = new NaiveBayes(fileName, foldNumber);
        double[][] featureMatrix = nbHandler.readFeatureValues(path);
        nbHandler.startNaiveBayes(featureMatrix);



        //double [][] normalizedMatrix = nbHandler.prepareFeatureMatrix(featureMatrix);
        //List<NaiveBayes.MeanVariance>
        /*Arrays.stream(normalizedMatrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/





    }

}
