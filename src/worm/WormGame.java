package worm;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import worm.Worm2D.WormListener;

public class WormGame extends JFrame implements ActionListener, WormListener {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WormGame frame = new WormGame();
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
	public WormGame() {
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
		JPanel worm = new Worm2D(this, mSettings);
		setContentPane(worm);
		setTitle("Worm | Press SPACE to Pause");
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}

	@Override
	public void onWormDied(Worm2D worm) {
		showMainPanel(worm.getLength());
	}

	@Override
	public void onGamePaused(Worm2D worm) {
		setTitle("PAUSED");
	}

	@Override
	public void onGameUnpaused(Worm2D worm) {
		setTitle("Score: " + worm.getLength());
	}

	@Override
	public void onLengthChanged(Worm2D worm) {
		setTitle("Score: " + worm.getLength());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
