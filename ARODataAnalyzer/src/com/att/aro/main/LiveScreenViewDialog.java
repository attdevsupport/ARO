package com.att.aro.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import com.att.aro.commonui.ImagePanel;


public class LiveScreenViewDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private ImagePanel imagePanel;
	

	/**
	 * Create the dialog.
	 */
	public LiveScreenViewDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(LiveScreenViewDialog.class.getResource("/com/att/aro/images/aro_24.png")));
		setModal(true);
		setTitle("Live Video Screen Capture");
		setResizable(false);
		setBounds(100, 100, 367, 722);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		String imagepath = "/com/att/aro/images/blackscreen.jpg";
	    Image image = Toolkit.getDefaultToolkit().getImage(LiveScreenViewDialog.class.getResource(imagepath));
	    imagePanel = new ImagePanel(image);
	    imagePanel.setBounds(0, 0, 360, 640);
	    
		contentPanel.add(imagePanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton stopButton = new JButton("Stop");
				stopButton.setIcon(new ImageIcon(LiveScreenViewDialog.class.getResource("/com/att/aro/images/X_active.png")));
				stopButton.setActionCommand("Stop");
				stopButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						onStop();
					}});
				buttonPane.add(stopButton);
				getRootPane().setDefaultButton(stopButton);
			}
		}
	}
	void onStop(){
		this.setVisible(false);
	}
	public void setImage(Image image){
		this.imagePanel.setImage(image);
	}
	public int getViewWidth(){
		return imagePanel.getWidth();
	}
	public int getViewHeight(){
		return imagePanel.getHeight();
	}
}
