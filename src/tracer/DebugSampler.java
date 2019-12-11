package tracer;

public class DebugSampler extends Sampler2D{
	
	public Vec3 sample( float u, float v )
	{
		return new Vec3(u, v, 0.0f);
	}

}
