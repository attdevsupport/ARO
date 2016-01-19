package com.att.aro.ui.view.video;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.util.ImageHelper;
import com.att.aro.core.util.Util;
import com.att.aro.ui.commonui.ImagePanel;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.SharedAttributesProcesses;

public class LiveScreenViewDialog extends JDialog implements IVideoImageSubscriber {
	
	LoggerImpl log = new LoggerImpl(this.getClass().getName());
	
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private ImagePanel imagePanel;

	private JTextField timeBox;

	private long startTime;

	private SharedAttributesProcesses theView;

	/**
	 * Create the dialog.
	 * subscribes to the collector.
	 * 
	 * @param mainFrame
	 * 
	 * @param collector
	 */
	public LiveScreenViewDialog(SharedAttributesProcesses theView, IDataCollector collector) {
		this.theView = theView;
		setModalityType(ModalityType.MODELESS);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setTitle("Live Video Screen Capture");
		setResizable(false);
		setBounds(100, 100, 367, 722);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		Image image = (new ImageIcon(getClass().getResource(ResourceBundleHelper.getImageString("ImageBasePath") 
									+ ResourceBundleHelper.getImageString("Image.blackScreen"))))
									.getImage();

		imagePanel = new ImagePanel(image);
		imagePanel.setBounds(0, 0, 360, 640);

		contentPanel.add(imagePanel);

		getContentPane().add(dashBoardPane(), BorderLayout.SOUTH);

		log.info("subscribed");
		setVisible(true);
		collector.addVideoImageSubscriber(this);

	}

	private JPanel dashBoardPane() {

		JPanel dashBoardPane = new JPanel(new BorderLayout());

		{ // timer display
			JPanel timerPane = new JPanel();
			timeBox = new JTextField();
			timeBox.setText("00:00:00");
			timerPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			JTextArea timeBoxLabel = new JTextArea();
			timeBoxLabel.setBackground(SystemColor.window);
			timeBoxLabel.setText("Elapsed Time:");
			timerPane.add(timeBoxLabel);
			timerPane.add(timeBox);
			dashBoardPane.add(timerPane, BorderLayout.WEST);
		}

		{ // stop button
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			dashBoardPane.add(buttonPane, BorderLayout.EAST);
			{
				JButton stopButton = new JButton("Stop");
				stopButton.setIcon(//new ImageIcon(LiveScreenViewDialog.class.getResource("/com/att/aro/images/X_active.png")));
						new ImageIcon(getClass().getResource(ResourceBundleHelper.getImageString("ImageBasePath") + ResourceBundleHelper.getImageString("Image.bpFailDark"))));

				stopButton.setActionCommand("Stop");
				stopButton.setFocusable(false);
				stopButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						onStop();
					}
				});
				buttonPane.add(stopButton);
				getRootPane().setDefaultButton(stopButton);
			}
		}

		return dashBoardPane;
	}

	/**
	 * forwards user action STOP command to theView
	 */
	void onStop() {
		setVisible(false);
		theView.stopCollector();
	}

	/**
	 * Used to fit image to panel
	 * 
	 * @return width
	 */
	public int getViewWidth() {
		return imagePanel.getWidth();
	}

	/**
	 * Used to fit image to panel
	 * 
	 * @return height
	 */
	public int getViewHeight() {
		return imagePanel.getHeight();
	}

	@Override
	public void receiveImage(BufferedImage image) {
		log.debug("receiveImage");
		if (isVisible()) {
			if (startTime == 0) {
				startTimer();
			}
			BufferedImage newimg = ImageHelper.resize(image, getViewWidth(), getViewHeight());
			imagePanel.setImage(newimg);
			updateTimer();
		}
	}

	/**
	 * 
	 */
	private void startTimer() {
		startTime = System.currentTimeMillis();
	}

	/**
	 * 
	 */
	private void updateTimer() {
		String theTime = "";
		int elapsed = (int) ((System.currentTimeMillis() - startTime) / 1000);

		theTime = Util.formatHHMMSS(elapsed);
		
		log.info("elapsed = " + elapsed + "  time:" + theTime);
		timeBox.setText(theTime);

	}
	
}
