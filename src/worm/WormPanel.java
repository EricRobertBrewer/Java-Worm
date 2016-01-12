package worm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class WormPanel extends JPanel implements ActionListener {
	
	private Worm2D mWorm;
	
	private WormListener mWormListener;
	
	// TODO Separate WormPanel (drawing) from Worm object (game mechanics)
	public WormPanel(WormListener wormListener, Settings settings) {
		mWormListener = wormListener;

		mWorm = new Worm2D(settings);
		
		setPreferredSize(new Dimension(mWorm.getDimensionWidth() * RECT_SIZE, mWorm.getDimensionHeight() * RECT_SIZE));
		
		setBackground(Color.BLACK);
		int[] directions = { Worm2D.DIRECTION_UP, Worm2D.DIRECTION_LEFT, Worm2D.DIRECTION_DOWN, Worm2D.DIRECTION_RIGHT, KeyEvent.VK_SPACE };
		String[] keys = { KEY_UP, KEY_LEFT, KEY_DOWN, KEY_RIGHT, KEY_PAUSE };
		Action[] actions = { pressedUp, pressedLeft, pressedDown, pressedRight, pressedPause };
		for (int i = 0; i < directions.length; i++) {
			getInputMap().put(KeyStroke.getKeyStroke(directions[i], 0), keys[i]);
			getActionMap().put(keys[i], actions[i]);
		}
		setFocusable(true);

		mTimer = new Timer(Settings.TIMER_DELAY[settings.speed], this);
		mTimer.setInitialDelay(0);
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
		for (int i = 0; i < mWorm.getFoodCount(); i++) {
			FoodCell foodCell = mWorm.getFoodCell(i);
			if (foodCell != null) {
				int x = foodCell.x * RECT_SIZE;
				int y = foodCell.y * RECT_SIZE;
				
				g.setColor(getColorBetweenLinear(COLOR_FOOD_FRESH, COLOR_FOOD_DECAYED, (float)foodCell.getFood().getFreshness() / Worm2D.FOOD_FRESHNESS_MAX));
				g.fillRect(x, y, RECT_SIZE, RECT_SIZE);
				
				int growthValue = Worm2D.getGrowthFromFood(foodCell.getFood().getFreshness(), Worm2D.FOOD_FRESHNESS_PER_GROWTH);
				g.setColor(COLOR_FOOD_GROWTH_INDICATOR);
				g.drawString(String.valueOf(growthValue), x + RECT_SIZE / 2 - 3, y + RECT_SIZE / 2 + 5);
			}
		}

		// Draw tail
		g.setColor(COLOR_WORM_TAIL);
		g.fillRect(mWorm.getTail().x * RECT_SIZE, mWorm.getTail().y * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		
		// Draw head
		g.setColor(COLOR_WORM_HEAD);
		g.fillRect(mWorm.getHead().x * RECT_SIZE, mWorm.getHead().y * RECT_SIZE, RECT_SIZE, RECT_SIZE);

		// Draw body
		g.setColor(COLOR_WORM_BODY);
		for (int i = 1; i < mWorm.getLength()-1; i++) {
			Cell cell = mWorm.getBody(i);
			g.fillRect(cell.x * RECT_SIZE, cell.y * RECT_SIZE, RECT_SIZE, RECT_SIZE);
		}

	}
	
	private Timer mTimer;

	@Override
	public void actionPerformed(ActionEvent action) {
		int length = mWorm.getLength();
		if (!mWorm.move(mDirection)) {
			wormDied();
			return;
		}
		if (length != mWorm.getLength()) {
			mWormListener.onLengthChanged(mWorm);
		}

		repaint();
	}
	
	private void wormDied() {
		mTimer.stop();
		// TODO Sweet worm animation where all sections of the worm starting from the tail become red
		mWormListener.onWormDied(mWorm);
		mWormListener = null;
	}
	
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
	
	private int mDirection = -1;

	private void changeDirection(int direction) {
		if (mDirection != Worm2D.getDirectionOpposite(direction)) {
			mDirection = direction;
			boolean wasTimerPaused = !mTimer.isRunning();
			mTimer.restart();
			if (wasTimerPaused) {
				mWormListener.onGameUnpaused(mWorm);
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
