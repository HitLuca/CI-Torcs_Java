package template;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

public class FuocoDriver extends AbstractDriver {

    private Core core = new Core();
    private Action action = new Action();

    public FuocoDriver() {
        initialize();
    }

    private void initialize() {
        this.enableExtras(new AutomatedClutch());
        this.enableExtras(new AutomatedGearbox());
        this.enableExtras(new AutomatedRecovering());
        this.enableExtras(new ABS());
    }

    @Override
    public void loadGenome(IGenome genome) {
        this.core.loadGenome(genome);
    }

    @Override
    public double getAcceleration(SensorModel sensors) {
        return action.accelerate;
    }

    @Override
    public double getSteering(SensorModel sensors) {
        return action.steering;
    }

    @Override
    public String getDriverName() {
        return "Fuoco";
    }

    @Override
    public Action controlWarmUp(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlQualification(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlRace(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action defaultControl(Action action, SensorModel sensors) {
//        if (action == null) {
//            action = new Action();
//        }
//        action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
//        if (sensors.getSpeed() > 60.0D) {
//            action.accelerate = 0.0D;
//            action.brake = 0.0D;
//        }
//
//        if (sensors.getSpeed() > 70.0D) {
//            action.accelerate = 0.0D;
//            action.brake = -1.0D;
//        }
//
//        if (sensors.getSpeed() <= 60.0D) {
//            action.accelerate = (80.0D - sensors.getSpeed()) / 80.0D;
//            action.brake = 0.0D;
//        }
//
//        if (sensors.getSpeed() < 30.0D) {
//            action.accelerate = 1.0D;
//            action.brake = 0.0D;
//        }

        action = this.core.computeAction(sensors);

        System.out.println("--------------" + getDriverName() + "--------------");
        System.out.println("Steering: " + action.steering);
        System.out.println("Acceleration: " + action.accelerate);
        System.out.println("Brake: " + action.brake);
        System.out.println("-----------------------------------------------");


        return action;
    }
}