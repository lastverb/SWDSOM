package cars;

import java.io.Serializable;
import java.util.ArrayList;

import som.net.SOMNet;

public class CarNet implements Serializable {
	private static final long serialVersionUID = 5565781144528244945L;
	public SOMNet net;
	public int[] winnerCarForNeuron;
	public ArrayList<ArrayList<Integer>> winnerCars;
	public Car[] carDb;
	public double[][] attrDb;
}
