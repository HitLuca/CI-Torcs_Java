package genetic;

import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by luca on 12/5/16.
 */
public class IndividualCrossover extends AbstractCrossover<Individual> {
    public IndividualCrossover(int crossoverPoints) {
        super(crossoverPoints);
    }

    @Override
    protected List<Individual> mate(Individual parent1, Individual parent2, int numberOfCrossoverPoints, Random rng) {
        List<Individual> offsprings = new ArrayList<>();
        List<Integer> crossoverPoints = new ArrayList<>();
        for (int i = 0; i < numberOfCrossoverPoints; i++) {
            crossoverPoints.add(rng.nextInt(parent1.getGenomeLength()));
        }
        Collections.sort(crossoverPoints);

        int end = parent1.getGenomeLength();

        Individual offspring1 = new Individual();
        Individual offspring2 = new Individual();

        offspring1.setGenome(0, crossoverPoints.get(0), parent1.getGenome());
        offspring2.setGenome(0, crossoverPoints.get(0), parent2.getGenome());

        boolean parentSwitch = false; // Used for parent switch when copying
        // data

        for (int i = 0; i < numberOfCrossoverPoints - 1; i++) {
            if (parentSwitch) {
                offspring1.setGenome(crossoverPoints.get(i), crossoverPoints.get(i + 1), parent1.getGenome());
                offspring2.setGenome(crossoverPoints.get(i), crossoverPoints.get(i + 1), parent2.getGenome());
            } else {
                offspring1.setGenome(crossoverPoints.get(i), crossoverPoints.get(i + 1), parent2.getGenome());
                offspring2.setGenome(crossoverPoints.get(i), crossoverPoints.get(i + 1), parent1.getGenome());
            }
            parentSwitch = !parentSwitch;
        }

        if (parentSwitch) {
            offspring1.setGenome(crossoverPoints.get(crossoverPoints.size() - 1), end, parent1.getGenome());
            offspring2.setGenome(crossoverPoints.get(crossoverPoints.size() - 1), end, parent2.getGenome());
        } else {
            offspring1.setGenome(crossoverPoints.get(crossoverPoints.size() - 1), end, parent2.getGenome());
            offspring2.setGenome(crossoverPoints.get(crossoverPoints.size() - 1), end, parent1.getGenome());
        }

        offsprings.add(offspring1);
        offsprings.add(offspring2);

        return offsprings;
    }
}
