package tracer;

import java.io.IOException;
import java.util.*;

/**
 * Holds shading parameters.
 */
public class Material {
	
	public Material() {
		color = new Vec3( 1, 1, 1 );
		perlin = new Vec3(color);
		ambient = 0.2f;
		diffuse	= 0.8f;
		specular = 0.0f;
		specularPower = 16.0f;
		reflectance = 0.0f;
		refractive = 0.0f;
		transparency = 1.0f;
	}
	
	public Vec3 color, perlin;
	public Vec3[] G;
	public int[] permutation; 
	public int N;
	
	public float ambient;
	public float diffuse;
	public float specular;
	public float specularPower;
	public float reflectance;
	public float refractive;
	public float transparency;
	public Sampler2D sampler;
	
	public Vec3[] randomVecs()
	{
		// Obtain random unit vectors in domain [-1;1]
		Vec3 v;
		Vec3[] G;
		float vx, vy, vz;
		int count;
		N = 256;
		G = new Vec3[N];
		permutation = this.permutation(N);
			
		// Generate 1D array of pseudo random unit vectors
		for(count=0 ; count<N ; count++)
		{
			do
			{
				vx = 2*(float)Math.random()-1;
				vy = 2*(float)Math.random()-1;
				vz = 2*(float)Math.random()-1;
			}
			while(!(Math.sqrt(vx*vx+vy*vy+vz*vz) < 1));
			
			v = new Vec3(vx, vy, vz);
			v.normalize();
			G[count] = v;
		}
		
		return G;
	}
	
	public float perlin(Vec3 v)
	{
		// Calculate the noise value
		float x, y, z, result;
		int i, j, k;
		x = v.x;
		y = v.y;
		z = v.z;
		result = 0;
		
		for(i = (int)Math.floor(x) ; i <= (int)Math.floor(x)+1 ; i++)
			for(j = (int)Math.floor(y) ; j <= (int)Math.floor(y)+1 ; j++)
				for(k = (int)Math.floor(z) ; k <= (int)Math.floor(z)+1 ; k++)
				{
					result += cubWeight(x-i)*cubWeight(y-j)*cubWeight(z-k)*(hash(i,j,k).dot(new Vec3(x-i, y-j, z-k)));
				}
		return result;
	}
	
	public float cubWeight(float t)
	{
		if(Math.abs(t) < 1)
			return 2*(float)Math.pow(Math.abs(t), 3)-3*(float)Math.pow(Math.abs(t), 2)+1;
		else
			return 0;
	}
	
	public Vec3 hash(int i, int j, int k)
	{
		return G[subHash(i+subHash(j+subHash(k)))];
	}
	
	public int subHash(int i)
	{
		i = Math.abs(i);
		int result = i%this.N;
		return this.permutation[result];
	}
	
	public int[] permutation(int size)
	{
		int count;
		int[] result;
		result = new int[size];
		
		ArrayList<Integer> per;
		per = new ArrayList<Integer>();
		
		// Create arraylist with numbers [0, 255]
		for(count = 0 ; count < size ; count++)
			per.add(count, count);
		
		// Create random array with numbers [0, 255]
		for(count = 0 ; count < size ; count++)
		{
			result[count] = (int)per.get((int)Math.floor(Math.random()*per.size()));
			per.remove(Math.random()*per.size());
		}
		return result;
	}
	
	public void parse( Parser p ) throws IOException {
		String filename;
		p.parseKeyword( "{" );
		while( !p.tryKeyword("}") && !p.endOfFile() ) {
			
			if( p.tryKeyword("color") ) {
				color.parse( p );
				perlin = new Vec3(color);
			} else if( p.tryKeyword("perlin") ) {
				color.parse( p );
				perlin.parse( p );
				G = randomVecs();
			} else if( p.tryKeyword("ambient") ) {
				ambient = p.parseFloat();
			} else if( p.tryKeyword("diffuse") ) {
				diffuse = p.parseFloat();
			} else if( p.tryKeyword("specular") ) {
				specular = p.parseFloat();
			} else if( p.tryKeyword("specularpower") ) {
				specularPower = p.parseFloat();
			} else if( p.tryKeyword("reflectance") ) {
				reflectance = p.parseFloat();
			} else if( p.tryKeyword("refractive") ) {
				refractive = p.parseFloat();
			} else if( p.tryKeyword("transparency") ) {
				transparency = p.parseFloat();
			} else if (p.tryKeyword("texture") ) {
				if( p.tryKeyword("debug") ) {
					sampler = new DebugSampler();
				} else if( p.tryKeyword("nearest") ) {
					filename = p.parseString();
					sampler = new NearestSampler(filename);
				} else if( p.tryKeyword("bilinear") ) {
					filename = p.parseString();
					sampler = new BilinearSampler(filename);
				}
			} else {
				System.out.println( p.tokenWasUnexpected() );	
			}
			
		} 
	}
	
}