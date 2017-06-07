package com.yihu.hospital.caihongqiji.views.customviews;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.yihu.hospital.caihongqiji.R;

import java.util.List;

public class BaseViewPager implements View.OnClickListener{

    private ViewPager mViewPager;
    private LinearLayout mLinearLayout;
    private View mRootView;
    private Activity mActivity;
    private ImageButton mImageButton1, mImageButton2, mImageButton3;

    public View getRootView() {
        return mRootView;
    }
    public void setImageBtnIcons() {
        mLinearLayout = (LinearLayout) mRootView.findViewById(R.id.title_menu);
        mLinearLayout.setVisibility(View.VISIBLE);
        // 添加imagebtn
        mImageButton1 = (ImageButton) mRootView.findViewById(R.id.ibt1);
        mImageButton2 = (ImageButton) mRootView.findViewById(R.id.ibt2);
        mImageButton3 = (ImageButton) mRootView.findViewById(R.id.ibt3);
        mImageButton1.setOnClickListener(this);
        mImageButton2.setOnClickListener(this);
        mImageButton3.setOnClickListener(this);
    }

    public BaseViewPager (Activity activity, List<View> contentViewList) {
        mActivity = activity;
        LayoutInflater inflater = mActivity.getLayoutInflater();
        mRootView = inflater.inflate(R.layout.base_view_pager, null);
        setImageBtnIcons();
        mViewPager = (ViewPager) mRootView.findViewById(R.id.viewpager);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateImageBtnIcons(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(new ViewPagerAdapter(contentViewList));

        //初始化状态
        mViewPager.setCurrentItem(1);
    }

    private void updateImageBtnIcons(int position) {
        initImageBtnIcons();
        setClickBtnIcon(position);
    }

    private void setClickBtnIcon(int position) {
        switch (position) {
            case 0:
                mImageButton1.setBackgroundResource(R.drawable.btn_click_document);
                break;
            case 1:
                mImageButton2.setBackgroundResource(R.drawable.btn_click_live);
                break;
            case 2:
                mImageButton3.setBackgroundResource(R.drawable.btn_click_me);
                break;
        }
    }

    private void initImageBtnIcons() {
        mImageButton1.setBackgroundResource(R.drawable.btn_normal_document);
        mImageButton2.setBackgroundResource(R.drawable.btn_normal_live);
        mImageButton3.setBackgroundResource(R.drawable.btn_normal_me);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibt1) {
            mViewPager.setCurrentItem(0);
        } else if (v.getId() == R.id.ibt2) {
            mViewPager.setCurrentItem(1);
        } else if (v.getId() == R.id.ibt3) {
            mViewPager.setCurrentItem(2);
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {

        private List<View> mChildViewList;
        public ViewPagerAdapter(List<View> viewList) {
            mChildViewList = viewList;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return mChildViewList.size();
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager)container).removeView(mChildViewList.get(position));
        }


        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager)container).addView(mChildViewList.get(position));
            return mChildViewList.get(position);
        }


    }
}
