package fuoco;

import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import com.sun.org.apache.bcel.internal.generic.FADD;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import scr.Action;
import scr.SensorModel;

import java.io.*;
import java.util.ArrayList;

import static org.nd4j.linalg.api.ndarray.INDArray.*;


public class FuocoCore implements Core {

    private MultiLayerNetwork[] nets;
    private int[] gearUp = new int[]{9000, 8500, 8500, 8000, 8000, 0};
    private int[] gearDown = new int[]{0, 3500, 4000, 4000, 4500, 4500};
    private int gear = 0;
    private double last_rmp = 0;
    private int change_gear = 3;

    private double[] steeringWeights;
    private double[] accelBrakegWeights;

    private boolean AutomatedGearbox;
    private boolean min;
    private double space_offset;
    private double brake_force;

    public FuocoCore() {
    }

    private ArrayList<INDArray> retrievePredictions(SensorModel sensors) {
        ArrayList<INDArray> predictions = new ArrayList<>(2);
        double[] steering = new double[nets.length];
        double[] accelBrake = new double[nets.length];
        for (int i = 0; i < nets.length; i++) {
            INDArray prediction = nets[i].output(sensors2INDArray(sensors));
            steering[i] = prediction.getDouble(0) * steeringWeights[i];
            accelBrake[i] = prediction.getDouble(1) * accelBrakegWeights[i];
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

        action.steering = predictions.get(0).meanNumber().doubleValue();

        double predicted;

        double d = 0;
        if (predictions.get(1).minNumber().doubleValue() < 0) {
            for (int i = 0; i < predictions.get(0).size(1); i++) {
                if (predictions.get(1).getDouble(i) > 0) {
                    d += predictions.get(1).getDouble(i) * 0.1;
                } else {
                    d += predictions.get(1).getDouble(i) * 2;
                }
            }
            predicted = d / predictions.get(1).size(1);
        } else {
            if (min) {
                predicted = predictions.get(1).minNumber().doubleValue();
            } else {
                predicted = predictions.get(1).meanNumber().doubleValue();
            }
        }

        if (predicted >= 0) {
            action.accelerate = predicted;
            action.brake = 0;
        } else {
            action.accelerate = 0;
            action.brake = -predicted;
        }

        if (!AutomatedGearbox) {
            if (change_gear < 3) {
                last_rmp = 5000;
                change_gear++;
            } else {
                change_gear = 0;
                last_rmp = sensors.getRPM();
            }

            if(gear < 1) {
                gear = 1;
            } else if(gear < 6 && last_rmp >= (double)this.gearUp[gear - 1]) {
                gear = gear + 1;
            } else {
                if(gear > 1 && last_rmp <= (double)this.gearDown[gear - 1]) {
                    gear = gear - 1;
                }
            }

            action.gear = gear;
        }

        double space = 0.000851898 * Math.pow(sensors.getSpeed(), 2) + 0.104532 * sensors.getSpeed() - 2.03841;

        if (sensors.getTrackEdgeSensors()[9] < space + space_offset) {
            action.accelerate = 0;
            action.brake *= brake_force;
        }

        return action;
    }

    public void loadGenome(IGenome genome) {
        nets = ((FuocoCoreGenome) genome).getNets();
        steeringWeights = ((FuocoCoreGenome) genome).getSteeringWeights();
        accelBrakegWeights = ((FuocoCoreGenome) genome).getAccelBrakegWeights();
        AutomatedGearbox = ((FuocoCoreGenome) genome).getAutomatedGearbox();
        min = ((FuocoCoreGenome) genome).getMin();
        space_offset = ((FuocoCoreGenome) genome).getSpace_offset();
        brake_force = ((FuocoCoreGenome) genome).getBrake_force();
    }

    public IGenome getGenome() throws IOException {
        return null;
    }
}
