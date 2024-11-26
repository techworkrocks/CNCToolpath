import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PathVisualizer extends JFrame implements ActionListener, CoordListener {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected PathPanel pathPanel;
	protected JLabel coordLabelX, coordLabelY;
	protected JButton loadButton, traceButton;
	protected JTextField toolRadiusTF;
	protected JCheckBox removeIntersectionsCB;
	protected String defaultDir = "C:\\peter\\oc_gw\\design\\preformer\\engraver\\data";
	
	static double toolRadius = .8;
	

	public PathVisualizer () {
		setBackground(Color.lightGray);
		setTitle("SVG Path Visualizer");
        
		JTextArea textArea = new JTextArea(30, 50);
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		MessageConsole mc = new MessageConsole(textArea);
		mc.redirectOut();
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                
		JPanel commandPanel = new JPanel();
		commandPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		loadButton = new JButton("Load SVG");
		loadButton.addActionListener(this);
		commandPanel.add( loadButton );
		JPanel radiusPanel = new JPanel();
		radiusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		radiusPanel.add(new JLabel("Tool Radius (mm):"));
		toolRadiusTF = new JTextField(""+toolRadius, 10);
		toolRadiusTF.setHorizontalAlignment(JTextField.CENTER);
		toolRadiusTF.addActionListener(this);
		radiusPanel.add(toolRadiusTF);
		commandPanel.add(radiusPanel);
		coordLabelX = new JLabel();
		coordLabelY = new JLabel();
		traceButton = new JButton("Trace Toolpath");
		traceButton.addActionListener(this);
		commandPanel.add( traceButton );
		removeIntersectionsCB = new JCheckBox("Remove Intersections");
		commandPanel.add(removeIntersectionsCB);
		
		Dimension labelMinSize = new Dimension(100,1);
		coordLabelX.setMinimumSize(labelMinSize);
		coordLabelY.setMinimumSize(labelMinSize);
		commandPanel.add(coordLabelX);
		commandPanel.add(coordLabelY);

		
		pathPanel = new PathPanel();
		pathPanel.addCoordListener(this);
		
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        		pathPanel, scroll);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(commandPanel, BorderLayout.NORTH);
        setSize(1400, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        splitPane.setDividerLocation(0.65);
    

    	if( System.getProperty("defaultDir") != null)
    		defaultDir = System.getProperty("defaultDir");

		fc = new JFileChooser();
		if(defaultDir != null)
			fc.setCurrentDirectory(new File(defaultDir));
	}
	

    JFileChooser fc;

    public static void main(String[] args) {
     	
        PathVisualizer pathVisualizer = new PathVisualizer();
        pathVisualizer.setVisible(true);

    }
    
    


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loadButton) {
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
		        List<Point2D> path = SVGPathReader.getInstance().readPathFromSVGFile(file);
		        List<Point2D> optimizedPath = ToolPathCalculator.getInstance().removeRedundants(path);
		        pathPanel.removeAllContours();
		        pathPanel.addContour(new Contour2D(file.getName(), path, Color.green) );
		        List<Point2D> toolPath = ToolPathCalculator.getInstance().calculateToolpath(path, Float.parseFloat(toolRadiusTF.getText()), removeIntersectionsCB.isSelected());
		        pathPanel.addContour(new Contour2D("Toolpath", toolPath, Color.blue) );
			}
		} 
		else if (e.getSource() == traceButton) {
			Contour2D initialContour = pathPanel.getContour( 0 );
			pathPanel.removeContour( 1 );
			
	        List<Point2D> toolPath = ToolPathCalculator.getInstance().calculateToolpath(initialContour.path, Float.parseFloat(toolRadiusTF.getText()), removeIntersectionsCB.isSelected());
	        pathPanel.addContour(new Contour2D("Toolpath", toolPath, Color.blue) );
		}
		
	}




	@Override
	public void coordsUpdated(Point2D p) {
		coordLabelX.setText(String.format("%.3f", p.getX()));
		coordLabelY.setText(String.format("%.3f", p.getY()));
		
	}
    

}

