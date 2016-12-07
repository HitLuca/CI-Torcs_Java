package genetic;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

import fuoco.FuocoDriverAlgorithm;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;
/**
 * Created by luca on 12/5/16.
 */


public class EvolutionLogger implements EvolutionObserver<Individual> {
    private PrintWriter out;
    private PrintStream stdout;
    private PrintStream fakeout;

    public EvolutionLogger() {
        super();
        stdout = System.out;

        try {
            fakeout = new PrintStream("/dev/null");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(fakeout);

        try {
            out = new PrintWriter("evolved_parameters.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void populationUpdate(PopulationData<? extends Individual> data) {
        System.setOut(stdout);

        System.out.println();
        System.out.println("Generation " + data.getGenerationNumber());
        System.out.println("Best fitness: " + data.getBestCandidateFitness());
        System.out.println("Mean fitness: " + data.getMeanFitness());
        System.out.println("Elapsed time: " + data.getElapsedTime());
        System.out.println();

        System.setOut(fakeout);

        out.println("Generation " + data.getGenerationNumber());
        out.println("Best fitness: " + data.getBestCandidateFitness());
        out.println("Mean fitness: " + data.getMeanFitness());
        out.println("Elapsed time: " + data.getElapsedTime());
        out.println();

        Individual best = data.getBestCandidate();

        double[] steeringWeights = best.getSteeringWeights();
        double[] accelerationWeights = best.getAccelerationWeights();

//        boolean automaticGear = best.getAutomaticGear();
//        boolean ABS = best.getABS();
//        boolean min = best.getMin();

        int offset = best.getOffset();
        float multFactor = best.getMultFactor();

        out.println("Steering weights");
        for (int i=0; i<steeringWeights.length; i++) {
            out.println(steeringWeights[i]);
        }
        out.println("Acceleration weights");
        for (int i=0; i<accelerationWeights.length; i++) {
            out.println(accelerationWeights[i]);
        }
//        out.println("Automatic gear");
//        out.println(automaticGear);
//        out.println("ABS");
//        out.println(ABS);
//        out.println("Min");
//        out.println(min);
        out.println("Offset");
        out.println(offset);
        out.println("Mult factor");
        out.println(multFactor);
        out.println();
        out.println();
        out.println();
        out.flush();
    }
}
