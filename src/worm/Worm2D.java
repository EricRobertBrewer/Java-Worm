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
	 * The number of possible empty spaces on the board ie. X * Y. Used for
	 * array sizing.
	 */
	private final int mMaxWiggleRoom;
	
	private static final int SPACE_WORM = -2;
	private static final int SPACE_EMPTY = -1;
	/** The board, which saves each space, whether the worm or empty or a munchie. */
	private int mBoard[][];

	private Cell mWorm[];
	/** Index of head within worm. */
	private int mHeadIndex = 0;
	private int mTailIndex = 0;

	public Worm2D(WormListener wormListener, Settings settings) {
		mWormListener = wormListener;
		
		mMaxX = Settings.SIZE_WIDTH[settings.size];
		mMaxY = Settings.SIZE_HEIGHT[settings.size];
		mMaxWiggleRoom = mMaxX * mMaxY;
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

		mWorm = new Cell[mMaxWiggleRoom];
		// Head pops out of his hole in center of board
		Cell head = new Cell(mMaxX / 2, mMaxY / 2);
		mWorm[mHeadIndex] = head;
		mBoard[head.getX()][head.getY()] = SPACE_WORM;

		mMunchieMax = settings.munchies;
		mMunchie = new Cell[mMunchieMax];
		for (int i = 0; i < mMunchieMax; i++) {
			placeRandomMunchie();
		}

		mTimer = new Timer(Settings.TIMER_DELAY[settings.speed], this);
		mTimer.setInitialDelay(0);
	}

	/**
	 * 
	 * @return length of worm including the head
	 */
	public int getLength() {
		return (mHeadIndex - mTailIndex + mMaxWiggleRoom) % mMaxWiggleRoom + 1;
	}

	private static final int MUNCHIE_VALUE_MAX = 1;
	private final int mMunchieMax;
	private int mNumMunchies;
	private final Random random = new Random();
	private Cell[] mMunchie;
	private int mTummySize = 1;
	
	/** @return True if there is space to place a new munchie on the board; otherwise, false */
	private boolean hasEmptySpacesOnBoard() {
		return (getLength() + mNumMunchies) < mMaxWiggleRoom;
	}

	/**
	 * 
	 * @return The newly placed Cell, or null if there were no empty spaces on the board
	 */
	public Cell placeRandomMunchie() {
		if (!hasEmptySpacesOnBoard()) {
			return null;
		}
		
		for (int i = 0; i < mMunchieMax; i++) {
			if (mMunchie[i] == null) {
				return placeOrReplaceMunchieAtIndex(i);
			}
		}
		return null;
	}
	
	private boolean removeMunchieAtIndex(int index) {
		if (mMunchie[index] == null) {
			return false;
		}
		
		Cell munchie = mMunchie[index];
		mBoard[munchie.getX()][munchie.getY()] = SPACE_EMPTY;
		mMunchie[index] = null;
		mNumMunchies--;
		return true;
	}
	
	/**
	 * @return The newly placed munchie. If there was no room on the board to place a munchie, returns null;
	 * */
	private Cell placeOrReplaceMunchieAtIndex(int index) {
		if (!hasEmptySpacesOnBoard()) {
			return null;
		}
		
		Cell oldMunchie = mMunchie[index];
		Cell newMunchie = null;
		
		boolean isPlaced = false;
		while (!isPlaced) {
			int x = random.nextInt(mMaxX);
			int y = random.nextInt(mMaxY);

			if (mBoard[x][y] == SPACE_EMPTY) {
				if (oldMunchie != null) {
					// Clear old map space with non-existent index
					mBoard[oldMunchie.getX()][oldMunchie.getY()] = SPACE_EMPTY;
				} else {
					mNumMunchies++;
				}
				mBoard[x][y] = index;
				newMunchie = new Cell(x, y);
				mMunchie[index] = newMunchie;

				isPlaced = true;
			}
		}
		
		return newMunchie;
	}

	private static final int RECT_SIZE = 10;
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// TODO Pause screen

		// TODO Color of higher value munchies is deeper
		// Draw munchies
		g.setColor(Color.MAGENTA);
		for (int i = 0; i < mMunchieMax; i++) {
			if (mMunchie[i] != null) {
				g.fillRect(mMunchie[i].getX() * RECT_SIZE, mMunchie[i].getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);
			}
		}

		// Draw head
		g.setColor(Color.RED);
		g.fillRect(mWorm[mHeadIndex].getX() * RECT_SIZE, mWorm[mHeadIndex].getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);

		// Draw body
		g.setColor(Color.YELLOW);
		for (int i = 1; i < getLength()-1; i++) {
			Cell cell = mWorm[(mTailIndex + i) % mMaxWiggleRoom];
			g.fillRect(cell.getX() * RECT_SIZE, cell.getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}

		// Draw tail
		if (mTailIndex != mHeadIndex) {
			g.setColor(Color.ORANGE);
			g.fillRect(mWorm[mTailIndex].getX() * RECT_SIZE, mWorm[mTailIndex].getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}
	}

	private Timer mTimer;

	@Override
	public void actionPerformed(ActionEvent action) {
		Cell head = mWorm[mHeadIndex];
		int headCandidateX = head.getX();
		int headCandidateY = head.getY();
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
			} else { // Legal move; Empty or Munchie?

				// Munchie! That means candidateSpace is the index of this munchie in the array mMunchie
				if (candidateSpace != SPACE_EMPTY) {
					// TODO Make munchies fade away over time
					mTummySize += MUNCHIE_VALUE_MAX;
					if (placeOrReplaceMunchieAtIndex(mBoard[headCandidateX][headCandidateY]) == null) {
						wormDied();
						return;
					}
				}
				// If candidateSpace == SPACE_EMPTY, move forward as usual

				// Move head
				Cell newHead = new Cell(headCandidateX, headCandidateY);
				mHeadIndex = (mHeadIndex + 1) % mMaxWiggleRoom; // Increment head index
				mWorm[mHeadIndex] = newHead; // Place new head in worm
				mBoard[newHead.getX()][newHead.getY()] = SPACE_WORM;

				if (mTummySize > 0) { // Tail grows; tail does not move forward
					mTummySize--;
					mWormListener.onLengthChanged(this);
				} else { // Move the tail forward, too
					Cell tail = mWorm[mTailIndex];
					mBoard[tail.getX()][tail.getY()] = SPACE_EMPTY;
					mTailIndex = (mTailIndex + 1) % mMaxWiggleRoom;
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
			mTimer.restart();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
