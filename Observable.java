package life;

public interface Observable {
	public void addObserver(View v);
	public void notifyObservers();
}
