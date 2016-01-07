package worm;

public class Settings {

	public static final int[] SIZE_WIDTH = { 20, 30, 40 };
	public static final int[] SIZE_HEIGHT = { 15, 25, 35 };
	public static final String SIZE_LABEL_NAME = "Size:";
	public static final String[] SIZE_NAMES = { "Small", "Medium", "Large" };
	public int size = 0;
	
	public static final int[] TIMER_DELAY = { 250, 180, 120, 80, 45 };
	public static final String SPEED_LABEL_NAME = "Speed:";
	public static final String[] SPEED_NAMES = { "Slow", "Medium", "Fast", "Faster", "Fastest" };
	public int speed = 0;
	
	public static final int MUNCHIE_MIN = 1;
	public static final int MUNCHIE_MAX = 20;
	public static final int MUNCHIE_STEP = 1;
	public static final String MUNCHIE_LABEL_NAME = "Food:";
	public int munchies = MUNCHIE_MIN;
}
