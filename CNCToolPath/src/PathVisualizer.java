import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PathVisualizer extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	double scaleFactor = 40;
	double offsetX = 0;
	double offsetY = 0;
	
	static double toolRadius = 0.7;
	
	private List<Contour2D> contours = new ArrayList<Contour2D>(); 

	public PathVisualizer () {
		setBackground(Color.lightGray);
		
		addMouseWheelListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Point area = new Point(this.getWidth(), this.getHeight());
        
        g2d.setColor(Color.gray);
        g2d.drawLine(area.x/2 + (int) this.offsetX, 0, area.x/2 + (int) this.offsetX, this.getHeight());
        g2d.drawLine(0, area.y/2 + (int) this.offsetY, this.getWidth(), area.y/2 + (int) this.offsetY);

        contours.forEach( c -> {
        	drawPolygon(g2d, c.path, c.color, area.x/2, area.y/2);
        });
    }
    
    private void addContour(Contour2D contour) {
    	contours.add(contour);
    	repaint();
    }

    private void drawPolygon(Graphics2D g2d, List<Point2D> polygon, Color color, int oX, int oY) {

        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Point2D p1 = polygon.get(i);
            Point2D p2 = polygon.get((i + 1) % n);
            g2d.setColor(color);
            g2d.drawLine(oX + (int) (p1.getX()*scaleFactor)+(int) offsetX, oY + (int) (p1.getY()*scaleFactor)+(int) offsetY, oX + (int) (p2.getX()*scaleFactor)+(int) offsetX, oY + (int) (p2.getY()*scaleFactor)+(int) offsetY);
        }
        
        int size = 4;
        for (int i=0; i<n; i++) {
            Point2D p1 = polygon.get(i);
            g2d.setColor(Color.black);
            g2d.fillOval((int) (oX + p1.getX()*scaleFactor)+(int) offsetX-size/2, oY + (int) (p1.getY()*scaleFactor)+(int) offsetY-size/2, size, size);
        }
    }

    public static void main(String[] args) {
    	String FILENAME = "C:\\peter\\oc_gw\\design\\preformer\\engraver\\data\\vancleef_umriss.svg";
    	//String FILENAME = "C:\\peter\\oc_gw\\design\\preformer\\engraver\\data\\test.svg";
    	//List<Point2D> path = new ArrayList<>();
    	
        JFrame frame = new JFrame("Path Visualizer");
        PathVisualizer panel = new PathVisualizer() ;

        List<Point2D> path = SVGPathReader.getInstance().readPathFromSVGFile(FILENAME);
        panel.addContour(new Contour2D(path, Color.green) );
        panel.addContour(new Contour2D(ToolPathCalculator.getInstance().calculateToolpath(path, toolRadius), Color.blue));
        //panel.addContour(new Contour2D(SVGPathReader.getInstance().readPathFromSVGFile(FILENAME), Color.green) );
        frame.add(panel);
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    Point lastDragPos = new Point();
    
	@Override
	public void mouseDragged(MouseEvent e) {
		this.offsetX += (e.getX() - lastDragPos.x);
		this.offsetY += (e.getY() - lastDragPos.y);
		
		lastDragPos.x = e.getX();
		lastDragPos.y = e.getY();
		
		repaint();
	}


	@Override
	public void mouseMoved(MouseEvent e) {}


	@Override
	public void mouseClicked(MouseEvent e) { }


	@Override
	public void mousePressed(MouseEvent e) {
		lastDragPos.x = e.getX();
		lastDragPos.y = e.getY();
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		lastDragPos.x = 0;
		lastDragPos.y = 0;	
	}


	@Override
	public void mouseEntered(MouseEvent e) {}


	@Override
	public void mouseExited(MouseEvent e) {}


	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
			Point center = new Point(this.getWidth()/2+(int)this.offsetX, this.getHeight()/2+(int)this.offsetY);
			Point mousePixel = new Point(e.getX(), e.getY());
			
			int diffX = mousePixel.x - center.x;
			int diffY = mousePixel.y - center.y;
			Point2D mouseDrawing = new Point2D.Double( diffX / this.scaleFactor, diffY / this.scaleFactor );
			
			// change scale factor
			int amount = e.getWheelRotation();
			this.scaleFactor = this.scaleFactor * (1.0 + (double) amount/10.0)  ;
			this.scaleFactor = Math.max(this.scaleFactor, 1);
			
			this.offsetX -= (mouseDrawing.getX() * scaleFactor) - (mousePixel.getX()-center.x);
			this.offsetY -= (mouseDrawing.getY() * scaleFactor) - (mousePixel.getY()-center.y);
			
			repaint();
		}
		
	}
    

}

