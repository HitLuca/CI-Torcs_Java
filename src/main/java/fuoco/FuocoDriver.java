package fuoco;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.race.Race;
import com.sun.java.util.jar.pack.DriverResource;
import scr.Action;
import scr.SensorModel;

import java.sql.DriverAction;
import java.sql.DriverManager;


public class FuocoDriver extends AbstractDriver {

    private Core core;
    private Action action = new Action();
    private boolean hasDamage = false;

    public FuocoDriver() {
        this.enableExtras(new AutomatedClutchGearbox());
        this.enableExtras(new AutomatedRecoveringV2());
//        this.enableExtras(new AutomatedClutch());
//        this.enableExtras(new AutomatedGearbox());
//        this.enableExtras(new AutomatedRecovering());
//        this.enableExtras(new ABS());
    }

    @Override
    public void loadGenome(IGenome genome) {
        this.core = new FuocoCore();
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
    public float[] initAngles() {
        return new float[]{-45, -19, -12, -7, -4, -2.5F, -1.7F, -1, -.5F, 0, .5F, 1, 1.7F, 2.5F, 4, 7, 12, 19, 45};
    }

    public boolean hasDamage() {
        return hasDamage;
    }

    @Override
    public Action defaultControl(Action action, SensorModel sensors) {
        action = this.core.computeAction(action, sensors);
        hasDamage = sensors.getDamage() > 0;
        return action;
    }
}