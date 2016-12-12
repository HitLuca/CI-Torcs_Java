package fuoco;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;
import java.io.*;
import java.security.cert.TrustAnchor;
import java.sql.Driver;
public class FuocoCore implements Core {
    private NeuralNet[] nets;
    private double space_offset;
    private double brake_force;
    private Matrix input = new Matrix(new double[29][1]);
    private double[] steering;
    private double[] accelBrake;
    private boolean surpassing=false;
    private double targetPos = 0.0;
    //    private int stuck = 0;
//    private int stuckstill = 0;
//    private int[] gearUp = new int[]{9000, 8500, 8500, 8000, 8000, 0};
//    private int[] gearDown = new int[]{0, 3500, 4000, 4000, 4500, 4500};
//    private double lastClutch = 0;
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
        if (sensors.getTrackEdgeSensors()[9] > distance) {
            action.accelerate = 1.0;
            action.brake = 0;
        }
    }

    private int[] behind = new int[]{2, 1, 0, 35, 34};
    private int[] left = new int[]{7, 8, 9, 10, 11};
    private int[] front = new int[]{13,14,15,16, 17, 18, 19, 20,21,22,23};
    private int[] infront = new int[]{17};
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
        boolean behind = close(sensors, this.behind, 15);
        boolean left = close(sensors, this.left, 5);
        boolean front = close(sensors, this.front, 10);
        boolean right = close(sensors, this.right, 5);
        boolean infront = close(sensors, this.infront, 10);
        double abs_steering = Math.abs(action.steering);
        double alpha = 0.2;
        System.out.println(surpassing+"  "+targetPos);
        if(infront && abs_steering < 0.2 && !surpassing){
            if(sensors.getTrackPosition()>0){
                targetPos=sensors.getTrackPosition()-1.25;
            } else {
                targetPos=sensors.getTrackPosition()+1.25;
            }
            surpassing = true;
        }
        if(!front && !left && !right){
            surpassing = false;
        }
        if(surpassing){
            double a = alpha;
            if(sensors.getSpeed()>100){
                a*=2;
            }
            action.steering = a * action.steering + (1-a) *DriversUtils.moveTowardsTrackPosition(sensors,0.7,targetPos);
        }
        if (behind && abs_steering < 0.4) {
            if (sensors.getDistanceRaced() > 50) {
                double trackPos = Math.abs(sensors.getTrackPosition());
                int myAngle = (int) (sensors.getAngleToTrackAxis() * 10);
                double[] opponents = sensors.getOpponentSensors();
                int[] behindIndexes = new int[]{3, 2, 1, 0, 35, 34, 33};
                int nearestOpponent = -1;
                double nearestOpponentDistance = 200.0;

                for (int i = 0; i < behindIndexes.length; i++) {
                    if (behindIndexes[i] != 200.0) {
                        if (opponents[i] < nearestOpponentDistance) {
                            nearestOpponent = i;
                            nearestOpponentDistance = opponents[i];
                        }
                    }
                }

                nearestOpponent += myAngle;

                if (nearestOpponent != 0 && trackPos < 0.5) {
                    if (nearestOpponent <= 3) {
                        action.steering = 0.1 * nearestOpponent;
                    } else if (nearestOpponent >= 33) {
                        action.steering = -0.1 * (36 - nearestOpponent);
                    }
                }
            }
        }
    }

    private void superSafe(Action action, SensorModel sensors) {
        action.accelerate *= 0.7;
        action.brake *= 1.5;
        if (sensors.getZSpeed() > 10) {
            action.accelerate *= 0.2;
        }
    }
    public Action computeAction(Action action, SensorModel sensors) {
        retrievePredictions(sensors);
        meanSteering(action);
        speedwaysSteeringHelp(action, sensors);
        accelBrake(action);
        noStuck(action, sensors, 20);
        pushAccel(action, sensors, 150);
        opponentsCare(action, sensors);
        brakeSpace(action, sensors, 20);
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