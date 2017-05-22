package com.yihu.hospital.caihongqiji.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.yihu.hospital.caihongqiji.model.LiveInfoJson;
import com.yihu.hospital.caihongqiji.utils.GlideCircleTransform;
import com.yihu.hospital.caihongqiji.utils.SxbLog;
import com.yihu.hospital.caihongqiji.utils.UIUtils;

import java.util.ArrayList;


/**
 * 直播列表的Adapter
 */
public class LiveShowAdapter extends ArrayAdapter<LiveInfoJson> {
    private static String TAG = "LiveShowAdapter";
    private int resourceId;
    private Activity mActivity;
    private class ViewHolder{
        TextView tvTitle;
        TextView tvHost;
        TextView tvMembers;
        TextView tvAdmires;
        TextView tvLbs;
        ImageView ivCover;
        ImageView ivAvatar;
    }

    public LiveShowAdapter(Activity activity, int resource, ArrayList<LiveInfoJson> objects) {
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
            convertView = LayoutInflater.from(getContext()).inflate(com.yihu.hospital.caihongqiji.R.layout.item_liveshow, null);

            holder = new ViewHolder();
            holder.ivCover = (ImageView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.cover);
            holder.tvTitle = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.live_title);
            holder.tvHost = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.host_name);
            holder.tvMembers = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.live_members);
            holder.tvAdmires = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.praises);
            holder.tvLbs = (TextView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.live_lbs);
            holder.ivAvatar = (ImageView) convertView.findViewById(com.yihu.hospital.caihongqiji.R.id.avatar);

            convertView.setTag(holder);
        }

        LiveInfoJson data = getItem(position);
        if (!TextUtils.isEmpty(data.getCover())){
            SxbLog.d(TAG, "load cover: " + data.getCover());
            RequestManager req = Glide.with(mActivity);
            req.load(data.getCover()).into(holder.ivCover);
        }else{
            holder.ivCover.setImageResource(com.yihu.hospital.caihongqiji.R.drawable.cover_background);
        }

        if (null == data.getHost() || TextUtils.isEmpty(data.getHost().getAvatar())){
            // 显示默认图片
            Bitmap bitmap = BitmapFactory.decodeResource(this.getContext().getResources(), com.yihu.hospital.caihongqiji.R.drawable.default_avatar);
            Bitmap cirBitMap = UIUtils.createCircleImage(bitmap, 0);
            holder.ivAvatar.setImageBitmap(cirBitMap);
        }else{
            SxbLog.d(TAG, "user avator: " + data.getHost().getAvatar());
            RequestManager req = Glide.with(mActivity);
            req.load(data.getHost().getAvatar()).transform(new GlideCircleTransform(mActivity)).into(holder.ivAvatar);
        }

        holder.tvTitle.setText(UIUtils.getLimitString(data.getTitle(), 10));
        if (!TextUtils.isEmpty(data.getHost().getUsername())){
            holder.tvHost.setText("@" + UIUtils.getLimitString(data.getHost().getUsername(), 10));
        }else{
            holder.tvHost.setText("@" + UIUtils.getLimitString(data.getHost().getUid(), 10));
        }
        holder.tvMembers.setText(""+data.getWatchCount());
        holder.tvAdmires.setText(""+data.getAdmireCount());
        if (!TextUtils.isEmpty(data.getLbs().getAddress())) {
            holder.tvLbs.setText(UIUtils.getLimitString(data.getLbs().getAddress(), 9));
        }else{
            holder.tvLbs.setText(getContext().getString(com.yihu.hospital.caihongqiji.R.string.live_unknown));
        }

        return convertView;
    }
}
