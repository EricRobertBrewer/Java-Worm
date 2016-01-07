package worm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
		
		JPanel sizePanel = new JPanel();
		sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.LINE_AXIS));
		sizePanel.add(new JLabel(Settings.SIZE_LABEL_NAME));
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
		sizePanel.add(mSizeComboBox);
		add(sizePanel);
		
		JPanel speedPanel = new JPanel();
		speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.LINE_AXIS));
		speedPanel.add(new JLabel(Settings.SPEED_LABEL_NAME));
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
		speedPanel.add(mSpeedComboBox);
		add(speedPanel);
		
		JPanel munchiesPanel = new JPanel();
		munchiesPanel.setLayout(new BoxLayout(munchiesPanel, BoxLayout.LINE_AXIS));
		munchiesPanel.add(new JLabel(Settings.MUNCHIE_LABEL_NAME));
		SpinnerNumberModel munchieSpinnerModel = new SpinnerNumberModel(mSettings.munchies,
				Settings.MUNCHIE_MIN,
				Settings.MUNCHIE_MAX,
				Settings.MUNCHIE_STEP);
		JSpinner munchieSpinner = new JSpinner(munchieSpinnerModel);
		munchieSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mSettings.munchies = (int) munchieSpinnerModel.getNumber();
			}
		});
		munchiesPanel.add(munchieSpinner);
		add(munchiesPanel);
		
		JButton playButton = new JButton("Play!");
		playButton.addActionListener(playButtonListener);
		add(playButton);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
