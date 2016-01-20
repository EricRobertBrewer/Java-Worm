package worm;

import java.awt.event.KeyEvent;
import java.util.Random;

//TODO Separate Worm into Worm parent with protected attributes/methods and Worm2D child
public class Worm2D {
	
	public Worm2D(Settings settings) {
		// TODO Just hold onto the settings. It's getting ridiculous
		
		mDimensionWidth = Settings.SIZE_WIDTH[settings.getSize()];
		mDimensionHeight = Settings.SIZE_HEIGHT[settings.getSize()];

		mSpace = new int[mDimensionWidth][mDimensionHeight];
		for (int i = 0; i < mDimensionWidth; i++) {
			for (int j = 0; j < mDimensionHeight; j++) {
				mSpace[i][j] = SPACE_EMPTY;
			}
		}

		mBody = new Cell[getMaxWiggleRoom()];
		// Head pops out of his hole in center of board
		Cell head = new Cell(mDimensionWidth / 2, mDimensionHeight / 2);
		mBody[mHeadIndex] = head;
		mSpace[head.x][head.y] = SPACE_WORM;
		
		mFoodFreshnessMax = settings.getFoodFreshnessMax();
		mFoodRateOfDecay = settings.getFoodRateOfDecay();
		mFoodFreshnessPerGrowth = settings.getFoodFreshnessPerGrowth();

		mFoodCell = new FoodCell[getMaxWiggleRoom()];
		for (int i = 0; i < settings.getFood(); i++) {
			placeRandomFood(mFoodFreshnessMax, mFoodRateOfDecay);
		}
		
		mHasTunnelVertical = new boolean[mDimensionWidth];
		int vert = 0;
		while (vert < mDimensionWidth) {
			for (int i = 0; i < settings.getFenceLength() && vert < mDimensionWidth; i++, vert++) {
				mHasTunnelVertical[vert] = false;
			}
			for (int i = 0; i < settings.getTunnelLength() && vert < mDimensionWidth; i++, vert++) {
				mHasTunnelVertical[vert] = true;
			}
		}
		mHasTunnelHorizontal = new boolean[mDimensionHeight];
		int horiz = 0;
		while (horiz < mDimensionHeight) {
			for (int i = 0; i < settings.getFenceLength() && horiz < mDimensionHeight; i++, horiz++) {
				mHasTunnelHorizontal[horiz] = false;
			}
			for (int i = 0; i < settings.getTunnelLength() && horiz < mDimensionHeight; i++, horiz++) {
				mHasTunnelHorizontal[horiz] = true;
			}
		}
	}

	private final int mDimensionWidth;
	
	public int getDimensionWidth() {
		return mDimensionWidth;
	}
	
	private final int mDimensionHeight;

	public int getDimensionHeight() {
		return mDimensionHeight;
	}
	
	/**
	 * The number of possible empty spaces on the board ie. X * Y. Used for array sizing.
	 */
	private final int getMaxWiggleRoom() {
		return mDimensionWidth * mDimensionHeight;
	}
	
	public static final int SPACE_WORM = -2;
	public static final int SPACE_EMPTY = -1;
	
	/** The board, which saves each space, whether the worm or empty or food.
	 *  A positive number indicates a food and its index within the food array.
	 */
	private int mSpace[][];
	
	/**
	 * @param x
	 * @param y
	 * @return one of either:
	 * worm ({@code SPACE_WORM}),
	 * empty ({@code SPACE_EMPTY}),
	 * or food (indicated by a non-negative integer)
	 */
	public int getSpace(int x, int y) {
		return mSpace[x][y];
	}

	/** Circular queue */
	private Cell mBody[];
	
	public Cell getBody(int sectionsFromTail) {
		int index = (mTailIndex + sectionsFromTail) % getMaxWiggleRoom();
		return mBody[index];
	}
	
	/** Index of head within worm. */
	private int mHeadIndex = 0;
	
	public Cell getHead() {
		return mBody[mHeadIndex];
	}
	
	private int mTailIndex = 0;
	
	public Cell getTail() {
		return mBody[mTailIndex];
	}
	
	private boolean mIsTailRetracted = false;
	
	/**
	 * Sometimes, {@code getLength()} will be called in the middle of {@code move(int)},
	 * ie. after {@code move(int)} has returned {@code false}, the tail index will have been incremented
	 * but the head index will have not. We keep track of in order to display a correct score.
	 * @return true if the tail has retracted (only during {@code move(int)}), otherwise, false
	 */
	public boolean isTailRetracted() {
		return mIsTailRetracted;
	}
	
	/**
	 * Better to use when drawing as compared to {@code getScore()}.
	 * @return length of worm including the head
	 */
	public int getLength() {
		return (mHeadIndex - mTailIndex + getMaxWiggleRoom()) % getMaxWiggleRoom() + 1;
	}
	
