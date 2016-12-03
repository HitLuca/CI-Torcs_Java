package fuoco;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

import java.io.FileNotFoundException;

public class FuocoDriver extends AbstractDriver {

    private Core core;
    private Action action = new Action();

    public FuocoDriver() {
        this.enableExtras(new AutomatedClutch());
        this.enableExtras(new AutomatedGearbox());
        this.enableExtras(new AutomatedRecovering());
        this.enableExtras(new ABS());
    }

    @Override
    public void loadGenome(IGenome genome) {
        if (genome instanceof FuocoCoreGenome) {
            try {
                core = new FuocoCore();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            core = new DefaultCore();
        }

        core.loadGenome(genome);
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
    public float[] initAngles() {
        return new float[]{-45, -19, -12, -7, -4, -2.5F, -1.7F, -1, -.5F, 0, .5F, 1, 1.7F, 2.5F, 4, 7, 12, 19, 45};
    }

    @Override
    public Action defaultControl(Action action, SensorModel sensors) {

        action = this.core.computeAction(sensors);

        System.out.println("--------------" + getDriverName() + "--------------");
        System.out.println("Steering: " + action.steering);
        System.out.println("Acceleration: " + action.accelerate);
        System.out.println("Brake: " + action.brake);
        System.out.println("-----------------------------------------------");


        return action;
    }
}