package worm;

public class Food {
	
	public Food() {
		this(1);
	}
	
	public Food(int freshness) {
		this(freshness, 0);
	}
	
	/**
	 * Constructor.
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param freshness amount of freshness this food has before it is decayed. Must be positive.
	 * @param rateOfDecay rate at which this food will decay per cycle. Must be positive or 0. If 0, food will not decay.
	 */
	public Food(int freshness, int rateOfDecay) {
		setFreshness(freshness);
		setRateOfDecay(rateOfDecay);
	}
	
	private int mFreshness;
	
	public int getFreshness() {
		return mFreshness;
	}

	/**
	 * @param freshness must be positive
	 */
	public void setFreshness(int freshness) {
		mFreshness = mInitialFreshness = Math.max(1, freshness);
	}

	private int mInitialFreshness;
	
	/**
	 * @return The freshness of this food before it started decaying
	 */
	public int getInitialFreshness() {
		return mInitialFreshness;
	}
	
	private int mRateOfDecay;
	
	/**
	 * @return how much the freshness of this food should decrement per cycle
	 */
	public int getRateOfDecay() {
		return mRateOfDecay;
	}
	
	public void setRateOfDecay(int rateOfDecay) {
		mRateOfDecay = Math.max(0, rateOfDecay);
	}
	
	
	public void decay() {
		mFreshness -= mRateOfDecay;
	}
	
	public boolean isDecayed() {
		return mFreshness <= 0;
	}
}
