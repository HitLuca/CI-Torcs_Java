package fuoco;

import cicontest.torcs.genome.IGenome;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import scr.Action;
import scr.SensorModel;

import java.io.IOException;


public class FuocoCore implements Core {

    private MultiLayerNetwork net;

    public Action computeAction(SensorModel sensors) {
        Action action = new Action();
        double[] d = new double[22];

        d[0] = sensors.getSpeed();
        d[1] = sensors.getTrackPosition();
        d[2] = sensors.getAngleToTrackAxis();

        for (int i = 3; i < d.length; i++) {
            d[i] = sensors.getTrackEdgeSensors()[i - 3];
        }

        INDArray features = Nd4j.create(d);

        INDArray predicted = net.output(features, false);

        action.accelerate = predicted.getDouble(0);
        action.brake = predicted.getDouble(1);
        action.steering = predicted.getDouble(2);
        return action;
    }

    public void loadGenome(IGenome genome) {
        net = ((FuocoCoreGenome) genome).getNet();
    }

    public IGenome getGenome() throws IOException {
        return null;
    }
}
