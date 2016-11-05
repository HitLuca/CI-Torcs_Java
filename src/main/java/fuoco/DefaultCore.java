package fuoco;

import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

public class DefaultCore implements Core {
    public Action computeAction(SensorModel sensors) {
        Action action = new Action();
        action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
        if (sensors.getSpeed() > 60.0D) {
            action.accelerate = 0.0D;
            action.brake = 0.0D;
        }

        if (sensors.getSpeed() > 70.0D) {
            action.accelerate = 0.0D;
            action.brake = -1.0D;
        }

        if (sensors.getSpeed() <= 60.0D) {
            action.accelerate = (80.0D - sensors.getSpeed()) / 80.0D;
            action.brake = 0.0D;
        }

        if (sensors.getSpeed() < 30.0D) {
            action.accelerate = 1.0D;
            action.brake = 0.0D;
        }

        return action;
    }

    public void loadGenome(IGenome genome) {

    }

    public IGenome getGenome() {
        return new DefaultCoreGenome();
    }
}
