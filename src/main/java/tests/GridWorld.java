package tests;


import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Random;

public class GridWorld {

    enum Obj {
        PIT,
        WALL,
        GOAL,
        PLAYER
    }

    enum Action {
        TOP,
        RIGHT,
        DOWN,
        LEFT
    }

    private static MultiLayerNetwork initNet(int iterations) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12165115614545L)
            .iterations(iterations)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .weightInit(WeightInit.RELU)
            .list()
            .layer(0, new DenseLayer.Builder()
                .nIn(64)
                .nOut(150)
                .activation("relu")
                .build())
            .layer(1, new DenseLayer.Builder()
                .nIn(150)
                .nOut(150)
                .activation("relu")
                .build())
            .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                .activation("identity")
                .nIn(150)
                .nOut(4)
                .build())
            .pretrain(false)
            .backprop(true)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }

    private static double[] initGrid() {
        int[][][] state = new int[4][4][4];
        state[2][0][Obj.PIT.ordinal()] = 1;
        state[1][1][Obj.WALL.ordinal()] = 1;
        state[2][2][Obj.GOAL.ordinal()] = 1;
        state[0][0][Obj.PLAYER.ordinal()] = 1;

        double[] r = new double[4*4*4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    r[16*i + 4*j + k] = state[i][j][k];
                }
            }
        }

        return r;
    }

    private static double[] predict(MultiLayerNetwork net, double[] state) {
        INDArray o = net.output(Nd4j.create(state));
        double[] r = new double[4];
        for (int i = 0; i < o.length(); i++) {
            r[i] = o.getDouble(i);
        }

        return r;
    }

    private static int argmax (double [] elems) {
        int bestIdx = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < elems.length; i++) {
            double elem = elems[i];
            if (elem > max) {
                max = elem;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private static double[] makeMove(double[] state, Action action) {
        switch (action) {
            case TOP:

                break;
            case RIGHT:
                break;
            case DOWN:
                break;
            case LEFT:
                break;
        }
    }

    public static void main(String[] args) {

        MultiLayerNetwork net = initNet(10);
        Random rnd = new Random();
        int epochs = 1000;
        double gamma = 0.9;
        double epsilon = 1;

        for (int e = 0; e < epochs; e++) {
            double[] state = initGrid();
            boolean status = true;

            while (status) {
                double[] qval = predict(net, state);
                Action action;
                if (rnd.nextDouble() < epsilon) {
                    action = Action.values()[rnd.nextInt(4)];
                } else {
                    action = Action.values()[argmax(qval)];
                }
            }

            if (epsilon > 0.1) {
                epsilon -= 1/epochs;
            }
        }





       rnd.nextDouble();







        System.out.println(o);
    }

}

