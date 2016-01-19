package com.att.aro.ui.model.diagnostic;

import java.text.DecimalFormat;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.bestpractice.impl.FileCompressionImpl;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TextFileCompression;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;


public class RequestResponseTableModel extends DataTableModel<HttpRequestResponseInfo> {
	
	private static final long serialVersionUID = 1L;

	private IHttpRequestResponseHelper httpHelper = ContextAware.getAROConfigContext().getBean(IHttpRequestResponseHelper.class);
	private FileCompressionImpl fileCompression = (FileCompressionImpl)ContextAware.getAROConfigContext().getBean("textFileCompression");

	private static final String[] COLUMNS = { 
		ResourceBundleHelper.getMessageString("rrview.time"),
		ResourceBundleHelper.getMessageString("rrview.direction"), 
		ResourceBundleHelper.getMessageString("rrview.reqtye"),
		ResourceBundleHelper.getMessageString("rrview.hostname"), 
		ResourceBundleHelper.getMessageString("rrview.objectname"),
		ResourceBundleHelper.getMessageString("rrview.contentlen"), 
		ResourceBundleHelper.getMessageString("rrview.http.compression") 
	};
	
	private static final int TIME_COL = 0;
	private static final int DIR_COL = 1;
	private static final int REQ_TYPE_STATUS_COL = 2;
	private static final int HOST_NAME_CONTENT_TYPE_COL = 3;
	private static final int OBJ_NAME_CONTENT_LENGTH = 4;
	private static final int ON_WIRE_CONTENT_LENGTH = 5;
	private static final int HTTP_COMPRESSION = 6;

	private Session session;
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	public void refresh(Session session) {
		setSession(session);
		setData(session.getRequestResponseInfo());
	}


	/**
	 * Initializes a new instance of the RequestResponseDetailsPanel class.
	 */
	public RequestResponseTableModel() {
		super(COLUMNS);
		
	}

	/**
	 * Returns a TableColumnModel that is based on the default table column
	 * model for the DataTableModel class. The TableColumnModel returned by this
	 * method has the same number of columns in the same order and structure as
	 * the table column model in the DataTableModel. When a DataTable object is
	 * created, this method is used to create the TableColumnModel if one is not
	 * specified. This method may be overridden in order to provide
	 * customizations to the default column model, such as providing a default
	 * column width and/or adding column renderers and editors.
	 * 
	 * @return A TableColumnModel object.
	 */
	@Override
	public TableColumnModel createDefaultTableColumnModel() {
		TableColumnModel cols = super.createDefaultTableColumnModel();
		TableColumn col = cols.getColumn(TIME_COL);
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000")));
		return cols;
	}

	/**
	 * Returns a class representing the specified column. This method is
	 * primarily used to sort numeric columns.
	 * 
	 * @param columnIndex
	 *            The index of the specified column.
	 * 
	 * @return A class representing the specified column.
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case TIME_COL:
			return Double.class;
		case ON_WIRE_CONTENT_LENGTH:
			return Long.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

	/**
	 * This is the one method that must be implemented by subclasses. This method defines how 
	 * the data object managed by this table model is mapped to its columns when displayed 
	 * in a row of the table. The getValueAt() method uses this method to retrieve table cell data.
	 * 
	 * @param
	 * 		item A object containing the column information.
			columnIndex The index of the specified column.
	 *		
	 * @return An object containing the table column value. 
	 */
	@Override
	protected Object getColumnValue(HttpRequestResponseInfo item, int columnIndex) {
		if (item.getDirection() != null) {
			switch (columnIndex) {
			case TIME_COL:
				return item.getTimeStamp();
			case DIR_COL:
				return item.getDirection();
			case REQ_TYPE_STATUS_COL:
				if (item.getDirection() ==  HttpDirection.REQUEST) {
					String type = item.getRequestType();
					return type != null ? type : ResourceBundleHelper.getMessageString("rrview.unknownType");
				} else {
					return item.getStatusCode();
				}
			case HOST_NAME_CONTENT_TYPE_COL:
				if (item.getDirection() == HttpDirection.REQUEST) {
					return item.getHostName();
				} else {
					return item.getContentType();
				}
			case OBJ_NAME_CONTENT_LENGTH:
				if (item.getDirection() == HttpDirection.REQUEST) {
					return item.getObjName();
				} else {
					return item.getContentLength();
				}
			case ON_WIRE_CONTENT_LENGTH:
				return httpHelper.getActualByteCount(item, getSession());
			case HTTP_COMPRESSION:
				return  getHttpCompression(item);
			}
		}
		return null;
	}

	
	private String getHttpCompression(HttpRequestResponseInfo item){
		
		String contentEncoding = item.getContentEncoding();

		if (item.getPacketDirection() == PacketDirection.DOWNLINK && 
				item.getContentLength() != 0 && 
				item.getContentType() != null && 
				fileCompression.isTextContent(item.getContentType())) {
			//same logic apply in FileCompressionImpl.java 
			if ("gzip".equals(contentEncoding)) {
				return TextFileCompression.GZIP.toString();
			} else if ("compress".equals(contentEncoding)) {
				return TextFileCompression.COMPRESS.toString();
			} else if ("deflate".equals(contentEncoding)) {
				return TextFileCompression.DEFLATE.toString();
			}else{
				// the content should be compressed but is not				
				return TextFileCompression.NONE.toString();				
			}

		} else {
			return TextFileCompression.NOT_APPLICABLE.toString();
		}
		
	}
 
}
