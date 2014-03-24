package edu.imsc.UncertainRoadNetworks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class PMF {
	public int min;
	public int max;
	public HashMap<Integer, Double> prob;
	
	public PMF() {
		this.min = 0;
		this.max = 0;
		this.prob = new HashMap<Integer, Double>();
		this.prob.put(0, 1.0);		
	}
	
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
	
	public PMF(ArrayList<Double> inputs) {
		this.min = Integer.MAX_VALUE;
		this.max = Integer.MIN_VALUE;
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
		int totalCount = 0;
		for (double input : inputs) {
			int newValue = Util.RoundDouble(input);
			this.min = (this.min > newValue) ? newValue : this.min;
			this.max = (this.max < newValue) ? newValue : this.max;
			Integer prev = (counts.get(newValue) == null) ? 0 : counts.get(newValue);
			counts.put(newValue, prev + 1);
			totalCount++;
		}
		this.prob = new HashMap<Integer, Double>();
		for (Entry<Integer, Integer> e : counts.entrySet()) {
			double newProb = (double)e.getValue()/totalCount;
			this.prob.put(e.getKey(), newProb);
		}
		if (totalCount == 0) {
			this.min = 0;
			this.max = 0;
			this.prob.put(0, 1.0);
		}
	}
	
	public Double Prob(Integer k) {
		Double retVal = this.prob.get(k);
		return (retVal != null) ? retVal : 0.0;
	}
	
	public PMF Add(PMF other) {
		PMF retPMF = new PMF(this.min + other.min, this.max + other.max);
		for (int h = retPMF.min; h <= retPMF.max; h++) {
			double sum = 0.0;
			for (int g = this.min; g <= this.max; g++) {
				sum += this.Prob(g) * other.Prob(h - g);
			}
			retPMF.prob.put(h, sum);
		}
		return retPMF;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Double> e : this.prob.entrySet())
			sb.append(String.format("P(%d): %f\t", e.getKey(), e.getValue()));
		sb.append("\n");
		return sb.toString();
	}

	public PMF Interpolate(Double actualTime, Double alpha) {
		Double betha = 1 - alpha;
		int min = Util.RoundDouble((betha*this.min) + (alpha*actualTime));
		int max = Util.RoundDouble((betha*this.max) + (alpha*actualTime));
		HashMap<Integer, Double> probs = new HashMap<Integer, Double>();
		for (int i = min; i <= max; ++i)
			probs.put(i, 0.0);
		for (int i = this.min; i <= this.max; ++i) {
			int newValue = Util.RoundDouble((betha*i)+(alpha*actualTime));
			probs.put(newValue, probs.get(newValue)+this.Prob(i));
		}
		PMF retPMF = new PMF(min, max);
		for (int i = min; i <= max; ++i) {
			retPMF.prob.put(i, probs.get(i));
		}
		return retPMF;
	}
}
