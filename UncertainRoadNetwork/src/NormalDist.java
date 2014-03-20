import java.util.ArrayList;


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
		return String.format("Mean: %f Var: %f\n", this.mean, this.var);		
	}

}
