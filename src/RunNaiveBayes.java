import java.util.Scanner;

public class RunNaiveBayes {

    public static void main ( String args[] ) {

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

        NaiveBayes nbHandler = new NaiveBayes(fileName, foldNumber);
        double[][] featureMatrix = nbHandler.readFeatureValues(path, fileName);
        double[][] testMatrix = nbHandler.readFeatureValues(path, testFileName);
        nbHandler.startNaiveBayes(featureMatrix, testMatrix);



        //double [][] normalizedMatrix = nbHandler.prepareFeatureMatrix(featureMatrix);
        //List<NaiveBayes.MeanVariance>
        /*Arrays.stream(normalizedMatrix).forEach(x -> {
            System.out.println(Arrays.toString(x));
        });*/





    }

}
