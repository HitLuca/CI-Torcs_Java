package fuoco;

import cicontest.torcs.controller.extras.IExtra;
import scr.Action;
import scr.SensorModel;

public class AutomatedClutchGearbox implements IExtra {

    private double lastClutch = 0;

    private int[] gearUp = new int[]{9000, 8500, 8500, 8000, 8000, 0};
    private int[] gearDown = new int[]{0, 3500, 4000, 4000, 4500, 4500};

    @Override
    public void process(Action action, SensorModel sensors) {
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

    @Override
    public void reset() {
        lastClutch = 0;
    }
}
