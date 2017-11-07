/*
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.justin.draganddrop.adapter;

import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.justin.draganddrop.R;
import com.example.justin.draganddrop.data.CopyData;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class ItemAdapter extends DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public ItemAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);

        switch (viewType){
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
                break;
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty, parent, false);
                break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        try{
            String text = mItemList.get(position).second;
            int index = text.indexOf("/");
            String name = text.substring(0, index);
            String phone = text.substring(index+1);
            holder.mText1.setText(name);
            holder.mText2.setText(phone);
            holder.itemView.setTag(mItemList.get(position).first);
        }
        catch (Exception e){}

    }

    @Override
    public int getItemViewType(int position) {
        int type = 1;
        if(mItemList.get(position).second.equals("")){
            type = 2;
        }
        else
            type = 1;
        return type;
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView mText1;
        TextView mText2;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText1 = (TextView) itemView.findViewById(R.id.copyname);
            mText2 = (TextView) itemView.findViewById(R.id.copyphone);
        }

        @Override
        public void onItemClicked(View view) {
            // 클릭이벤트 처리하면 됩니당
        }

        @Override
        public boolean onItemLongClicked(View view) {
            // 롱클릭 이벤트 처리하면 됩니다.
            return true;
        }
    }

}
