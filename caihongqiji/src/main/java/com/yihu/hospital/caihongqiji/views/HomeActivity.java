package com.yihu.hospital.caihongqiji.views;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

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
import com.yihu.hospital.caihongqiji.views.customviews.BaseViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 */
public class HomeActivity extends BaseFragmentActivity implements LogoutView {
    private LoginHelper mLoginHelper;
    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView mTextView1, mTextView2, mTextView3;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<View> viewList = new ArrayList<>();
        View view1 = getLayoutInflater().inflate(R.layout.home_activity_1, null);
        View view2 = getLayoutInflater().inflate(R.layout.home_activity_2, null);
        View view3 = getLayoutInflater().inflate(R.layout.home_activity_3, null);
        initView2(view2);
        initView3(view3);
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);
        BaseViewPager viewPager = new BaseViewPager(this, viewList);
        setContentView(viewPager.getRootView());
        SxbLog.i(TAG, "HomeActivity onCreate");
    }

    private void initView2(View rootView) {
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RoomListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initView3(View rootView) {
        mTextView1 = (TextView) rootView.findViewById(R.id.tv12);
        mTextView2 = (TextView) rootView.findViewById(R.id.tv22);
        mTextView3 = (TextView) rootView.findViewById(R.id.tv32);
        mButton = (Button) rootView.findViewById(R.id.btn_exit);

        mTextView1.setText(MySelfInfo.getInstance().getNickName());
        mTextView2.setText(MySelfInfo.getInstance().getId());
        mTextView3.setText(MySelfInfo.getInstance().getNickName());
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginHelper == null) {
                    mLoginHelper = new LoginHelper(HomeActivity.this, HomeActivity.this);
                }

                mLoginHelper.standardLogout(MySelfInfo.getInstance().getId());
            }
        });
    }

    @Override
    protected void onStart() {
        SxbLog.i(TAG, "HomeActivity onStart");
        super.onStart();
    }

    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed");
//        if (mLoginHelper == null) {
//            mLoginHelper = new LoginHelper(this, this);
//        }
//
//        mLoginHelper.standardLogout(MySelfInfo.getInstance().getId());
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "HomeActivity onDestroy");
        super.onDestroy();
    }


    @Override
    public void logoutSucc() {
        Log.i(TAG, "HomeActivity logoutSucc");
        MySelfInfo.getInstance().setLogout(true);
        MySelfInfo.getInstance().writeToCache(this);
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void logoutFail() {

    }
}
