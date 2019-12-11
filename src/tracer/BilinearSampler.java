package tracer;

public class BilinearSampler extends Sampler2D{
	
	public BilinearSampler( String filename ) {
		super(filename);
	}
	
	public Vec3 sample( float u, float v )
	{
		float uu = (float)(this.sourceWidth*u - Math.floor(this.sourceWidth*u));
		float vv = (float)(this.sourceHeight*v - Math.floor(this.sourceHeight*v));
		
		//Get colors of surrounding pixels
		Vec3[][] vecarray = new Vec3[2][2];
		int[] colorarra;
		for (int x=0; x<=1; x++)
			for (int y=0; y<=1; y++)
			{
				colorarra = this.rawRead((int)scaleU(u)+x, (int)scaleV(v)+y);
				vecarray[x][y] = new Vec3(scaleOutput(colorarra[0]), scaleOutput(colorarra[1]), scaleOutput(colorarra[2]));
			}
		
		Vec3 c = vecarray[0][0].times((1-uu)*(1-vv)).add( vecarray[1][0].times(uu*(1-vv)) ).add( vecarray[0][1].times((1-uu)*vv) ).add( vecarray[1][1].times(uu*vv) );
		
		return c;
	}

}
