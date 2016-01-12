package worm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

// TODO Separate Worm into Worm parent with protected attributes/methods and Worm2D child
public class Worm2D extends JPanel implements ActionListener {
	
	public interface WormListener {
		public void onWormDied(Worm2D worm);
		public void onGamePaused(Worm2D worm);
		public void onGameUnpaused(Worm2D worm);
		public void onLengthChanged(Worm2D worm);
	}
	
	private WormListener mWormListener;

	private final int mMaxX;
	private final int mMaxY;
	/**
	 * The number of possible empty spaces on the board ie. X * Y. Used for array sizing.
	 */
	private final int getMaxWiggleRoom() {
		return mMaxX * mMaxY;
	}
	
	private static final int SPACE_WORM = -2;
	private static final int SPACE_EMPTY = -1;
	/** The board, which saves each space, whether the worm or empty or food.
	 *  A positive number indicates a food and its index within the food array.
	 */
	private int mBoard[][];

	/** Circular queue */
	private Cell mWorm[];
	/** Index of head within worm. */
	private int mHeadIndex = 0;
	private int mTailIndex = 0;

	// TODO Separate WormPanel (drawing) from Worm object (game mechanics)
	public Worm2D(WormListener wormListener, Settings settings) {
		mWormListener = wormListener;
		
		mMaxX = Settings.SIZE_WIDTH[settings.size];
		mMaxY = Settings.SIZE_HEIGHT[settings.size];
		setPreferredSize(new Dimension(mMaxX * RECT_SIZE, mMaxY * RECT_SIZE));
		
		setBackground(Color.BLACK);
		int[] directions = { DIRECTION_UP, DIRECTION_LEFT, DIRECTION_DOWN, DIRECTION_RIGHT, KeyEvent.VK_SPACE };
		String[] keys = { KEY_UP, KEY_LEFT, KEY_DOWN, KEY_RIGHT, KEY_PAUSE };
		Action[] actions = { pressedUp, pressedLeft, pressedDown, pressedRight, pressedPause };
		for (int i = 0; i < directions.length; i++) {
			getInputMap().put(KeyStroke.getKeyStroke(directions[i], 0), keys[i]);
			getActionMap().put(keys[i], actions[i]);
		}
		setFocusable(true);

		mBoard = new int[mMaxX][mMaxY];
		for (int i = 0; i < mMaxX; i++) {
			for (int j = 0; j < mMaxY; j++) {
				mBoard[i][j] = SPACE_EMPTY;
			}
		}

		mWorm = new Cell[getMaxWiggleRoom()];
		// Head pops out of his hole in center of board
		Cell head = new Cell(mMaxX / 2, mMaxY / 2);
		mWorm[mHeadIndex] = head;
		mBoard[head.x][head.y] = SPACE_WORM;

		mFoodCell = new FoodCell[getMaxWiggleRoom()];
		for (int i = 0; i < settings.food; i++) {
			placeRandomFood(FOOD_FRESHNESS_MAX, FOOD_RATE_OF_DECAY);
		}

		mTimer = new Timer(Settings.TIMER_DELAY[settings.speed], this);
		mTimer.setInitialDelay(0);
	}

	/**
	 * 
	 * @return length of worm including the head
	 */
	public int getLength() {
		return (mHeadIndex - mTailIndex + getMaxWiggleRoom()) % getMaxWiggleRoom() + 1;
	}

	/** @return True if there is space to place a new food on the board; otherwise, false */
	private boolean hasEmptySpacesOnBoard() {
		return (getLength() + mFreshFood) < getMaxWiggleRoom();
	}

	private static final int FOOD_FRESHNESS_MAX = 100;
	private static final int FOOD_RATE_OF_DECAY = 1;
	private static final int FOOD_FRESHNESS_PER_GROWTH = 20;
	protected static int getGrowthFromFood(int freshness, int freshnessPerGrowth) {
		return (int) Math.ceil((double)freshness / (double)freshnessPerGrowth);
	}
	
	private int mFreshFood;
	private final Random random = new Random();
	private FoodCell[] mFoodCell;
	private int mTummySize = 1;
	
