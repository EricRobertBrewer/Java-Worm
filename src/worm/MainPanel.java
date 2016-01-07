package worm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainPanel extends JPanel {
	
	private JComboBox<String> mSizeComboBox;
	private JComboBox<String> mSpeedComboBox;
	
	private Settings mSettings;
	
	public MainPanel(Settings settings, ActionListener playButtonListener, int lastScore) {
		// TODO Add borders, glue, etc.
		// TODO setPreferredSize
		mSettings = settings;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		add(new JLabel("Worm"));
		// TODO Save high score for each settings permutation, display
		add(new JLabel("High Score: "));
		if (lastScore > 1) {
			add(new JLabel("Your score: " + String.valueOf(lastScore)));
		}
		
		JPanel settingsPane = new JPanel();
		settingsPane.setLayout(new BoxLayout(settingsPane, BoxLayout.LINE_AXIS));
		mSizeComboBox = new JComboBox<String>();
		for (int i = 0; i < Settings.SIZE_NAMES.length; i++) {
			mSizeComboBox.addItem(Settings.SIZE_NAMES[i]);
		}
		mSizeComboBox.setSelectedIndex(mSettings.size);
		mSizeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mSettings.size = mSizeComboBox.getSelectedIndex();
			}
		});
		settingsPane.add(mSizeComboBox);
		mSpeedComboBox = new JComboBox<String>();
		for (int i = 0; i < Settings.SPEED_NAMES.length; i++) {
			mSpeedComboBox.addItem(Settings.SPEED_NAMES[i]);
		}
		mSpeedComboBox.setSelectedIndex(mSettings.speed);
		mSpeedComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mSettings.speed = mSpeedComboBox.getSelectedIndex();
			}
		});
		settingsPane.add(mSpeedComboBox);
		add(settingsPane);
		
		JButton playButton = new JButton("Play!");
		playButton.addActionListener(playButtonListener);
		add(playButton);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
