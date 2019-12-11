package tracer;

import java.io.IOException;

/**
 * 3D flat, onbounded, traceable thingy, represented by normal vector and distance from the origin
 */
public class Plane extends Traceable {

	Vec3 normal;
	float offset;
	Vec3 udirection, vdirection;
	
	public Plane( Vec3 n, float o ) {
		normal = new Vec3(n);
		normal.normalize();
		offset = o;
		material = new Material();
	}
	public Plane() {
		normal = new Vec3(0,1,0);
		offset = 0.0f;
		material = new Material();
	}
	
	public void parse( Parser p ) throws IOException {
		p.parseKeyword( "{" );
		while( !p.tryKeyword("}") && !p.endOfFile() ) {
			
			if( p.tryKeyword("normal") ) {
				normal.parse( p );
			} else if( p.tryKeyword("offset") ) {
				offset = p.parseFloat();
			} else if( p.tryKeyword("udirection") ) {
				udirection = new Vec3();
				udirection.parse( p );
			} else if( p.tryKeyword("vdirection") ) {
				vdirection = new Vec3();
				vdirection.parse( p );				
			} else if( p.tryKeyword("material") ) {
				material.parse( p );
			} else {
				System.out.println( p.tokenWasUnexpected() );	
			}
		}	
		
		// Generate udirection and/or vdirection if either of them or none is given
		if (udirection == null && vdirection == null)
		{
			udirection = normal.cross(normal);
			vdirection = udirection.cross(normal);
		}
		else if (udirection == null)
			udirection = normal.cross(vdirection);
		else if (vdirection == null)
			vdirection = normal.cross(udirection);

	}
	
	public IntersectionInfo intersect( Ray r ) {
	
		float t, distance, parallel;
		Vec3 location;
		
		parallel = this.normal.dot(r.direction);
		
		//Determine if there is an intersection between the ray and this plane
		if (parallel != 0)
		{
			t = (- this.normal.dot(r.origin)- this.offset) / parallel;
			if (t > 0)
			{
				location = r.origin.add(r.direction.times(t));
				distance = r.direction.times(t).length();
				
				//UV mapping of the plane
				if (this.material.sampler != null)
				{
					float u, v;
					u = location.dot(udirection);
					v = location.dot(vdirection);
					
					return new IntersectionInfo(location, this.normal, distance, this, u, v);
				}
				return new IntersectionInfo(location, this.normal, distance, this);
			}
		}
		
		// For now, simply return "no hit". Replace the line below by meaningful code*/
		return new IntersectionInfo(false);
	}
	
	public boolean hit( Ray r ) {
		// replace this by meaningfull code.
		float t, parallel;
		
		parallel = this.normal.dot(r.direction);
		
		if (parallel != 0)
		{
			t = (- this.normal.dot(r.origin)- this.offset) / parallel;
			if (t>=0 && t<=1)
				return true;
		}
		
		return false;
		
	}
	
}