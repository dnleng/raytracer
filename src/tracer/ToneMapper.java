package tracer;

/**
 * Used for mapping floating point representations of colors into packed-int
 * colors as used by Java. This implementation also supports gamma correction.
 */
public class ToneMapper {
	
	public ToneMapper( float gamma ) {
		invGamma = 1.0f/gamma;	
	}

	public float invGamma;
	
	public int map( float r, float g, float b ) {
		
		// apply gamma correction
		r = (float) Math.pow(r, this.invGamma);
		g = (float) Math.pow(g, this.invGamma);
		b = (float) Math.pow(b, this.invGamma);
		
		// add code for color clamping below this line
		if (r<0 || g<0 || b<0)
		{
			try {
				throw new Exception("Color below zero");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (r>1 || g>1 || b>1)
		{
			float greatest = 0;
			greatest = Math.max(r, g);
			greatest = Math.max(greatest, b);
			r = r/greatest;
			g = g/greatest;
			b = b/greatest;
		}
		
		// convert to int
		int intR = (int)( r*255.0f );
		int intG = (int)( g*255.0f );
		int intB = (int)( b*255.0f );
		int intA = 255;
		
		// pack the colours into an int
		return (intA<<24) | (intR<<16) | (intG<<8) | intB;

	}


	
}