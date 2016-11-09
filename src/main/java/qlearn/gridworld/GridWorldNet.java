package qlearn.gridworld;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import qlearn.NeuralNet;
import qlearn.Settings;

import java.io.IOException;
import java.util.Random;

public class GridWorldNet extends NeuralNet<GridState, GridState.Move> {

    public GridWorldNet(String filename) throws IOException {
        super(filename);
    }

    public GridWorldNet(int iterations) {
        super(new NeuralNetConfiguration.Builder()
                .iterations(iterations)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .learningRateDecayPolicy(LearningRatePolicy.Score)
                .lrPolicyDecayRate(0.8)
                .learningRate(Settings.learningRate)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(Settings.gridX*Settings.gridY*Settings.elements)
                        .nOut(Settings.hiddenNeurons)
                        .activation("sigmoid")
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(Settings.hiddenNeurons)
                        .nOut(Settings.hiddenNeurons)
                        .activation("sigmoid")
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                        .activation("identity")
                        .nIn(Settings.hiddenNeurons)
                        .nOut(4)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build());
    }

    public GridState.Move[] predictActions(GridState state) {
        double[] qVals = predict(state);
        GridState.Move[] actions = new GridState.Move[qVals.length];

        for(int i = 0; i < qVals.length; i++) {
            int amax = argmax(qVals);
            actions[i] = GridState.Move.values()[amax];
            qVals[amax] = Double.NEGATIVE_INFINITY;
        }

        return actions;
    }

    public GridState.Move predictBestAction(GridState state) {
        return GridState.Move.values()[argmax(predict(state))];
    }

    public GridState.Move predictRandomAction() {
        return GridState.Move.values()[new Random().nextInt(GridState.Move.values().length)];
    }

}
