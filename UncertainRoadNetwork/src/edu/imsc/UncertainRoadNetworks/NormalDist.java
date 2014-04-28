package edu.imsc.UncertainRoadNetworks;
import java.util.ArrayList;
import org.apache.commons.math3.distribution.NormalDistribution;


public class NormalDist {
	public double mean;
	public double var;
	
	public NormalDist(double mean, double var){
		this.mean = mean;
		this.var = var;
	}
	
	public NormalDist(double[] inputs){
		double sum = 0.0;
		for (double input : inputs)
			sum += input;
		this.mean = sum / inputs.length;
		sum = 0.0;
		for (double input : inputs)
			sum += Math.pow(input - this.mean, 2);
		this.var = sum / inputs.length;
	}
	
	public NormalDist(ArrayList<Double> inputs) {
		Double sum = 0.0;
		for (Double input : inputs)
			sum += input;
		this.mean = sum / inputs.size();
		sum = 0.0;
		for (Double input : inputs)
			sum += Math.pow(input - this.mean, 2);
		this.var = sum / inputs.size();
		if (inputs.size() == 0) {
			this.mean = 0;
			this.var = 0;
		}
	}
	
	public NormalDist Add(NormalDist other) {
		return new NormalDist(this.mean + other.mean, this.var + other.var);
	}
	
	public String toString() {
		return String.format("Mean: %f Var: %f", this.mean, this.var);		
	}

	public NormalDist Interpolate(Double actualTravelTime, Double alpha) {
		return new NormalDist((1-Util.alpha)*this.mean + Util.alpha*actualTravelTime, Math.pow((1-Util.alpha), 2)*this.var);
	}
	
	public Double GetScore(Double actualTime) {
		if (this.var == 0) {
			return Math.abs(this.mean - actualTime);
		}
		Double std = Math.sqrt(this.var);
		Double v = (actualTime - this.mean)/std;
		NormalDistribution normDist = new NormalDistribution(0, 1);
		Double score = -std * (1.0/Math.sqrt(Math.PI) - 2*normDist.density(v) - v*(2*normDist.cumulativeProbability(v) - 1));
		if (score == Double.NaN)
			System.out.println("busted");
		return score;
	}

}
