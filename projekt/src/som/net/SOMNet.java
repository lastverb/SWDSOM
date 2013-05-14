package som.net;

import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;
import static java.lang.System.arraycopy;

import java.io.Serializable;

public class SOMNet implements Serializable {
	private static final long serialVersionUID = 1L;
	public int width;
	public int height;
	public int outputsCount;
	public int vectorLength;
	public Vector input;
	public Vector[] weights;
	public double[] distances;

	public SOMNet(int width, int height, int inputs) {
		this.width = width;
		this.height = height;
		outputsCount = width * height;
		vectorLength = inputs;
		this.input = new Vector(vectorLength);
		this.distances = new double[outputsCount];

		weights = new Vector[outputsCount];
		for (int i = 0; i < outputsCount; i++)
			weights[i] = new Vector(vectorLength);
	}

	public void setInput(double[] vector) {
		arraycopy(vector, 0, input.val, 0, vectorLength);
		input.normalize();
	}

	public void calculate() {
		for (int i = 0; i < outputsCount; i++) {
			distances[i] = input.euclideanDistance(weights[i]);
		}
	}

	public int winner() {
		int winner = 0;
		for (int i = 1; i < outputsCount; i++)
			if (distances[i] < distances[winner])
				winner = i;
		return winner;
	}
	
	public int looser() {
		int looser = 0;
		for (int i = 1; i < outputsCount; i++)
			if (distances[i] > distances[looser])
				looser = i;
		return looser;
	}
	
	public double weightDistance(int i, int j) {
		return weights[i].euclideanDistance(weights[j]);
	}

	public double[][][] getWeightsMap() {
		double[][][] map = new double[width][height][vectorLength];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int outputNo = i * width + j;
				arraycopy(weights[outputNo].val, 0, map[i][j], 0, vectorLength);
			}
		}

		return map;
	}

	public int[] winners(int winnersCount) {
		int[] winners = new int[winnersCount];
		for (int i = 0; i < outputsCount; i++) {
			for (int j = 0; j < winnersCount; j++) {
				if (distances[i] < distances[winners[j]]) {
					arraycopy(winners, j, winners, j + 1, winnersCount - j - 1);
					winners[j] = i;
					break;
				}
			}
		}
		return winners;
	}

	public double[][] teach(double[][] trainset, double minLearningFactor, double maxLearningFactor, int minRadius, int maxRadius, int maxEpoch, boolean useGauss) {
		double organizationMeasure[][] = new double[maxEpoch][2];
		for (int epoch = 0; epoch < maxEpoch; epoch++) {
			// shuffling trainset is needed?
			for (int pattern = 0; pattern < trainset.length; pattern++) {
				setInput(trainset[pattern]);
				calculate();
				modifyWeights(minLearningFactor, maxLearningFactor, minRadius, maxRadius, epoch, maxEpoch, useGauss);
			}
			organizationMeasure[epoch] = calculateOrganizationMeasure();
		}
		return organizationMeasure;
	}

	public double[] calculateOrganizationMeasure() {
		double denominator = 2 * width * height - width - height;

		double deltah = 0;
		double deltav = 0;

		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				int wij = h * width + w;
				int wij1 = wij + 1;
				int wi1j = (h + 1) * width + w;

				if (w < width - 1) {
					deltah += weights[wij].clone().minus(weights[wij1]).length();
				}
				if (h < height - 1) {
					deltav += weights[wij].clone().minus(weights[wi1j]).length();
				}
			}
		}
		double deltami = (deltah + deltav) / denominator;

		double deltah2 = 0;
		double deltav2 = 0;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				int wij = h * width + w;
				int wij1 = wij + 1;
				int wi1j = (h + 1) * width + w;

				if (w < width - 1) {
					deltah2 += pow2(weights[wij].clone().minus(weights[wij1]).length() - deltami);
				}
				if (h < height - 1) {
					deltav2 += pow2(weights[wij].clone().minus(weights[wi1j]).length() - deltami);
				}
			}
		}
		double deltasigma = sqrt((deltah2 + deltav2) / denominator);

                double[] ret=new double[2];
                ret[0]=deltasigma;
                ret[1]=deltami;
		return ret;
	}

	public void randomizeWeights() {
		for (int i = 0; i < outputsCount; i++) {
			for (int j = 0; j < vectorLength; j++) {
				weights[i].val[j] = random();
			}
			weights[i].normalize();
		}
	}
	
	public void setWeight(int weightNo, double[] val) {
		arraycopy(val, 0, weights[weightNo].val, 0, vectorLength);
		weights[weightNo].normalize();
	}

	public void modifyWeights(double minLearningFactor, double maxLearningFactor, int minRadius, int maxRadius, int epoch, int maxEpoch, boolean useGauss) {
		int winner = winner();
		double curRadius = inTime(minRadius, maxRadius, epoch, maxEpoch);
		double curFactor = inTime(minLearningFactor, maxLearningFactor, epoch, maxEpoch);
		for (int i = 0; i < outputsCount; i++) {
			if (topologicD(winner, i) <= curRadius) {
				Vector delta = input.clone().minus(weights[i]).multiply(curFactor);

				if (useGauss) {
					delta.multiply(gaussNeighborhoodFunction(winner, i, curRadius));
				}

				weights[i].plus(delta);
				weights[i].normalize();
			}
		}
	}

	public double gaussNeighborhoodFunction(int winner, int other, double radius) {
		return exp(-pow2(topologicD(winner, other)) / (2 * pow2(radius)));
	}

	public double topologicD(int winner, int other) {
		int winnerx = winner % width;
		int winnery = winner / width;
		int otherx = other % width;
		int othery = other / width;
		return sqrt(pow2(winnerx - otherx) + pow2(winnery - othery));
	}

	public double inTime(double minV, double maxV, double minT, double maxT) {
		return maxV * pow(minV / maxV, minT / maxT);
	}

	public int[] winnerPosition() {
		int[] position = new int[2];
		int winner = winner();

		position[0] = winner % width;
		position[1] = winner / width;

		return position;
	}

	public static double pow2(double x) {
		return x * x;
	}
}