	/**
	 * 
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
			int x = random.nextInt(mMaxX);
			int y = random.nextInt(mMaxY);
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
		if (mBoard[x][y] == SPACE_EMPTY) {
			mBoard[x][y] = mFreshFood;
			mFoodCell[mFreshFood] = new FoodCell(x, y, freshness, rateOfDecay);
			mFreshFood++;
			return mFoodCell[mFreshFood-1]; // The newly added FoodCell
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
		mBoard[food.x][food.y] = SPACE_EMPTY;
		if (index != mFreshFood-1) { // Replace removed FoodCell with last FoodCell in array
			FoodCell lastFood = mFoodCell[mFreshFood-1];
			mBoard[lastFood.x][lastFood.y] = index;
			mFoodCell[index] = lastFood;
		}
		mFoodCell[mFreshFood-1] = null;
		mFreshFood--;
		return true;
	}
	
	private static final int RECT_SIZE = 11;
	private static final Color COLOR_WORM_HEAD = Color.RED;
	private static final Color COLOR_WORM_BODY = Color.YELLOW;
	private static final Color COLOR_WORM_TAIL = Color.ORANGE;
	private static final Color COLOR_FOOD_GROWTH_INDICATOR = Color.RED;
	private static final Color COLOR_FOOD_FRESH = Color.GREEN; // (0, 255, 0)
	private static final Color COLOR_FOOD_DECAYED = new Color(102, 51, 0);
	/**
	 * Useful for drawing a color which gradually fades into another color.
	 * @param a starting color (0.0 away from {@code distance})
	 * @param b color to fade towards (will achieve this color if {@code distance} is 1.0)
	 * @param distance linear distance from {@code a} to {@code b}. Between 0.0 and 1.0.
	 * @return a generated color {@code distance} between {@code a} and {@code b}
	 */
	private static final Color getColorBetweenLinear(Color a, Color b, float distance) {
		float red = (a.getRed() * distance) + ((1 - distance) * b.getRed());
		float green = (a.getGreen() * distance) + ((1 - distance) * b.getGreen());
		float blue = (a.getBlue() * distance) + ((1 - distance) * b.getBlue());
		return new Color(red/255, green/255, blue/255);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// TODO Pause screen

		// Draw food
		for (int i = 0; i < mFreshFood; i++) {
			if (mFoodCell[i] != null) {
				int x = mFoodCell[i].x * RECT_SIZE;
				int y = mFoodCell[i].y * RECT_SIZE;
				
				g.setColor(getColorBetweenLinear(COLOR_FOOD_FRESH, COLOR_FOOD_DECAYED, (float)mFoodCell[i].getFood().getFreshness() / FOOD_FRESHNESS_MAX));
				g.fillRect(x, y, RECT_SIZE, RECT_SIZE);
				
				int growthValue = getGrowthFromFood(mFoodCell[i].getFood().getFreshness(), FOOD_FRESHNESS_PER_GROWTH);
				g.setColor(COLOR_FOOD_GROWTH_INDICATOR);
				g.drawString(String.valueOf(growthValue), x + RECT_SIZE / 2 - 3, y + RECT_SIZE / 2 + 5);
			}
		}

		// Draw head
		g.setColor(COLOR_WORM_HEAD);
		g.fillRect(mWorm[mHeadIndex].x * RECT_SIZE, mWorm[mHeadIndex].y * RECT_SIZE, RECT_SIZE, RECT_SIZE);

		// Draw body
		g.setColor(COLOR_WORM_BODY);
		for (int i = 1; i < getLength()-1; i++) {
			Cell cell = mWorm[(mTailIndex + i) % getMaxWiggleRoom()];
			g.fillRect(cell.x * RECT_SIZE, cell.y * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}

		// Draw tail
		if (mTailIndex != mHeadIndex) {
			g.setColor(COLOR_WORM_TAIL);
			g.fillRect(mWorm[mTailIndex].x * RECT_SIZE, mWorm[mTailIndex].y * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}
	}

	private Timer mTimer;

	@Override
	public void actionPerformed(ActionEvent action) {
		Cell head = mWorm[mHeadIndex];
		int headCandidateX = head.x;
		int headCandidateY = head.y;
		switch (mDirection) {
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

		// Out of bounds
		if (headCandidateX < 0 || headCandidateX >= mMaxX || headCandidateY < 0 || headCandidateY >= mMaxY) {
			wormDied();
			return;
		} else { // Still in bounds
			int candidateSpace = mBoard[headCandidateX][headCandidateY];

			if (candidateSpace == SPACE_WORM) { // Trying to eat itself
				wormDied();
				return;
			} else { // Legal move; Empty or Food?

				// Food! That means candidateSpace is the index of this food in the array mFood
				if (candidateSpace != SPACE_EMPTY) {
					int foodIndex = mBoard[headCandidateX][headCandidateY];
					mTummySize += getGrowthFromFood(mFoodCell[foodIndex].getFood().getFreshness(), FOOD_FRESHNESS_PER_GROWTH);
					if (!removeFoodAtIndex(foodIndex) || placeRandomFood(FOOD_FRESHNESS_MAX, FOOD_RATE_OF_DECAY) == null) {
						wormDied();
						return;
					}
				}

				// TODO Be nice; move tail forward first, to allow head chasing tail in adjacent space
				// Move head
				Cell newHead = new Cell(headCandidateX, headCandidateY);
				mHeadIndex = (mHeadIndex + 1) % getMaxWiggleRoom(); // Increment head index
				mWorm[mHeadIndex] = newHead; // Place new head in worm
				mBoard[newHead.x][newHead.y] = SPACE_WORM;

				// Move tail
				if (mTummySize > 0) { // Tail grows; tail does not move forward
					mTummySize--;
					mWormListener.onLengthChanged(this);
				} else { // Move the tail forward, too
					Cell tail = mWorm[mTailIndex];
					mBoard[tail.x][tail.y] = SPACE_EMPTY;
					mTailIndex = (mTailIndex + 1) % getMaxWiggleRoom();
				}
				
				// Food decays
				for (int i = 0; i < mFreshFood; i++) {
					if (mFoodCell[i] != null) {
						mFoodCell[i].getFood().decay();
						if (mFoodCell[i].getFood().isDecayed()) {
							removeFoodAtIndex(i);
							if (mFreshFood == 0) {
								wormDied();
								break;
							}
						}
					}
				}
			}
		}

		repaint();
	}
	
	private void wormDied() {
		mTimer.stop();
		mWormListener.onWormDied(this);
		mWormListener = null;
	}

	private static final int DIRECTION_UP = KeyEvent.VK_W;
	private static final int DIRECTION_LEFT = KeyEvent.VK_A;
	private static final int DIRECTION_DOWN = KeyEvent.VK_S;
	private static final int DIRECTION_RIGHT = KeyEvent.VK_D;
	private static int getDirectionOpposite(int direction) {
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
	
	private static final String KEY_UP = "up";
	private static final String KEY_LEFT = "left";
	private static final String KEY_DOWN = "down";
	private static final String KEY_RIGHT = "right";
	private Action pressedUp = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(DIRECTION_UP);
		}
		private static final long serialVersionUID = 1L;
	};
	private Action pressedLeft = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(DIRECTION_LEFT);
		}
		private static final long serialVersionUID = 1L;
	};
	private Action pressedDown = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(DIRECTION_DOWN);
		}
		private static final long serialVersionUID = 1L;
	};
	private Action pressedRight = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(DIRECTION_RIGHT);
		}
		private static final long serialVersionUID = 1L;
	};
	
	private static final String KEY_PAUSE = "pause";
	private Action pressedPause = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mTimer.isRunning()) {
				mWormListener.onGamePaused(Worm2D.this);
				mTimer.stop();
			} else {
				mTimer.start();
				mWormListener.onGameUnpaused(Worm2D.this);
			}
		}
		private static final long serialVersionUID = 1L;
	};
	
	private int mDirection = -1;

	private void changeDirection(int direction) {
		if (mDirection != getDirectionOpposite(direction)) {
			mDirection = direction;
			boolean wasTimerPaused = !mTimer.isRunning();
			mTimer.restart();
			if (wasTimerPaused) {
				mWormListener.onGameUnpaused(this);
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
