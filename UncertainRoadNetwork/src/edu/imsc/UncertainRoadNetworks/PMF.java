package edu.imsc.UncertainRoadNetworks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class PMF {
	public static int binWidth = 15;
	public static Double cutOff = 0.000001;
	
	public int min;
	public int max;
	public double mean;
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
		
	@SuppressWarnings("unchecked")
	@Override
	protected Object clone() {
		PMF retPMF = new PMF(this.min, this.max);
		retPMF.prob = (HashMap<Integer, Double>)this.prob.clone();
		return retPMF;
	};
	
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
	
	public void ComputeMean() {
		mean = 0.0;
		for (Entry<Integer, Double> e : prob.entrySet()) {
			mean += e.getKey() * e.getValue();
		}
	}
	
	public void Adjust() {
		//this.Normalize();
		while (this.Prob(this.max) <= PMF.cutOff && this.max > this.min) {
			if (this.prob.containsKey(this.max)) this.prob.remove(this.max);
			this.max -= binWidth;
		}
		while (this.Prob(this.min) <= PMF.cutOff && this.min < this.max) {
			if (this.prob.containsKey(this.min)) this.prob.remove(this.min);
			this.min += binWidth;
		}
		this.Normalize();
	}
	
	public void Normalize() {
		Double sum = 0.0;
		if (this.min == this.max && this.Prob(min) == 0) {
			this.prob = new HashMap<Integer, Double>();
			return;
		}
		for (Entry<Integer, Double> e : this.prob.entrySet()) {
			sum += e.getValue();
		}
		for (int i = this.min; i <= this.max; i += PMF.binWidth) {
			Double old = this.Prob(i);
			this.prob.put(i, old/sum);			
		}		
	}
	
	public Double Prob(Integer k) {
		Double retVal = this.prob.get(k);
		return (retVal != null) ? retVal : 0.0;
	}
	
	public Double ExpectedValue() {
		Double ev = 0.0;
		for (Entry<Integer, Double> e : this.prob.entrySet()) {
			ev += e.getValue() * e.getKey();
		}
		return ev;
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
		Double sum = 0.0;
		for (Entry<Integer, Double> e : this.prob.entrySet()) {
			if (e.getValue().equals(0.0)) continue;
			//sb.append(String.format("P(%d): %f\t", e.getKey(), e.getValue()));
			sum += e.getValue();
		}
		sb.append("Min: " + Integer.toString(this.min) + "\t");
		sb.append("Max: " + Integer.toString(this.max) + "\t");
		sb.append("Expected Value: " + Double.toString(this.ExpectedValue()) + "\t");
		sb.append("Sum: " + Double.toString(sum));
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
	
	/*public Double GetScore(Double actualTime) {
		HashMap<Integer, Double> cdf = new HashMap<Integer, Double>();
		Double sum = 0.0;
		for (int i = this.min; i <=this.max; i += binWidth) {
			sum += Prob(i);
			cdf.put(i, sum);
		}
		Double score = 0.0;
		for (int i = this.min; i <= this.max; i += binWidth) {
			if (i < actualTime)
				score += binWidth * Math.pow(cdf.get(i), 2);
			else
				score += binWidth * Math.pow((1-cdf.get(i)), 2);
		}
		return score;
	}*/
	
	public Double GetScore(Double actualTime) {
		Double cdf = 0.0;
		Double score = 0.0;
		int rounded = Util.RoundDouble(actualTime, binWidth);
		/*for (int i = min; i < rounded && i <= max; i += binWidth) {
			cdf += Prob(i);
			score += Math.pow(cdf, 2)*binWidth;
		}
		if (rounded <= max) {
			cdf += Prob(rounded);
			score += (actualTime - (rounded - ((double)binWidth/2))) * Math.pow(cdf, 2);
			score += ((rounded + ((double)binWidth/2)) - actualTime) * Math.pow(1-cdf, 2);
		}
		for (int i = rounded + 1; i <= this.max; i += binWidth ) {
			cdf += Prob(i);
			score += Math.pow(1-cdf, 2)*binWidth;
		}*/
		int gmin = Math.min(this.min, rounded);
		int gmax = Math.max(this.max, rounded);
		for (int i = gmin; i < rounded; i += binWidth) {
			cdf += Prob(i);
			score += Math.pow(cdf,  2) * binWidth;
		}
		cdf += Prob(rounded);
		score += (actualTime - (rounded - ((double)binWidth/2))) * Math.pow(cdf, 2);
		score += ((rounded + ((double)binWidth/2)) - actualTime) * Math.pow(1-cdf, 2);
		for (int i = rounded + binWidth; i < gmax; i += binWidth) {
			cdf += Prob(i);
			score += Math.pow(1-cdf, 2)*binWidth;
		}
		return score;
	}
}
