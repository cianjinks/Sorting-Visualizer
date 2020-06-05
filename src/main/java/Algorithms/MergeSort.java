package Algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class MergeSort {

    private ArrayList<ArrayList<int[]>> simulation = new ArrayList<>();
    private int[] data;
    private HashMap<String, Integer> colors = new HashMap<>();

    public MergeSort(int[] data) {
        this.data = data;
        colors.put("green", 0);
        colors.put("yellow", 1);
        colors.put("purple", 2);
        colors.put("red", 3);
    }

    public void sort(int left, int right) {
        if(left < right) {
            int middle = (left + right) / 2;
            sort(left, middle);
            sort(middle + 1, right);

            merge(left, middle, right);
        }
    }

    private void merge(int left, int middle, int right) {

        int n1 = (middle - left + 1);
        int n2 = (right - middle);

        int[] leftArray = new int[n1];
        int[] rightArray = new int[n2];

        for (int i = 0; i < n1; i++) {
            leftArray[i] = data[left + i];
        }
        for (int j = 0; j < n2; j++) {
            rightArray[j] = data[middle + j + 1];
        }

        int i = 0, j = 0;
        int k = left;
        int totalSub = rightArray.length + leftArray.length;
        while(i < n1 && j < n2) {
            if(leftArray[i] <= rightArray[j]) {
                data[k] = leftArray[i];
                simulation.add(generateFrame(k, totalSub));
                i++;
            } else {
                data[k] = rightArray[j];
                simulation.add(generateFrame(k, totalSub));
                j++;
            }
            k++;
        }

        while(i < n1) {
            data[k] = leftArray[i];
            simulation.add(generateFrame(k, totalSub));
            i++;
            k++;
        }
        while(j < n2) {
            data[k] = rightArray[j];
            simulation.add(generateFrame(k, totalSub));
            j++;
            k++;
        }
    }

    public ArrayList<ArrayList<int[]>> getSimulation() {
        return simulation;
    }

    private ArrayList<int[]> generateFrame(int completed) {
        ArrayList<int[]> frame = new ArrayList<>();
        for(int i = 0; i < data.length; i++) {
            if(i < completed) {
                frame.add(new int[]{data[i], colors.get("purple")});
            } else {
                frame.add(new int[]{data[i], colors.get("green")});
            }
        }
        return frame;
    }

    private ArrayList<int[]> generateFrame(int i, int totalSub) {
        ArrayList<int[]> frame;
        if(totalSub == data.length) {
            frame = generateFrame(i - 1);
        } else {
            frame = generateFrame(0);
        }
        frame.set(i, new int[] {frame.get(i)[0], colors.get("yellow")});
        return frame;
    }

    public void complete() {
        simulation.add(generateCompletedFrame());
    }

    private ArrayList<int[]> generateCompletedFrame() {
        ArrayList<int[]> frame = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            frame.add(new int[]{data[i], colors.get("purple")});
        }
        return frame;
    }
}
