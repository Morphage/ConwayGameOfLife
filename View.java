package life;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class View extends JFrame implements Observer {
	public int rows;      // number of rows on the grid
	public int turns;     // number of turns for the simulation so far
	public int interval;  // the value of the position of the slider knob
	
	private boolean running; // to know in which state the program is in
	
	private Model lifeModel;
	private Color [][] viewGrid;   // stores the grid drawn in the GUI 
	
	// stores a button and its location in the grid
	private Hashtable<JButton, Point> buttonMap = new Hashtable<JButton, Point>();
	
	// GUI component
	private Container contentPane;
	private JLabel    count;
	private JPanel    buttonPane;
	private JPanel     grid;
	
	// Command buttons
	private ClearButton clearButton;
	private StepButton  stepButton;
	private RunButton   runButton;
	private StopButton  stopButton;
	
	// Timer
	private Timer timer;
		
	/*
	 * View constructor
	 * Creates the view for the model given by the parameter
	 */
	public View(Model _model) {
		super("The Game of Life");      // Create GUI window
		lifeModel = _model;
		lifeModel.addObserver(this);
		rows = lifeModel.getRows();
		turns = 0;
		interval = 1;
		
		viewGrid = new Color[rows][rows];
		
		// Action performed every tick of the timer
		timer = new Timer(2000/interval, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stepButton.execute();  // Execute step operation
			}
		});
		
		// Initialise components and paint the cell grid
		initGrid();
		initComponents();
	}
	
	// Initialises view components
	public void initComponents() {
		// Get content pane of the frame
		contentPane = getContentPane();
		
		// Add buttons
		stepButton  = new StepButton();
		runButton   = new RunButton();
		clearButton = new ClearButton();
		stopButton  = new StopButton();
		
		// Add action listener for the buttons
		CommandListener commandListener = new CommandListener();
		clearButton.addActionListener(commandListener);
		stepButton.addActionListener(commandListener);
		runButton.addActionListener(commandListener);
		stopButton.addActionListener(commandListener);
		
		// Add grid
		contentPane.add(grid, BorderLayout.CENTER);
		
		// Turns label 
		count = new JLabel("" + turns, SwingConstants.CENTER);
		contentPane.add(count, BorderLayout.NORTH);
		
		// Button panel: Clear / Step / Run / Stop
		buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(clearButton);
		buttonPane.add(stepButton);
		buttonPane.add(runButton);
		buttonPane.add(stopButton);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		
		// Slider
		JSlider slider = new JSlider(JSlider.VERTICAL,1,10,1);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		
		for(int i=1; i < 10; i += 2) {
			labelTable.put(new Integer(i), new JLabel("" + i));
		}
		
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);		
		slider.setMajorTickSpacing(1);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		slider.addChangeListener(new ChangeSpeed());
		contentPane.add(slider,BorderLayout.EAST);
		
		
		// Additional GUI settings
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 800);
		setVisible(true);
	}
	
	// Paints the initial grid
	public void initGrid() {
		grid = new JPanel((new GridLayout(rows,rows)));
		ChangeColor colorListener = new ChangeColor();
				
		for (int row = 0; row < rows; row++) {
			for(int column = 0; column < rows; column++) {
				JButton cell = new JButton();
				cell.addMouseListener(colorListener);
				cell.setBackground(lifeModel.getColor(row, column));
				
				buttonMap.put(cell, new Point(row,column));
				grid.add(cell);
			}
		}
		
		setGridsEqual(viewGrid, lifeModel.getGrid());
	}
	
	// Updates cell grid
	public void updateGrid() {		
		for (int row = 0; row < rows; row++) {
			for(int column = 0; column < rows; column++) {
				// Only update the cells which have changed colour instead of redrawing the entire grid
				if(viewGrid[row][column] != lifeModel.getGrid()[row][column]) {
					JButton cell = (JButton) grid.getComponent(row*rows + column);
					cell.setBackground(lifeModel.getColor(row,column));				
					buttonMap.put(cell, new Point(row,column));
				}
			}
		}

		setGridsEqual(viewGrid, lifeModel.getGrid());
		grid.validate();
	}
	
	/*
	 * Observer pattern implementation
	 * update method called when there has been
	 * a change in the life model
	 */
	public void update() {
		updateGrid();
	}
	
	/*
	 * CommandListener for clear / step / run / stop buttons 
	 */
	public class CommandListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {		
			Command command = (Command) e.getSource();
			command.execute();
		}
	}
	
	// Clear button
	public class ClearButton extends JButton implements Command {
		public ClearButton() {
			super("Clear");
		}
		
		public void execute() {
			lifeModel.clearGrid();
			turns = 0;
			count.setText("" + turns);
		}
	}
	
	// Step button
	public class StepButton extends JButton implements Command {
		public StepButton() {
			super("Step");
		}
		
		public void execute() {
			lifeModel.step();
			turns++;
			count.setText("" + turns);
		}			
	}
	
	// Run button
	public class RunButton extends JButton implements Command {		
		public RunButton() {
			super("Run");
			running = false;
		}
		
		public void execute() {
			clearButton.setEnabled(running);
			stepButton.setEnabled(running);
			stopButton.setEnabled(running);
			grid.setEnabled(running); // while updating the user can't change the cells on the grid
			
			if(!running) {
				timer.start();   // start the timer
				running = true;
				runButton.setText("Pause");
			} 
			else {
				timer.stop();    // stop the timer
				running = false;
				runButton.setText("Run");
			}
		}			
	}
	
	// Stop button
	public class StopButton extends JButton implements Command {
		public StopButton() {
			super("Stop");
		}
		
		public void execute() {
			System.exit(DISPOSE_ON_CLOSE);
		}			
	}
	
	// Recording the speed intervals for the timer
	private class ChangeSpeed implements ChangeListener {
		public void stateChanged(final ChangeEvent changeEvent) {
			final JSlider source = (JSlider) changeEvent.getSource();
			if(!source.getValueIsAdjusting()) {
				interval = (int) source.getValue();
				timer.setInitialDelay(2000/interval);
				timer.setDelay(2000/interval);
			}
		}		
	}
	
	// Changes the contents of squares using the mouse
	private class ChangeColor extends MouseAdapter {
		public void mousePressed(MouseEvent event) {
			JButton source = (JButton) event.getSource();
			Point point = buttonMap.get(source);
			
			if(!running) {      // activate mouse listener when program isn't running
				if(SwingUtilities.isLeftMouseButton(event)) {
					lifeModel.changeCell(point.x, point.y, Color.RED);
					changeViewCell(point.x, point.y, Color.RED);
					source.setBackground(Color.RED);
				}
				else if (SwingUtilities.isRightMouseButton(event)) {
					lifeModel.changeCell(point.x, point.y, Color.GREEN);
					changeViewCell(point.x, point.y, Color.GREEN);
					source.setBackground(Color.GREEN);
				}
				else if (SwingUtilities.isMiddleMouseButton(event)){
					lifeModel.changeCell(point.x, point.y, Color.GRAY);
					changeViewCell(point.x, point.y, Color.GRAY);
					source.setBackground(Color.GRAY);
				}
				grid.validate();
			}
		}
		
		/*
		 * Method needed to update the grid displayed by the view in "real-time"
		 * when the user clicks on a cell to change the colour
		 */
		public void changeViewCell(int row, int column, Color newColor) {
			viewGrid [row][column] = newColor;		
		}
	}
	
	/*
	 * Set two grids equal using this method to avoid using the
	 * equals operator because changes to one grid would also happen
	 * to the other grid.
	 */
	public void setGridsEqual(Color [][] grid1, Color [][] grid2) {
		for (int row = 0; row < rows; row++) {
			for(int column = 0; column < rows; column++) {
				grid1[row][column] = grid2[row][column];
			}
		}
	}
}
