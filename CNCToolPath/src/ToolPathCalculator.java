import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToolPathCalculator {

	private static final double EQUAL_POINT_EPSILON = 0.00001;
    
	private static ToolPathCalculator instance = null;
	
	public static ToolPathCalculator getInstance() {
		if (instance == null) {
			instance = new ToolPathCalculator();
		}		
		return instance;
	}
    
    public List<Point2D> calculateToolpath(List<Point2D> inputPolygon, double toolRadius, boolean removeIntersections, float maxPathSegmentLength) {
        List<Point2D> offsetPolygon = calculateOffsetPolygon(inputPolygon, toolRadius);
        List<Point2D> finePolygon = ensureMinimalSegmentLength(offsetPolygon, maxPathSegmentLength);
        List<Point2D> retPath = new ArrayList<>();
        if(removeIntersections) {
            retPath = createAdjustedToolPath(finePolygon);    	        	
        } else {
            retPath = finePolygon;        	
        }
        
        return removeDuplicates( retPath );
    }
    
    private List<Point2D> ensureMinimalSegmentLength(List<Point2D> offsetPolygon, float maxPathSegmentLength) {
        
        List<List<Point2D>> segments = new ArrayList<>();
        for(int i=0; i<offsetPolygon.size(); i++) {
        	List<Point2D> seg = new ArrayList<>( Arrays.asList(offsetPolygon.get(i), offsetPolygon.get((i+1) % offsetPolygon.size())));
        	splitSegment(seg, maxPathSegmentLength);
        	segments.add(seg);
        }
        return segments.stream().flatMap(seg -> seg.stream()).toList();
	}

	private void splitSegment(List<Point2D> seg, float maxPathSegmentLength) {
		
		if(seg.size() == 2) {
			Point2D p1 = seg.get(0);
			Point2D p2 = seg.get(1);
			if (p1.distance(p2) > maxPathSegmentLength) {
				Point2D c = new Point2D.Float((float) (p1.getX()+p2.getX())/2, (float) (p1.getY()+p2.getY())/2);
				List<Point2D> seg1 = new ArrayList<>(Arrays.asList(p1, c));
				List<Point2D> seg2 = new ArrayList<>(Arrays.asList(c, p2));
				splitSegment(seg1, maxPathSegmentLength);
				splitSegment(seg2, maxPathSegmentLength);
				seg.clear();
				seg.addAll(seg1);
				seg.addAll(seg2);
			}
		} else {
			throw new IllegalArgumentException("seg parameter should have 2 elements");
		}
		return;
	}

	private List<Point2D> calculateOffsetPolygon(List<Point2D> polygon, double offset) {
        List<Point2D> newVertices = new ArrayList<>();
        int n = polygon.size();

        for (int i = 0; i < n; i++) {
            Point2D p1 = polygon.get(i);
            Point2D p2 = polygon.get((i + 1) % n);
            Point2D p3 = polygon.get((i + 2) % n);

            Line2D offsetLine1 = createParallelLine(p1, p2, offset);
            Line2D offsetLine2 = createParallelLine(p2, p3, offset);

            Point2D intersection = getIntersectionPoint(offsetLine1, offsetLine2);
            if (intersection != null) {
                newVertices.add(intersection);
            }
        }

        return newVertices;
    }

    private Line2D createParallelLine(Point2D p1, Point2D p2, double offset) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double len = Math.sqrt(dx * dx + dy * dy);
        double offsetX = (dy / len) * offset;
        double offsetY = -(dx / len) * offset;

        Point2D p1Offset = new Point2D.Double(p1.getX() + offsetX, p1.getY() + offsetY);
        Point2D p2Offset = new Point2D.Double(p2.getX() + offsetX, p2.getY() + offsetY);

        return new Line2D.Double(p1Offset, p2Offset);
    }

    private static Point2D getIntersectionPoint(Line2D line1, Line2D line2) {
        double x1 = line1.getX1(), y1 = line1.getY1();
        double x2 = line1.getX2(), y2 = line1.getY2();
        double x3 = line2.getX1(), y3 = line2.getY1();
        double x4 = line2.getX2(), y4 = line2.getY2();

        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0) return null; // parallel

        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        //double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;

        double intersectionX = x1 + ua * (x2 - x1);
        double intersectionY = y1 + ua * (y2 - y1);

        return new Point2D.Double(intersectionX, intersectionY);
    }

    private List<Point2D> createAdjustedToolPath(List<Point2D> offsetPolygon) {
        List<Point2D> adjustedPath = new ArrayList<>();
        List<Point2D> intersections = new ArrayList<>();
        int n = offsetPolygon.size();

        for (int i = 0; i < n; i++) {
          Point2D p1 = offsetPolygon.get(i);
          Point2D p2 = offsetPolygon.get((i + 1) % n);
          
          adjustedPath.add(p1);
          
          List<Point2D> is = getIntersections(p1, p2, offsetPolygon);
          
          if(is.size() == 1) {
        	  adjustedPath.add(is.get(0));
        	  intersections.add(is.get(0));
          } else if (is.size() > 1) {
        	  throw new IllegalArgumentException("Segment with more than 1 intersection is not supported!");
          }
        }

        adjustedPath.add(offsetPolygon.get(0)); 

        List<Point2D> noDups = removeDuplicates( adjustedPath );
        
        // remove inner segments
        List<Point2D> cleanedPath = removeInnerSegments ( noDups, intersections );
        
        // close path
        return cleanedPath;
    }
    
    private List<Point2D> removeDuplicates( List<Point2D> path) {
    	List<Point2D> ret = new ArrayList<Point2D>(); 
    	
    	Point2D last = null;
    	for(Point2D p : path) {
    		if (last == null) {
    			ret.add(p);
    		} else {
    			if (!last.equals(p)) {
    				ret.add(p);
    			}
    		}
    		last = p;
    	}
    	
    	return ret;
    }
    
    private List<Point2D> removeInnerSegments(List<Point2D> polygon, List<Point2D> intersections) {
    	List<Point2D> ret = new ArrayList<Point2D>(); 
    	boolean outSide = true;
    	
	    int n = polygon.size();
    	for(int i=0; i<n; i++) {  
			Point2D p1 = polygon.get(i);
			Point2D p2 = polygon.get((i + 1) % n);
    		if(outSide) {
        		ret.add(p1);
        		ret.add(p2);
        		if (containsWithEpsilon(intersections, p2, EQUAL_POINT_EPSILON)) {
        			outSide = false; // now switching over to inside
        		}
    		} else {
    			if(containsWithEpsilon(intersections, p2, EQUAL_POINT_EPSILON)) {
    				outSide = true;
    			}
    		}
    		
    		
    	}
		// TODO Auto-generated method stub
		return ret;
	}
    
    private boolean containsWithEpsilon(List<Point2D> list, Point2D p, double epsilon) {
    	for (int i=0; i<list.size(); i++) {
    		if(list.get(i).distance(p) < epsilon) {
    			return true;
    		}
    	}
    	return false;
    }

	private List<Point2D> getIntersections(Point2D a, Point2D b, List<Point2D> polygon) {
    	List<Point2D> ret = new ArrayList<Point2D>();

	    int n = polygon.size();
    	for(int i=0; i<n; i++) {
  	      Point2D p1 = polygon.get(i);
  	      Point2D p2 = polygon.get((i + 1) % n);
          Point2D intersection = getIntersectionPoint(new Line2D.Double(a, b), new Line2D.Double(p1, p2));
          if (intersection != null && isPointOnSegment(a, b, intersection) && isPointOnSegment(p1, p2, intersection)) {
        	  ret.add(intersection);
          }
    		
    	}

    	return ret;
    }

    private boolean isPointOnSegment(Point2D p1, Point2D p2, Point2D p) {
        double minX = Math.min(p1.getX(), p2.getX());
        double maxX = Math.max(p1.getX(), p2.getX());
        double minY = Math.min(p1.getY(), p2.getY());
        double maxY = Math.max(p1.getY(), p2.getY());

        //return (p.getX() >= minX && p.getX() <= maxX && p.getY() >= minY && p.getY() <= maxY);
        return (p.getX() > minX && p.getX() < maxX && p.getY() > minY && p.getY() < maxY);
    }

	public List<Point2D> removeRedundants(List<Point2D> path) {
		List<Point2D> ret = new ArrayList<>();
		
		for (int i=0; i<path.size(); i++) {
			if(i == 0 || i +1 == path.size()) {
				ret.add(path.get(i));
			} else {
				
			}
		}
		return ret;
	}

}
