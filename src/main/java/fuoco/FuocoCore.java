package fuoco;

import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

import java.io.*;

public class FuocoCore implements Core {

    private NeuralNet[] nets;
    private double space_offset;
    private double brake_force;

    private Matrix input = new Matrix(new double[29][1]);
    private double[] steering;
    private double[] accelBrake;

    private int stuck = 0;
    private int stuckstill = 0;

    private int[] gearUp = new int[]{9000, 8500, 8500, 8000, 8000, 0};
    private int[] gearDown = new int[]{0, 3500, 4000, 4000, 4500, 4500};

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

    public Action computeAction(Action action, SensorModel sensors) {

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

        int max = 0;
        for(int i = 0; i < 19; i++) {
            if (sensors.getTrackEdgeSensors()[i] > sensors.getTrackEdgeSensors()[max]) {
                max = i;
            }
        }

        if (sensors.getSpeed() < 20) {
            action.accelerate = 1.0;
            action.brake = 0.0;
            action.steering = action.steering * 0.8 + 0.2 * (9 - max) / 9.0;
        } else {
            double space = 0.000851898 * Math.pow(sensors.getSpeed(), 2) + 0.104532 * sensors.getSpeed() - 2.03841;

            if (sensors.getTrackEdgeSensors()[9] < space + space_offset) {
                action.accelerate = 0;
                action.brake *= brake_force;
                action.steering = action.steering * 0.8 + 0.2 * (9 - max) / 9.0;
            } else if (sensors.getTrackEdgeSensors()[9] > 0.3) {
                action.accelerate = 1.0;
                action.brake = 0.0;
            }
        }

        int gear = sensors.getGear();
        double rpm = sensors.getRPM();
        action.gear = gear;
        if(gear < 1) {
            action.gear = 1;
        } else if(gear < 6 && rpm >= (double)this.gearUp[gear - 1]) {
            action.gear = gear + 1;
        } else {
            if(gear > 1 && rpm <= (double)this.gearDown[gear - 1]) {
                action.gear = gear - 1;
            }
        }

        if(sensors.getSpeed() < 5.0D && sensors.getDistanceFromStartLine() > 0.0D) {
            ++this.stuckstill;
        }

        if(Math.abs(sensors.getAngleToTrackAxis()) > 0.5235987901687622D) {
            if(this.stuck > 0 || Math.abs(sensors.getTrackPosition()) > 0.85D) {
                ++this.stuck;
            }
        } else if(this.stuck > 0 && Math.abs(sensors.getAngleToTrackAxis()) < 0.3D) {
            this.stuck = 0;
            this.stuckstill = 0;
        }

        if(this.stuckstill > 40) {
            this.stuck = 26;
        }

        if(this.stuck > 25) {
            action.accelerate = 0.7D;
            action.brake = 0.0D;
            action.gear = -1;
            action.steering = -1.0D;
            if(sensors.getAngleToTrackAxis() < 0.0D) {
                action.steering = 1.0D;
            }

            if(sensors.getTrackEdgeSensors()[9] > 3.0D || sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0.0D) {
                action.gear = 1;
                if(sensors.getSpeed() < -0.2D) {
                    action.brake = 1.0D;
                    action.accelerate = 0.0D;
                }
            }

            if(sensors.getSpeed() > 0.0D) {
                action.steering = -action.steering;
            }
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
