package worm;

public class Settings {

	public static final int[] SIZE_WIDTH = { 20, 30, 40 };
	public static final int[] SIZE_HEIGHT = { 15, 25, 35 };
	public static final String[] SIZE_NAMES = { "Small", "Medium", "Large" };
	public int size = 0;
	
	public static final int[] TIMER_DELAY = { 250, 180, 120, 80, 45 };
	public static final String[] SPEED_NAMES = { "Slow", "Medium", "Fast", "Faster", "Fastest" };
	public int speed = 0;
	
	// TODO Add munchieCount where 1 < munchie < 20
}
