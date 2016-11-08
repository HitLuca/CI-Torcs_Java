package qlearn;


import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.List;

public abstract class NeuralNet<S extends State<E>, E extends Enum> implements FitablePredictable<S, E> {

    private MultiLayerNetwork net;

    public NeuralNet(String filename) throws IOException {
        net = ModelSerializer.restoreMultiLayerNetwork(filename);
    }

    public NeuralNet(MultiLayerConfiguration configuration) {
        net = new MultiLayerNetwork(configuration);
        net.init();
    }

    public void fit(List<S> states, List<double[]> qVals) {
        double[][] statesD = new double[states.size()][];
        double[][] qValsD = new double[states.size()][];

        for(int i = 0; i < states.size(); i++) {
            statesD[i] = states.get(i).getValues();
            qValsD[i] = qVals.get(i);
        }

        net.fit(new DataSet(Nd4j.create(statesD), Nd4j.create(qValsD)));
    }

    public double[] predict(S state) {
        INDArray o = net.output(Nd4j.create(state.getValues()));
        double[] r = new double[o.length()];
        for (int i = 0; i < o.length(); i++) {
            r[i] = o.getDouble(i);
        }
        return r;
    }

    public void save(String filename) throws IOException {
        ModelSerializer.writeModel(net, filename, true);
    }

    protected static int argmax(double[] elems) {
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
}
