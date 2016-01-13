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
	
	public static final int FOOD_MIN = 1;
	public static final int FOOD_MAX = 20;
	public static final int FOOD_STEP = 1;
	public static final String FOOD_LABEL_NAME = "Food:";
	public int food = FOOD_MIN;
	
	private int mFenceLength = 1;
	
	public int getFenceLength() {
		return mFenceLength;
	}
	
	public void setFenceLength(int length) {
		mFenceLength = length;
	}
	
	private int mTunnelLength = 0;
	
	public int getTunnelLength() {
		return mTunnelLength;
	}
	
	public void setTunnelLength(int length) {
		mTunnelLength = length;
	}
}
