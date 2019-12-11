package tracer;

public class NearestSampler extends Sampler2D{
	
	public NearestSampler( String filename ) {
		super(filename);
	}
	
	public Vec3 sample( float u, float v )
	{
		//Get the color at (u, v) in the texture
		int [] result = new int[3];
		result = this.rawRead((int)scaleU(u), (int)scaleV(v));
		return new Vec3(scaleOutput(result[0]), scaleOutput(result[1]), scaleOutput(result[2]));
	}
	
	
}
