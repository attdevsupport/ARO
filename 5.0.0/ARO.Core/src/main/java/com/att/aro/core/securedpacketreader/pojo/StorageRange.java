package com.att.aro.core.securedpacketreader.pojo;

public class StorageRange {
	//a mapping from [x,y] to [nx, ny]
		private int xvalue;
		private int yvalue;

		private int nxvalue;
		private int nyvalue;
		public int getXvalue() {
			return xvalue;
		}
		public void setXvalue(int xvalue) {
			this.xvalue = xvalue;
		}
		public int getYvalue() {
			return yvalue;
		}
		public void setYvalue(int yvalue) {
			this.yvalue = yvalue;
		}
		public int getNxvalue() {
			return nxvalue;
		}
		public void setNxvalue(int nxvalue) {
			this.nxvalue = nxvalue;
		}
		public int getNyvalue() {
			return nyvalue;
		}
		public void setNyvalue(int nyvalue) {
			this.nyvalue = nyvalue;
		}
		
}
