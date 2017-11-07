package com.example.justin.draganddrop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.justin.draganddrop.R;
import com.example.justin.draganddrop.data.ContactData;

/**
 * Created by Justin on 2017-10-13.
 */

public class ContactListAdapter extends BaseAdapter {
    Context mContext;
    public ContactListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return ContactData.Id.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_contactlist, viewGroup, false);
        }
        TextView tvName = (TextView)view.findViewById(R.id.name);
        TextView tvPhone = (TextView)view.findViewById(R.id.phone);

        tvName.setText(ContactData.Name.get(i));
        tvPhone.setText(ContactData.PhoneNumber.get(i));
        view.setTag("LIST");

        return view;
    }
}
