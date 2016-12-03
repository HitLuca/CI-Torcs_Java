package fuoco;

import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import scr.Action;
import scr.SensorModel;

import javax.sound.midi.Track;
import java.io.*;
import java.util.ArrayList;

import static org.nd4j.linalg.api.ndarray.INDArray.*;


public class FuocoCore implements Core {

    private MultiLayerNetwork[] nets;
//    private PrintWriter file = new PrintWriter("sensors.csv");

    public FuocoCore() throws FileNotFoundException {
    }

    private ArrayList<INDArray> retrievePredictions(SensorModel sensors) {
        ArrayList<INDArray> predictions = new ArrayList<>(2);

        double[] steering = new double[nets.length];
        double[] accelBrake = new double[nets.length];
        for (int i = 0; i < nets.length; i++) {
            INDArray prediction = nets[i].output(sensors2INDArray(sensors));
            steering[i] = prediction.getDouble(0);
            accelBrake[i] = prediction.getDouble(1);
        }

        predictions.add(Nd4j.create(steering));
        predictions.add(Nd4j.create(accelBrake));

        return predictions;
    }

    private INDArray sensors2INDArray(SensorModel sensors) {
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

        return Nd4j.create(d);
    }

    public Action computeAction(SensorModel sensors) {
        Action action = new Action();

        ArrayList<INDArray> predictions = retrievePredictions(sensors);
        double destination = predictions.get(0).meanNumber().doubleValue() * 0.9;

        action.steering = DriversUtils.moveTowardsTrackPosition(sensors, 1, destination);
        double speed = (predictions.get(1).minNumber().doubleValue() + 1) * 0.5;

        if (speed > sensors.getSpeed()) {
            action.brake = 1.0;
        } else {
            action.accelerate = 1.0;
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
