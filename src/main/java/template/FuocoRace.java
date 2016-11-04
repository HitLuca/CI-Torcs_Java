package template;

import cicontest.algorithm.abstracts.AbstractRace;

public class FuocoRace extends AbstractRace {
	
	public int[] runRace(FuocoDriverGenome[] drivers, boolean withGUI){
		int size = Math.min(10, drivers.length);
		FuocoDriver[] driversList = new FuocoDriver[size];
		for(int i=0; i<size; i++){
			driversList[i] = new FuocoDriver();
			driversList[i].loadGenome(drivers[i]);
		}
		return runRace(driversList, withGUI, true);
	}


}
