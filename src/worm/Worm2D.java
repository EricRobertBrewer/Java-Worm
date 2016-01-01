package worm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

public class Worm2D extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int RECT_SIZE = 10;
	private final int mMaxX;
	private final int mMaxY;
	/**
	 * The number of possible empty spaces on the board ie. X * Y. Used for
	 * array sizing.
	 */
	private final int mMaxWiggleRoom;
	
	protected final int getMaxWiggleRoom() {
		return mMaxWiggleRoom;
	}

	protected static final int SPACE_WORM = -1;
	protected static final int SPACE_EMPTY = 0;
	/** The board, which saves each space, whether the worm or empty or a munchie. */
	private int mBoard[][];

	private Cell mWorm[];
	/** Index of head within worm. */
	private int mHeadIndex = 0;
	private int mTailIndex = 0;

	/**
	 * Create the panel.
	 */
	public Worm2D(int x, int y) {
		this(x, y, 1);
	}

	public Worm2D(int x, int y, int munchies) {
		mMaxX = x;
		mMaxY = y;
		mMaxWiggleRoom = x * y;
		setPreferredSize(new Dimension(mMaxX * RECT_SIZE, mMaxY * RECT_SIZE));
		setBackground(Color.BLACK);

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

		mNumMunchies = munchies;
		mMunchie = new Cell[mNumMunchies];
		for (int i = 0; i < mNumMunchies; i++) {
			placeMunchie(i);
		}

		addKeyListener(new DirectionAdapter());
		setFocusable(true);

		mTimer = new Timer(TIMER_DELAY, this);
		mTimer.setInitialDelay(0);
		mTimer.start();
	}

	public int getWormLength() {
		return (mHeadIndex - mTailIndex + mMaxWiggleRoom) % mMaxWiggleRoom;
	}

	private static final int MUNCHIE_RANGE = 5;
	private final int mNumMunchies;
	private final Random random = new Random();
	private Cell mMunchie[];
	private int mTummySize = 2;

	protected void placeMunchie(int index) {
		boolean isPlaced = false;
		do {
			int x = random.nextInt(mMaxX);
			int y = random.nextInt(mMaxY);

			if (mBoard[x][y] == SPACE_EMPTY) {
				int munchieSize = random.nextInt(MUNCHIE_RANGE) + 1;
				mBoard[x][y] = munchieSize;
				mMunchie[index] = new Cell(x, y);

				isPlaced = true;
			}
		} while (!isPlaced);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(Color.MAGENTA);
		for (int i = 0; i < mNumMunchies; i++) {
			g.fillRect(mMunchie[i].getX() * RECT_SIZE, mMunchie[i].getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}

		g.setColor(Color.ORANGE);
		g.fillRect(mWorm[mHeadIndex].getX() * RECT_SIZE, mWorm[mHeadIndex].getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);

		g.setColor(Color.YELLOW);
		for (int i = 0; i < getWormLength(); i++) {
			Cell cell = mWorm[(mTailIndex + i) % mMaxWiggleRoom];
			g.fillRect(cell.getX() * RECT_SIZE, cell.getY() * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}
	}

	private static final int TIMER_DELAY = 120;
	private Timer mTimer;
	private boolean mIsWormDead = false;

	@Override
	public void actionPerformed(ActionEvent action) {
		if (mDirection != DIRECTION_PAUSE) {
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
				mIsWormDead = true;
			}

			if (!mIsWormDead) { // Still in bounds
				int candidateSpace = mBoard[headCandidateX][headCandidateY];

				if (candidateSpace == SPACE_WORM) { // Trying to eat itself
					mIsWormDead = true;
				} else { // Legal move; Empty or Munchie?

					// Move head
					Cell newHead = new Cell(headCandidateX, headCandidateY);
					mHeadIndex = (mHeadIndex + 1) % mMaxWiggleRoom; // Increment head index
					mWorm[mHeadIndex] = newHead; // Place new head in worm

					mBoard[newHead.getX()][newHead.getY()] = SPACE_WORM;

					if (candidateSpace != SPACE_EMPTY) {
						mTummySize += candidateSpace;
						for (int i = 0; i < mNumMunchies; i++) {
							if (mMunchie[i].getX() == newHead.getX() && mMunchie[i].getY() == newHead.getY()) {
								placeMunchie(i);
								break;
							}
						}
					}

					if (mTummySize > 0) { // Tail grows; tail stays
						mTummySize--;
					} else { // Move the tail forward, too
						Cell tail = mWorm[mTailIndex];
						mBoard[tail.getX()][tail.getY()] = SPACE_EMPTY;
						mTailIndex = (mTailIndex + 1) % mMaxWiggleRoom;
					}
				}
			}
		}

		repaint();
	}

	protected static final int DIRECTION_UP = KeyEvent.VK_W;
	protected static final int DIRECTION_LEFT = KeyEvent.VK_A;
	protected static final int DIRECTION_DOWN = KeyEvent.VK_S;
	protected static final int DIRECTION_RIGHT = KeyEvent.VK_D;
	protected static final int DIRECTION_PAUSE = KeyEvent.VK_SPACE;
	protected int mDirection = DIRECTION_PAUSE;

	protected class DirectionAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);

			int key = e.getKeyCode();
			switch (key) {
			case DIRECTION_UP:
				if (mDirection != DIRECTION_DOWN) {
					mDirection = key;
					mTimer.restart();
				}
				break;
			case DIRECTION_LEFT:
				if (mDirection != DIRECTION_RIGHT) {
					mDirection = key;
					mTimer.restart();
				}
				break;
			case DIRECTION_DOWN:
				if (mDirection != DIRECTION_UP) {
					mDirection = key;
					mTimer.restart();
				}
				break;
			case DIRECTION_RIGHT:
				if (mDirection != DIRECTION_LEFT) {
					mDirection = key;
					mTimer.restart();
				}
				break;
			case DIRECTION_PAUSE:
				mDirection = key;
				break;
			}
		}
	}
}
