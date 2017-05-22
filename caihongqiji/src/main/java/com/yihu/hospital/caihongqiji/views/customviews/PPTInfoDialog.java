package com.yihu.hospital.caihongqiji.views.customviews;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yihu.hospital.caihongqiji.views.LiveActivity;


/**
 * 文本输入框
 */
public class PPTInfoDialog extends Dialog {

    private static final String TAG = PPTInfoDialog.class.getSimpleName();
    private LiveActivity mVideoPlayActivity;

    private WebView mWebView;

    public PPTInfoDialog(Context context, int theme, LiveActivity activity, String url) {
        super(context, theme);
        Log.i("wzw", "wzw dialog ppt in ");
        mVideoPlayActivity = activity;
        setContentView(com.yihu.hospital.caihongqiji.R.layout.web_view);
        mWebView = (WebView) findViewById(com.yihu.hospital.caihongqiji.R.id.webview);
        mWebView.loadUrl(url);
//        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
        mWebView.getSettings().setJavaScriptEnabled(true);//是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
        mWebView.getSettings().setSupportZoom(true);//是否可以缩放，默认true
        mWebView.getSettings().setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
        mWebView.getSettings().setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        mWebView.getSettings().setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
        mWebView.getSettings().setAppCacheEnabled(true);//是否使用缓存
        mWebView.getSettings().setDomStorageEnabled(true);//DOM Storage important
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
    });
        // displayWebview.getSettings().setUserAgentString("User-Agent:Android");//设置用户代理，一般不用
    }



}
