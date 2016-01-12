package worm;

public interface WormListener {

	/** Guaranteed to be the last method called, and only once per instance. */
	public void onWormDied(Worm2D worm);
	public void onGamePaused(Worm2D worm);
	public void onGameUnpaused(Worm2D worm);
	public void onLengthChanged(Worm2D worm);
}
