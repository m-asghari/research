package edu.imsc.UncertainRoadNetworks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class PMF {
	private static int binWidth = 15;
	
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
			int newValue = Util.RoundDouble(input, binWidth);
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
	}
	
	public PMF(ArrayList<Double> inputs) {
		this.min = Integer.MAX_VALUE;
		this.max = Integer.MIN_VALUE;
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
		int totalCount = 0;
		for (double input : inputs) {
			int newValue = Util.RoundDouble(input, binWidth);
			this.min = (this.min > newValue) ? newValue : this.min;
			this.max = (this.max < newValue) ? newValue : this.max;
			Integer prev = (counts.get(newValue) == null) ? 0 : counts.get(newValue);
			counts.put(newValue, prev + 1);
			totalCount++;
		}
		this.prob = new HashMap<Integer, Double>();
		for (Entry<Integer, Integer> e : counts.entrySet()) {
			double newProb = (double)e.getValue()/totalCount;
			if (newProb != 0.0) 
				this.prob.put(e.getKey(), newProb);
		}
		if (totalCount == 0) {
			this.min = 0;
			this.max = 0;
			this.prob.put(0, 1.0);
		}
	}
	
	public void Adjust() {
		while (this.prob.get(this.min) == 0.0) {
			this.prob.remove(this.min);
			this.min += binWidth;
		}
		while (this.prob.get(this.max) == 0.0) {
			this.prob.remove(this.max);
			this.max -= binWidth;
		}
	}
	
	public Double Prob(Integer k) {
		Double retVal = this.prob.get(k);
		return (retVal != null) ? retVal : 0.0;
	}
	
	/*public PMF Add(PMF other) {
		PMF retPMF = new PMF(this.min + other.min, this.max + other.max);
		for (int h = retPMF.min; h <= retPMF.max; h++) {
			double sum = 0.0;
			for (int g = this.min; g <= this.max; g++) {
				sum += this.Prob(g) * other.Prob(h - g);
			}
			retPMF.prob.put(h, sum);
		}
		return retPMF;
	}*/
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Double> e : this.prob.entrySet())
			sb.append(String.format("P(%d): %f\t", e.getKey(), e.getValue()));
		return sb.toString();
	}

	public PMF Interpolate(Double actualTime, Double alpha) {
		Double betha = 1 - alpha;
		int min = Util.RoundDouble((betha*this.min) + (alpha*actualTime), binWidth);
		int max = Util.RoundDouble((betha*this.max) + (alpha*actualTime), binWidth);
		HashMap<Integer, Double> probs = new HashMap<Integer, Double>();
		for (int i = min; i <= max; i+=binWidth)
			probs.put(i, 0.0);
		for (int i = this.min; i <= this.max; i += binWidth) {
			int newValue = Util.RoundDouble((betha*i)+(alpha*actualTime), binWidth);
			probs.put(newValue, probs.get(newValue)+this.Prob(i));
		}
		PMF retPMF = new PMF(min, max);
		for (int i = min; i <= max; i += binWidth) {
			retPMF.prob.put(i, probs.get(i));
		}
		return retPMF;
	}
	
	public Double GetScore(Double actualTime) {
		System.out.println("actualTime: " + Double.toString(actualTime));
		Double cdf = 0.0;
		Double score = 0.0;
		int rounded = Util.RoundDouble(actualTime, binWidth);
		for (int i = min; i < rounded && i <= max; ++i) {
			cdf += Prob(i);
			System.out.println("cdf: " + Double.toString(cdf));
			score += Math.pow(cdf, 2);
			System.out.println("score: " + Double.toString(score));
		}
		if (rounded <= max) {
			cdf += Prob(rounded);
			System.out.println("cdf: " + Double.toString(cdf));
			score += (actualTime - (rounded - 0.5)) * Math.pow(cdf, 2);
			System.out.println("score: " + Double.toString(score));
			score += ((rounded + 0.5) - actualTime) * Math.pow(1-cdf, 2);
			System.out.println("score: " + Double.toString(score));
		}
		for (int i = rounded + 1; i <= this.max; ++i ) {
			cdf += Prob(i);
			System.out.println("cdf: " + Double.toString(cdf));
			score += Math.pow(1-cdf, 2);
			System.out.println("score: " + Double.toString(score));
		}
		return score;
	}
}
