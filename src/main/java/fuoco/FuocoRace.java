package fuoco;

import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.torcs.genome.IGenome;

public class FuocoRace extends AbstractRace {
	
	public int[] runRace(IGenome[] drivers, boolean withGUI){
		int size = Math.min(10, drivers.length);
		FuocoDriver[] driversList = new FuocoDriver[size];
		for(int i=0; i<size; i++){
			driversList[i] = new FuocoDriver();
			driversList[i].loadGenome(drivers[i]);
		}
		return runRace(driversList, withGUI, true);
	}
}
