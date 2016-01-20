package worm;

import java.awt.Color;
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

public class SettingsPanel extends JPanel {
	
	public SettingsPanel(Settings settings, ActionListener playButtonListener, int lastScore) {
		// TODO setPreferredSize
		
		// TODO Center all labels horizontally in frame
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JLabel titleLabel = new JLabel("Worm");
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		add(titleLabel);
		// TODO Save high score for each settings permutation, display
		JLabel highScoreLabel = new JLabel("High Score: ");
		highScoreLabel.setHorizontalAlignment(JLabel.CENTER);
		add(highScoreLabel);
		
		JLabel lastScoreLabel = new JLabel("Your score: " + String.valueOf(lastScore));
		lastScoreLabel.setHorizontalAlignment(JLabel.CENTER);
		lastScoreLabel.setVisible(lastScore > 1);
		add(lastScoreLabel);
		
		add(Box.createRigidArea(new Dimension(5, 5)));
		
		add(makeSizePanel(settings));
		
		add(makeSpeedPanel(settings));
		
		add(makeFoodPanel(settings));
		
		// TODO Add boolean and checkbox doesFoodDecay
		
		JLabel fenceAndTunnelErrorLabel = new JLabel("Fences and tunnels cannot both be 0.");
		fenceAndTunnelErrorLabel.setForeground(Color.RED);
		fenceAndTunnelErrorLabel.setHorizontalAlignment(JLabel.CENTER);
		fenceAndTunnelErrorLabel.setVisible(false);
		add(makeFencePanel(settings, fenceAndTunnelErrorLabel));
		add(makeTunnelPanel(settings, fenceAndTunnelErrorLabel));
		add(fenceAndTunnelErrorLabel);
		
		JButton playButton = new JButton("Play!");
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (settings.getFenceLength() == 0 && settings.getTunnelLength() == 0) {
					fenceAndTunnelErrorLabel.setVisible(true);
				} else {
					playButtonListener.actionPerformed(e);
				}
			}
		});
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
	
	private JPanel makeFencePanel(Settings settings, JLabel errorLabel) {
		JPanel fencePanel = new JPanel();
		fencePanel.setLayout(new BoxLayout(fencePanel, BoxLayout.LINE_AXIS));
		fencePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		fencePanel.add(new JLabel("Fence length:"));
		SpinnerNumberModel fenceSpinnerModel = new SpinnerNumberModel(settings.getFenceLength(), 0, 5, 1);
		JSpinner fenceSpinner = new JSpinner(fenceSpinnerModel);
		fenceSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int fences = (int) fenceSpinnerModel.getNumber();
				settings.setFenceLength(fences);
				if (fences > 0) {
					errorLabel.setVisible(false);
				}
			}
		});
		fencePanel.add(fenceSpinner);
		return fencePanel;
	}
	
	private JPanel makeTunnelPanel(Settings settings, JLabel errorLabel) {
		JPanel tunnelPanel = new JPanel();
		tunnelPanel.setLayout(new BoxLayout(tunnelPanel, BoxLayout.LINE_AXIS));
		tunnelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tunnelPanel.add(new JLabel("Tunnel length:"));
		SpinnerNumberModel tunnelSpinnerModel = new SpinnerNumberModel(settings.getTunnelLength(), 0, 5, 1);
		JSpinner tunnelSpinner = new JSpinner(tunnelSpinnerModel);
		tunnelSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int tunnels = (int) tunnelSpinnerModel.getNumber();
				settings.setTunnelLength(tunnels);
				if (tunnels > 0) {
					errorLabel.setVisible(false);
				}
			}
		});
		tunnelPanel.add(tunnelSpinner);
		return tunnelPanel;
	}

	private static final long serialVersionUID = 1L;
}
