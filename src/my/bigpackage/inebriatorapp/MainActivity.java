package my.bigpackage.inebriatorapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 1;
	public static final int BLUETOOTH_CONNECTION_FAILED = 2;
	
	private Button MessageB;
	private Button EditLiquorB;
	private Handler H;
	private BluetoothManager BM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connecting);
        
        // This Handler will receive and handle updates throughout this
        //  activity and in BluetoothManager
        this.H = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                	case BLUETOOTH_CONNECTION_FAILED:	// alert user of issue connecting to Bluetooth, then close application
                		String error = (String) msg.obj;
                		AlertBox("Inebriator Connection Failed", error);
                		finish();
                		break;
                	case BluetoothManager.FOUND_INEBRIATOR:	// alert activity that Inebriator has been successfully paired
                		initializeMainLayout();
                		break;
                	case BluetoothManager.MESSAGE_READ:	// alert activity that message has been received from Inebriator
                		AlertBox("MESSAGE RECEIVED", (String)msg.obj);
                		break;
                }
            };
        };
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		this.InitializeBluetooth();
	}
	
	public void initializeMainLayout()
	{
		setContentView(R.layout.activity_main);
		
		this.MessageB = (Button)this.findViewById(R.id.messageB);
        this.MessageB.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		BM.connectToInebriator();
        		BM.sendMessage("BITCHES");
        		//BM.destroyConnection();
        	}
        });
        
        this.EditLiquorB = (Button)this.findViewById(R.id.editLiquorB);
        this.EditLiquorB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, EditLiquorActivity.class);
        		MainActivity.this.startActivity(i);
        		finish();
			}
		});
	}
	
	private void InitializeBluetooth()
	{
		this.BM = BluetoothManager.getInstance(this, this.H);
		
		if (!this.BM.createAdapter())
		{
			// Alert user that Bluetooth is not available on their device
			String error = "Bluetooth is not supported on this device.";
			this.H.obtainMessage(BLUETOOTH_CONNECTION_FAILED, -1, -1, error).sendToTarget();
			return;
		}
		
		if (!this.BM.isAdapterEnabled())
		{
			// Have user turn on Bluetooth
			AlertBox("Bluetooth Required", "Please turn on Bluetooth, is it required to connect with the Inebriator.");
			
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		else
		{
			BM.pairWithInebriator();
		}
	}
	
	// General method for displaying alerts to user
	public void AlertBox(String title, String message)
	{
		if (message == null)
		{ return; }
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message + "\r\n\r\nPress OK to exit.")
			.setPositiveButton("OK", new DialogInterface.OnClickListener()
										{ public void onClick(DialogInterface arg0, int arg1)
											{ finish(); }
										})
			.show();
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				BM.connectToInebriator();
			}
			else if(resultCode == RESULT_CANCELED) {
				// User cancelled turning on Bluetooth, so alert them that this is bad
				String error = "You must turn on Bluetooth to use this application.";
				this.H.obtainMessage(this.BLUETOOTH_CONNECTION_FAILED, -1, -1, error).sendToTarget();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// When application is closed, make sure to destroy any lingering connection to Inebriator
	protected void OnStop()
	{
		if (this.BM != null) { this.BM.destroyConnection(); }
	}
}
