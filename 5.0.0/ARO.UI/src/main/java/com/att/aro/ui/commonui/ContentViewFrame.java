package com.att.aro.ui.commonui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.model.diagnostic.ContentException;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class ContentViewFrame extends JFrame {

	private static final long serialVersionUID = 1L;

//	private static final Logger LOGGER = Logger.getLogger(ContentViewFrame.class.getName());

	private HttpRequestResponseInfo rrInfo;
	private Session session;
	private JScrollPane contentComponent;
	private JPanel buttonPanel;
	private JButton closeButton;

	private IHttpRequestResponseHelper httpHelper = ContextAware.getAROConfigContext().getBean(IHttpRequestResponseHelper.class);

	/**
	 * Initializes a new instance of the ContentViewFrame class using the
	 * specified content.
	 * 
	 * @param rrInfo
	 *            - The HttpRequestResponseInfo object containing the content.
	 */
	public ContentViewFrame(Session session,HttpRequestResponseInfo rrInfo)
			throws ContentException, IOException,Exception {
		this.rrInfo = rrInfo;
		this.session = session;
		initialize();
	}

	/**
	 * Returns the request/response object whose content is being viewed.
	 * 
	 * @return HttpRequestResponseInfo The request/response object associated
	 *         with this ContentViewer.
	 */
	public HttpRequestResponseInfo getRrInfo() {
		return rrInfo;
	}

	/**
	 * This method initializes the frame.
	 * 
	 * @throws ContentException
	 * @throws IOException
	 */
	private void initialize() throws ContentException, IOException,Exception {
		this.setMinimumSize(new Dimension(400, 400));
		this.setTitle(ResourceBundleHelper.getMessageString("fileChooser.rrContentViewerTitle"));
		this.setLocationByPlatform(true);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(getContentComponent(), BorderLayout.CENTER);
		panel.add(getButtonPanel(), BorderLayout.SOUTH);
		this.setContentPane(panel);
	}

	private JScrollPane getContentComponent() throws ContentException,
			IOException,Exception {
		if (contentComponent == null) {
			JComponent result = null;
			if (rrInfo.getContentType() != null
					&& rrInfo.getContentType().contains(
							ResourceBundleHelper.getMessageString("fileChooser.contentType.image"))) {
				ImageIcon icon = new ImageIcon(httpHelper.getContent(rrInfo,session));
				JLabel imagelJLabel = new JLabel(icon);
				imagelJLabel.setVerticalAlignment(JLabel.CENTER);
				result = imagelJLabel;
			} else {
				JTextArea contentTxtArea = new JTextArea(
						httpHelper.getContentString(rrInfo,session));
				contentTxtArea.setLineWrap(true);
				contentTxtArea.setEditable(false);
				result = contentTxtArea;
			}
			contentComponent = new JScrollPane(result);
		}
		return contentComponent;
	}

	/**
	 * @return the buttonPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new BorderLayout());
			JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
			panel.add(getCloseButton());
			buttonPanel.add(panel, BorderLayout.EAST);
		}
		return buttonPanel;
	}

	/**
	 * Initializes and returns the Close button for this ContentViewer.
	 * 
	 * @return JButton The Close button.
	 */
	public JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(ResourceBundleHelper.getMessageString("Button.close"));
			closeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ContentViewFrame.this.dispose();
				}

			});
		}
		return closeButton;
	}

}
