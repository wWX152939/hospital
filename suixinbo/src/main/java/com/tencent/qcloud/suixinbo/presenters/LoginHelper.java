package com.tencent.qcloud.suixinbo.presenters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LoginView;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LogoutView;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

/**
 * 登录的数据处理类
 */
public class LoginHelper extends Presenter {
    private Context mContext;
    private static final String TAG = LoginHelper.class.getSimpleName();
    private LoginView mLoginView;
    private LogoutView mLogoutView;

    public LoginHelper(Context context) {
        mContext = context;
    }

    public LoginHelper(Context context, LoginView loginView) {
        mContext = context;
        mLoginView = loginView;
    }

    public LoginHelper(Context context, LogoutView logoutView) {
        mContext = context;
        mLogoutView = logoutView;
    }

    public LoginHelper(Context context, LoginView loginView, LogoutView logoutView) {
        mContext = context;
        mLoginView = loginView;
        mLogoutView = logoutView;
    }


    //登录模式登录
    private StandardLoginTask loginTask;

    class StandardLoginTask extends AsyncTask<String, Integer, UserServerHelper.RequestBackInfo> {

        @Override
        protected UserServerHelper.RequestBackInfo doInBackground(String... strings) {

            if (strings.length == 3) {
                return UserServerHelper.getInstance().checkyqm(strings[0], strings[1], strings[2]);
            } else {
                return UserServerHelper.getInstance().loginId(strings[0], strings[1]);
            }
        }

        @Override
        protected void onPostExecute(UserServerHelper.RequestBackInfo result) {

            if (result != null) {
                if (result.getErrorCode() == 0) {
                    MySelfInfo.getInstance().writeToCache(mContext);
                    //登录
                    Log.i("wzw", "trace1 id:" + MySelfInfo.getInstance().getId()
                        + " getUserSig:" + MySelfInfo.getInstance().getUserSig());
                    iLiveLogin(MySelfInfo.getInstance().getId(), MySelfInfo.getInstance().getUserSig());
                } else {
                    mLoginView.loginFail("Module_TLSSDK", result.getErrorCode(), result.getErrorInfo());
                }
            } else {
                Toast.makeText(mContext, "服务器异常", Toast.LENGTH_SHORT).show();
            }

        }
    }


    public void iLiveLogin(String id, String sig) {
        //登录
        ILiveLoginManager.getInstance().iLiveLogin(id, sig, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                if (mLoginView != null)
                    mLoginView.loginSucc();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (mLoginView != null)
                    mLoginView.loginFail(module, errCode, errMsg);
            }
        });
    }


    /**
     * 退出imsdk <p> 退出成功会调用退出AVSDK
     */
    public void iLiveLogout() {
        //TODO 新方式登出ILiveSDK
        ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                SxbLog.i(TAG, "IMLogout succ !");
                //清除本地缓存
//                MySelfInfo.getInstance().clearCache(mContext);
                mLogoutView.logoutSucc();
                SharedPreferences.Editor editor = mContext.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                editor.putBoolean("living", false);
                editor.apply();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                SxbLog.e(TAG, "IMLogout fail ：" + module + "|" + errCode + " msg " + errMsg);
            }
        });
    }

    /**
     * 独立模式 登录
     */
    public void standardLogin(String id, String password) {
        loginTask = new StandardLoginTask();
        loginTask.execute(id, password);

    }

    /**
     * 验证邀请码
     */
    public void checkYQM(String id, String password, String code) {
        loginTask = new StandardLoginTask();
        loginTask.execute(id, password, code);

    }


    /**
     * 独立模式 注册
     */
    public void standardRegister(final String id, final String psw, final String email, String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final UserServerHelper.RequestBackInfo result = UserServerHelper.getInstance().registerId(id, psw, email);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    public void run() {

                        if (result != null && result.getErrorCode() == 0) {
                            standardLogin(id, psw);
                        } else if (result != null) {
                            //
                            Toast.makeText(mContext, "  " + result.getErrorCode() + " : " + result.getErrorInfo(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }


    /**
     * 独立模式 登出
     */
    public void standardLogout(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserServerHelper.RequestBackInfo result = UserServerHelper.getInstance().logoutId();
                if (result != null && (result.getErrorCode() == 0 || result.getErrorCode() == 10008)) {
                }
            }
        }).start();
        iLiveLogout();
    }


    @Override
    public void onDestory() {
        mLoginView = null;
        mLogoutView = null;
        mContext = null;
    }
}
