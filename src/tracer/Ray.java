package tracer;

import java.util.Iterator;
import java.util.*;
/**
 * Represents a ray: a ray can `trace' itself, calculating a color.
 */
public class Ray {

	public Vec3 origin;
	public Vec3 direction;
	
	public Ray( Vec3 o, Vec3 d ) {
		origin = new Vec3( o );
		direction = new Vec3( d );	
	}
	
	/**
	 * Calculates the local light term, given an IntersectionInfo and a Light.
	 * This method does <b>not</b> do an occlusion test; do this yourself (e.g.
	 * using a shadow feeler).
	 */
	public Vec3 localLight( IntersectionInfo info, Light light, Vec3 colorvec ) {

		// Vector from intersection to the light		
		Vec3 lightdirection = light.location.minus(info.location);
		lightdirection.normalize();
		
		float color;
		color = info.normal.dot(lightdirection) * info.object.material.diffuse;
		
		// Dot product could be negative; avoid this
		color = Math.max(0, color);
		
		// Determine diffuse component
		Vec3 diffuse = light.color.times(color);
		diffuse = diffuse.times(light.intensity);
		
		diffuse = diffuse.times(colorvec);
		
		// Check for direction of light
		Vec3 h = new Vec3().minus(this.direction);
		h.normalize();
		h = h.add(lightdirection);
		h.normalize();
		
		// Determine c component
		float c;
		c = (float)Math.pow(h.dot(info.normal), info.object.material.specularPower);
		c = info.object.material.specular * c;
		
		// Apply specular (glossy) reflection
		Vec3 specular = light.color.times(c);
		return diffuse.add( specular );
	}
	
	/**
	 * Does the actual `raytracing'. Returns the color this ray `hits.'
	 * @param currentObject This object is ignored in the intersection tests; in
	 *    effect, this object is `invisible' to the ray. This is useful for
	 *    reflection rays and shadow feelers: it avoids precision-errors by just
	 *    ignoring the object you've just bounced off of. If all objects are
	 *    convex (which they are in this tracer) this is actually not a hack but
	 *    completely correct.
	 * @param maxReflectionsLeft Maximum recursion depth for reflection
	 *    calculations .
	 */
	public Vec3 trace( Traceable currentObject, int maxReflectionsLeft, Stack<Float> breking, int maxRefractionsLeft ) {
		// Test all Traceable object in the scene for collision.
		// Store the nearest one.
		IntersectionInfo nearestHit = null;
		Iterator i = Tracer.scene.iterator();
		
		while( i.hasNext() ) {
			
			Traceable t = (Traceable)i.next();
			if( t != currentObject ) {
				IntersectionInfo info = t.intersect( this );
				if(  info.hit  &&  (nearestHit==null || info.distance<nearestHit.distance)  ) {
					nearestHit = info;
				}
			}
			
		}
		if( nearestHit != null ) {
			// Actually hit something
			Material material = nearestHit.object.material;
			Vec3 color = material.color;

			//Texture sampling
			if (material.sampler != null)
				color = color.times(material.sampler.sample(nearestHit.u, nearestHit.v));

			//Calculate perlin noise
			if(!material.perlin.equals(material.color))
			{	
				float noise = Math.abs(material.perlin(nearestHit.location));
				color = color.times(material.ambient);
				color = color.add(material.perlin.times(noise).add(material.color.times(1-noise)));
			}
			
			Vec3 color2 = new Vec3(color);
			color = color.times(material.ambient);
			
			// Local contribution of light
			Iterator lightIter = Tracer.lights.iterator();
			while( lightIter.hasNext() ) {
				Light light = (Light)lightIter.next();
				Vec3 shadowFeelerDirection = light.location.minus( nearestHit.location );
				Ray shadowFeeler = new Ray( nearestHit.location, shadowFeelerDirection );
				if( !shadowFeeler.hit( nearestHit.object ) ) {
					color = color.add(  localLight( nearestHit, light, color2 )  );
				}
			}

			// Global illumination: add recursively computed reflection
			Traceable nextObject = nearestHit.object;
			if (maxReflectionsLeft > 0 && nextObject.material.reflectance > 0)
			{ 
				Vec3 reflectcolor = new Vec3();
				Ray rr = this.reflectRay(nearestHit);
				reflectcolor = reflectcolor.add(rr.trace(nextObject, maxReflectionsLeft - 1, breking, maxRefractionsLeft));
				
				if (nextObject != null)
					reflectcolor = reflectcolor.times(nextObject.material.reflectance);
				color = color.add(reflectcolor);
			}
			
			//Refraction
			Vec3 objecttoeye = new Vec3().minus(this.direction);
			objecttoeye.normalize();
			//Goes into object
			if (nearestHit.object.material.refractive > 0)
			{	
				//Ray goes into the object
				if (objecttoeye.dot(nearestHit.normal) > 0 && maxRefractionsLeft > 0)
				{
					//Look at the first object without removing it
					float n2;
					//Check if you are in another object or if you are outside in the air
					if (breking.empty())
						n2 = 1.0f;
					else
						n2 = breking.peek();					
					float n1 = nearestHit.object.material.refractive;
					breking.push(n2);
					
					//Reflect if the ray does not go into the object
					if ( (n1 * (Math.sin(Math.acos(objecttoeye.dot(nearestHit.normal)))) / n2) > 1 )
					{
						Vec3 reflectcolor = new Vec3();
						Ray rr = this.reflectRay(nearestHit);
						reflectcolor = reflectcolor.add(rr.trace(nextObject, maxReflectionsLeft - 1, breking, maxRefractionsLeft));
						
						color = color.times(1-nearestHit.object.material.transparency).add(reflectcolor.times(nearestHit.object.material.transparency));
					}
					//The ray goes into the object
					else
					{
						Vec3 d = new Vec3(this.direction);
						d.normalize();
						//Calculate the incoming ray
						Ray rr = this.refractRay(nearestHit.location, nearestHit.normal, d, n1, n2 );
						
						Vec3 refractcolor = new Vec3(rr.trace(null, maxReflectionsLeft - 1, breking, maxRefractionsLeft-1));
						//Add transparency of the object
						color = color.times(1-nearestHit.object.material.transparency).add(refractcolor.times(nearestHit.object.material.transparency));
					}
					
				}

				// Goes out of the object
				else if(objecttoeye.dot(nearestHit.normal) < 0 && maxRefractionsLeft > 0)
				{
					//Look at the first object without removing it
					float n2;
					if (breking.empty())
						n2 = 1.0f;
					else
						n2 = breking.pop();
					
					float n1;
					if (breking.empty())
						n1 = 1.0f;
					else
						n1 = breking.peek();

					Vec3 d = new Vec3(this.direction);
					Vec3 normal = new Vec3().minus(nearestHit.normal);
					d.normalize();
					//Calculate the outcoming ray
					Ray rr = this.refractRay(nearestHit.location ,normal, d, n1, n2 );
					
					Vec3 refractcolor = new Vec3(rr.trace(null, maxReflectionsLeft, breking, maxRefractionsLeft-1));
					color = (refractcolor);
				}
			}
			return color;
			
			
		} else {
			// Hit nothing; return background color
			return new Vec3( 0.0f, 0.0f, 0.0f );
		}
		
	}
	
