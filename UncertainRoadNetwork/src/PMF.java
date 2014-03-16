import java.util.HashMap;
import java.util.Map.Entry;


public class PMF {
	public int min;
	public int max;
	public HashMap<Integer, Double> prob;
	
	public PMF(int min, int max) {
		this.min = min;
		this.max = max;
		this.prob = new HashMap<Integer, Double>();
	}
	
	public PMF(double[] inputs) {
		this.min = Integer.MAX_VALUE;
		this.max = Integer.MIN_VALUE;
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
		int totalCount = 0;
		for (double input : inputs) {
			int newValue = (int) input;
			this.min = (this.min > newValue) ? newValue : this.min;
			this.max = (this.max < newValue) ? newValue : this.max;
			counts.put(newValue, counts.get(newValue) + 1);
			totalCount++;
		}
		this.prob = new HashMap<Integer, Double>();
		for (Entry<Integer, Integer> e : counts.entrySet()) {
			double newProb = (double)e.getValue()/totalCount;
			this.prob.put(e.getKey(), newProb);
		}
	}
	
	public PMF Add(PMF other) {
		PMF retPMF = new PMF(this.min + other.min, this.max + other.max);
		for (int h = retPMF.min; h <= retPMF.max; h++) {
			double sum = 0.0;
			for (int g = 0; g <= h; g++) {
				sum += this.prob.get(g) * other.prob.get(h - g);
			}
			retPMF.prob.put(h, sum);
		}
		return retPMF;
	}
}
