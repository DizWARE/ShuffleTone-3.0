package com.DizWARE.ShuffleTone.Activites;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.DizWARE.ShuffleTone.R;

/***
 * Dialog that allows user to pick between calls, texts, or both
 * 
 * @author Tyler Robinson
 */
public class ListTypeChooser extends AlertDialog
{
	private TextView tv_hint;
	private TextView tv_calls;
	private TextView tv_texts;
	
	private ToggleButton tb_calls;
	private ToggleButton tb_texts;
	
	private Button btn_ok;
	private Button btn_cancel;
	
	private boolean mOpposite;
	
	private OnOkClick clickHandler;
	
	/***
	 * Constructs a new ListTypeChooser
	 * 
	 * @param context - Application context
	 * @param title - String to go up in the dialog title bar
	 * @param hint - String to go before the toggle buttons
	 * @param clickHandler - Handle for when the user clicks ok. This is going to 
	 * be different depending on what is calling it
	 * @param opposite - Set true if the two options should be opposites
	 */
	protected ListTypeChooser(Context context,String title, String hint, OnOkClick clickHandler, boolean opposite) {
		super(context);
		
		//Inflates our layout and gathers all the UI elements from it
		RelativeLayout v =(RelativeLayout) this.getLayoutInflater().inflate(R.layout.list_type_dialog, null);
		
		tv_hint = (TextView)v.findViewById(R.id.tv_hint);
		tv_calls = (TextView)v.findViewById(R.id.tv_calls);
		tv_texts = (TextView)v.findViewById(R.id.tv_texts);
		
		tb_calls = (ToggleButton)v.findViewById(R.id.tb_calls);
		tb_texts = (ToggleButton)v.findViewById(R.id.tb_texts);
		
		btn_ok = (Button)v.findViewById(R.id.btn_ok);
		btn_cancel = (Button)v.findViewById(R.id.btn_cancel);
		
		this.clickHandler = clickHandler;
		
		mOpposite = opposite;
		
		//Button handler. Uses the given click handle to do something based on the on and off position of calls and texts
		btn_ok.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v) { 
				if(ListTypeChooser.this.clickHandler.handleClick(tb_calls.isChecked(), tb_texts.isChecked())) 
					ListTypeChooser.this.dismiss();
			}
		});
		
		//Cancels the dialog
		btn_cancel.setOnClickListener(new View.OnClickListener() {			
			@Override public void onClick(View v)  { ListTypeChooser.this.dismiss(); }
		});
		
		//If the user gave some text for the hint view, update it. Otherwise rip it out of the layout
		if(hint.length() > 0) tv_hint.setText(hint);
		else v.removeView(tv_hint);
		
		//Handles how the on/off buttons handle when user clicks on them. 
			//Basically changes the drawing state
		OnCheckedChangeListener checkChanged = new OnCheckedChangeListener() {			
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) buttonView.setBackgroundResource(R.drawable.selectbox_checked);
				else buttonView.setBackgroundResource(R.drawable.selectbox_unchecked);
				if(mOpposite)
				{
					if(buttonView == tb_calls && tb_texts.isChecked() == isChecked)
						tb_texts.toggle();
					else if(buttonView == tb_texts && tb_calls.isChecked() == isChecked)
						tb_calls.toggle();						
				}
			}
		};
		
		tb_calls.setOnCheckedChangeListener(checkChanged);
		tb_texts.setOnCheckedChangeListener(checkChanged);		

		if(mOpposite) tb_calls.toggle();
		
		tv_calls.setOnClickListener(new View.OnClickListener() {		
			@Override public void onClick(View v) { 
				tb_calls.toggle(); 
				} });
		tv_texts.setOnClickListener(new View.OnClickListener() {		
			@Override public void onClick(View v) { 
				tb_texts.toggle(); 
				} });
		
		//Set the dialog layout and title
		this.setView(v);
		this.setTitle(title);
	}
	
	/***
	 * Sets the initial state of the check buttons
	 * @param calls - Initial state for the calls checkbox
	 * @param texts - Initial state for the texts checkbox
	 */
	public void setState(boolean calls, boolean texts)
	{
		if(tb_calls.isChecked() != calls) tb_calls.toggle();
		if(tb_texts.isChecked() != texts) tb_texts.toggle();
	}

	/***
	 * Creates a brand new ListTypeChooser(since the constructor is protected
	 * 
	 * @param context - Application context(must not be null
	 * @param title - Title of this dialog box(cannot be null, but can be empty)
	 * @param hint - Text to go in the hint location(cannot be null, but can be empty)
	 * @param clickHandler - Handler for when the Ok button is clicked(cannot be null)
	 * 
	 * @return - The newly created dialog box
	 */
	public static ListTypeChooser create(Context context, String title, 
			String hint, OnOkClick clickHandler, boolean opposite)
	{ return new ListTypeChooser(context, title, hint, clickHandler, opposite); }
	
	/***
	 * Interface that will be able to define work based on 2 boolean expressions
	 * 
	 * @author Tyler Robinson
	 */
	interface OnOkClick { public boolean handleClick(boolean calls, boolean texts);	}
}

