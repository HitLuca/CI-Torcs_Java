package genetic;

import cicontest.torcs.race.RaceResult;
import fuoco.FuocoDriverAlgorithm;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;

/**
 * Created by luca on 12/5/16.
 */
public class CustomEvaluator implements FitnessEvaluator<Individual> {
    private FuocoDriverAlgorithm algorithm;
    private PrintStream stdout;
    private PrintStream fakeout;

    public CustomEvaluator(FuocoDriverAlgorithm algorithm) {
        this.algorithm = algorithm;
        stdout = System.out;

        try {
            fakeout = new PrintStream("/dev/null");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    public double getFitness(Individual candidate, List<? extends Individual> population) {
        if (!candidate.isEvaluated()) {
            double fitness = 0;

            int offset = candidate.getOffset();
            float multFactor = candidate.getMultFactor();

            List<FuocoDriverAlgorithm.FuocoResults> results = null;
            try {
                results = algorithm.fitness(offset, multFactor);
            } catch (Exception e) {
                e.printStackTrace();
            }

            assert results != null;
            for (FuocoDriverAlgorithm.FuocoResults result: results) {
                RaceResult raceResult = result.res;
                fitness += raceResult.getTime();
            }
            System.setOut(stdout);
            System.out.println("Individual fitness: " + fitness);
            System.setOut(fakeout);

            candidate.setFitness(fitness);
            return fitness;
        } else {
            System.out.println("Individual fitness: " + candidate.getFitness());
            return candidate.getFitness();
        }
    }

    @Override
    public boolean isNatural() {
        return false;
    }
}
