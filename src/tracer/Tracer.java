package tracer;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.util.*;
/**
 * The main class. Displays the window and coordinates the raytracing.
 */
public class Tracer extends Frame {
	
	public Tracer() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
	}
	
	static int maxReflectionDepth = 0;
	static float gamma = 1.0f;
	
	MemoryImageSource imageSource;
	Image offscreenImage;
	int[] pixelBuffer;
	public static Camera camera;
	public static ArrayList scene;
	public static ArrayList lights;
	public static int width = 400;
	public static int height = 400;
	
	public static float antialiasing = 0;
	
	/**
	 * Loads and parses the file "scene.txt". Adds the things it recognizes to
	 * the `scene' and `lights' ArrayLists.
	 */
	public void loadScene() {
		camera = new Camera();
		scene = new ArrayList();
		lights = new ArrayList();
		try {
			
			Parser p = new Parser( "scene.txt" );

			while( !p.endOfFile() ) {
				if( p.tryKeyword( "width" ) ) {
					width = (int)p.parseFloat();
				} else if( p.tryKeyword( "height" ) ) {
					height = (int)p.parseFloat();
				} else if( p.tryKeyword( "maxreflectiondepth" ) ) {
					maxReflectionDepth = (int)p.parseFloat();
				} else if( p.tryKeyword( "gamma" ) ) {
					gamma = p.parseFloat();
				} else if( p.tryKeyword( "antialiasing" ) ) {
					antialiasing = p.parseFloat();
				} else if( p.tryKeyword( "sphere" ) ) {
					Sphere s = new Sphere();
					s.parse( p );
					scene.add( s );
				} else if( p.tryKeyword( "plane" ) ) {
					Plane pl = new Plane();
					pl.parse( p );
					scene.add( pl );
				} else if( p.tryKeyword( "triangle" ) ) {
					Triangle t = new Triangle();
					t.parse( p );
					scene.add( t );
				} else if( p.tryKeyword( "camera" ) ) {
					camera.parse( p );
				} else if( p.tryKeyword( "light" ) ) {
					Light l = new Light();
					l.parse( p );
					lights.add( l );
				} else {
					System.out.println( p.tokenWasUnexpected() );		
				}
			}			
			
			
		} catch( IOException e ) {}
	}
	
	/**
	 * Redraws the offscreenImage onto the screen.
	 */
	public void paint( Graphics gr ) {
		if( offscreenImage != null ) {
			if( pixelBuffer != null ) {
				imageSource.newPixels();
			}
			gr.drawImage( offscreenImage, 0, 50, null );
		}
	}
	
	/**
	 * The actual raytracing starts here.
	 * Initializes the pixel buffers and raytraces each pixel.
	 * Redraws the screen each time an additional 8 rows have been rendered.
	 */
	public void render() {
		
		pixelBuffer = new int[ width*height ];
		imageSource = new MemoryImageSource( width, height, pixelBuffer, 0, width );
		imageSource.setAnimated( true );
		offscreenImage = Toolkit.getDefaultToolkit().createImage( imageSource );
		
		
		ToneMapper toneMapper = new ToneMapper( gamma );
		
		System.out.println( "Starting raytracing." );
		
		for( int y=0; y<height; ++y ) {
			for( int x=0; x<width; ++x ) {
		
				int index = (height-y-1)*width + x;
				Vec3 color = tracePixel( x, y );
				pixelBuffer[ index ] = toneMapper.map( color.x, color.y, color.z );
				
				
			}
			if( (y&7) == 0 ) paint( getGraphics() );
		}
		
		System.out.println( "Finished raytracing." );
		
	}
	
	/**
	 * Given (x,y) coordinates of the pixel to be traced, constructs the primary
	 * ray, raytraces it (by calling Ray.trace) and returns the result (the
	 * color for the pixel).
	 */
	public Vec3 tracePixel( int x, int y ) {
		
		// Compute a ray from the origin of the camera through the center of pixel (x,y)
		Vec3 startPoint = new Vec3(camera.origin.x + camera.left, camera.origin.y + camera.bottom, camera.near-camera.origin.z);
		Vec3 relative = new Vec3((float)(x+0.5)*(camera.right-camera.left)/width, (float)(y+0.5)*(camera.top-camera.bottom)/height, 0);
		Vec3 direction = startPoint.add(relative);
		Ray r = new Ray( camera.origin, direction );
		Stack<Float> breking;
		breking = new Stack<Float>();
		int maxRefractions = 4;
		Vec3 color = r.trace( null, maxReflectionDepth, breking, maxRefractions ); 
		
		// Antialiasing by dividing the pixel in cells, and then calculate the color for a random space in each cell in the pixel
		if (antialiasing > 0)
			for (int n = 0; n < antialiasing; n++)
				for (int t = 0; t < antialiasing; t++)
				{
					relative = new Vec3((float)(x+(n+Math.random())/antialiasing)*(camera.right-camera.left)/width, (float)(y+(t+Math.random())/antialiasing)*(camera.top-camera.bottom)/height, 0);
					direction = startPoint.add(relative);
					r = new Ray( camera.origin, direction );
					color = color.add(r.trace( null, maxReflectionDepth, breking, maxRefractions )).times(0.5f);
				}
			
		return color;
		
	}

	public static void main(String args[]) {
		Tracer mainFrame = new Tracer();
		mainFrame.setTitle("Tracer");
		
		System.out.println( "Parsing scene description." );
		mainFrame.loadScene();
		mainFrame.setSize(Tracer.width+10, Tracer.height+50);
		mainFrame.setVisible(true);
		mainFrame.render();
		
	}
}
