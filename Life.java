package life;

// Assumption: If more than one argument is passed to the command line
// the first argument is taken to be the screen size
public class Life {
	private static int rows = 0;
	private static final int DEFAULT_ROWS = 30;
	
	public static void main(String [] args) {
		try {
			if (args.length == 0){
				rows = DEFAULT_ROWS;
			}
			else if((rows = Integer.parseInt(args[0])) < 4) {
				System.err.println("The minimum row size should be 4.");
				System.exit(1);
			}
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Model model = new Model(rows);
					new View(model);
				}
			});
		}
		catch (NumberFormatException e) {
			System.err.println("The size of the screen must be a number: " + e.toString());
		}
	}
}