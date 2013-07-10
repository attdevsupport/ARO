package com.att.aro.bp.spriteimage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.att.aro.commonui.DataTable;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.util.Util;

public class SpriteImageResultPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(SpriteImageResultPanel.class.getName());

	private static final int ROW_HEIGHT = 20;
	private static final int NO_OF_ROWS = 6;
	private static final int SCROLL_PANE_HEIGHT = SpriteImageResultPanel.NO_OF_ROWS * SpriteImageResultPanel.ROW_HEIGHT;
	private static final int SCROLL_PANE_LENGTH = 300;

	private JLabel title;
	private JPanel contentPanel;
	private JScrollPane scrollPane;
	private SpriteImageTableModel tableModel;
	private DataTable<SpriteImageEntry> contentTable;

	/**
	 * Initializes a new instance of the SpriteImageResultPanel class.
	 * 
	 */
	public SpriteImageResultPanel() {
		this.setLayout(new BorderLayout());
		this.add(getContentPanel(), BorderLayout.CENTER);
		JPanel contentPanelWidth = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel contentPanelWidthAdjust = new JPanel(new GridBagLayout());
		contentPanelWidthAdjust.add(contentPanelWidth, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(
						5, 5, 5, 30), 0, 0));
		contentPanelWidthAdjust.setBackground(Color.WHITE);
		this.add(contentPanelWidthAdjust, BorderLayout.EAST);
	}

	/**
	 * Returns the title label.
	 * 
	 * @return JLabel title 
	 */
	private JLabel getTitle() {
		if (this.title == null) {
			this.title = new JLabel(Util.RB.getString("spriteimages.table.header"), SwingConstants.CENTER);
		}
		return this.title;
	}

	/**
	 * Initializes and returns the content panel.
	 * 
	 * @return JPanel content panel
	 */
	private JPanel getContentPanel() {
		if (this.contentPanel == null) {
			contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(getTitle(), BorderLayout.NORTH);
			contentPanel.add(getScrollPane(), BorderLayout.CENTER);
		}
		return this.contentPanel;
	}

	/**
	 * Initializes and returns the Scroll Pane.
	 * 
	 * @return JScrollPane scroll pane
	 */
	private JScrollPane getScrollPane() {
		if (this.scrollPane == null) {
			this.scrollPane = new JScrollPane(getContentTable());
			this.scrollPane.setPreferredSize(new Dimension(SpriteImageResultPanel.SCROLL_PANE_LENGTH, SpriteImageResultPanel.SCROLL_PANE_HEIGHT));
		}
		return this.scrollPane;
	}

	/**
	 * Initializes and returns the content table.
	 * 
	 * @return DataTable content table
	 */
	private DataTable<SpriteImageEntry> getContentTable() {
		if (this.contentTable == null) {
			this.tableModel = new SpriteImageTableModel();
			this.contentTable = new DataTable<SpriteImageEntry>(this.tableModel);
			this.contentTable.setAutoCreateRowSorter(true);
			this.contentTable.setGridColor(Color.LIGHT_GRAY);
			this.contentTable.setRowHeight(SpriteImageResultPanel.ROW_HEIGHT);
			this.contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			this.contentTable.addMouseListener(getSpriteImageTableMouseListener());
		}
		return this.contentTable;
	}

	/**
	 * Sets the SpriteImage best practice result data.
	 * 
	 * @param data
	 *            the data to be displayed in the result content table
	 */
	public void setData(Collection<SpriteImageEntry> data) {
		this.tableModel.setData(data);
	}

	/**
	 * Returns mouse listener.
	 * 
	 * @return mouse listener
	 */
	private MouseListener getSpriteImageTableMouseListener() {

		MouseListener ml = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				SpriteImageEntry entry = SpriteImageResultPanel.this.contentTable.getSelectedItem();
				if ((e.getClickCount() == 2) && (entry != null)) {
					ApplicationResourceOptimizer aro = ApplicationResourceOptimizer.getAroFrame();
					aro.displayDiagnosticTab();
					aro.getAroAdvancedTab().setHighlightedRequestResponse(entry.getHttpRequestResponse());
				}
			}
		};

		return ml;
	}
}
