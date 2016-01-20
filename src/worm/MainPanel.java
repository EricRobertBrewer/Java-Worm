package worm;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainPanel extends JPanel {
	
	public MainPanel(Settings settings, ActionListener playButtonListener, int lastScore) {
		// TODO Add borders, glue, etc.
		// TODO setPreferredSize
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		add(new JLabel("Worm"));
		// TODO Save high score for each settings permutation, display
		add(new JLabel("High Score: "));
		if (lastScore > 1) {
			add(new JLabel("Your score: " + String.valueOf(lastScore)));
		}
		
		add(Box.createRigidArea(new Dimension(5, 5)));
		
		add(makeSizePanel(settings));
		
		add(makeSpeedPanel(settings));
		
		add(makeFoodPanel(settings));
		
		// TODO Add boolean doesFoodDecay
		
		add(makeFencePanel(settings));
		
		add(makeTunnelPanel(settings));
		
		JButton playButton = new JButton("Play!");
		playButton.addActionListener(playButtonListener);
		add(playButton);
	}
	
	private JPanel makeSizePanel(Settings settings) {
		JPanel sizePanel = new JPanel();
		sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.LINE_AXIS));
		sizePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sizePanel.add(new JLabel(Settings.SIZE_LABEL_NAME));
		JComboBox<String> sizeComboBox = new JComboBox<String>();
		for (int i = 0; i < Settings.SIZE_NAMES.length; i++) {
			sizeComboBox.addItem(Settings.SIZE_NAMES[i]);
		}
		sizeComboBox.setSelectedIndex(settings.getSize());
		sizeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settings.setSize(sizeComboBox.getSelectedIndex());
			}
		});
		sizePanel.add(sizeComboBox);
		return sizePanel;
	}
	
	private JPanel makeSpeedPanel(Settings settings) {
		// TODO Make speed selectable by slider
		JPanel speedPanel = new JPanel();
		speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.PAGE_AXIS));
		speedPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JSlider speedSlider = new JSlider(Settings.SPEED_MIN, Settings.SPEED_MAX, settings.getSpeed());
		
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.LINE_AXIS));
		outputPanel.add(new JLabel(Settings.SPEED_LABEL_NAME));
		SpinnerNumberModel speedSpinnerModel = new SpinnerNumberModel(settings.getSpeed(),
				Settings.SPEED_MIN,
				Settings.SPEED_MAX,
				Settings.SPEED_STEP);
		JSpinner speedSpinner = new JSpinner(speedSpinnerModel);
		speedSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int speed = (int) speedSpinnerModel.getNumber();
				settings.setSpeed(speed);
				speedSlider.setValue(speed);
			}
		});
		outputPanel.add(speedSpinner);
		speedPanel.add(outputPanel);
		
		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				speedSpinnerModel.setValue(speedSlider.getValue());
			}
		});
		speedPanel.add(speedSlider);
		
		return speedPanel;
	}
	
	private JPanel makeFoodPanel(Settings settings) {
		JPanel foodPanel = new JPanel();
		foodPanel.setLayout(new BoxLayout(foodPanel, BoxLayout.LINE_AXIS));
		foodPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		foodPanel.add(new JLabel(Settings.FOOD_LABEL_NAME));
		SpinnerNumberModel foodSpinnerModel = new SpinnerNumberModel(settings.getFood(),
				Settings.FOOD_MIN,
				Settings.FOOD_MAX,
				Settings.FOOD_STEP);
		JSpinner foodSpinner = new JSpinner(foodSpinnerModel);
		foodSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				settings.setFood((int) foodSpinnerModel.getNumber());
			}
		});
		foodPanel.add(foodSpinner);
		return foodPanel;
	}
	
	private JPanel makeFencePanel(Settings settings) {
		JPanel fencePanel = new JPanel();
		fencePanel.setLayout(new BoxLayout(fencePanel, BoxLayout.LINE_AXIS));
		fencePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		fencePanel.add(new JLabel("Fence length:"));
		SpinnerNumberModel fenceSpinnerModel = new SpinnerNumberModel(settings.getFenceLength(), 0, 5, 1);
		JSpinner fenceSpinner = new JSpinner(fenceSpinnerModel);
		fenceSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				settings.setFenceLength((int) fenceSpinnerModel.getNumber());
			}
		});
		fencePanel.add(fenceSpinner);
		return fencePanel;
	}
	
	private JPanel makeTunnelPanel(Settings settings) {
		JPanel tunnelPanel = new JPanel();
		tunnelPanel.setLayout(new BoxLayout(tunnelPanel, BoxLayout.LINE_AXIS));
		tunnelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tunnelPanel.add(new JLabel("Tunnel length:"));
		SpinnerNumberModel tunnelSpinnerModel = new SpinnerNumberModel(settings.getTunnelLength(), 0, 5, 1);
		JSpinner tunnelSpinner = new JSpinner(tunnelSpinnerModel);
		tunnelSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				settings.setTunnelLength((int) tunnelSpinnerModel.getNumber());
			}
		});
		tunnelPanel.add(tunnelSpinner);
		return tunnelPanel;
	}

	private static final long serialVersionUID = 1L;
}
