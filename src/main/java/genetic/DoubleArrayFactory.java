package genetic;

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import java.util.Random;

/**
 * Created by luca on 11/7/16.
 */
public class DoubleArrayFactory extends AbstractCandidateFactory<double[]> {
    private int arraySize;
    private int min;
    private int max;

    public DoubleArrayFactory(int arraySize, int min, int max) {
        this.arraySize = arraySize;
        this.min = min;
        this.max = max;
    }

    public double[] generateRandomCandidate(Random rng) {
        double[] candidate = new double[arraySize];

        for (int i = 0; i < candidate.length; i++) {
            candidate[i] = min + (max - min) * rng.nextDouble();
        }
        return candidate;
    }
}
