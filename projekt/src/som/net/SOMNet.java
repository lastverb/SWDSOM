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
//		input.normalize();
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

	public double[][] teach(double[][] trainset, double minLearningFactor, double maxLearningFactor, double minRadius, double maxRadius, int maxEpoch, boolean useGauss) {
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
		test(trainset);
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
//			weights[i].normalize();
			System.out.println(i + " " + weights[i]);
		}
	}
	
	public void setWeight(int weightNo, double[] val) {
		arraycopy(val, 0, weights[weightNo].val, 0, vectorLength);
//		weights[weightNo].normalize();
	}

	public void modifyWeights(double minLearningFactor, double maxLearningFactor, double minRadius, double maxRadius, int epoch, int maxEpoch, boolean useGauss) {
		int winner = winner();
		double curRadius = inTime(minRadius, maxRadius, epoch, maxEpoch);
		double curFactor = inTime(minLearningFactor, maxLearningFactor, epoch, maxEpoch);
		System.out.println("----" + epoch + " " + curRadius + " " + curFactor + "----");
		for(int i = 0; i < outputsCount; ++i)
			System.out.print(input.euclideanDistance(weights[i]) + " ");
		System.out.print("\n");
		for (int i = 0; i < outputsCount; i++) {
			if (topologicD(winner, i) <= curRadius) {
				Vector delta = input.clone().minus(weights[i]).multiply(curFactor);

				if (useGauss) {
					delta.multiply(gaussNeighborhoodFunction(winner, i, curRadius));
				}

				weights[i].plus(delta);
//				weights[i].normalize();
				System.out.println(i + " " + weights[i]);
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

	public static double inTime(double minV, double maxV, double curT, double maxT) {
		return minV + (maxV-minV) * (maxT - curT)/(maxT);
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
	
	private void test(double[][] trainset){
		System.out.println("----- PO UCZENIU -----");
		Vector w = new Vector(17);
		w.val[0] = 0.5733333333333334;
		w.val[1] = 0.8636363636363636;
		w.val[2] = 0.032;
		w.val[3] = 0.0;
		w.val[4] = 0.6666666666666666;
		w.val[5] = 0.33244444444444443;
		w.val[6] = 1.0;
		w.val[7] = 1.0;
		w.val[8] = 1.0;
		w.val[9] = 1.0;
		w.val[10] = 1.0;
		w.val[11] = 1.0;
		w.val[12] = 1.0;
		w.val[13] = 1.0;
		w.val[14] = 0.0;
		w.val[15] = 1.0;
		w.val[16] = 0.0;
		
		System.out.println("-- Kia Carens --");
		setInput(trainset[0]);
		for(int i = 0; i < 4; ++i)
			System.out.print(input.euclideanDistance(weights[i]) + " ");
		System.out.print("\n");
		
		System.out.println("-- Mitsubishi Outlander --");
		setInput(trainset[1]);
		for(int i = 0; i < 4; ++i)
			System.out.print(input.euclideanDistance(weights[i]) + " ");
		System.out.print("\n");
		
		System.out.println("-- Chevrolet Captiva --");
		for(int i = 0; i < 4; ++i)
			System.out.print(w.euclideanDistance(weights[i]) + " ");
		System.out.print("\n");
	}
}
