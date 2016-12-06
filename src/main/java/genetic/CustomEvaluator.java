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

            double[] steeringWeights = candidate.getSteeringWeights();
            double[] accelerationWeights = candidate.getAccelerationWeights();

            boolean automaticGear = candidate.getAutomaticGear();
            boolean ABS = candidate.getABS();
            boolean min = candidate.getMin();

            int offset = candidate.getOffset();
            float multFactor = candidate.getMultFactor();

            List<FuocoDriverAlgorithm.FuocoResults> results = null;
            try {
                results = algorithm.fitness(steeringWeights, accelerationWeights, ABS, automaticGear, min, offset, multFactor);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (FuocoDriverAlgorithm.FuocoResults result: results) {
                RaceResult raceResult = result.res;

                if (!result.damage) {
                    if (raceResult.isFinished()) {
                        fitness += raceResult.getTime();
                    } else {
                        fitness += 2000;
                    }
                } else {
                    fitness += 1000 - raceResult.getTime();
                }
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
