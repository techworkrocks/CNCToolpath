import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.PrintStream;
import java.util.List;

public class Contour2D {
	public String name;
	public List<Point2D> path;
	public Color color;
	
	public Contour2D(String name, List<Point2D> path, Color color) {
		this.name = name;
		this.path = path;
		this.color = color;
	}

	public void dumpTo(PrintStream out, boolean polarCoordinates) {
		out.println("----------------------------------------");
		out.println("Contour: " + name + " #Points: " + path.size() + "["+(polarCoordinates?"POLAR COORDINATES":"KARTESIAN COORDINATES")+"]");
		Float lastAngle = null;
		for(Point2D p : path) {
			if ( polarCoordinates ) {
				//float angle = (float) ((float) Math.atan2(p.getX(), p.getY()) * 180.0 / Math.PI);
				float angle = (float) ((float) StrictMath.atan2(p.getY(), p.getX()) * 180.0 / Math.PI);
				if(lastAngle != null && angle - lastAngle < -180)
					angle += 360;
				else if(lastAngle != null && angle - lastAngle > 180)
					angle -= 360;
//				if(angle < 0)
//					angle = 360f + angle;
				float radius = (float) Math.sqrt(p.getX()*p.getX() + p.getY()*p.getY());
				
				//out.print("(" + p.getX() + "; " + p.getY() + ") = ");	
				out.println(radius + " " + angle);		
				lastAngle = angle;
			} else {
				out.println(p.getX() + " " + p.getY());				
			}
		}
		out.println();
		
	}
}
