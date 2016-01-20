package worm;

public class Settings {
	
	public Settings() {
	}

	public static final int[] SIZE_WIDTH = { 20, 30, 40 };
	public static final int[] SIZE_HEIGHT = { 15, 25, 35 };
	public static final String SIZE_LABEL_NAME = "Size:";
	public static final String[] SIZE_NAMES = { "Small", "Medium", "Large" };
	private int mSize = 0;
	
	public int getSize() {
		return mSize;
	}
	
	public void setSize(int size) {
		mSize = size;
	}
	
	public static final int SPEED_MIN = 45;
	public static final int SPEED_MAX = 250;
	public static final int SPEED_STEP = 1;
	public static final String SPEED_LABEL_NAME = "Speed:";
	private int mSpeed = SPEED_MIN;
	
	public int getSpeed() {
		return mSpeed;
	}
	
	public static int getTimerDelay(int speed) {
		return SPEED_MAX - speed + SPEED_MIN;
	}
	
	public void setSpeed(int speed) {
		mSpeed = speed;
	}
	
	public static final int FOOD_MIN = 1;
	public static final int FOOD_MAX = 20;
	public static final int FOOD_STEP = 1;
	public static final String FOOD_LABEL_NAME = "Food:";
	private int mFood = FOOD_MIN;
	
	public int getFood() {
		return mFood;
	}
	
	public void setFood(int food) {
		mFood = food;
	}
	
	private int mFoodFreshnessMax = 100;
	
	public int getFoodFreshnessMax() {
		return mFoodFreshnessMax;
	}
	
	public void setFoodFreshnessMax(int freshnessMax) {
		mFoodFreshnessMax = freshnessMax;
	}
	
	private int mFoodRateOfDecay = 1;
	
	public int getFoodRateOfDecay() {
		return mFoodRateOfDecay;
	}
	
	public void setFoodRateOfDecay(int rateOfDecay) {
		mFoodRateOfDecay = rateOfDecay;
	}
	
	private int mFoodFreshnessPerGrowth = 20;
	
	public int getFoodFreshnessPerGrowth() {
		return mFoodFreshnessPerGrowth;
	}
	
	public void setFoodFreshnessPerGrowth(int freshnessPerGrowth) {
		mFoodFreshnessPerGrowth = freshnessPerGrowth;
	}
	
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
