package com.yihu.hospital.caihongqiji.presenters.viewinface;


/**
 * 登录回调
 */
public interface LoginView extends MvpView{

    void loginSucc();

    void loginFail(String module, int errCode, String errMsg);
}
