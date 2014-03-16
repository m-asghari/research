
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
	
	public NormalDist Add(NormalDist other) {
		return new NormalDist(this.mean + other.mean, this.var + other.var);
	}

}
