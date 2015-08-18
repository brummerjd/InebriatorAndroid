package my.bigpackage.inebriatorapp;

import java.text.DecimalFormat;

public class Drink {
	
	private final DecimalFormat DF = new DecimalFormat("#.##");
	
	// this amount is measured in shots
	private double L1Amt = 0, L2Amt = 0, L3Amt = 0, L4Amt = 0,
			L5Amt = 0, L6Amt = 0, L7Amt = 0, L8Amt = 0;
	
	public void setL1Amt(double amt) { this.L1Amt = amt; }
	public void setL2Amt(double amt) { this.L2Amt = amt; }
	public void setL3Amt(double amt) { this.L3Amt = amt; }
	public void setL4Amt(double amt) { this.L4Amt = amt; }
	public void setL5Amt(double amt) { this.L5Amt = amt; }
	public void setL6Amt(double amt) { this.L6Amt = amt; }
	public void setL7Amt(double amt) { this.L7Amt = amt; }
	public void setL8Amt(double amt) { this.L8Amt = amt; }
	
	public String getDrinkInstructions()
	{
		return DF.format(this.L1Amt) + BluetoothManager.MESSAGE_BREAK
				+ DF.format(this.L2Amt) + BluetoothManager.MESSAGE_BREAK
				+ DF.format(this.L3Amt) + BluetoothManager.MESSAGE_BREAK
				+ DF.format(this.L4Amt) + BluetoothManager.MESSAGE_BREAK
				+ DF.format(this.L5Amt) + BluetoothManager.MESSAGE_BREAK
				+ DF.format(this.L6Amt) + BluetoothManager.MESSAGE_BREAK
				+ DF.format(this.L7Amt) + BluetoothManager.MESSAGE_BREAK
				+ DF.format(this.L8Amt);
	}
}
