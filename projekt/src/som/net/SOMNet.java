package som.net;

import static java.lang.Math.exp;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;
import static java.lang.System.arraycopy;

import java.io.Serializable;
import java.util.Random;

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

	public void calculateDistancesToInput() {
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
	
	public double getWeight(int i, int j, int k){
		return weights[i * width + j].val[k];
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

	public void teach(double[][] trainset, double minLearningFactor, double maxLearningFactor, double minRadius, double maxRadius, int maxEpoch, boolean useGauss) {
		for (int epoch = 0; epoch < maxEpoch; epoch++) {
			shuffle(trainset);
			for (int pattern = 0; pattern < trainset.length; pattern++) {
				setInput(trainset[pattern]);
				calculateDistancesToInput();
				modifyWeights(minLearningFactor, maxLearningFactor, minRadius, maxRadius, epoch, maxEpoch, useGauss);
			}
		}
//		test(trainset);
	}
	
	public void randomizeWeights() {
		for (int i = 0; i < outputsCount; i++) {
			for (int j = 0; j < vectorLength; j++) {
				weights[i].val[j] = random();
			}
			weights[i].normalize();
//			System.out.println(i + " " + weights[i]);
		}
	}
	
	public void setWeight(int weightNo, double[] val) {
		arraycopy(val, 0, weights[weightNo].val, 0, vectorLength);
		weights[weightNo].normalize();
	}

	public void modifyWeights(double minLearningFactor, double maxLearningFactor, double minRadius, double maxRadius, int epoch, int maxEpoch, boolean useGauss) {
		int winner = winner();
		double curRadius = inTime(minRadius, maxRadius, epoch, maxEpoch);
		double curFactor = inTime(minLearningFactor, maxLearningFactor, epoch, maxEpoch);
//		System.out.println("----" + epoch + " " + curRadius + " " + curFactor + "----");
//		for(int i = 0; i < outputsCount; ++i)
//			System.out.print(input.euclideanDistance(weights[i]) + " ");
//		System.out.print("\n");
		for (int i = 0; i < outputsCount; i++) {
			if (topologicD(winner, i) <= curRadius) {
				Vector delta = input.clone().minus(weights[i]).multiply(curFactor);

				if (useGauss) {
					delta.multiply(gaussNeighborhoodFunction(winner, i, curRadius));
				}

				weights[i].plus(delta);
				weights[i].normalize();
//				System.out.println(i + " " + weights[i]);
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

	public static double pow2(double x) {
		return x * x;
	}
	
	public static void shuffle(double[][] set){
		Random rnd = new Random();
	    int mid = set.length / 2;
	    for (int i = mid; i < set.length; i++) {
	        int lo = rnd.nextInt(mid);
	        double[] buffer = set[lo];
	        set[lo] = set[i];
	        set[i] = buffer;
	    }
	}
	
	public void test(double[][] trainset){
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
