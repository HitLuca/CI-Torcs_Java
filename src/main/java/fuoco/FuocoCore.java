package fuoco;

import cicontest.torcs.genome.IGenome;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;
import scr.Action;
import scr.SensorModel;

import java.io.*;

public class FuocoCore implements Core {

//    private MultiLayerNetwork[] nets;
    private NeuralNet[] nets;
    private double space_offset;
    private double brake_force;

    private Matrix input = new Matrix(new double[29][1]);
    private double[] steering;
    private double[] accelBrake;

    private void retrievePredictions(SensorModel sensors) {
        sensors2INDArray(sensors);
        for (int i = 0; i < nets.length; i++) {
            Matrix prediction = nets[i].predict(input);
            steering[i] = prediction.values[0][0];
            accelBrake[i] = prediction.values[1][0];
        }
    }

    private void sensors2INDArray(SensorModel sensors) {
        input.values[0][0] = sensors.getAngleToTrackAxis()/Math.PI;
        for (int i = 1; i < 20; i++) {
            input.values[i][0] = sensors.getTrackEdgeSensors()[i - 1]/200.0;
        }
        input.values[20][0] = sensors.getTrackPosition();
        input.values[21][0] = sensors.getSpeed()/300.0;
        input.values[22][0] = sensors.getLateralSpeed()/300.0;
        input.values[23][0] = sensors.getZSpeed()/300.0;
        for (int i = 24; i < 28; i++) {
            input.values[i][0] = sensors.getWheelSpinVelocity()[i - 24]/100.0;
        }
        input.values[28][0] = sensors.getRPM()/10000.0;
    }

    public Action computeAction(SensorModel sensors) {
        Action action = new Action();

        retrievePredictions(sensors);

        double meanSteering = 0;
        for (int i = 0; i < nets.length; i++) {
            meanSteering += steering[i];
        }
        meanSteering /= nets.length;

        double meanAccelBrake = 0;
        double accelBrakeMin = Double.MAX_VALUE;
        for (int i = 0; i < nets.length; i++) {
            meanAccelBrake += accelBrake[i];
            if (accelBrakeMin > accelBrake[i]) {
                accelBrakeMin = accelBrake[i];
            }
        }
        meanAccelBrake /= nets.length;

        action.steering = meanSteering;

        double predicted;

        double d = 0;
        if (accelBrakeMin < 0) {
            for (int i = 0; i < accelBrake.length; i++) {
                if (accelBrake[i] > 0) {
                    d += accelBrake[i] * 0.1;
                } else {
                    d += accelBrake[i] * 2;
                }
            }
            predicted = d / accelBrake.length;
        } else {
            predicted = meanAccelBrake;
        }

        if (predicted >= 0) {
            action.accelerate = predicted;
            action.brake = 0;
        } else {
            action.accelerate = 0;
            action.brake = -predicted;
        }

        if (sensors.getSpeed() > 225) {
            if (action.steering > 0) {
                action.steering = Math.pow(action.steering, 4);
            } else {
                action.steering = -Math.pow(action.steering, 4);
            }
        } else if (sensors.getSpeed() > 150) {
            if (action.steering > 0) {
                action.steering = Math.pow(action.steering, 2);
            } else {
                action.steering = -Math.pow(action.steering, 2);
            }
        }

        if (sensors.getSpeed() < 10) {
            action.accelerate = 1D;
            action.brake = 0D;
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
        space_offset = ((FuocoCoreGenome) genome).getSpace_offset();
        brake_force = ((FuocoCoreGenome) genome).getBrake_force();

        steering = new double[nets.length];
        accelBrake = new double[nets.length];
    }

    public IGenome getGenome() throws IOException {
        return null;
    }
}
