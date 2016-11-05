package fuoco;

import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;


public class FuocoCore implements Core {
    public Action computeAction(SensorModel a) {
        return new Action();
    }

    public void loadGenome(IGenome genome) {
    }

    public IGenome getGenome() {
        return new FuocoCoreGenome();
    }
}
