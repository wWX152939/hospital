package com.yihu.caihongqijimain;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.yihu.hospital.caihongqiji.views.LoginActivity;

//import com.tencent.tdemolive.LiveActivity;

/**
 * 示例菜单
 */
public class MainMenu extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("wzw", "wzw onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mainmenu);
        Intent intent = new Intent();
        intent.setClass(MainMenu.this, LoginActivity.class);
        startActivity(intent);

//        listDemo.add("Live: 简单直播");
//        listDemo.add("Suixinbo:新随心播");
//        adapterDemo = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
//                listDemo);
//        lvMenu.setAdapter(adapterDemo);
//        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent();
//
//                switch (position) {
//                    case 0:
//                        intent.setClass(MainMenu.this, LiveActivity.class);
//                        break;
//                    case 1:
//                        intent.setClass(MainMenu.this, LoginActivity.class);
//                        break;
//                }
//
//                startActivity(intent);
//            }
//        });
    }

    String TAG = "wzw";
    //Activity创建或者从后台重新回到前台时被调用
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart called.");
    }

    //Activity从后台重新回到前台时被调用
    @Override
    protected void onRestart() {
        finish();
        super.onDestroy();
        Log.i(TAG, "onRestart called. finish");
    }

    //Activity创建或者从被覆盖、后台重新回到前台时被调用
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called.");
    }

    //Activity窗口获得或失去焦点时被调用,在onResume之后或onPause之后
    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged called.");
    }*/

    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause called.");
        //有可能在执行完onPause或onStop后,系统资源紧张将Activity杀死,所以有必要在此保存持久数据
    }

    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop called.");
    }

    //退出当前Activity时被调用,调用之后Activity就结束了
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestory called.");
    }

}
