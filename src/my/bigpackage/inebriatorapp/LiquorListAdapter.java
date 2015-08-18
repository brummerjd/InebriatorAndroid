package my.bigpackage.inebriatorapp;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LiquorListAdapter extends ArrayAdapter<Ingredient> {
	
	private List<Ingredient> Items;
	private int LayoutResourceId;
	private Context Context;
	
	public LiquorListAdapter(Context context, int layoutResourceId, List<Ingredient> items) {
		super(context, layoutResourceId, items);
		this.LayoutResourceId = layoutResourceId;
		this.Context = context;
		this.Items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		IngredientHolder holder = null;
		
		LayoutInflater inflater = ((Activity)this.Context).getLayoutInflater();
		row = inflater.inflate(this.LayoutResourceId, parent, false);
		
		holder = new IngredientHolder();
		holder.Ingred = this.Items.get(position);
		holder.Name = (TextView)row.findViewById(R.id.ingredNameTV);
		holder.Amount = (TextView)row.findViewById(R.id.ingredAmtTV);
		holder.IngredRL = (RelativeLayout)row.findViewById(R.id.ingredRL);
		
		row.setTag(holder);
		
		this.setupItem(holder);
		return row;
	}
	
	private void setupItem(IngredientHolder holder) {
		holder.Name.setText(holder.Ingred.getName());
		holder.Amount.setText(" " + Ingredient.convertForDisplay(holder.Ingred.getNumberOfShots()));
	}
	
	public Drink getDrink() {
		Drink d = new Drink();
		d.setL1Amt(Items.get(0).getNumberOfShots());
		d.setL2Amt(Items.get(1).getNumberOfShots());
		d.setL3Amt(Items.get(2).getNumberOfShots());
		d.setL4Amt(Items.get(3).getNumberOfShots());
		d.setL5Amt(Items.get(4).getNumberOfShots());
		d.setL6Amt(Items.get(5).getNumberOfShots());
		d.setL7Amt(Items.get(6).getNumberOfShots());
		d.setL8Amt(Items.get(7).getNumberOfShots());
		return d;
	}
	
	public static class IngredientHolder {
		Ingredient Ingred;
		TextView Name;
		TextView Amount;
		RelativeLayout IngredRL;
	}
}
