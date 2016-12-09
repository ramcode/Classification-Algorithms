package com.ub.cse601.project3.util;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class CrossValidation {
    public double[][] dataSet;
    public int kFold;
    List<double[][]> kSplits = new ArrayList<>();


    public CrossValidation() {

    }

    public CrossValidation(double[][] dataSet, int kFold) {
        this.dataSet = dataSet;
        this.kFold = kFold;
    }

    public List<Object[]> generateKFoldSplit(double[][] dataSet, int splits) {
        List<Object[]> splitList = new ArrayList<>();
        int splitSize = dataSet.length / splits;
        int testSplitStartIndex = 0;
        for (int k = 0; k < splits; k++) {
            Object[] split = new Object[2];
            double[][] testData = new double[splitSize][dataSet[0].length];
            double[][] trainData = new double[dataSet.length - splitSize][dataSet[0].length];
            for (int i = 0, x = 0, y = 0; i < dataSet.length; i++) {
                if (i >= testSplitStartIndex && i < testSplitStartIndex + splitSize) {
                    testData[x] = dataSet[i];
                    x++;
                } else {
                    trainData[y] = dataSet[i];
                    y++;
                }
            }
            split[0] = trainData;
            split[1] = testData;
            splitList.add(split);
            testSplitStartIndex += splitSize;
        }
        return splitList;
    }

    public static Object[] generatePartitionsForSplit(double[][] trainData, int splitIndex) {
        Object[] partitions = new Object[2];
        RealMatrix rm = MatrixUtils.createRealMatrix(trainData);
        partitions[0] = rm.getSubMatrix(0, splitIndex, 0, trainData[0].length - 1).getData();
        partitions[1] = rm.getSubMatrix(splitIndex + 1, trainData.length - 1, 0, trainData[0].length - 1).getData();
        return partitions;
    }

    public double[][] getNormalizedMatrix(double[][] dataMatrix, List<Integer> ignoreList) {
        RealMatrix rm = MatrixUtils.createRealMatrix(dataMatrix);
        double[][] normalizedData = new double[dataMatrix.length][dataMatrix[0].length];
        RealMatrix normalizedMatrix = MatrixUtils.createRealMatrix(normalizedData);
        //exclude last 2 columns
        //double[][] tempMatrix = rm.getSubMatrix(0, dataMatrix.length - 1, 0, dataMatrix[0].length - 3).getData();

        for (int i = 0; i < dataMatrix[0].length - 2; i++) {

            if (!ignoreList.contains(i)) {
                normalizedMatrix.setColumn(i, rm.getColumn(i));
                double min = StatUtils.min(normalizedMatrix.getColumn(i));
                double max = StatUtils.max(normalizedMatrix.getColumn(i));
                for (int j = 0; j < normalizedMatrix.getColumn(i).length; j++) {
                    double x = normalizedMatrix.getEntry(j, i);
                    x = (x - min) / (max - min);
                    normalizedMatrix.setEntry(j, i, x);

                }

            }
            normalizedMatrix.setColumn(dataMatrix[0].length - 2, rm.getColumn(dataMatrix[0].length - 2));
            normalizedMatrix.setColumn(dataMatrix[0].length - 1, rm.getColumn(dataMatrix[0].length - 1));
        }
        return normalizedMatrix.getData();
    }
}
