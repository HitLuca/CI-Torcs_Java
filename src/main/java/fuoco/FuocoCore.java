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
    private double lastClutch = 0;

    private void retrievePredictions(SensorModel sensors) {
        sensors2Matrix(sensors);
        for (int i = 0; i < nets.length; i++) {
            Matrix prediction = nets[i].predict(input);
            steering[i] = prediction.values[0][0];
            accelBrake[i] = prediction.values[1][0];
        }
    }

    private void sensors2Matrix(SensorModel sensors) {
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

    private void meanSteering(Action action) {
        double meanSteering = 0;
        for (int i = 0; i < nets.length; i++) {
            meanSteering += steering[i];
        }
        meanSteering /= nets.length;
        action.steering = meanSteering;
    }

    private double minAccelBrake() {
        double accelBrakeMin = Double.MAX_VALUE;
        for (int i = 0; i < accelBrake.length; i++) {
            if (accelBrakeMin > accelBrake[i]) {
                accelBrakeMin = accelBrake[i];
            }
        }
        return accelBrakeMin;
    }

    private double meanAccelBrake() {
        double meanAccelBrake = 0;
        for (int i = 0; i < accelBrake.length; i++) {
            meanAccelBrake += accelBrake[i];
        }
        return meanAccelBrake / nets.length;
    }


    private void minAccelBrake (Action action) {
        double accelBrakeMin = minAccelBrake();

        if (accelBrakeMin >= 0) {
            action.accelerate = accelBrakeMin;
            action.brake = 0;
        } else {
            action.accelerate = 0;
            action.brake = -accelBrakeMin;
        }
    }

    private void accelBrake(Action action) {
        double meanAccelBrake = meanAccelBrake();
        double accelBrakeMin = minAccelBrake();

        if (accelBrakeMin < 0) {
            double d = 0;
            for (int i = 0; i < accelBrake.length; i++) {
                if (accelBrake[i] > 0) {
                    d += accelBrake[i] * 0.1;
                } else {
                    d += accelBrake[i] * 2;
                }
            }
            accelBrakeMin = d / accelBrake.length;
        } else {
            accelBrakeMin = meanAccelBrake;
        }

        if (accelBrakeMin >= 0) {
            action.accelerate = accelBrakeMin;
            action.brake = 0;
        } else {
            action.accelerate = 0;
            action.brake = -accelBrakeMin;
        }
    }

    private void speedwaysSteeringHelp(Action action, SensorModel sensors) {
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
    }

    private void automatedGearbox(Action action, SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();
        action.gear = gear;
        if(gear < 1) {
            action.gear = 1;
        } else if(gear < 6 && rpm >= (double)this.gearUp[gear - 1]) {
            action.gear = gear + 1;
            lastClutch = 1;
        } else {
            if(gear > 1 && rpm <= (double)this.gearDown[gear - 1]) {
                action.gear = gear - 1;
                lastClutch = 1;
            }
        }
    }

    private double maxTrackEdgeSteering(SensorModel sensors) {
        int max = 0;
        for(int i = 0; i < 19; i++) {
            if (sensors.getTrackEdgeSensors()[i] > sensors.getTrackEdgeSensors()[max]) {
                max = i;
            }
        }
        return (9 - max) / 9.0;
    }

    private void brakeSpace(Action action, SensorModel sensors, double speed) {
        if (sensors.getSpeed() > speed) {
            double space = 0.000851898 * Math.pow(sensors.getSpeed(), 2)
                    + 0.104532 * sensors.getSpeed()
                    - 2.03841;

            if (sensors.getTrackEdgeSensors()[9] < space + space_offset) {
                action.accelerate = 0;
                action.brake *= brake_force;
                action.steering = action.steering * 0.8 + 0.2 * maxTrackEdgeSteering(sensors);
            }
        }
    }

    private void noStuck(Action action, SensorModel sensors, double speed) {
        if (sensors.getSpeed() < speed) {
            action.accelerate = 1.0;
            action.brake = 0.0;
            action.steering = action.steering * 0.8 + 0.2 * maxTrackEdgeSteering(sensors);
        }
    }

    private void pushAccel(Action action, SensorModel sensors, double distance) {
        if (sensors.getTrackEdgeSensors()[9] > distance / 200.0) {
            action.accelerate = 1.0;
            action.brake = 0.0;
        }
    }

//    private void accelBrakeHelp(Action action, SensorModel sensors) {
//        if (sensors.getSpeed() < 20) {
//            action.accelerate = 1.0;
//            action.brake = 0.0;
//            action.steering = action.steering * 0.8 + 0.2 * maxTrackEdgeSteering(sensors);
//        } else {
//            double space = 0.000851898 * Math.pow(sensors.getSpeed(), 2) + 0.104532 * sensors.getSpeed() - 2.03841;
//
//            if (sensors.getTrackEdgeSensors()[9] < space + space_offset) {
//                action.accelerate = 0;
//                action.brake *= brake_force;
//                action.steering = action.steering * 0.8 + 0.2 * maxTrackEdgeSteering(sensors);
//            } else if (sensors.getTrackEdgeSensors()[9] > 0.3) {
//                action.accelerate = 1.0;
//                action.brake = 0.0;
//            }
//        }
//    }

    private int[] behind = new int[]{2, 1, 0, 35, 34};
    private int[] left = new int[]{7, 8, 9, 10, 11};
    private int[] front = new int[]{16, 17, 18, 19, 20};
    private int[] right = new int[]{25, 26, 27, 28, 29};


    private boolean close(SensorModel sensors, int[] dir, double threshold) {
        for(int i = 0; i < dir.length; i++) {
            if (sensors.getOpponentSensors()[dir[i]] < threshold) {
                return true;
            }
        }
        return false;
    }

    private void opponentsCare(Action action, SensorModel sensors) {
        boolean behind = close(sensors, this.behind, 5);
        boolean left = close(sensors, this.left, 5);
        boolean front = close(sensors, this.front, 5);
        boolean right = close(sensors, this.right, 5);

        if (front && sensors.getSpeed() > 5) {
            action.accelerate = 0;
            action.brake += 0.1;
        }

        if (left) {
            if (action.steering > 0.1) {
                action.accelerate = 1.0;
                action.brake = 0;
                action.steering *= 2; //= maxTrackEdgeSteering(sensors);
            }
        }
        if (right) {
            if (action.steering < 0.1) {
                action.accelerate = 1.0;
                action.brake = 0;
                action.steering *= 2; //= maxTrackEdgeSteering(sensors);
            }
        }
//        if (front) {
//            action.accelerate -= 0.5;
//        }
//        if (behind) {
//            action.accelerate += 0.2;
//        }
    }

    private void speedLim(Action action, SensorModel sensors, double speed) {
        if (sensors.getSpeed() > speed) {
            action.accelerate = 0;
        }
    }

    private void recover(Action action, SensorModel sensors) {
        if(sensors.getSpeed() < 5.0 && sensors.getDistanceFromStartLine() > 0.0) {
            ++this.stuckstill;
        }

        if(Math.abs(sensors.getAngleToTrackAxis()) > 0.5235987901687622) {
            if(this.stuck > 0 || Math.abs(sensors.getTrackPosition()) > 0.85) {
                ++this.stuck;
            }
        } else if(this.stuck > 0 && Math.abs(sensors.getAngleToTrackAxis()) < 0.3) {
            this.stuck = 0;
            this.stuckstill = 0;
        }

        if(this.stuckstill > 50) {
            this.stuck = 26;
        }

        if(this.stuck > 25) {
            action.accelerate = 1.0;
            action.brake = 0.0;
            action.gear = -1;
            action.steering = -1.0;
            if(sensors.getAngleToTrackAxis() < 0.0) {
                action.steering = 1.0;
            }

            if(sensors.getTrackEdgeSensors()[9] > 5.0 || sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0.0D) {
                action.gear = 1;
                if(sensors.getSpeed() < -0.2) {
                    action.brake = 1.0;
                    action.accelerate = 0.0;
                }
                this.stuck = 0;
                this.stuckstill = 0;
            }

            if(sensors.getSpeed() > 0.0) {
                action.steering = -action.steering;
            }
        }
    }
    public void automatedClutch(Action action, SensorModel sensors){
        double clutch = lastClutch;
        float maxClutch = 0.5F;
        if(sensors.getDistanceRaced() < 10.0) {
            clutch = (double)maxClutch;
        }
        if(clutch > 0.0) {
            double delta = 0.05000000074505806;
            if(sensors.getGear() < 2) {
                delta /= 2.0;
                maxClutch *= 1.3;
                if(sensors.getCurrentLapTime() < 1.5) {
                    clutch = (double)maxClutch;
                }
            }
            clutch = Math.min((double)maxClutch, clutch);
            if(clutch != (double)maxClutch) {
                clutch -= delta;
                clutch = Math.max(0.0, clutch);
            } else {
                clutch -= 0.009999999776482582;
            }
        }
        action.clutch = clutch;
        lastClutch = clutch;
    }

    private void superSafe(Action action, SensorModel sensors) {
        action.accelerate *= 0.7;
        action.brake *= 1.5;
        action.gear = 1;
        if (sensors.getZSpeed() > 5) {
            action.accelerate = 0.1;
        }
    }

    public Action computeAction(Action action, SensorModel sensors) {
        retrievePredictions(sensors);
        meanSteering(action);
        minAccelBrake(action);
        noStuck(action, sensors, 5);
        brakeSpace(action, sensors, 20);
        speedLim(action, sensors, 65);
        superSafe(action,sensors);
        recover(action, sensors);

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
