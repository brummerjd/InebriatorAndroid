package my.bigpackage.inebriatorapp;

import java.util.ArrayList;

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
import android.widget.ListView;

public class MainActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 1;
	public static final int BLUETOOTH_CONNECTION_FAILED = 2;
	
	private Button MessageB;
	private LiquorListAdapter LiquorLA;
	private ListView LiquorLV;
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
                		AlertBoxToClose("Inebriator Connection Failed", error);
                		finish();
                		break;
                	case BluetoothManager.FOUND_INEBRIATOR:	// alert activity that Inebriator has been successfully paired
                		initializeMainLayout();
                		break;
                	case BluetoothManager.MESSAGE_READ:	// alert activity that message has been received from Inebriator
                		switch (((String)msg.obj).charAt(0)) {
                			case BluetoothManager.RESPONSE_MAKING_DRINK:
                				AlertBox("YAY", "Making drank");
                				BM.destroyConnection();
                				break;
                		}
                		break;
                	case AmountEditorDialog.INGREDIENT_UPDATED:
                		LiquorLA.getItem(msg.arg1).setNumberOfShots(msg.arg2*0.25);
                		LiquorLA.notifyDataSetChanged();
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
//        		BM.requestDrink(LiquorLA.getDrink());
        		Log.e("DRINK", LiquorLA.getDrink().getDrinkInstructions());
        	}
        });
        
        ArrayList<Ingredient> ls = new ArrayList<Ingredient>();
        for (int i = 1; i <= 8; i++) { ls.add(new Ingredient("Liquor " + i, 0)); }
        
        this.LiquorLA = new LiquorListAdapter(this, R.layout.list_item_liquor, ls);
        this.LiquorLV = (ListView)findViewById(R.id.liquorLV);
        this.LiquorLV.setAdapter(this.LiquorLA);
	}
	
	public void ingredientButtonOnClickHandler(View v) {
		//((LiquorListAdapter.IngredientHolder)v.getTag()).Ingred.setNumberOfShots(1);
		//this.LiquorLA.notifyDataSetChanged();
		
		AmountEditorDialog aed = new AmountEditorDialog(this, this.H, LiquorLA.getPosition(((LiquorListAdapter.IngredientHolder)v.getTag()).Ingred));
		aed.showDialog();
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
			AlertBoxToClose("Bluetooth Required", "Please turn on Bluetooth, is it required to connect with the Inebriator.");
			
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
											{  }
										})
			.show();
	}
	
	public void AlertBoxToClose(String title, String message)
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
				this.H.obtainMessage(BLUETOOTH_CONNECTION_FAILED, -1, -1, error).sendToTarget();
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
