package Algorithms;

import java.util.ArrayList;
import java.util.HashMap;

public class QuickSort {

    /**
     * Implements Quick Sort choosing last element as pivot
     */

    private ArrayList<ArrayList<int[]>> simulation = new ArrayList<>();
    private int[] data;
    private int[] sorted;
    private int low;
    private int high;
    private HashMap<String, Integer> colors = new HashMap<>();

    public QuickSort(int[] data) {
        this.data = data;
        this.low = 0;
        this.high = data.length-1;

        // Copy data to sorted then sort
        sorted = new int[data.length];
        System.arraycopy(data, 0, sorted, 0, data.length);
        BubbleSort bs = new BubbleSort(sorted);
        bs.sort();

        colors.put("green", 0);
        colors.put("yellow", 1);
        colors.put("purple", 2);
        colors.put("red", 3);
    }

    public void sort(int low, int high) {
        if(low < high) {
            int pindex = partition(low, high);
            sort(low, pindex - 1);
            sort(pindex + 1, high);
        }
    }

    private int partition(int low, int high) {
        int pivot = data[high];
        int i  = low - 1;

        for(int j = low; j <= high-1; j++) {
            if(data[j] < pivot) {
                i++;
                int tmp = data[i];
                data[i] = data[j];
                data[j] = tmp;
            }

            // TEST
            ArrayList<int[]> frame = generateFrame();
            if(i >= 0) {
                for(int loop = i + 1; loop <= j -1 ; loop++) {
                    frame.set(loop, new int[]{frame.get(loop)[0], colors.get("yellow")});
                }
                frame.set(i, new int[]{frame.get(i)[0], colors.get("red")});
                frame.set(j, new int[]{frame.get(j)[0], colors.get("red")});
                simulation.add(frame);
            }

        }
        int tmp = data[i + 1];
        data[i + 1] = data[high];
        data[high] = tmp;

        ArrayList<int[]> frame = generateFrame();
        frame.set(i + 1, new int[]{frame.get(i + 1)[0], colors.get("red")});
        frame.set(high, new int[]{frame.get(high)[0], colors.get("red")});
        simulation.add(frame);
        return (i + 1);
    }

    public ArrayList<ArrayList<int[]>> getSimulation() {
        return simulation;
    }

    public void complete() {
        simulation.add(generateCompletedFrame());
    }

    private ArrayList<int[]> generateFrame() {
        ArrayList<int[]> frame = new ArrayList<>();
        boolean lastComplete = true;
        for (int i = 0; i < data.length; i++) {
            if(data[i] == sorted[i]) {
                if(lastComplete) {
                    frame.add(new int[]{data[i], colors.get("purple")});
                } else {
                    frame.add(new int[]{data[i], colors.get("green")});
                }
            } else {
                frame.add(new int[]{data[i], colors.get("green")});
                lastComplete = false;
            }
        }
        return frame;
    }

    private ArrayList<int[]> generateCompletedFrame() {
        ArrayList<int[]> frame = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            frame.add(new int[]{data[i], colors.get("purple")});
        }
        return frame;
    }

}
