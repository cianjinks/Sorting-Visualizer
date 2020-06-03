package Algorithms;

import java.util.ArrayList;
import java.util.HashMap;

public class BubbleSort {

    private ArrayList<ArrayList<int[]>> simulation = new ArrayList<>();
    private int[] data;
    private HashMap<String, Integer> colors = new HashMap<>();

    public BubbleSort(int[] data) {
        this.data = data;
        colors.put("green", 0);
        colors.put("yellow", 1);
        colors.put("purple", 2);
    }

    public void simulate() {
        for(int i = 0; i < data.length - 1; i++) {
            for(int j = 0; j < data.length - i - 1; j++) {
                if(data[j] > data[j + 1]) {
                    int tmp = data[j];
                    data[j] = data[j + 1];
                    data[j + 1] = tmp;
                }
                ArrayList<int[]> frame = generateFrame(i);
                frame.set(j, new int[]{frame.get(j)[0], colors.get("yellow")});
                frame.set(j + 1, new int[]{frame.get(j)[0], colors.get("yellow")});
                simulation.add(frame);
            }
        }
        simulation.add(generateFrame(data.length));
    }

    public ArrayList<ArrayList<int[]>> getSimulation() {
        return simulation;
    }

    private ArrayList<int[]> generateFrame(int completed) {
        ArrayList<int[]> frame = new ArrayList<>();
        for(int i = 0; i < data.length; i++) {
            if(i >= data.length - completed) {
                frame.add(new int[]{data[i], colors.get("purple")});
            } else {
                frame.add(new int[]{data[i], colors.get("green")});
            }
        }
        return frame;
    }
}