	/**
	 * @return score of game. Considers tail retraction after game loss which occurs
	 * in {@code move(int)} due to eating worm body.
	 */
	public int getScore() {
		return getLength() + (isTailRetracted() ? 1 : 0);
	}

	/** @return true if there is space to place a new food on the board; otherwise, false */
	private boolean hasEmptySpacesOnBoard() {
		return (getLength() + mFoodCount) < getMaxWiggleRoom();
	}

	private int mFoodFreshnessMax;
	
	public int getFoodFreshnessMax() {
		return mFoodFreshnessMax;
	}
	
	private int mFoodRateOfDecay;
	
	public int getFoodRateOfDecay() {
		return mFoodRateOfDecay;
	}
	
	private int mFoodFreshnessPerGrowth;
	
	public int getFoodFreshnessPerGrowth() {
		return mFoodFreshnessPerGrowth;
	}
	
	public static int getGrowthFromFood(int freshness, int freshnessPerGrowth) {
		return (int) Math.ceil((double)freshness / (double)freshnessPerGrowth);
	}
	
	private int mFoodCount;
	
	public int getFoodCount() {
		return mFoodCount;
	}
	
	private FoodCell[] mFoodCell;
	
	public FoodCell getFoodCell(int index) {
		return mFoodCell[index];
	}
	
	private int mTummySize = 1;
	
	/**
	 * @return number of body segments still waiting to be grown from tail
	 */
	public int getDigestingFood() {
		return mTummySize;
	}
	
	private final Random random = new Random();

	/**
	 * Place a food in a randomly chosen, legal space on the board.
	 * @param freshness
	 * @param rateOfDecay
	 * @return the newly placed {@code FoodCell}, or {@code null} if there were no empty spaces on the board
	 */
	public FoodCell placeRandomFood(int freshness, int rateOfDecay) {
		if (!hasEmptySpacesOnBoard()) {
			return null;
		}
		
		FoodCell foodCell = null;
		while (foodCell == null) {
			int x = random.nextInt(mDimensionWidth);
			int y = random.nextInt(mDimensionHeight);
			foodCell = placeFood(x, y, freshness, rateOfDecay);
		}
		return foodCell;
	}
	
	/**
	 * Place a food in a specified location
	 * @param x
	 * @param y
	 * @param freshness
	 * @param rateOfDecay
	 * @return the newly placed {@code FoodCell}, or {@code null} if the cell was occupied
	 */
	public FoodCell placeFood(int x, int y, int freshness, int rateOfDecay) {
		if (mSpace[x][y] != SPACE_EMPTY) {
			return null;
		}
		mSpace[x][y] = mFoodCount;
		mFoodCell[mFoodCount] = new FoodCell(x, y, freshness, rateOfDecay);
		mFoodCount++;
		return mFoodCell[mFoodCount-1]; // The newly added FoodCell
	}

	/**
	 * Don't iteratively call this function starting at the end of the array. OK if starting from the beginning.
	 * @param index index in food array
	 * @return true if a food has been removed. Otherwise, false.
	 */
	private boolean removeFoodAtIndex(int index) {
		if (mFoodCell[index] == null) {
			return false;
		}
		
		FoodCell food = mFoodCell[index];
		mSpace[food.x][food.y] = SPACE_EMPTY;
		if (index != mFoodCount-1) { // Replace removed FoodCell with last FoodCell in array
			FoodCell lastFood = mFoodCell[mFoodCount-1];
			mSpace[lastFood.x][lastFood.y] = index;
			mFoodCell[index] = lastFood;
		}
		mFoodCell[mFoodCount-1] = null;
		mFoodCount--;
		return true;
	}
	
	private boolean mHasTunnelVertical[];
	private boolean mHasTunnelHorizontal[];
	
	public boolean hasTunnelVertical(int x) {
		return mHasTunnelVertical[x];
	}
	
	public void setHasTunnelVertical(int x, boolean hasTunnel) {
		mHasTunnelVertical[x] = hasTunnel;
	}

	public boolean hasTunnelHorizontal(int y) {
		return mHasTunnelHorizontal[y];
	}
	
	public void setHasTunnelHorizontal(int y, boolean hasTunnel) {
		mHasTunnelHorizontal[y] = hasTunnel;
	}

