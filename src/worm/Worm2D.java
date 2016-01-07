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
	
	private static final int SPACE_WORM = -1;
	private static final int SPACE_EMPTY = 0;
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
		getInputMap().put(KeyStroke.getKeyStroke(DIRECTION_UP, 0), KEY_UP);
		getInputMap().put(KeyStroke.getKeyStroke(DIRECTION_LEFT, 0), KEY_LEFT);
		getInputMap().put(KeyStroke.getKeyStroke(DIRECTION_DOWN, 0), KEY_DOWN);
		getInputMap().put(KeyStroke.getKeyStroke(DIRECTION_RIGHT, 0), KEY_RIGHT);
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KEY_PAUSE);
		getActionMap().put(KEY_UP, pressedUp);
		getActionMap().put(KEY_LEFT, pressedLeft);
		getActionMap().put(KEY_DOWN, pressedDown);
		getActionMap().put(KEY_RIGHT, pressedRight);
		getActionMap().put(KEY_PAUSE, pressedPause);
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

		mNumMunchies = 5;
		mMunchie = new Cell[mNumMunchies];
		for (int i = 0; i < mNumMunchies; i++) {
			placeMunchie(i);
		}

		mTimer = new Timer(Settings.TIMER_DELAY[settings.speed], this);
		mTimer.setInitialDelay(0);
	}

	public int getLength() {
		return (mHeadIndex - mTailIndex + mMaxWiggleRoom) % mMaxWiggleRoom;
	}

	private static final int MUNCHIE_RANGE = 5;
	private final int mNumMunchies;
	private final Random random = new Random();
	private Cell mMunchie[];
	private int mTummySize = 2;

	private void placeMunchie(int index) {
		// True if there is space to place a new munchie on the board, otherwise, false (never enters loop)
		boolean hasRoomToPlace = ((getLength() + mNumMunchies) < mMaxWiggleRoom);
		boolean isPlaced = false;
		while (hasRoomToPlace && !isPlaced) {
			int x = random.nextInt(mMaxX);
			int y = random.nextInt(mMaxY);

			if (mBoard[x][y] == SPACE_EMPTY) {
				int munchieSize = random.nextInt(MUNCHIE_RANGE) + 1;
				mBoard[x][y] = munchieSize;
				mMunchie[index] = new Cell(x, y);

				isPlaced = true;
			}
		}
	}

	private static final int RECT_SIZE = 10;
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// TODO Pause screen

		// TODO Color of higher value munchies is deeper
		g.setColor(Color.MAGENTA);
		for (int i = 0; i < mNumMunchies; i++) {
			g.fillRect(mMunchie[i].getX() * RECT_SIZE, mMunchie[i].getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}

		g.setColor(Color.ORANGE);
		g.fillRect(mWorm[mHeadIndex].getX() * RECT_SIZE, mWorm[mHeadIndex].getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);

		g.setColor(Color.YELLOW);
		for (int i = 0; i < getLength(); i++) {
			Cell cell = mWorm[(mTailIndex + i) % mMaxWiggleRoom];
			g.fillRect(cell.getX() * RECT_SIZE, cell.getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);
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

				// Move head
				Cell newHead = new Cell(headCandidateX, headCandidateY);
				mHeadIndex = (mHeadIndex + 1) % mMaxWiggleRoom; // Increment head index
				mWorm[mHeadIndex] = newHead; // Place new head in worm
				mBoard[newHead.getX()][newHead.getY()] = SPACE_WORM;

				// Munchie!
				if (candidateSpace != SPACE_EMPTY) {
					mTummySize += candidateSpace;
					// TODO Each munchie's value is it's index in the array + 1, so we don't have to go through this for-loop
					for (int i = 0; i < mNumMunchies; i++) {
						if (mMunchie[i].getX() == newHead.getX() && mMunchie[i].getY() == newHead.getY()) {
							placeMunchie(i);
							break;
						}
					}
				}

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
