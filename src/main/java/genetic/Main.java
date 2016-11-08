package genetic;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.DoubleArrayCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.Stagnation;
import org.uncommons.watchmaker.swing.evolutionmonitor.EvolutionMonitor;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by luca on 11/7/16.
 */
public class Main {
    public static void main(String[] args) {
        CandidateFactory<double[]> candidateFactory = new DoubleArrayFactory(Settings.totalParameters, Settings.minWeight, Settings.maxWeight);
        List<EvolutionaryOperator<double[]>> operators = new LinkedList<EvolutionaryOperator<double[]>>();
        operators.add(new DoubleArrayMutation(new Probability(Settings.mutationProbability)));
        operators.add(new DoubleArrayCrossover(Settings.crossoverPoints));

        EvolutionaryOperator<double[]> pipeline = new EvolutionPipeline<double[]>(operators);

        FitnessEvaluator<double[]> fitnessEvaluator = new CustomEvaluator();
        SelectionStrategy<Object> selection = new RouletteWheelSelection();
        Random rng = new MersenneTwisterRNG();

        EvolutionEngine<double[]> engine = new GenerationalEvolutionEngine<double[]>(candidateFactory,
                pipeline, fitnessEvaluator, selection, rng);

        engine.addEvolutionObserver(new EvolutionObserver<double[]>() {
            @Override
            public void populationUpdate(PopulationData<? extends double[]> populationData) {
                System.out.println("Generation " + populationData.getGenerationNumber());
                System.out.println("Best fitness: " + populationData.getBestCandidateFitness());
                System.out.println("Mean fitness: " + populationData.getMeanFitness());
                System.out.println();
            }
        });

        EvolutionMonitor<double[]> monitor = new EvolutionMonitor<double[]>();

        engine.addEvolutionObserver(monitor);

        JFrame frame = new JFrame("Evolution monitor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(monitor.getGUIComponent());
        frame.pack();
        frame.setVisible(true);

        double[] result = engine.evolve(Settings.individuals, Settings.elites, new Stagnation(Settings.stagnation, true));
    }
}
