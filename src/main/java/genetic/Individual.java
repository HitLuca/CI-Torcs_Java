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
    private int maxOffset = 100;
    private float minMulFactor = 0;
    private float maxMulFactor = 2;

    private List<Object> genome;

    private int networksNumber = 4; // TODO get it to be a parameter

    public Individual() {
        genome = new ArrayList<>();
        int max = 2 * networksNumber + 5;

        for (int i = 0; i < 2 * networksNumber; i++) {
            genome.add(0.0);
        }
        genome.add(false); // Automatic gear
        genome.add(false); // ABS
        genome.add(false); // Use min on acceleration calculation (otherwise mean)

        genome.add(0);
        genome.add(0f);
    }

    public Individual(Random rng) {
        List<Object> steeringWeights = createNetworkWeights(rng, networksNumber);
        List<Object> accelerationWeights = createNetworkWeights(rng, networksNumber);

        genome = new ArrayList<>();

        genome.addAll(steeringWeights); // Steering weights
        genome.addAll(accelerationWeights); // Acceleration weights

        genome.add(rng.nextBoolean()); // Automatic gear
        genome.add(rng.nextBoolean()); // ABS
        genome.add(rng.nextBoolean()); // Use min on acceleration calculation (otherwise mean)

        genome.add(rng.nextInt(maxOffset - minOffset) + minOffset);
        genome.add(rng.nextFloat() * (maxMulFactor - minMulFactor) + minMulFactor);
        normalizeNetworkWeights();
    }

    private List<Object> createNetworkWeights(Random rng, int networksNumber) {
        double[] weights = new double[networksNumber];
        List<Object> elements = new ArrayList<>();

        double min = 1;
        double max = 0;

        for (int i=0; i<networksNumber; i++) {
            double weight = rng.nextDouble();
            if (weight > max) {
                max = weight;
            }
            if (weight < min) {
                min = weight;
            }
            weights[i] = weight;
        }

        for (int i=0; i<networksNumber; i++) {
            weights[i] = (weights[i] - min) / (max-min);
            elements.add(weights[i]);
        }

        return elements;
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
        normalizeNetworkWeights();
        evaluated = false;
    }

    private void normalizeNetworkWeights() {
        normalizeWeights(0);
        normalizeWeights(networksNumber);
    }

    private void normalizeWeights(int index) {
        double sum = 0;

        for (int i=0; i<networksNumber; i++) {
            sum += (double) genome.get(i+index);
        }

        if (sum == 0) {
            sum = 1;
        }

        for (int i=0; i<networksNumber; i++) {
            double newValue = (double)genome.get(i+index)/ sum;
            if (Double.isNaN(newValue)) {
                System.err.println("Nan encountered");
                System.err.println(newValue);
                System.err.println(sum);
                for (int j=0; j<networksNumber; j++) {
                    System.err.println(genome.get(j+index));
                }
            }
            genome.set(i+index, newValue);
        }
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
        normalizeNetworkWeights();
        evaluated = false;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
        this.evaluated = true;
    }

    public double[] getSteeringWeights() {
        double[] steeringWeights = new double[networksNumber];
        for (int i=0; i<networksNumber; i++) {
            steeringWeights[i] = (double) genome.get(i);
        }
        return steeringWeights;
    }

    public double[] getAccelerationWeights() {
        double[] accelerationWeights = new double[networksNumber];
        int offset = networksNumber;

        for (int i=0; i<networksNumber; i++) {
            accelerationWeights[i] = (double) genome.get(i + offset);
        }
        return accelerationWeights;
    }

    public boolean getAutomaticGear() {
        return (boolean) genome.get(2 * networksNumber);
    }

    public boolean getABS() {
        return (boolean) genome.get(2 * networksNumber + 1);
    }

    public int getOffset() {
        return (int) genome.get(2 * networksNumber + 3);
    }

    public float getMultFactor() {
        return (float) genome.get(2 * networksNumber + 4);
    }

    public boolean getMin() {
        return (boolean) genome.get(2 * networksNumber + 2);
    }

    public boolean isEvaluated() {
        return evaluated;
    }
}
