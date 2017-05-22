package com.yihu.hospital.caihongqiji.views.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


/**
 * 设置等页面条状控制或显示信息的控件
 */
public class LineControllerView extends LinearLayout {

    private String name;
    private boolean isBottom;
    private String content;
    private boolean canNav;
    private boolean isSwitch;

    public LineControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(com.yihu.hospital.caihongqiji.R.layout.view_line_controller, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, com.yihu.hospital.caihongqiji.R.styleable.LineControllerView, 0, 0);
        try {
            name = ta.getString(com.yihu.hospital.caihongqiji.R.styleable.LineControllerView_name);
            content = ta.getString(com.yihu.hospital.caihongqiji.R.styleable.LineControllerView_content);
            isBottom = ta.getBoolean(com.yihu.hospital.caihongqiji.R.styleable.LineControllerView_isBottom, false);
            canNav = ta.getBoolean(com.yihu.hospital.caihongqiji.R.styleable.LineControllerView_canNav,false);
            isSwitch = ta.getBoolean(com.yihu.hospital.caihongqiji.R.styleable.LineControllerView_isSwitch,false);
            setUpView();
        } finally {
            ta.recycle();
        }
    }


    private void setUpView(){
        TextView tvName = (TextView) findViewById(com.yihu.hospital.caihongqiji.R.id.name);
        tvName.setText(name);
        TextView tvContent = (TextView) findViewById(com.yihu.hospital.caihongqiji.R.id.content);
        tvContent.setText(getShortenStr(content));
        View bottomLine = findViewById(com.yihu.hospital.caihongqiji.R.id.bottomLine);
        bottomLine.setVisibility(isBottom ? VISIBLE : GONE);
        ImageView navArrow = (ImageView) findViewById(com.yihu.hospital.caihongqiji.R.id.rightArrow);
        navArrow.setVisibility(canNav ? VISIBLE : GONE);
        LinearLayout contentPanel = (LinearLayout) findViewById(com.yihu.hospital.caihongqiji.R.id.contentText);
        contentPanel.setVisibility(isSwitch ? GONE : VISIBLE);
        Switch switchPanel = (Switch) findViewById(com.yihu.hospital.caihongqiji.R.id.btnSwitch);
        switchPanel.setVisibility(isSwitch?VISIBLE:GONE);

    }


    /**
     * 设置文字内容
     *
     * @param content 内容
     */
    public void setContent(String content){
        this.content = content;
        TextView tvContent = (TextView) findViewById(com.yihu.hospital.caihongqiji.R.id.content);
        tvContent.setText(getShortenStr(content));
    }


    /**
     * 获取内容
     *
     */
    public String getContent(){
        TextView tvContent = (TextView) findViewById(com.yihu.hospital.caihongqiji.R.id.content);
        return tvContent.getText().toString();
    }


    /**
     * 设置是否可以跳转
     *
     * @param canNav 是否可以跳转
     */
    public void setCanNav(boolean canNav){
        this.canNav = canNav;
        ImageView navArrow = (ImageView) findViewById(com.yihu.hospital.caihongqiji.R.id.rightArrow);
        navArrow.setVisibility(canNav ? VISIBLE : GONE);
    }

    private String getShortenStr(String str){
        if (str == null) return "";
        if (str.length()>23){
            return str.substring(0,23)+"...";
        }
        return str;
    }
}
