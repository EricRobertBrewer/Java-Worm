package worm;

import java.awt.event.KeyEvent;
import java.util.Random;

//TODO Separate Worm into Worm parent with protected attributes/methods and Worm2D child
public class Worm2D {
	
	public Worm2D(Settings settings) {
		mWidth = Settings.SIZE_WIDTH[settings.size];
		mHeight = Settings.SIZE_HEIGHT[settings.size];

		mSpace = new int[mWidth][mHeight];
		for (int i = 0; i < mWidth; i++) {
			for (int j = 0; j < mHeight; j++) {
				mSpace[i][j] = SPACE_EMPTY;
			}
		}

		mBody = new Cell[getMaxWiggleRoom()];
		// Head pops out of his hole in center of board
		Cell head = new Cell(mWidth / 2, mHeight / 2);
		mBody[mHeadIndex] = head;
		mSpace[head.x][head.y] = SPACE_WORM;

		mFoodCell = new FoodCell[getMaxWiggleRoom()];
		for (int i = 0; i < settings.food; i++) {
			placeRandomFood(FOOD_FRESHNESS_MAX, FOOD_RATE_OF_DECAY);
		}
	}

	private final int mWidth;
	
	public int getDimensionWidth() {
		return mWidth;
	}
	
	private final int mHeight;

	public int getDimensionHeight() {
		return mHeight;
	}
	
	/**
	 * The number of possible empty spaces on the board ie. X * Y. Used for array sizing.
	 */
	private final int getMaxWiggleRoom() {
		return mWidth * mHeight;
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
	
	/**
	 * @return length of worm including the head
	 */
	public int getLength() {
		return (mHeadIndex - mTailIndex + getMaxWiggleRoom()) % getMaxWiggleRoom() + 1;
	}

	/** @return True if there is space to place a new food on the board; otherwise, false */
	private boolean hasEmptySpacesOnBoard() {
		return (getLength() + mFoodCount) < getMaxWiggleRoom();
	}

	// TODO Make these settings
	public static final int FOOD_FRESHNESS_MAX = 100;
	public static final int FOOD_RATE_OF_DECAY = 1;
	public static final int FOOD_FRESHNESS_PER_GROWTH = 20;
	
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
	
	private final Random random = new Random();

	/**
	 * Place a food in a randomly chosen, legal space on the board.
	 * @param freshness
	 * @param rateOfDecay
	 * @return The newly placed {@code FoodCell}, or {@code null} if there were no empty spaces on the board
	 */
	public FoodCell placeRandomFood(int freshness, int rateOfDecay) {
		if (!hasEmptySpacesOnBoard()) {
			return null;
		}
		
		FoodCell foodCell = null;
		while (foodCell == null) {
			int x = random.nextInt(mWidth);
			int y = random.nextInt(mHeight);
			foodCell = placeFood(x, y, freshness, rateOfDecay);
		}
		return foodCell;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param freshness
	 * @param rateOfDecay
	 * @return the newly placed {@code FoodCell}, or {@code null} if the cell was occupied
	 */
	public FoodCell placeFood(int x, int y, int freshness, int rateOfDecay) {
		if (mSpace[x][y] == SPACE_EMPTY) {
			mSpace[x][y] = mFoodCount;
			mFoodCell[mFoodCount] = new FoodCell(x, y, freshness, rateOfDecay);
			mFoodCount++;
			return mFoodCell[mFoodCount-1]; // The newly added FoodCell
		}
		return null;
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

	/**
	 * Steps are executed in this order:
	 * Checks to see if move in the given direction is within dimensional bounds.
	 * Increments tail forward, or leaves it in place if body should grow (due to eaten food).
	 * Checks if the next space in the given direction is any part of the worm's body.
	 * Checks if the space is food. If so, add another food in an empty space at 100% freshness.
	 * Move the head forward in the given direction.
	 * All food decays at one times its rate of decay.
	 * @param direction
	 * @return true if the worm is attempting to move into an empty space; otherwise, false.
	 * Ways to die:
	 * 1. attempting to move out of dimensional bounds,
	 * 2. attempting to move into any part of the worm's body,
	 * 3. all available space is filled with either food or body spaces,
	 * 4. all remaining food disappears (decays).
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

		// TODO Remove bounds; wrap worm
		// Out of bounds
		if (headCandidateX < 0 || headCandidateX >= mWidth || headCandidateY < 0 || headCandidateY >= mHeight) {
			return false;
		}

		// Still in bounds
		// Move tail
		if (mTummySize > 0) { // Tail grows; tail does not move forward
			mTummySize--;
		} else { // Move the tail forward, too
			Cell tail = mBody[mTailIndex];
			mSpace[tail.x][tail.y] = SPACE_EMPTY;
			mBody[mTailIndex] = null;
			mTailIndex = (mTailIndex + 1) % getMaxWiggleRoom();
		}
		
		int candidateSpace = mSpace[headCandidateX][headCandidateY];
		if (candidateSpace == SPACE_WORM) { // Trying to eat itself
			return false;
		}

		// Legal move; Empty or Food?
		
		// Food! That means candidateSpace is the index of this food in the food array
		if (candidateSpace != SPACE_EMPTY) {
			int foodIndex = mSpace[headCandidateX][headCandidateY];
			mTummySize += getGrowthFromFood(mFoodCell[foodIndex].getFood().getFreshness(), FOOD_FRESHNESS_PER_GROWTH);
			if (!removeFoodAtIndex(foodIndex) || placeRandomFood(FOOD_FRESHNESS_MAX, FOOD_RATE_OF_DECAY) == null) {
				return false;
			}
		}
		
		// Move head
		Cell newHead = new Cell(headCandidateX, headCandidateY);
		mHeadIndex = (mHeadIndex + 1) % getMaxWiggleRoom(); // Increment head index
		mBody[mHeadIndex] = newHead; // Place new head in worm
		mSpace[newHead.x][newHead.y] = SPACE_WORM;
		
		// Food decays
		for (int i = 0; i < mFoodCount; i++) {
			if (mFoodCell[i] != null) {
				mFoodCell[i].getFood().decay();
				if (mFoodCell[i].getFood().isDecayed()) {
					removeFoodAtIndex(i);
					if (mFoodCount == 0) {
						return false;
					}
				}
			}
		}
		
		return true; // Didn't die; move was legal
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
}
