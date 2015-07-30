package my.bigpackage.inebriatorapp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class EditLiquorActivity extends Activity {
	
	private LayoutInflater Inflater;
	private Button NewLiquorB;
	private ListView LiquorLV;
	private ArrayAdapter LiquorAA;
	private CountDownTimer Timer;
	private Handler H;
	private BluetoothManager BM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_liquor);
		
		this.BM = BluetoothManager.getInstance(this, null);
		
		this.H = new Handler() {
            public void handleMessage(android.os.Message msg) {
            	switch (msg.what)
            	{
            		case BluetoothManager.MESSAGE_READ:
            			int messageBreakLocation = ((String)msg.obj).indexOf(BluetoothManager.MESSAGE_BREAK);
                    	if (messageBreakLocation < 1) { return; }
                    	String action = ((String)msg.obj).substring(0, messageBreakLocation+1);
                        
                    	if (action.equals(BluetoothManager.RESPONSE_MENU_START))
                    	{
                    		BM.destroyConnection();
                    		
                    		String[] liquors = action.substring(messageBreakLocation+1).split(BluetoothManager.MESSAGE_BREAK);
                    		LiquorAA.clear();
                    		for (String l : liquors)
                    		{
                    			LiquorAA.add(l);
                    		}
                    		LiquorAA.notifyDataSetChanged();
                    	}
            			break;
            	}
            };
        };
        
        this.Inflater = this.getLayoutInflater();
        
        this.NewLiquorB = (Button)this.findViewById(R.id.newLiquorB);
        this.NewLiquorB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(EditLiquorActivity.this)
					.setView(Inflater.inflate(R.layout.alertdialog_add_liquor, null))
					.setPositiveButton("OK", new DialogInterface.OnClickListener()
										{ public void onClick(DialogInterface arg0, int arg1)
											{ Timer.start(); }
										})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
										{ public void onClick(DialogInterface arg0, int arg1)
											{  }
										})
					.show();
			}
		});
		
		this.LiquorLV = (ListView)this.findViewById(R.id.liquorLV);
        this.LiquorLV.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				
			}
        });
        
        this.Timer = new CountDownTimer(5000, 1000)
		{
			@Override
			public void onFinish() {
				BM.destroyConnection();
				Log.e("TAG", "TIME OUT");
			}
			@Override
			public void onTick(long millisUntilFinished) { }
		};
        
        this.populateLiquorList();
	}
	
	private void populateLiquorList()
	{
		this.LiquorAA = new ArrayAdapter(this, R.layout.list_item_liquor, new ArrayList<String>());
		this.LiquorLV.setAdapter(this.LiquorAA);
		
		this.BM.requestLiquorList();
	}
}