	public Ray reflectRay (IntersectionInfo nearestHit)
	{
		//Calculate the new ray after reflection
		Vec3 loc = new Vec3(nearestHit.location);
		Vec3 addvec, directionlastobject;
		directionlastobject = new Vec3().minus(this.direction);
		addvec = nearestHit.normal.times(directionlastobject.dot(nearestHit.normal) * 2);
		Vec3 dir = new Vec3(this.direction.add(addvec));
		//Create new ray with a new start position and a new direction
		Ray rr = new Ray(loc, dir);
		return rr;
	}
	
	public Ray refractRay( Vec3 location,  Vec3 normal, Vec3 direction, float refrindex1, float refrindex2 )
	{
		//Calculate the new ray after refraction
		Vec3 t = direction.minus(normal.times(direction.dot(normal)));
		t = t.times(refrindex1 / refrindex2);
		float tobesqrt = (refrindex1 * refrindex1 *(1- (direction.dot(normal)*direction.dot(normal)) )) / (refrindex2*refrindex2);
		tobesqrt = 1 - tobesqrt;
		Vec3 t2 = normal.times((float)Math.sqrt(tobesqrt));
		t = t.minus(t2);
		//Move the starting location, so that the ray won't intersect this location again
		Vec3 raystart = new Vec3(location.add(t.times(0.0001f)));
		//Create new ray with a new start position and a new direction
		Ray ray = new Ray(raystart, t);
		return ray;
	}
	
	/**
	 * Checks if the ray (origin + t*direction) hits the scene with 0 <= t <= 1.
	 * @param ignoreObject Similar to trace's currentObject parameter.
	 *    @see trace.
	 */
	public boolean hit( Traceable ignoreObject )
	{
		// Iterate over light objects
		Iterator i = Tracer.scene.iterator();
		boolean hit = false;
		
		while( i.hasNext() && !hit) 
		{
			// Check for intersection of blocking objects between light and current object
			// Ignore own object
			Traceable t = (Traceable)i.next();

			if( t != ignoreObject )
				hit = t.hit( this );
		}
		
		// Return if blocking object exists
		return hit;
	}
	
} 