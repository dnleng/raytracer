package tracer;

import java.io.IOException;

/**
 * 3D triangle represented by three vectors 
 */
public class Triangle extends Traceable
{

	Vec3 normal;
	Vec3 p1, p2, p3;
	float u1, u2, u3, v1, v2, v3;
	
	public Triangle( Vec3 p1, Vec3 p2, Vec3 p3 )
	{
		normal = (p2.minus(p1)).cross(p3.minus(p1));
		normal.normalize();
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		material = new Material();
	}
	
	public Triangle()
	{
		normal = new Vec3(0,1,0);
		p1 = new Vec3();
		p2 = new Vec3();
		p3 = new Vec3();
		material = new Material();
	}
	
	public void parse( Parser p ) throws IOException
	{
		p.parseKeyword( "{" );
		while( !p.tryKeyword("}") && !p.endOfFile() ) {
			
			if( p.tryKeyword("p1") ) {
				p1.parse( p );
			} else if( p.tryKeyword("p2") ) {
				p2.parse( p );
			} else if( p.tryKeyword("p3") ) {
				p3.parse( p );
			} else if( p.tryKeyword("u1") ) {
				u1 = p.parseFloat();
			} else if( p.tryKeyword("u2") ) {
				u2 = p.parseFloat();
			} else if( p.tryKeyword("u3") ) {
				u3 = p.parseFloat();
			} else if( p.tryKeyword("v1") ) {
				v1 = p.parseFloat();
			} else if( p.tryKeyword("v2") ) {
				v2 = p.parseFloat();
			} else if( p.tryKeyword("v3") ) {
				v3 = p.parseFloat();
			} else if( p.tryKeyword("material") ) {
				material.parse( p );
			} else {
				System.out.println( p.tokenWasUnexpected() );	
			}
			
		}
	}
	
	public IntersectionInfo intersect( Ray r )
	{	
		// Applying Cramer's rule
		float t, beta, gamma, M;
		
		// Initializing matrix and vector elements
		float a, b, c, d, e, f, g, h, i, j, k, l;
		a = p1.x-p2.x;
		b = p1.y-p2.y;
		c = p1.z-p2.z;
		d = p1.x-p3.x;
		e = p1.y-p3.y;
		f = p1.z-p3.z;
		g = r.direction.x;
		h = r.direction.y;
		i = r.direction.z;
		j = p1.x-r.origin.x;
		k = p1.y-r.origin.y;
		l = p1.z-r.origin.z;
		M = a*(e*i-h*f)+b*(g*f-d*i)+c*(d*h-e*g);
		
		// Check for triangle-based plane intersection
		t = (f*(a*k-j*b)+e*(j*c-a*l)+d*(b*l-k*c))/M;
		if(t > 0)
			return new IntersectionInfo(false);
		
		// Check for triangle intersection
		gamma = (i*(a*k-j*b)+h*(j*c-a*l)+g*(b*l-k*c))/M;
		if(gamma<0 || gamma > 1)
			return new IntersectionInfo(false);
		
		beta = (j*(e*i-h*f)+k*(g*f-d*i)+l*(d*h-e*g))/M;
		if(beta<0 || beta > 1-gamma)
			return new IntersectionInfo(false);
		
		// Return intersection
		Vec3 location;
		float distance;
		
		location = r.origin.add(r.direction.times(t));
		distance = r.direction.times(t).length();
		
		//UV mapping of the triangle
		if (this.material.sampler != null)
		{
			float u, v;
			u = u1 + beta*(u2-u1) + gamma*(u3-u1);
			v = v1 + beta*(v2-v1) + gamma*(v3-v1);
			return new IntersectionInfo(location, this.normal, distance, this, u, v);
		}
	
		return new IntersectionInfo(location, normal, distance, this);
	}
	
	public boolean hit( Ray r )
	{
		// Applying Cramer's rule
		float t, beta, gamma, M;
		
		// Initializing matrix and vector elements
		float a, b, c, d, e, f, g, h, i, j, k, l;
		a = p1.x-p2.x;
		b = p1.y-p2.y;
		c = p1.z-p2.z;
		d = p1.x-p3.x;
		e = p1.y-p3.y;
		f = p1.z-p3.z;
		g = r.direction.x;
		h = r.direction.y;
		i = r.direction.z;
		j = p1.x-r.origin.x;
		k = p1.y-r.origin.y;
		l = p1.z-r.origin.z;
		M = a*(e*i-h*f)+b*(g*f-d*i)+c*(d*h-e*g);
		
		// Check for triangle-based plane intersection
		t = (f*(a*k-j*b)+e*(j*c-a*l)+d*(b*l-k*c))/M;
		if(t > 0 || t < -1)
			return false;
		
		// Check for triangle intersection
		gamma = (i*(a*k-j*b)+h*(j*c-a*l)+g*(b*l-k*c))/M;
		if(gamma<0 || gamma > 1)
			return false;
		
		beta = (j*(e*i-h*f)+k*(g*f-d*i)+l*(d*h-e*g))/M;
		if(beta<0 || beta > 1-gamma)
			return false;
		
		// Return hit
		return true;
	}
	
}