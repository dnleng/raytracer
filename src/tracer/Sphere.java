package tracer;

import java.io.IOException;

/**
 * A 3D roundish traceable thingy with a center and a radius
 */
public class Sphere extends Traceable {

	Vec3 origin;
	float radius;
	
	public Sphere( Vec3 o, float r ) {
		origin = new Vec3(o);
		radius = r;	
		material = new Material();
	}
	public Sphere() {
		origin = new Vec3();
		material = new Material();	
	}
	
	public void parse( Parser p ) throws IOException {
		p.parseKeyword( "{" );
		while( !p.tryKeyword("}") && !p.endOfFile() ) {
			
			if( p.tryKeyword("origin") ) {
				origin.parse( p );
			} else if( p.tryKeyword("radius") ) {
				radius = p.parseFloat();
			} else if( p.tryKeyword("material") ) {
				material.parse( p );
			} else {
				System.out.println( p.tokenWasUnexpected() );	
			}
			
		}
	}
	
	public IntersectionInfo intersect( Ray r ) {
		// delete the line of code below, and properly compute if the Ray r hits the Spere, and if so, 
		// record the nearest intersection point in the IntersectionInfo record, as well as the distance
		// of this intersection point to the origin of the ray. Initially, you may put a zero normal vector
		// in the IntersectionInfo, but as soon as we compute the local lighting model, you have to compute
		// a proper normal vector of the sphere at the intersection point. 
		Vec3 location, normalvector;
		float distance;

		float a = r.direction.dot(r.direction);
		float b = r.origin.minus(this.origin).dot(r.direction) * 2;
		float c = r.origin.minus(this.origin).dot(r.origin.minus(this.origin)) - radius*radius;

		float discriminant = b*b - 4*a*c;
		float x1,x2, closestx;
		
		if (discriminant >= 0)
		{
			discriminant = (float)Math.sqrt(discriminant);
			x1 = (-b + discriminant) / (2*a);
			x2 = (-b - discriminant) / (2*a);
			
			if (x1>=0 && x2>=0)
				closestx = Math.min(x1,x2);
			else if (x1>=0)
				closestx = x1;
			else if (x2>=0)
				closestx = x2;
			else 
				return new IntersectionInfo(false);
			
			location = r.origin.add(r.direction.times(closestx));
			
			distance = r.direction.times(closestx).length();
			
			normalvector = location.minus(this.origin);
			normalvector.normalize();
			
			//UV mapping of the sphere
			if (this.material.sampler != null)
			{
				float theta, phi;
				theta = (float)Math.acos((location.y - this.origin.y) / this.radius);
				phi = (float)Math.atan2(location.x - this.origin.x, location.z - this.origin.z);
				phi += Math.PI;
				
				float u, v;
				u = (float) (phi%(2*Math.PI) / (2*Math.PI));
				v = (float) ((Math.PI - theta) / Math.PI);
				return new IntersectionInfo(location, normalvector, distance, this, u, v);
			}
			
			return new IntersectionInfo(location, normalvector, distance, this);
		}
		
		
		// For now, simply return "no hit". Replace the line below by meaningful code
		return new IntersectionInfo(false);
	}
	
	public boolean hit( Ray r ) {
		// (replaced the lines below with meaningful code)
		
		// Determine a, b and c
		float a = r.direction.dot(r.direction);
		float b = r.origin.minus(this.origin).dot(r.direction) * 2;
		float c = r.origin.minus(this.origin).dot(r.origin.minus(this.origin)) - radius*radius;
		float discriminant = b*b - 4*a*c;
		float closestx, x1, x2;
		
		if (discriminant >= 0)
		{
			discriminant = (float)Math.sqrt(discriminant);
			x1 = (-b + discriminant) / (2*a);
			x2 = (-b - discriminant) / (2*a);
			
			// Determine closest hit point
			if (x1>=0 && x2>=0)
				closestx = Math.min(x1,x2);
			else if (x1>=0)
				closestx = x1;
			else if (x2>=0) 
				closestx = x2;
			else
				return false;
			
			// Check range
			if (closestx>=0 && closestx <=1)
				return true;
		}

		
		
		return false;
	}
	
}