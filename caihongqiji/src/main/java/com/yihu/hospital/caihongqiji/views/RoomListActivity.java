package com.yihu.hospital.caihongqiji.views;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

import com.tencent.TIMUserProfile;
import com.tencent.ilivesdk.ILiveSDK;
import com.yihu.hospital.caihongqiji.R;
import com.yihu.hospital.caihongqiji.model.MySelfInfo;
import com.yihu.hospital.caihongqiji.presenters.InitBusinessHelper;
import com.yihu.hospital.caihongqiji.presenters.LoginHelper;
import com.yihu.hospital.caihongqiji.presenters.ProfileInfoHelper;
import com.yihu.hospital.caihongqiji.presenters.viewinface.LogoutView;
import com.yihu.hospital.caihongqiji.presenters.viewinface.ProfileView;
import com.yihu.hospital.caihongqiji.utils.SxbLog;
import com.yihu.hospital.caihongqiji.views.customviews.BaseFragmentActivity;

import java.util.List;

/**
 * 主界面
 */
public class RoomListActivity extends BaseFragmentActivity implements ProfileView, LogoutView {
    private FragmentTabHost mTabHost;
    private LayoutInflater layoutInflater;
    private ProfileInfoHelper infoHelper;
    private LoginHelper mLoginHelper;
    private final Class fragmentArray[] = {FragmentList.class, FragmentPublish.class, FragmentProfile.class};
    private int mImageViewArray[] = {R.drawable.tab_live, R.drawable.icon_publish, R.drawable.tab_profile};
    private String mTextviewArray[] = {"live", "publish", "profile"};
    private static final String TAG = RoomListActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        SxbLog.i(TAG, "RoomListActivity onCreate");
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        boolean living = pref.getBoolean("living", false);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        layoutInflater = LayoutInflater.from(this);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.contentPanel);

        int fragmentCount = fragmentArray.length;

        // wzw 注释掉publish
//        for (int i = 0; i < fragmentCount; i++) {
//            //为每一个Tab按钮设置图标、文字和内容
//            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
//            //将Tab按钮添加进Tab选项卡中
//            mTabHost.addTab(tabSpec, fragmentArray[i], null);
//            mTabHost.getTabWidget().setDividerDrawable(null);
//
//        }
//        mTabHost.getTabWidget().getChildTabViewAt(1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
////                DialogFragment newFragment = InputDialog.newInstance();
////                newFragment.show(ft, "dialog");
//
//                startActivity(new Intent(HomeActivity.this, PublishLiveActivity.class));
//
//            }
//        });

        // wzw publish隐藏
        for (int i = 0; i < fragmentCount; i++) {
            if (i == 1) {
                continue;
            }
            //为每一个Tab按钮设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            mTabHost.getTabWidget().setDividerDrawable(null);

        }

        // wzw 隐藏tabwidget
        mTabHost.getTabWidget().setVisibility(View.GONE);

        // 检测是否需要获取头像
//        if (TextUtils.isEmpty(MySelfInfo.getInstance().getAvatar())) {
//            infoHelper = new ProfileInfoHelper(this);
//            infoHelper.getMyProfile();
//        }
//        if (living) {
//            NotifyDialog dialog = new NotifyDialog();
//            dialog.show(getString(R.string.title_living), getSupportFragmentManager(), new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent intent = new Intent(HomeActivity.this, LiveActivity.class);
//                    MySelfInfo.getInstance().setIdStatus(Constants.HOST);
//                    MySelfInfo.getInstance().setJoinRoomWay(true);
//                    CurLiveInfo.setHostID(MySelfInfo.getInstance().getId());
//                    CurLiveInfo.setHostName(MySelfInfo.getInstance().getId());
//                    CurLiveInfo.setHostAvator("");
//                    CurLiveInfo.setRoomNum(MySelfInfo.getInstance().getMyRoomNum());
////                    CurLiveInfo.setMembers(item.getInfo().getMemsize()); // 添加自己
////                    CurLiveInfo.setAdmires(item.getInfo().getThumbup());
////                    CurLiveInfo.setAddress(item.getLbs().getAddress());
//                    startActivity(intent);
//                }
//            }, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                }
//            });
//        }
    }

    @Override
    protected void onStart() {
        SxbLog.i(TAG, "RoomListActivity onStart");
        super.onStart();
        if (ILiveSDK.getInstance().getAVContext() == null) {//retry
            InitBusinessHelper.initApp(getApplicationContext());
            SxbLog.i(TAG, "RoomListActivity retry login");
            mLoginHelper = new LoginHelper(this, this);
            mLoginHelper.iLiveLogin(MySelfInfo.getInstance().getId(), MySelfInfo.getInstance().getUserSig());
        }
    }

    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.tab_content, null);
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageResource(mImageViewArray[index]);
        return view;
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
//        super.onBackPressed();
//        SxbLog.i(TAG, "onBackPressed");
//        if (mLoginHelper == null) {
//            mLoginHelper = new LoginHelper(this, this);
//        }
//
//        mLoginHelper.standardLogout(MySelfInfo.getInstance().getId());
    }

    @Override
    protected void onDestroy() {
        SxbLog.i(TAG, "RoomListActivity onDestroy");
        super.onDestroy();
    }

    @Override
    public void updateProfileInfo(TIMUserProfile profile) {
        SxbLog.i(TAG, "updateProfileInfo");
        if (null != profile) {
            MySelfInfo.getInstance().setAvatar(profile.getFaceUrl());
            if (!TextUtils.isEmpty(profile.getNickName())) {
                MySelfInfo.getInstance().setNickName(profile.getNickName());
            } else {
                MySelfInfo.getInstance().setNickName(profile.getIdentifier());
            }
        }
    }

    @Override
    public void updateUserInfo(int reqid, List<TIMUserProfile> profiles) {
    }

    @Override
    public void logoutSucc() {
//        if (mLoginHelper != null) {
//            mLoginHelper.onDestory();
//        }
        SxbLog.i(TAG, "RoomListActivity logoutSucc");

        finish();
//        super.onBackPressed();
//        Intent intent = new Intent();
//        intent.setClass(this, LoginActivity.class);
//        startActivity(intent);
    }

    @Override
    public void logoutFail() {

    }
}
