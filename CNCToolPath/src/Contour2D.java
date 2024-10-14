import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;

public class Contour2D {
	public List<Point2D> path;
	public Color color;
	
	public Contour2D(List<Point2D> path, Color color) {
		this.path = path;
		this.color = color;
	}
}
