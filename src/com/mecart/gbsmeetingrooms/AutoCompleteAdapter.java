package com.mecart.gbsmeetingrooms;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> fullList;
    private ArrayList<String> mOriginalValues;
    private ArrayFilter mFilter;

    public AutoCompleteAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {

        super(context, resource, textViewResourceId, objects);
        fullList = (ArrayList<String>) objects;
        mOriginalValues = new ArrayList<String>(fullList);

    }

    @Override
    public int getCount() {
        return fullList.size();
    }

    @Override
    public String getItem(int position) {
        return fullList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
    	return getCustomView(position,convertView,parent, parent.getContext());
    	
    }
    
	public View getCustomView(int position, View convertView, ViewGroup parent, Context context) { 
		LayoutInflater inflater = LayoutInflater.from(context);
		View mySpinner = inflater.inflate(R.layout.my_spinner_layout, parent, false); 
		TextView main_text = (TextView) mySpinner .findViewById(R.id.text_room_name); 
		//main_text.setText((CharSequence)stringArray.get(position));
		main_text.setText(getItem(position));
		
		Typeface hpSimplified = Typeface.createFromAsset(context.getAssets(), "HPSimplified_Rg.ttf");
		main_text.setTypeface(hpSimplified);
		
		
		Typeface hpSimplifiedBold = Typeface.createFromAsset(context.getAssets(), "HPSimplified_Bd.ttf");
		TextView bullet = (TextView) mySpinner.findViewById(R.id.bullet);
		bullet.setTypeface(hpSimplifiedBold);
	 
		return mySpinner;
	}
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }




    
    private class ArrayFilter extends Filter {
        private Object lock;

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (lock) {
                    mOriginalValues = new ArrayList<String>(fullList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    ArrayList<String> list = new ArrayList<String>(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                ArrayList<String> values = mOriginalValues;
                int count = values.size();

                ArrayList<String> newValues = new ArrayList<String>(count);

                for (int i = 0; i < count; i++) {
                    String item = values.get(i);
                    if (item.toLowerCase().contains(prefixString)) {
                        newValues.add(item);
                    }

                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

        if(results.values!=null){
        fullList = (ArrayList<String>) results.values;
        }else{
            fullList = new ArrayList<String>();
        }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}