	/**
	 * Steps are executed in this order:
	 * <ol>
	 * <li>Checks to see if move in the given direction is within dimensional <b>bounds</b>.</li>
	 * <li>Increments <b>tail</b> forward, or leaves it in place if body should grow (due to eaten food).</li>
	 * <li>Checks if the next space in the given direction is any part of the worm's <b>body</b>.</li>
	 * <li>Checks if the space is <b>food</b>. If so, add another food in an empty space at 100% freshness.</li>
	 * <li>Move the <b>head</b> forward in the given direction.</li>
	 * <li>All food <b>decays</b> at one times its rate of decay.</li>
	 * </ol>
	 * @param direction
	 * @return true if the worm is attempting to move into an empty space; otherwise, false.
	 * Ways to die:
	 * <ol>
	 * <li>attempting to move out of dimensional bounds</li>
	 * <li>attempting to move into any part of the worm's body</li>
	 * <li>all available space is filled with either food or body spaces</li>
	 * <li>all remaining food disappears (decays).</li>
	 * </ol>
	 * <b>Note:</b> Attempting to move after {@code false} is returned could lead to adverse behavior.
	 */
	public boolean move(int direction) {
		Cell head = mBody[mHeadIndex];
		int headCandidateX = head.x;
		int headCandidateY = head.y;
		switch (direction) {
		case DIRECTION_UP:
			headCandidateY--;
			break;
		case DIRECTION_LEFT:
			headCandidateX--;
			break;
		case DIRECTION_DOWN:
			headCandidateY++;
			break;
		case DIRECTION_RIGHT:
			headCandidateX++;
			break;
		}

		// Check out of bounds and presence of tunnel or fence
		if (headCandidateX < 0 || headCandidateX >= mDimensionWidth || headCandidateY < 0 || headCandidateY >= mDimensionHeight) {
			if (getAxis(direction) == Axis.HORIZONTAL && hasTunnelHorizontal(headCandidateY)) {
				headCandidateX = (headCandidateX + mDimensionWidth) % mDimensionWidth; // Enter tunnel, pop out on other side
			} else if (getAxis(direction) == Axis.VERTICAL && hasTunnelVertical(headCandidateX)) {
				headCandidateY = (headCandidateY + mDimensionHeight) % mDimensionHeight; // Enter tunnel, pop out on other side
			} else {
				return false;
			}
		}

		// Move tail
		if (mTummySize > 0) { // Tail grows; tail does not move forward
			mTummySize--;
		} else { // Move the tail forward, too
			Cell tail = mBody[mTailIndex];
			mSpace[tail.x][tail.y] = SPACE_EMPTY;
			mBody[mTailIndex] = null;
			mTailIndex = (mTailIndex + 1) % getMaxWiggleRoom();
			mIsTailRetracted = true;
		}
		
		int candidateSpace = mSpace[headCandidateX][headCandidateY];
		if (candidateSpace == SPACE_WORM) { // Trying to eat itself
			return false;
		}

		// Legal move; Empty or Food?
		
		// Food! That means candidateSpace is the index of this food in the food array
		if (candidateSpace != SPACE_EMPTY) {
			int foodIndex = candidateSpace;
			mTummySize += getGrowthFromFood(mFoodCell[foodIndex].getFood().getFreshness(), mFoodFreshnessPerGrowth);
			if (!removeFoodAtIndex(foodIndex) || placeRandomFood(mFoodFreshnessMax, mFoodRateOfDecay) == null) {
				return false;
			}
		}
		
		// Move head
		Cell newHead = new Cell(headCandidateX, headCandidateY);
		mHeadIndex = (mHeadIndex + 1) % getMaxWiggleRoom(); // Increment head index
		mBody[mHeadIndex] = newHead; // Place new head in worm
		mSpace[newHead.x][newHead.y] = SPACE_WORM;
		mIsTailRetracted = false;
		
		return decayAll() > 1;
	}
		
	public int decayAll() {
		for (int i = 0; i < mFoodCount; i++) {
			if (mFoodCell[i] != null) {
				mFoodCell[i].getFood().decay();
				if (mFoodCell[i].getFood().isDecayed()) {
					removeFoodAtIndex(i); // Causes mFoodCount to decrement! Be careful!
					i--;
				}
			}
		}
		
		return mFoodCount;
	}

	public static final int DIRECTION_UP = KeyEvent.VK_W;
	public static final int DIRECTION_LEFT = KeyEvent.VK_A;
	public static final int DIRECTION_DOWN = KeyEvent.VK_S;
	public static final int DIRECTION_RIGHT = KeyEvent.VK_D;
	
	public static int getDirectionOpposite(int direction) {
		switch (direction) {
		case DIRECTION_UP:
			return DIRECTION_DOWN;
		case DIRECTION_DOWN:
			return DIRECTION_UP;
		case DIRECTION_LEFT:
			return DIRECTION_RIGHT;
		case DIRECTION_RIGHT:
			return DIRECTION_LEFT;
		}
		return direction;
	}
	
	enum Axis {
		VERTICAL,
		HORIZONTAL
	}
	
	public static Axis getAxis(int direction) {
		switch (direction) {
		case DIRECTION_UP:
		case DIRECTION_DOWN:
			return Axis.VERTICAL;
		case DIRECTION_LEFT:
		case DIRECTION_RIGHT:
			return Axis.HORIZONTAL;
		}
		return null;
	}
}
