package genetic;

import fuoco.FuocoDriverAlgorithm;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.SigmaScaling;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.Stagnation;
import org.uncommons.watchmaker.swing.evolutionmonitor.EvolutionMonitor;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luca on 12/5/16.
 */
public class EvolveDriver {
    private static EvolutionEngine<Individual> engine;
    private static EvolutionLogger logger;
    private static FuocoDriverAlgorithm algorithm;
    private static PrintStream stdout;

    public static void main(String[] args) {
        stdout = System.out;

        algorithm = new FuocoDriverAlgorithm();
        algorithm.setTracks();
        setupGeneticAlgorithm(algorithm);
        Individual result = engine.evolve(10, 1, new Stagnation(15, false));

        System.setOut(stdout);
        System.out.println();
        System.out.println("END");
        System.out.println("Best candidate fitness:" + result.getFitness());
        System.out.println();
    }

    private static void setupGeneticAlgorithm(FuocoDriverAlgorithm algorithm) {
        CandidateFactory<Individual> candidateFactory = new ArrayFactory();

        List<EvolutionaryOperator<Individual>> operators = new ArrayList<>(2);
        operators.add(new IndividualCrossover(1));
        operators.add(new IndividualMutation(new Probability(0.1), new Probability(0.1)));

        EvolutionaryOperator<Individual> evolutionaryOperator = new EvolutionPipeline<>(operators);

        @SuppressWarnings("rawtypes")
        FitnessEvaluator<Individual> fitnessEvaluator = new CustomEvaluator(algorithm);

        MersenneTwisterRNG rng = new MersenneTwisterRNG();

        engine = new GenerationalEvolutionEngine<>(candidateFactory,
                evolutionaryOperator, fitnessEvaluator, new SigmaScaling(), rng);

        ((GenerationalEvolutionEngine)engine).setSingleThreaded(true);

        EvolutionMonitor<Individual> monitor = new EvolutionMonitor<>();
        logger = new EvolutionLogger();

        engine.addEvolutionObserver(logger);
        engine.addEvolutionObserver(monitor);

        JFrame frame = new JFrame("Evolution monitor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(monitor.getGUIComponent());
        frame.pack();
        frame.setVisible(true);
    }
}
