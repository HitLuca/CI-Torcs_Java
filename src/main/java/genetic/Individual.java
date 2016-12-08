package genetic;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by luca on 12/5/16.
 */
public class Individual {
    private double fitness;
    private boolean evaluated = false;

    private int minOffset = 0;
    private int maxOffset = 80;
    private float minMulFactor = 1;
    private float maxMulFactor = 2;

    private List<Object> genome;


    public Individual() {
        genome = new ArrayList<>();

        genome.add(0); // Offset
        genome.add(0f); // Brake multiplicative factor
    }

    public Individual(Random rng) {
        genome = new ArrayList<>();

        genome.add(rng.nextInt(maxOffset - minOffset) + minOffset); // Offset
        genome.add(rng.nextFloat() * (maxMulFactor - minMulFactor) + minMulFactor); // Brake multiplicative factor
    }

    public double getFitness() {
        return fitness;
    }

    public void applyMutation(int index, double mutation) {
        Object element = genome.get(index);

        if (element instanceof Float) {
            float newValue = (float) ((float)genome.get(index) + mutation * (maxMulFactor - minMulFactor) + minMulFactor);
            if (newValue > maxMulFactor) {
                newValue = maxMulFactor;
            }
            if (newValue < minMulFactor) {
                newValue = minMulFactor;
            }
            genome.set(index, newValue);
        }
        else if (element instanceof Double) { //Network weight
            double newValue = (double) genome.get(index) + mutation;
            if (newValue > 1) {
                newValue = 1;
            }
            if (newValue < 0) {
                newValue = 0;
            }
            genome.set(index, newValue);
        } else if (element instanceof Boolean) {
            if (mutation >= 0.5) {
                genome.set(index, !(boolean) element);
            }
        } else if (element instanceof Integer) {
            int newValue = (int) ((int)genome.get(index) + mutation * (maxOffset - minOffset) + minOffset);
            if (newValue > maxOffset) {
                newValue = maxOffset;
            }
            if (newValue < minOffset) {
                newValue = minOffset;
            }
            genome.set(index, newValue);
        } else {
            System.out.println("Not implemented...");
        }
        evaluated = false;
    }

    public int getGenomeLength() {
        return genome.size();
    }

    public List<Object> getGenome() {
        return genome;
    }

    public void setGenome(int start, int end, List<Object> data) {
        for (int i = start; i < end; i++) {
            genome.set(i, data.get(i));
        }
        evaluated = false;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
        this.evaluated = true;
    }

    public int getOffset() {
        return (int) genome.get(0);
    }

    public float getMultFactor() {
        return (float) genome.get(1);
    }


    public boolean isEvaluated() {
        return evaluated;
    }
}
