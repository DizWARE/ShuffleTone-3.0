package com.DizWARE.ShuffleTone;

import java.util.ArrayList;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/***
 * Dialog with an embedded list inside. 
 * 
 * 
 * This should probably be changed a bit, so that it takes in 
 * a custom row, so that this can be used in more than one context
 * 
 * Also, it may be good to note that this was hacked together. The
 * code is very sloppy, and may be hard to upgrade
 * 
 * @author Tydiz
 *
 */
public class ListDialog extends Dialog 
	implements View.OnClickListener, OnCheckedChangeListener
{
	FileBrowser context;
	
	Button btn_ok;
	Button btn_noFilter;
	Button btn_cancel;
	
	ListView lv_list;
	
	ArrayList<String> finalItems;
	
	ArrayList<String> items;
	
	int sortCategory;
	
	/***
	 * Creates a dialog with a list view embedded in it
	 * 
	 * @param context - Activity that this is linked to
	 * @param items - Items that we want showing up in the dialog
	 * @param sortCategory - Category that these items are from
	 */
	public ListDialog(FileBrowser context, ArrayList<String> items,
			int sortCategory) 
	{
		super(context);
		this.setContentView(R.layout.listdialog);
		this.context = context;
		this.items = items;
		this.sortCategory = sortCategory;
		
		finalItems = new ArrayList<String>();
		
		this.setTitle("Category Selector");
		
		//***GUI Junk***
		btn_ok = (Button)findViewById(R.id.btn_ok);
		btn_noFilter = (Button)findViewById(R.id.btn_noFilter);
		btn_cancel = (Button)findViewById(R.id.btn_cancel);
		
		//***Click Listeners***
		btn_ok.setOnClickListener(this);
		btn_noFilter.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		
		//Sets up the list view
		lv_list = (ListView)findViewById(R.id.lv_list);
		
		ListAdapter adapter = new ListAdapter();
		lv_list.setAdapter(adapter);
	}
	
	/***
	 * Starts the dialog. 
	 * 
	 * Note: due to the lack of things done here, this is completely
	 * unnecessary, but if this were expanded to be a generic 
	 * list dialog(which it should be), this will be required
	 */
	@Override
	protected void onStart() 
	{
		super.onStart();
	}
	
	/***
	 * Handles the user interaction with the buttons on the dialog
	 */
	@Override
	public void onClick(View v) 
	{
		Button btn = (Button)v;
		
		//If the sort was "Type"
		if(sortCategory == -1)
		{
			//If the filter is accepted and something was selected
				//Apply the filter
			if(btn == btn_ok && !finalItems.isEmpty())
				context.filterResults(finalItems, items);
			//otherwise clear the filter from the original list
			if(btn == btn_noFilter)
				context.filterResults(items, items);
		}
		else
		{
			//If the filter is accepted and something was selected
				//Apply the filter
			if(btn == btn_ok&& !finalItems.isEmpty())
				context.filterResults(finalItems, sortCategory);
			//otherwise clear the filter from the original list
			if(btn == btn_noFilter)
				context.filterResults(items, sortCategory);		
		}
		
		this.cancel();
	}
	
	/***
	 * Adapter that fills up the list. If the ListDialog took an adapter
	 * instead of using this one, this would help generalize a ListDialog
	 * 
	 *  Here's a couple TODO's to remind me to change
	 *  TODO
	 *  TODO
	 *  TODO
	 * 
	 * @author Tydiz
	 */
	private class ListAdapter extends ArrayAdapter<String> 
	{
		public ListAdapter() 
		{
			super(context,R.layout.sortrow,R.id.tv_list,items);
		}
		
		/***
		 * Dynamically builds each list item with a label and a
		 * toggle button
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
    		LayoutInflater inflater = getLayoutInflater();
    		
    		//Breaks the row definition file into pieces so things can be 
    			//give definition
    		View row = inflater.inflate(R.layout.sortrow, null);
    		
			ToggleButton tb_select = (ToggleButton)row.findViewById(R.id.tb_selected);
			TextView tv_list = (TextView)row.findViewById(R.id.tv_list);
			
			tv_list.setText(items.get(position));
			
			tb_select.setOnCheckedChangeListener(ListDialog.this);				
			
			tb_select.setTag(items.get(position));
			
			return row;
		}

		
		
	}

	@Override
	/***
	 * Handles when an item is checked or unchecked
	 */
	public void onCheckedChanged(CompoundButton buttonView, 
			boolean isChecked) 
	{
		ToggleButton btn = (ToggleButton)buttonView;
		String item = (String)btn.getTag();
		
		//If the item is checked
		if(isChecked)
		{
			btn.setButtonDrawable(R.drawable.selectbox_checked);
			if(!finalItems.contains(item))
				finalItems.add(item);
		}	
		//if the item is unchecked
		else 
		{
			btn.setButtonDrawable(R.drawable.selectbox_unchecked);
			if(finalItems.contains(item))
				finalItems.remove(item);
		}	
		
	}
}
