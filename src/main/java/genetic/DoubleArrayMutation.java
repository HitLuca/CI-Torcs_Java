package genetic;

import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by luca on 11/7/16.
 */
public class DoubleArrayMutation implements EvolutionaryOperator<double[]> {
    Probability probability;

    public DoubleArrayMutation(Probability probability) {
        this.probability = probability;
    }

    public List<double[]> apply(final List<double[]> selectedCandidates, Random rng) {
        List<double[]> results = new ArrayList<double[]>();
        for (int i = 0; i < selectedCandidates.size(); i++) {
            results.add(i, selectedCandidates.get(i).clone());
        }

        for (double[] candidate : results) {
            if (probability.nextEvent(rng)) {
                int dimension = rng.nextInt(candidate.length);
                candidate[dimension] += rng.nextGaussian();
            }
        }
        return results;
    }
}
