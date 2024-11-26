import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SVGPathReader {
	private static SVGPathReader instance = null;
	
	public static SVGPathReader getInstance() {
		if (instance == null) {
			instance = new SVGPathReader();
		}		
		return instance;
	}
	
	public List<Point2D> readPathFromSVGFile(File file) {
		List<Point2D> path = null;
    	try
    	{
        	SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            SimpleSVGHandler handler = new SimpleSVGHandler();

            saxParser.parse(file.getAbsoluteFile(), handler);  
            
            path = handler.getCoordinates();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    	return path;
	}
}


class SimpleSVGHandler extends DefaultHandler {
   	private List<Point2D> path = new ArrayList<>();
    
   	@Override
   	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
   		if("path".equals(qName)) {
   			String cs = attributes.getValue("d");
   			StringTokenizer t = new StringTokenizer(cs, " ");
   			boolean isAbsolute = true;
   			boolean vertical = false;
   			boolean horizontal = false;
   				
   			while (t.hasMoreElements() ) {
   				String el = t.nextToken();
   				if(el.length() == 1 && !"0".equals(el)) { // some letter
   	   				if("m".equals(el) || "l".equals(el)) {
   	   					isAbsolute = false; // points indicate movements from one point to the next
   						vertical = false;
   						horizontal = false;
   	   				} else if ("M".equals(el) || "L".equals(el)) {
   	   					isAbsolute = true;
   						vertical = false;
   						horizontal = false;
   	   				} else if ("v".equals(el)) { // vertical (y) delta only
   	   					isAbsolute = false;
   	   					vertical = true;
   	   					horizontal = false;
   	   				} else if ("V".equals(el)) { // vertical (y) delta only
   	   					isAbsolute = true;
   	   					vertical = true;
   	   					horizontal = false;
   	   				} else if ("h".equals(el)) { // vertical (y) delta only
   	   					isAbsolute = false;
   	   					horizontal = true;
   	   					vertical = false;
   	   				} else if ("H".equals(el)) { // vertical (y) delta only
   	   					isAbsolute = true;
   	   					horizontal = true;
   	   					vertical = false;
   	   				} else {
   	   					System.out.println("Unhandled control character: "+el);
   	   				}
   				} else {
   					if(vertical || horizontal) {
   						Point2D p = path.getLast();
   						// only a single value expected
   						double z =  Double.parseDouble(el);
   						if (isAbsolute) {
   							if(vertical)
   								p = new Point2D.Double(p.getX(), z);
   							else
   								p = new Point2D.Double(z, p.getY());
   						} else {
   							if(vertical)
   								p = new Point2D.Double(p.getX(), p.getY()+z);
   							else
   								p = new Point2D.Double(p.getX()+z, p.getY());
   						}

   						path.add(p);
   					} else {
	   					// assuming a coordinate pair
	   					StringTokenizer tc = new StringTokenizer(el, ",");
	   					double x = Double.parseDouble(tc.nextToken());
	   					double y = Double.parseDouble(tc.nextToken());
	   					Point2D p = new Point2D.Double(x, y);
						Point2D last = path.size() > 0 ? path.getLast() : new Point2D.Double(0,0);
	
	   					if(isAbsolute) {
	   						if(! last.equals(p)) {
	   	   						path.add(p);   							
	   						}
	   					} else {
	   						if(p.getX() != 0 || p.getY() != 0) {
	   	   						path.add(new Point2D.Double(last.getX()+p.getX(), last.getY()+p.getY()));   							
	   						}
	   					}
   					}
   				}
   			}
   			//System.out.println( cs );
   			
   		}
   	}

	public List<Point2D> getCoordinates() {
		return path;
	}
	
}