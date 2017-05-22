package com.yihu.hospital.caihongqiji.adapters;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yihu.hospital.caihongqiji.model.CurLiveInfo;
import com.yihu.hospital.caihongqiji.model.RecordInfo;
import com.yihu.hospital.caihongqiji.views.ActivityReplay;

import java.util.ArrayList;


/**
 * 点播列表的Adapter
 */
public class RecordAdapter extends ArrayAdapter<RecordInfo> {
    private static String TAG = "RecordAdapter";
    private int resourceId;
    private Activity mActivity;
    private class ViewHolder{
        TextView tvName;
        TextView tvUser;
        TextView tvTime;
        TextView tvVideoId;
        TextView tvUrl;
    }

    public RecordAdapter(Activity activity, int resource, ArrayList<RecordInfo> objects) {
        super(activity, resource, objects);
        resourceId = resource;
        mActivity = activity;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder)convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);

            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.tv_name);
            holder.tvUser = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.tv_user);
            holder.tvTime = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.tv_time);
            holder.tvVideoId = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.tv_videoid);
            holder.tvUrl = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.tv_url);

            convertView.setTag(holder);
        }

        final RecordInfo data = getItem(position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurLiveInfo.setRecordInfo(data);
                mActivity.startActivity(new Intent(mActivity.getApplicationContext(), ActivityReplay.class));
            }
        });

        holder.tvName.setText(data.getStrName());
        if (!TextUtils.isEmpty(data.getStrUser()))
            holder.tvUser.setText(data.getStrUser());
        if (!TextUtils.isEmpty(data.getStrCreateTime()))
            holder.tvTime.setText( data.getStrCreateTime());
        holder.tvVideoId.setText(data.getStrVideoId());
        holder.tvUrl.setText(data.getPlayUrl());

        return convertView;
    }
}
