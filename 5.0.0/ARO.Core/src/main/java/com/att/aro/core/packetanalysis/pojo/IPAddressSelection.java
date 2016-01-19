package com.att.aro.core.packetanalysis.pojo;

import java.awt.Color;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * Contains methods for managing the settings applied to each IP address listed in the SelectApplications/IPs
 * dialog.
 */
public class IPAddressSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	private InetAddress ipAddress;
	private boolean selected = true;
	private Color color = Color.GRAY;
	private String domainName; //Greg Strory

	/**
	 * Initializes an instance of the IPAddressSelection class, using the
	 * specified IP address.
	 * 
	 * @param ipAddress
	 *            The IP address to manage the settings.
	 */
	public IPAddressSelection(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public IPAddressSelection(InetAddress ipAddress, String domainName) {
		this.ipAddress = ipAddress;
		this.domainName = domainName;
	}

	/**
	 * Initializes an instance of the IPAddressSelection class, using another instance 
	 * of the IPAddressSelection class.
	 * 
	 * @param sel
	 *            An IPAddressSelection object to be copied to a new object.
	 */
	public IPAddressSelection(IPAddressSelection sel) {
		this.ipAddress = sel.ipAddress;
		this.selected = sel.selected;
		this.color = sel.color;
		this.domainName = sel.getDomainName();
	}

	/**
	 * Returns the IP address.
	 * 
	 * @return The IP address.
	 */
	public InetAddress getIpAddress() {
		return ipAddress;
	}

	/**
	 * Retrieves the selection status of the IP address.
	 * 
	 * @return A boolean value that is true if this IP address is selected, and
	 *         false otherwise.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Registers the specified selected status with the local selected status
	 * for this IP address.
	 * 
	 * @param selected
	 *            A boolean value that indicates the selected status to set
	 *            for this IP address.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Returns the Color associated with this IP address.
	 * 
	 * @return The Color object associated with this IP address.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Registers the specified color with the local color object for this IP
	 * address.
	 * 
	 * @param color
	 *            The Color to set for this IP address.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

}
