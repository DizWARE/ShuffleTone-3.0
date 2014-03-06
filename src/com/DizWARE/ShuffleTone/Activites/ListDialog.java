package com.DizWARE.ShuffleTone.Activites;

import java.io.Serializable;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.DizWARE.ShuffleTone.R;

/***
 * This class is an easy way to create and show a fully function dialog with a list view and Ok/Cancel buttons
 * 
 * @author Tyler Robinson
 * @param <T> - The type of item we are storing into the list adapter
 */
public class ListDialog<T> extends AlertDialog implements Serializable {
	
	private static final long serialVersionUID = 159287737128485009L;

	private FrameLayout fl_titlebar;
	
	private Button btn_ok;
	private Button btn_cancel;
	
	private ListView lv_list;
	private ArrayAdapter<T> mAdapter;
	
	private OnOkClick mClickHandler;
	
	/***
	 * Constructs a new list dialog. Uses the given title for the dialog title, and defines the way the list loads
	 * using the adapter that is given. It is expected that the adapter will have already been filled
	 * 
	 * @param context - Application context that loaded the dialog
	 * @param title - The title for our dialog
	 * @param adapter - This adapter defines how data will be loaded into the list. Expexted to already be loaded
	 */
	protected ListDialog(Context context, String title, ArrayAdapter<T> adapter, OnOkClick clickHandler) {
		super(context);
		RelativeLayout rl_main = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.list_dialog, null);
		
		fl_titlebar = (FrameLayout)rl_main.findViewById(R.id.fl_titlebar);
		
		btn_ok = (Button)rl_main.findViewById(R.id.btn_ok);
		btn_cancel = (Button)rl_main.findViewById(R.id.btn_cancel);
		
		lv_list = (ListView)rl_main.findViewById(R.id.lv_list);		
		lv_list.setAdapter(adapter);	
		mAdapter = adapter;
		
		mClickHandler = clickHandler;
		
		//Accepts the changes
		btn_ok.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v) {
				if(mClickHandler.handleClick(lv_list.getAdapter()))
					ListDialog.this.dismiss();
			}
		});
		
		//Cancels the dialog
		btn_cancel.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v)  { ListDialog.this.dismiss(); }
		});
		
		this.setView(rl_main);
		this.setTitle(title);
	}
	
	public ListDialog(Context context, String title) 
	{
		super(context);
		RelativeLayout rl_main = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.list_dialog, null);
		
		fl_titlebar = (FrameLayout)rl_main.findViewById(R.id.fl_titlebar);
		
		btn_ok = (Button)rl_main.findViewById(R.id.btn_ok);
		btn_cancel = (Button)rl_main.findViewById(R.id.btn_cancel);
		
		lv_list = (ListView)rl_main.findViewById(R.id.lv_list);		
		
		//Cancels the dialog
		btn_cancel.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v)  { ListDialog.this.dismiss(); }
		});
		
		this.setView(rl_main);
		this.setTitle(title);
	}
	
	public ListDialog(Context context, String title, ArrayAdapter<T> adapter) 
	{
		super(context);
		RelativeLayout rl_main = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.list_dialog, null);
		
		fl_titlebar = (FrameLayout)rl_main.findViewById(R.id.fl_titlebar);
		
		btn_ok = (Button)rl_main.findViewById(R.id.btn_ok);
		btn_cancel = (Button)rl_main.findViewById(R.id.btn_cancel);
		
		lv_list = (ListView)rl_main.findViewById(R.id.lv_list);	
		lv_list.setAdapter(adapter);	
		mAdapter = adapter;
		
		//Cancels the dialog
		btn_cancel.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v)  { ListDialog.this.dismiss(); }
		});
		
		this.setView(rl_main);
		this.setTitle(title);
	}
	
	public ListDialog(Context context, String title, OnOkClick clickHandler) 
	{
		super(context);
		RelativeLayout rl_main = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.list_dialog, null);
		
		fl_titlebar = (FrameLayout)rl_main.findViewById(R.id.fl_titlebar);
		
		btn_ok = (Button)rl_main.findViewById(R.id.btn_ok);
		btn_cancel = (Button)rl_main.findViewById(R.id.btn_cancel);
		
		lv_list = (ListView)rl_main.findViewById(R.id.lv_list);		
		
		mClickHandler = clickHandler;
		
		//Accepts the changes
		btn_ok.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v) {
				if(mClickHandler.handleClick(lv_list.getAdapter()))
					ListDialog.this.dismiss();
			}
		});
		
		//Cancels the dialog
		btn_cancel.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v)  { ListDialog.this.dismiss(); }
		});
		
		this.setView(rl_main);
		this.setTitle(title);
	}
	
	/***
	 * Gets the list view of this dialog
	 */
	@Override public ListView getListView() { return lv_list; }
	
	public ArrayAdapter<T> getAdapter() { return mAdapter; }
	
	/***
	 * Adds a view to the empty frame layout at the top of the dialog.
	 * This allows for custom dialog title bars in our dialog
	 * 
	 * @param titlebar - The view to be used as our dialog title bar
	 */
	public void addTitleBar(View titlebar) 
	{ 
		fl_titlebar.removeAllViews();
		fl_titlebar.addView(titlebar); 
	}
	
	public void setOkHandler(OnOkClick clickHandler)
	{
		this.mClickHandler = clickHandler;
	}
	
	public void setAdapter(ArrayAdapter<T> adapter)
	{
		this.mAdapter = adapter;
		lv_list.setAdapter(adapter);
	}
	
	/***
	 * Creates a new list dialog
	 * 
	 * @param <T> - Type of data being put into the list
	 * @param context - Application context that loaded the dialog
	 * @param title - The title for our dialog
	 * @param adapter - This adapter defines how data will be loaded into the list. Expexted to already be loaded
	 * 
	 * @return - Returns an instance of the list dialog with the given data
	 */
	public static <T> ListDialog<T> create(Context context, String title, ArrayAdapter<T> adapter, OnOkClick clickHandler)
	{ return new ListDialog<T>(context, title, adapter, clickHandler); }
	
	/***
	 * Interface that will be able to define work of the ok button
	 * 
	 * @author Tyler Robinson
	 */
	interface OnOkClick { public boolean handleClick(ListAdapter adapter);	}
	

}
