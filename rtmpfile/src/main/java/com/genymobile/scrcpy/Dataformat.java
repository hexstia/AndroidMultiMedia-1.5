package com.genymobile.scrcpy;

public class Dataformat {
//		on;480;8000000;false;
	public String swtch = "off";
	public String pix = "1680";
	public String ra_bit = "8000000";// 8Mb
	public String tf = "false";

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		Dataformat obj1 = (Dataformat) obj;

		if (this.swtch.equals(obj1.swtch) && this.pix.equals(obj1.pix) && this.ra_bit.equals(obj1.ra_bit)
				&& this.tf.equals(obj1.tf)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Dataformat [swtch=" + swtch + ", pix=" + pix + ", ra_bit=" + ra_bit + ", tf=" + tf + "]";
	}

	public void setValue(int i, String value) {
		switch (i) {
		case 0:
			swtch = value;
			break;
		case 1:
			pix = value;
			break;
		case 2:
			ra_bit = value;
			break;
		case 3:
			tf = value;
			break;
		}
	}
}
