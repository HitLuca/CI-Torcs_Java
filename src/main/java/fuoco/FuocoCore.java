package fuoco;

import cicontest.torcs.genome.IGenome;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import scr.Action;
import scr.SensorModel;

import java.io.IOException;


public class FuocoCore implements Core {

    private MultiLayerNetwork[] nets;

//    private static double sigmoid(double x) {
//        return (1/( 1 + Math.pow(Math.E,(-1*x))));
//    }

    private double[] computeAction(INDArray sensors) {
        double[] predictions = new double[]{0, 0};
        double[] steering = new double[nets.length];
        double[] accelBrake = new double[nets.length];

        for (int i = 0; i < nets.length; i++) {
            INDArray prediction = nets[i].output(sensors);
            steering[i] = prediction.getDouble(0);
            accelBrake[i] = prediction.getDouble(1);
        }

        predictions[0] = Nd4j.create(steering).mean().getDouble(0);
        predictions[1] = Nd4j.create(accelBrake).min().getDouble(0);

        return predictions;
    }

    public Action computeAction(SensorModel sensors) {
        Action action = new Action();
        double[] d = new double[29];

        d[0] = sensors.getAngleToTrackAxis()/Math.PI;
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

        double[] predicted = computeAction(Nd4j.create(d));

        action.steering = predicted[0];


        if (predicted[1] >= 0) {
            action.accelerate = predicted[1];
            action.brake = 0;
        } else {
            action.accelerate = 0;
            action.brake = -predicted[1];
        }

        return action;
    }

    public void loadGenome(IGenome genome) {
        nets = ((FuocoCoreGenome) genome).getNets();
    }

    public IGenome getGenome() throws IOException {
        return null;
    }
}
