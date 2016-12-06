package genetic;

import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by luca on 12/5/16.
 */
public class IndividualMutation implements EvolutionaryOperator<Individual> {
    private Probability probability;
    private Probability singleElementProbability;

    public IndividualMutation(Probability probability, Probability singleElementProbability) {
        this.probability = probability;
        this.singleElementProbability = singleElementProbability;
    }

    @Override
    public List<Individual> apply(List<Individual> selectedCandidates, Random rng) {
        List<Individual> newCandidates = new ArrayList<>(selectedCandidates.size());
        newCandidates.addAll(selectedCandidates);

        for (int i = 0; i < newCandidates.size(); i++) {
            if (probability.nextEvent(rng)) {
                Individual mutated = newCandidates.get(i);
                int genomeLength = mutated.getGenomeLength();

                for (int dimension=0; dimension<genomeLength; dimension++) {
                    if (singleElementProbability.nextEvent(rng)) {
                        mutated.applyMutation(dimension, rng.nextGaussian());
                    }
                }

                newCandidates.set(i, mutated);
            }
        }

        return newCandidates;
    }
}
