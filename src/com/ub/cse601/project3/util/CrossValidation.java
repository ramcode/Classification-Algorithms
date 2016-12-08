package com.ub.cse601.project3.util;

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
    public CrossValidation(double[][] dataSet, int kFold){
        this.dataSet = dataSet;
        this.kFold = kFold;
    }

    public List<Object[]> generateKFoldSplit(double[][] dataSet, int splits){
        List<Object[]> splitList = new ArrayList<>();
        int splitSize = dataSet.length/splits;
        int testSplitStartIndex = 0;
        for(int k=0; k<splits; k++){
            Object[] split = new Object[2];
            double[][] testData = new double[splitSize][dataSet[0].length];
            double[][] trainData = new double[dataSet.length-splitSize][dataSet[0].length];
            for(int i=0; i<dataSet.length; i++){
                if(i>=testSplitStartIndex && i<testSplitStartIndex+splitSize){
                    testData[i] = dataSet[i];
                }
                else{
                    trainData[i] = dataSet[i];
                }
            }
            split[0] = trainData;
            split[1] = testData;
            splitList.add(split);
            testSplitStartIndex+= splitSize;
        }
        return splitList;
    }
}
