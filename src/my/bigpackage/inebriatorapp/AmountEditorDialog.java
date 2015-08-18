package my.bigpackage.inebriatorapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AmountEditorDialog extends Activity {
	
	public static final int INGREDIENT_UPDATED = 70;
	
	private Context Context;
	private Handler ContextHandler;
	private int IngredPos;
	private TextView AmountTV;
	private SeekBar AmountSB;
	
	public AmountEditorDialog(Context context, Handler handler, int ingredientPosition)
	{
		this.Context = context;
		this.ContextHandler = handler;
		this.IngredPos = ingredientPosition;
	}
	
	public void showDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this.Context);
		b.setTitle("Change Amount");
		
		// Inflate LinearLayout to add to AlertDialog
		LayoutInflater inflater = ((Activity)this.Context).getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.activity_amount_editor_dialog, null);
		LinearLayout editAmountLL = (LinearLayout)dialogView.findViewById(R.id.amountEditorDialogLL);
		b.setView(editAmountLL);
		
		b.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContextHandler.obtainMessage(INGREDIENT_UPDATED, IngredPos, AmountSB.getProgress(), null).sendToTarget();
			}
		});
		b.setNegativeButton("Cancel", null);
		
		this.AmountTV = (TextView)dialogView.findViewById(R.id.amountTV);
		
		this.AmountSB = (SeekBar)dialogView.findViewById(R.id.amountSB);
		this.AmountSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				AmountTV.setText(Ingredient.convertForDisplay(progressValue*0.25));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
		});
		this.AmountSB.setProgress(4);
		
		b.create().show();
	}

}
