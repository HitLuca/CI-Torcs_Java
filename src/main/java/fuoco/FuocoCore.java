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

    private static double sigmoid(double x) {
        return (1/( 1 + Math.pow(Math.E,(-1*x))));
    }

    public Action computeAction(SensorModel sensors) {
        Action action = new Action();
        double[] d = new double[29];

        d[0] = sensors.getAngleToTrackAxis()/3.1416;
        for (int i = 1; i < 20; i++) {
            d[i] = sensors.getTrackEdgeSensors()[i - 1]/200.0;
        }
        d[20] = sensors.getTrackPosition();
        d[21] = sensors.getSpeed()/300.0;
        d[22] = sensors.getLateralSpeed()/300.0;
        d[23] = sensors.getZSpeed()/300.0;
        for (int i = 24; i < 28; i++) {
            d[i] = sensors.getWheelSpinVelocity()[i - 24]/100.0;
        }
        d[28] = sensors.getRPM()/10000.0;

        INDArray predicted = net.output(Nd4j.create(d));

        action.steering = Math.tanh(predicted.getDouble(0));
        action.accelerate = sigmoid(predicted.getDouble(1));
        action.brake = sigmoid(predicted.getDouble(2));

        return action;
    }

    public void loadGenome(IGenome genome) {
        net = ((FuocoCoreGenome) genome).getNet();
    }

    public IGenome getGenome() throws IOException {
        return null;
    }
}
