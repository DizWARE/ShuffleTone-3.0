package com.DizWARE.ShuffleTone.Activites;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Others.Constants;
import com.DizWARE.ShuffleTone.Others.FilterType;

public class FilterAdapter extends ArrayAdapter<FilterType> 
{
	Activity mContext;
	int mRowId;
	boolean mView;
	List<FilterType> originalList;
	
	public FilterAdapter(Activity context, boolean view) {
		super(context, 0);
		
		mContext = context;		
		mView = view;
		
		if(view)
			mRowId = R.layout.filter_view_dialog_row;
		else
			mRowId = R.layout.filter_dialog_row;
	}
	
	public void addAll(List<FilterType> filters)
	{
		originalList = filters;
		for(FilterType filter : filters)
			this.add(filter);
	}
	
	@Override public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
			convertView = this.mContext.getLayoutInflater().inflate(mRowId, null);
		FilterType filter = (FilterType)this.getItem(position);
		
		prepareBoth(convertView, filter);
		
		if(mView)
			prepareEdit(convertView, filter);
		else
			prepareAdd(convertView, filter);
		
		return convertView;
	}
	
	public void prepareBoth(View convertView, FilterType filter)
	{
		TextView tv_name = (TextView)convertView.findViewById(R.id.tv_name);
		tv_name.setText(filter.filter);
	}
	
	public void prepareEdit(View convertView, FilterType filter)
	{
		TextView tv_type = (TextView)convertView.findViewById(R.id.tv_type);	
		ImageButton ib_remove = (ImageButton)convertView.findViewById(R.id.ib_remove);
		
		ib_remove.setTag(filter);
		
		String type = "File Path";
		
		if(filter.category == Constants.CAT_ARTIST)
			type = "Artist";
		
		tv_type.setText(type);
		
		ib_remove.setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				FilterAdapter.this.remove((FilterType)v.getTag());
				originalList.remove(v.getTag());
				FilterAdapter.this.notifyDataSetChanged();
			}
		});
	}
	
	public void prepareAdd(View convertView, FilterType filter)
	{
		ToggleButton cb_checked = (ToggleButton)convertView.findViewById(R.id.tb_add_filter);
		cb_checked.setTag(filter);
		
		cb_checked.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					buttonView.setButtonDrawable(R.drawable.selectbox_checked);
				else
					buttonView.setButtonDrawable(R.drawable.selectbox_unchecked);
				
				((FilterType)buttonView.getTag()).selected = isChecked;
			}
		});
		
		cb_checked.setChecked(filter.selected);
	}
	
	public List<FilterType> getData()
	{
		return getSelected();
	}
	
	private List<FilterType> getSelected()
	{
		ArrayList<FilterType> list = new ArrayList<FilterType>();
		
		for(int i = 0; i < this.getCount(); i++)
			if(this.getItem(i).selected)
				list.add(this.getItem(i));
		
		return list;
	}
}
