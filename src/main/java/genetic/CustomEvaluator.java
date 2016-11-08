package genetic;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by luca on 11/7/16.
 */
public class CustomEvaluator implements FitnessEvaluator<double[]> {
    private ClassificationProblem problem;
    private MultiLayerConfiguration configuration;

    public CustomEvaluator() {
        problem = new ClassificationProblem(Settings.dataPoints, Settings.classes); //Points, classes
        configuration = configureNN(Settings.learning_rate, Settings.classes);
        //saveNetworkConfiguration();
    }

    private static MultiLayerConfiguration configureNN(double learning_rate, int classes) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12165115614545L)
                .learningRate(learning_rate)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(2)
                        .nOut(10)
                        .activation("sigmoid")
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(10)
                        .nOut(10)
                        .activation("sigmoid")
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                        .nIn(10)
                        .nOut(classes)
                        .activation("softmax")
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();
        return conf;
    }

    private void saveNetworkConfiguration() {
        try {
            FileUtils.writeStringToFile(new File("output/configuration.json"), configuration.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MultiLayerConfiguration loadConfiguration() {
        MultiLayerConfiguration configuration = null;
        try {
            configuration = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File("output/configuration.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    @Override
    public double getFitness(double[] candidate, List<? extends double[]> list) {
        //configuration = loadConfiguration();
        MultiLayerNetwork net = new MultiLayerNetwork(configuration);
        INDArray weights = Nd4j.create(candidate);
        net.init();
        net.setParams(weights);

        Evaluation eval = new Evaluation(4);
        List<Pair<INDArray, INDArray>> dataset = problem.getData();

        for (Pair pair : dataset) {
            INDArray output = net.output((INDArray) pair.getKey());
            eval.eval((INDArray) pair.getValue(), output);
        }
        return eval.accuracy();
    }

    @Override
    public boolean isNatural() {
        return true;
    }
}
