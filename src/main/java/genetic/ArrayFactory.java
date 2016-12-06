package genetic;

import org.uncommons.watchmaker.framework.CandidateFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by luca on 12/5/16.
 */
public class ArrayFactory implements CandidateFactory<Individual> {
    public ArrayFactory() {
        super();
    }

    @Override
    public List<Individual> generateInitialPopulation(int populationSize, Random rng) {
        List<Individual> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            population.add(generateRandomCandidate(rng));
        }
        return population;
    }

    @Override
    public List<Individual> generateInitialPopulation(int populationSize, Collection<Individual> seedCandidates, Random rng) {
        List<Individual> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            population.add(generateRandomCandidate(rng));
        }
        return population;
    }

    @Override
    public Individual generateRandomCandidate(Random rng) {
        Individual candidate = new Individual(rng);
        return candidate;
    }
}
