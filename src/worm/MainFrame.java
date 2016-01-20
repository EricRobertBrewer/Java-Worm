package worm;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame implements ActionListener, WormListener {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private Settings mSettings;

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		mSettings = new Settings();
		
		showMainPanel(-1);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void showMainPanel(int lastScore) {
		setContentPane(new MainPanel(mSettings, this, lastScore));
		setTitle("Play Worm");
		setResizable(true);
		pack();
		setLocationRelativeTo(null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		beginGame();
	}
	
	private void beginGame() {
		// TODO Game Panel shouldn't have extra space, ~8 pixels now
		// TODO 2 player
		JPanel worm = new WormPanel(this, mSettings);
		setContentPane(worm);
		worm.requestFocusInWindow();
		
		setTitle("Worm | Press SPACE to Pause");
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}
	
	private static String getScoreDisplay(Worm2D worm) {
		return "Score: " + worm.getLength();
	}

	@Override
	public void onWormDied(Worm2D worm) {
		//setTitle("GAME OVER | " + getScoreDisplay(worm));
	}
	
	@Override
	public void onGameExited(Worm2D worm) {
		showMainPanel(worm.getLength());
	}

	@Override
	public void onGamePaused(Worm2D worm) {
		setTitle("PAUSED | " + getScoreDisplay(worm));
	}

	@Override
	public void onGameUnpaused(Worm2D worm) {
		setTitle(getScoreDisplay(worm));
	}

	@Override
	public void onLengthChanged(Worm2D worm) {
		setTitle(getScoreDisplay(worm));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
