package worm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class WormPanel extends JPanel {
	
	private Worm2D mWorm;
	
	private WormListener mWormListener;
	
	public WormPanel(WormListener wormListener, Settings settings) {
		mWormListener = wormListener;

		mWorm = new Worm2D(settings);
		
		setPreferredSize(new Dimension(
				mWorm.getDimensionWidth() * RECT_SIZE + FENCE_THICKNESS * 2,
				mWorm.getDimensionHeight() * RECT_SIZE + FENCE_THICKNESS * 2));
		setBackground(Color.BLACK);
		setFocusable(true);
		
		addActions();

		mTimer = new Timer(Settings.getTimerDelay(settings.getSpeed()), mTimerListener);
		mTimer.setInitialDelay(0);
		
		addMouseListener(mMouseListener);
	}
	
	private static final int RECT_SIZE = 12;
	
	private static final Color COLOR_WORM_HEAD = Color.RED;
	private static final Color COLOR_WORM_BODY = Color.YELLOW;
	private static final Color COLOR_WORM_TAIL = Color.ORANGE;
	private static final Color COLOR_WORM_GROWING_TAIL = Color.MAGENTA;
	
	private static final Color COLOR_FOOD_GROWTH_INDICATOR = Color.RED;
	private static final Font FONT_FOOD_GROWTH_INDICATOR = new Font(Font.MONOSPACED, 0, 14);
	private static final Color COLOR_FOOD_FRESH = Color.GREEN; // (0, 255, 0)
	private static final Color COLOR_FOOD_DECAYED = new Color(102, 51, 0);
	
	private static final int FENCE_THICKNESS = 8;
	private static final int FENCE_ARC_WIDTH = 2;
	private static final int FENCE_ARC_HEIGHT = 2;
	private static final Color COLOR_FENCE = Color.WHITE;
	
	private static final Color COLOR_WORM_DEAD = Color.GRAY;
	private static final Font FONT_GAME_OVER = new Font("Arial", Font.BOLD|Font.ITALIC, 36);
	private static final Color COLOR_GAME_OVER = new Color(255, 255, 255, 255 * 7 / 10);
	private static final Font FONT_EXIT = new Font("Arial", Font.BOLD, 14);
	private static final Color COLOR_EXIT = new Color(255, 255, 255, 255 * 6 / 10);
	
	/**
	 * Useful for drawing a color which gradually fades into another color.
	 * @param a starting color (0.0 away from {@code distance})
	 * @param b color to fade towards (will achieve this color if {@code distance} is 1.0)
	 * @param distance linear distance from {@code a} to {@code b}. Between 0.0 and 1.0.
	 * @return generated color {@code distance} between {@code a} and {@code b}
	 */
	private static Color getColorBetweenLinear(Color a, Color b, float distance) {
		float red = (a.getRed() * distance) + ((1 - distance) * b.getRed());
		float green = (a.getGreen() * distance) + ((1 - distance) * b.getGreen());
		float blue = (a.getBlue() * distance) + ((1 - distance) * b.getBlue());
		return new Color(red/255, green/255, blue/255);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// TODO Pause screen
		
		drawFood(g, COLOR_FOOD_FRESH, COLOR_FOOD_DECAYED, COLOR_FOOD_GROWTH_INDICATOR);
		if (!mIsWormDead) {
			drawTail(g, getLastDigestedFood() > 0 ? COLOR_WORM_GROWING_TAIL : COLOR_WORM_TAIL);
			drawHead(g, COLOR_WORM_HEAD);
			drawBody(g, COLOR_WORM_BODY);
		} else {
			drawTail(g, mDeathAnimationCount >= mWorm.getLength() ? COLOR_WORM_DEAD : COLOR_WORM_TAIL);
			drawHead(g, COLOR_WORM_DEAD);
			drawBody(g, COLOR_WORM_DEAD, COLOR_WORM_BODY, mWorm.getLength()-mDeathAnimationCount);
			drawGameOver(g);
		}
		drawFence(g, COLOR_FENCE);
	}
	
	private void drawFood(Graphics g, Color fresh, Color decayed, Color indicator) {
		g.setFont(FONT_FOOD_GROWTH_INDICATOR);
		for (int i = 0; i < mWorm.getFoodCount(); i++) {
			FoodCell foodCell = mWorm.getFoodCell(i);
			int x = foodCell.x * RECT_SIZE + FENCE_THICKNESS;
			int y = foodCell.y * RECT_SIZE + FENCE_THICKNESS;
			
			g.setColor(getColorBetweenLinear(fresh, decayed, (float)foodCell.getFood().getFreshness() / mWorm.getFoodFreshnessMax()));
			g.fillRect(x, y, RECT_SIZE, RECT_SIZE);
			
			int growthValue = Worm2D.getGrowthFromFood(foodCell.getFood().getFreshness(), mWorm.getFoodFreshnessPerGrowth());
			g.setColor(indicator);
			g.drawString(String.valueOf(growthValue), x + RECT_SIZE / 2 - 3, y + RECT_SIZE / 2 + 5);
		}
	}
	
	/**
	 * Used for drawing tail growth as a different color.
	 */
	private int[] mDigestedFood = {0, 0};
	
	/**
	 * Swap between 0 and 1.
	 */
	private int mDigestedFoodIndex = 0;
	
	private int getLastDigestedFood() {
		return mDigestedFood[1-mDigestedFoodIndex];
	}

	private void drawTail(Graphics g, Color c) {
		g.setColor(c);
		Cell tail = mWorm.getTail();
		int x = tail.x * RECT_SIZE + FENCE_THICKNESS;
		int y = tail.y * RECT_SIZE + FENCE_THICKNESS;
		g.fillRect(x, y, RECT_SIZE, RECT_SIZE);
	}
	
	private void drawHead(Graphics g, Color c) {
		g.setColor(c);
		Cell head = mWorm.getHead();
		int x = head.x * RECT_SIZE + FENCE_THICKNESS;
		int y = head.y * RECT_SIZE + FENCE_THICKNESS;
		g.fillRect(x, y, RECT_SIZE, RECT_SIZE);
	}
	
	private void drawBody(Graphics g, Color c) {
		drawBody(g, c, c, 0);
	}
	
	private void drawBody(Graphics g, Color fromHead, Color fromTail, int sectionsFromTail) {
		for (int i = 1; i < mWorm.getLength()-1; i++) {
			g.setColor(i < sectionsFromTail ? fromTail : fromHead);
			Cell cell = mWorm.getBody(i);
			int x = cell.x * RECT_SIZE + FENCE_THICKNESS;
			int y = cell.y * RECT_SIZE + FENCE_THICKNESS;
			g.fillRect(x, y, RECT_SIZE, RECT_SIZE);
		}
	}
	
	private void drawFence(Graphics g, Color c) {
		g.setColor(c);
		int x, y = FENCE_THICKNESS + RECT_SIZE * mWorm.getDimensionHeight();
		for (int i = 0; i < mWorm.getDimensionWidth(); i++) {
			x = i * RECT_SIZE + FENCE_THICKNESS;
			if (!mWorm.hasTunnelVertical(i)) {
				g.fillRoundRect(x, 0, RECT_SIZE, FENCE_THICKNESS, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
				g.fillRoundRect(x, y, RECT_SIZE, FENCE_THICKNESS, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
			} else {
				g.drawRoundRect(x, 0, RECT_SIZE, FENCE_THICKNESS, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
				g.drawRoundRect(x, y, RECT_SIZE, FENCE_THICKNESS, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
			}
		}
		x = FENCE_THICKNESS + RECT_SIZE * mWorm.getDimensionWidth();
		for (int j = 0; j < mWorm.getDimensionHeight(); j++) {
			y = j * RECT_SIZE + FENCE_THICKNESS;
			if (!mWorm.hasTunnelHorizontal(j)) {
				g.fillRoundRect(0, y, FENCE_THICKNESS, RECT_SIZE, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
				g.fillRoundRect(x, y, FENCE_THICKNESS, RECT_SIZE, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
			} else {
				g.drawRoundRect(0, y, FENCE_THICKNESS, RECT_SIZE, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
				g.drawRoundRect(x, y, FENCE_THICKNESS, RECT_SIZE, FENCE_ARC_WIDTH, FENCE_ARC_HEIGHT);
			}
		}
	}
	
	private void drawGameOver(Graphics g) {
		g.setColor(COLOR_GAME_OVER);
		g.setFont(FONT_GAME_OVER);
		int x = FENCE_THICKNESS + mWorm.getDimensionWidth() * RECT_SIZE / 2 - 112;
		int y = FENCE_THICKNESS + mWorm.getDimensionHeight() * RECT_SIZE / 2 + 2;
		g.drawString("GAME OVER", x, y);
		
		g.setColor(COLOR_EXIT);
		g.setFont(FONT_EXIT);
		x += 71;
		y += 22;
		g.drawString("Click to exit", x, y);
	}
	
	private Timer mTimer;
	
	private ActionListener mTimerListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent action) {
			int length = mWorm.getLength();
			if (!mWorm.move(mDirection)) {
				wormDied();
			} else {
				if (length != mWorm.getLength()) {
					mWormListener.onLengthChanged(mWorm);
				}
				
				mDigestedFoodIndex = 1 - mDigestedFoodIndex;
				mDigestedFood[mDigestedFoodIndex] = mWorm.getDigestingFood();
				
				repaint();
			}
		}
	};
	
	// TODO Change to state: alive, dead, paused, etc.
	private boolean mIsWormDead = false;
	
	private void wormDied() {
		mIsWormDead = true;
		removeActions();
		mWormListener.onWormDied(mWorm);
		drawDeathAnimation();
	}
	
	private int mDeathAnimationCount = 0;
	
	private void drawDeathAnimation() {
		mTimer.stop();
		mTimer.removeActionListener(mTimerListener);
		mTimer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
				mDeathAnimationCount++;
				if (mDeathAnimationCount > mWorm.getLength()) {
					mTimer.stop();
					mTimer.removeActionListener(this);
				}
			}
		});
		mTimer.setDelay(80);
		mTimer.start();
	}
	
	public void exit() {
		mTimer.stop();
		removeMouseListener(mMouseListener);
		mWormListener.onGameExited(mWorm);
		mWormListener = null;
	}
	
	private final MouseListener mMouseListener = new MouseListener() {
		@Override
		public void mouseReleased(MouseEvent e) {
		}
		@Override
		public void mousePressed(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			if (mIsWormDead) {
				exit();
			}
		}
	};
	
	private static final String KEY_UP = "up";
	private static final String KEY_LEFT = "left";
	private static final String KEY_DOWN = "down";
	private static final String KEY_RIGHT = "right";
	
	private final Action pressedUp = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(Worm2D.DIRECTION_UP);
		}
		private static final long serialVersionUID = 1L;
	};
	private final Action pressedLeft = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(Worm2D.DIRECTION_LEFT);
		}
		private static final long serialVersionUID = 1L;
	};
	private final Action pressedDown = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(Worm2D.DIRECTION_DOWN);
		}
		private static final long serialVersionUID = 1L;
	};
	private final Action pressedRight = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			changeDirection(Worm2D.DIRECTION_RIGHT);
		}
		private static final long serialVersionUID = 1L;
	};
	
	private static final String KEY_PAUSE = "pause";
	
	private final Action pressedPause = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mIsWormDead) {
				return;
			}
			if (mTimer.isRunning()) {
				mWormListener.onGamePaused(mWorm);
				mTimer.stop();
			} else {
				mTimer.start();
				mWormListener.onGameUnpaused(mWorm);
			}
		}
		private static final long serialVersionUID = 1L;
	};
	
	private static final int[] ALL_DIRECTIONS = { Worm2D.DIRECTION_UP, Worm2D.DIRECTION_LEFT, Worm2D.DIRECTION_DOWN, Worm2D.DIRECTION_RIGHT, KeyEvent.VK_SPACE };
	private static final String[] ALL_KEYS = { KEY_UP, KEY_LEFT, KEY_DOWN, KEY_RIGHT, KEY_PAUSE };
	private final Action[] mAllActions = { pressedUp, pressedLeft, pressedDown, pressedRight, pressedPause };
	
	private void addActions() {
		for (int i = 0; i < ALL_DIRECTIONS.length; i++) {
			getInputMap().put(KeyStroke.getKeyStroke(ALL_DIRECTIONS[i], 0), ALL_KEYS[i]);
			getActionMap().put(ALL_KEYS[i], mAllActions[i]);
		}
	}
	
	private void removeActions() {
		for (int i = 0; i < ALL_DIRECTIONS.length; i++) {
			getInputMap().remove(KeyStroke.getKeyStroke(ALL_DIRECTIONS[i], 0));
			getActionMap().remove(ALL_KEYS[i]);
		}
	}

	private int mDirection = -1;

	private void changeDirection(int direction) {
		if (mDirection != Worm2D.getDirectionOpposite(direction)) {
			mDirection = direction;
			boolean wasTimerRunning = mTimer.isRunning();
			mTimer.restart();
			if (!wasTimerRunning) {
				mWormListener.onGameUnpaused(mWorm);
			}
		}
	}

	private static final long serialVersionUID = 1L;
}
