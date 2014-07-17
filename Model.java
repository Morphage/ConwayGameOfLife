package life;

import java.awt.Color;

public class Model implements Observable {
	private int rows;
	private Observer viewObserver;
	private Color [][] cellGrid;
	
	public Model(int rows) {
		this.rows = rows;
		cellGrid = new Color[rows][rows];
		initializeGrid();
	}
	
	/*
	 * Initialise grid
	 */
	public void initializeGrid() {
		for(int row = 0; row < rows; row++) {
			for(int column = 0; column < rows; column++) {
				cellGrid [row][column] = Color.GRAY;     // Initialise board with dead cells
			}
		}
	}
	
	/*
	 * Clear grid and send notification to all observers
	 */
	public void clearGrid() {
		initializeGrid();
		notifyObservers();
	}
	
	// Change color of cell at position (row, column)
	public void changeCell(int row, int column, Color newColor) {
		cellGrid [row][column] = newColor;		
	}
	
	/*
	 * Applies the rules to the current state representation and updates the state.
	 */
	public void step() {
		Color [][] nextCellGrid = new Color[rows][rows];   // stores grid of next generation of cells
		
		for(int row = 0; row < rows; row++) {
			for(int column = 0; column < rows; column++) {
				int neighbors = getNeighbors(row,column);
				
				if((neighbors <= 1 || neighbors >= 4) && isAlive(row,column) == 1) {
					nextCellGrid[row][column] = Color.GRAY;	
				} 
				else if(neighbors == 3 && isAlive(row,column) == 0) {
					nextCellGrid[row][column] = getMajorityColor(row,column);
				} 
				else {
					nextCellGrid[row][column] = cellGrid[row][column];
				}				
			}
		}
				
		// Only notify observers if the new cell grid isn't the same as the previous one
		if(!gridsEqual(cellGrid, nextCellGrid)) {
			cellGrid = nextCellGrid;
			notifyObservers();
		}
	}
	
	/*
	 * Returns the number of neighbours of the cell at position (row, column)
	 */
	public int getNeighbors(int row, int column) {			
		return isAlive(wrap(row-1),wrap(column-1)) + isAlive(wrap(row-1),column) + 
		       isAlive(wrap(row-1),wrap(column+1)) + isAlive(row,wrap(column-1)) + 
		       isAlive(row,wrap(column+1)) + isAlive(wrap(row+1),wrap(column-1)) + 
		       isAlive(wrap(row+1),column) + isAlive(wrap(row+1),wrap(column+1)) ;	
	}
	
	/*
	 * Returns the majority colour of the surrounding cells of the cell at position (row, column)
	 * If there is no majority, a colour is chosen at random
	 */
	public Color getMajorityColor(int row, int column) {
		int color = colorValue(wrap(row-1),wrap(column-1)) + colorValue(wrap(row-1),column) + 
		            colorValue(wrap(row-1),wrap(column+1)) + colorValue(row,wrap(column-1)) +
		            colorValue(row,wrap(column+1)) + colorValue(wrap(row+1),wrap(column-1)) +
		            colorValue(wrap(row+1),column) + colorValue(wrap(row+1),wrap(column+1)) ;
		
		if(color > 0)
			return Color.GREEN;
		
		return Color.RED;
	}
	
	/*
	 * Determines if the cell at position (row, column) is alive
	 * Returns 1 if the cell is alive, 0 otherwise
	 */
	public int isAlive(int row, int column) {
		if(cellGrid [row][column] != Color.GRAY)
			return 1;
		return 0;
	}
	
	/*
	 * Returns the color value of a cell at position (row, column)
	 * Return -1 for color red
	 * Return  1 for color green
	 * Return  0 for color gray
	 */
	public int colorValue(int row, int column) {
		if(cellGrid [row][column] == Color.RED)
			return -1;
		else if(cellGrid [row][column] == Color.GREEN)
			return 1;
		return 0;
	}
	
	// Get the number of rows
	public int getRows() {
		return rows;
	}
	
	// Get the cell grid
	public Color [][] getGrid() {
		return cellGrid;
	}
	
	// Get the color of the cell at position (row, column)
	public Color getColor(int row, int column) {
		return cellGrid[row][column];
	}
	
	// Wrap function for board with no edges
	private int wrap(int n){
	    if (n < 0) return rows + n;
	    return n % rows;
	}
	
	// Determine if two cell grids are equivalent
	public boolean gridsEqual(Color [][] grid1, Color [][] grid2) {
		for(int row = 0; row < rows; row++) {
			for(int column = 0; column < rows; column++) {
				if(grid1[row][column] != grid2[row][column])
					return false;
			}
		}
		return true;
	}
	
	/*
	 * Implementation of Observer pattern
	 */
	public void addObserver(View view) {
		viewObserver = (Observer) view;
	}
	
	public void notifyObservers() {
		viewObserver.update();
	}
}
