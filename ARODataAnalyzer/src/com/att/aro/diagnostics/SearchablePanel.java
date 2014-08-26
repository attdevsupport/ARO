package com.att.aro.diagnostics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.util.Util;

public abstract class SearchablePanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private JTextField search;
	private JButton searchNext;
	protected ApplicationResourceOptimizer aro;
	private JTextArea searchableTextArea = null;
	
	/**
	 * Sets specific searchable text area, overriding default HttpRequestResponse search.
	 */
	void setSearchableTextArea(JTextArea textArea) {
		searchableTextArea = textArea;
	}
	
	/**
	 * Initializes and returns the search element for the Request-Response Table.
	 */
	public JTextField getSearch() {
		if (search == null) {
			search = new Search(this.aro, this, searchableTextArea);
			search.setEnabled(false);
		}
		return search;
	}

	/**
	 * Initializes and returns the search next button for the Request-Response Table.
	 */
	JButton getSearchNextButton() {
		if (searchNext == null) {
			searchNext = new JButton(Util.RB.getString("element.search.next"));
			searchNext.setEnabled(false);
			searchNext.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg) {
					((Search) search).doNextSearch();
				}
			});
		}
		return searchNext;
	}
	
	/** 
	 * Enables search feature.
	 */
	public void enableSearch() {
		search.setEnabled(true);
	}
}
