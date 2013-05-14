package som.net;

import static java.lang.Math.sqrt;
import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;
import static som.net.SOMNet.pow2;

import java.io.Serializable;
import java.util.Arrays;

public class Vector implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	public double[] val;

	public Vector(int size) {
		val = new double[size];
	}

	public Vector(double[] val) {
		this.val = new double[val.length];
		arraycopy(val, 0, this.val, 0, val.length);
	}

	public Vector normalize() {
		double length = length();
		if (length != 0) {
			for (int i = 0; i < val.length; i++)
				val[i] /= length;
		} else {
			fill(val, 0);
		}
		return this;
	}

	public Vector minus(Vector other) {
		for (int i = 0; i < val.length; i++)
			val[i] -= other.val[i];
		return this;
	}

	public Vector plus(Vector other) {
		for (int i = 0; i < val.length; i++)
			val[i] += other.val[i];
		return this;
	}

    @Override
	public String toString() {
		return Arrays.toString(val);
	}

	public int size() {
		return val.length;
	}

	public double length() {
		return euclideanDistance(new Vector(val.length));
	}

	public double euclideanDistance(Vector other) {
		double distance = 0;
		for (int i = 0; i < val.length; i++)
			distance += pow2(val[i] - other.val[i]);
		return sqrt(distance);
	}

	public Vector multiply(double x) {
		for (int i = 0; i < val.length; i++)
			val[i] *= x;
		return this;
	}

    @Override
	public Vector clone() {
		Vector v = new Vector(val.length);
		arraycopy(val, 0, v.val, 0, val.length);
		return v;
	}
}
