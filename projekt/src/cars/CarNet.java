package cars;

import java.io.Serializable;

import som.net.SOMNet;

public class CarNet implements Serializable {
	private static final long serialVersionUID = 5565781144528244945L;
	public SOMNet net;
	public int[] map;
	public Car[] carDb;
	public double[][] attrDb;
}
