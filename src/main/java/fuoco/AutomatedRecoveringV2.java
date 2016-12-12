package fuoco;

import cicontest.torcs.controller.extras.IExtra;
import scr.Action;
import scr.SensorModel;

public class

AutomatedRecoveringV2 implements IExtra {

    private int stuck = 0;
    private int stuckstill = 0;

    @Override
    public void process(Action action, SensorModel sensors) {
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

        if(this.stuckstill > 50) {
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

            if(sensors.getTrackEdgeSensors()[9] > 5.0D || sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0.0D) {
                action.gear = 1;
                if(sensors.getSpeed() < -0.2D) {
                    action.brake = 1.0D;
                    action.accelerate = 0.0D;
                }
                this.stuck = 0;
                this.stuckstill = 0;
            }

            if(sensors.getSpeed() > 0.0D) {
                action.steering = -action.steering;
            }
        }
    }

    @Override
    public void reset() {
        this.stuck = 0;
        this.stuckstill = 0;
    }
}
