package Algorithms;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectionSort {

    private ArrayList<ArrayList<int[]>> simulation = new ArrayList<>();
    private int[] data;
    private HashMap<String, Integer> colors = new HashMap<>();

    public SelectionSort(int[] data) {
        this.data = data;
        colors.put("green", 0);
        colors.put("yellow", 1);
        colors.put("purple", 2);
    }

    public void sort() {
        for(int i = 0; i < data.length - 1; i++) {

            int minimumIndex = i;
            for (int j = i+1; j < data.length; j++) {
                if (data[j] < data[minimumIndex]) {
                    minimumIndex = j;
                }
                ArrayList<int[]> frame = generateFrame(i);
                frame.set(j, new int[]{frame.get(j)[0], colors.get("yellow")});
                frame.set(minimumIndex, new int[]{frame.get(minimumIndex)[0], colors.get("yellow")});
                simulation.add(frame);
            }

            int tmp = data[minimumIndex];
            data[minimumIndex] = data[i];
            data[i] = tmp;
        }
        simulation.add(generateFrame(data.length));
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

}
