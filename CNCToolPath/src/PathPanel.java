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

import javax.swing.JPanel;

public class PathPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {

	private List<CoordListener> coordListeners = new ArrayList<>();
	private List<Contour2D> contours = new ArrayList<Contour2D>(); 
	double scaleFactor = 40;
	double offsetX = 0;
	double offsetY = 0;
	
	public PathPanel() {
		super();
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
    
    public void addContour(Contour2D contour) {
    	contours.add(contour);
    	contour.dumpTo(System.out, true);
    	repaint();
    }
    
    public void removeAllContours() {
    	contours.clear();
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
            g2d.setColor((i==0 ? Color.red : (i==n-1 ? Color.blue : Color.black) ) );
            g2d.fillOval((int) (oX + p1.getX()*scaleFactor)+(int) offsetX-size/2, oY + (int) (p1.getY()*scaleFactor)+(int) offsetY-size/2, size, size);
        }
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
	public void mouseMoved(MouseEvent e) {
        Point area = new Point(this.getWidth(), this.getHeight());

		float pX = (float) ((e.getX() -area.x/2 - offsetX) / scaleFactor);
		float pY = (float) ((e.getY() -area.y/2 - offsetY) / scaleFactor);
		Point2D p = new Point2D.Float( pX, pY );
		coordListeners.forEach( listener -> {
			listener.coordsUpdated(p);
		});
	}


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

	public void addCoordListener(CoordListener listener) {
		coordListeners.add(listener);
		
	}

}
