package com.tencent.qcloud.suixinbo.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.suixinbo.R;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.InitBusinessHelper;
import com.tencent.qcloud.suixinbo.presenters.LoginHelper;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LoginView;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LogoutView;
import com.tencent.qcloud.suixinbo.utils.SxbLog;
import com.tencent.qcloud.suixinbo.views.customviews.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录类
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, LoginView {
    TextView mBtnLogin, mBtnRegister;
    EditText mPassWord, mUserName;
    View mLoginView;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private LoginHelper mLoginHeloper;
    private final int REQUEST_PHONE_PERMISSIONS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitBusinessHelper.initApp(getApplicationContext());
        SxbLog.i(TAG, "LoginActivity onCreate");
        mLoginHeloper = new LoginHelper(this, this, new LogoutView() {
            @Override
            public void logoutSucc() {

            }

            @Override
            public void logoutFail() {

            }
        });
        checkPermission();
        //获取个人数据本地缓存
        MySelfInfo.getInstance().getCache(getApplicationContext());
//        if (needLogin() == true) {//本地没有账户需要登录
//            initView();
//        } else {
//            //有账户登录直接IM登录
//            SxbLog.i(TAG, "LoginActivity onCreate");
//            mLoginHeloper.iLiveLogin(MySelfInfo.getInstance().getId(), MySelfInfo.getInstance().getUserSig());
//        }

        initView();

        Intent intent = getIntent();
        String id = intent.getStringExtra(RegisterActivity.ID);
        String name = intent.getStringExtra(RegisterActivity.NAME);

        mUserName.setText(id == null ? MySelfInfo.getInstance().getId() : id);
        mPassWord.setText(name == null ? MySelfInfo.getInstance().getPwd() : name);

        // 初始化直播模块
/*        ILVLiveConfig liveConfig = new ILVLiveConfig();
        liveConfig.messageListener(MessageEvent.getInstance());
        ILVLiveManager.getInstance().init(liveConfig);*/
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "wzw onDestroy");
        mLoginHeloper.onDestory();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.registerNewUser) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        }
        if (view.getId() == R.id.btn_login) {//登录账号系统TLS
            if (mUserName.getText().equals("")) {
                Toast.makeText(LoginActivity.this, "name can not be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mPassWord.getText().equals("")) {
                Toast.makeText(LoginActivity.this, "password can not be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            mLoginHeloper.standardLogin(mUserName.getText().toString(), mPassWord.getText().toString());
        } else if (view.getId() == R.id.login_view) {
            jumpIntoHomeActivity();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_independent_login);
        mBtnLogin = (TextView) findViewById(R.id.btn_login);
        mUserName = (EditText) findViewById(R.id.username);
        mPassWord = (EditText) findViewById(R.id.password);
        mLoginView = (View) findViewById(R.id.login_view);
        mBtnRegister = (TextView) findViewById(R.id.registerNewUser);
        mBtnRegister.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);
        mLoginView.setOnClickListener(this);
    }


    /**
     * 判断是否需要登录
     *
     * @return true 代表需要重新登录
     */
    public boolean needLogin() {
        if (MySelfInfo.getInstance().getId() != null) {
            return false;//有账号不需要登录
        } else {
            return true;//需要登录
        }

    }


    /**
     * 直接跳转主界面
     */
    private void jumpIntoHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void loginSucc() {
        Toast.makeText(LoginActivity.this, "" + MySelfInfo.getInstance().getId() + " login_view ", Toast.LENGTH_SHORT).show();
        mLoginView.setVisibility(View.VISIBLE);
    }

    @Override
    public void loginFail(String mode, int code ,String errorinfo) {
        if (code == 30003) {
            // wzw 登录成功判断邀请码
            final EditText et = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("请输入邀请码")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(et)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            mLoginHeloper.checkYQM(mUserName.getText().toString(), mPassWord.getText().toString(), et.getText().toString());
//                            if (et.getText().toString().equals("1234")) {
//
//                            } else {
//                                Toast.makeText(LoginActivity.this, "邀请码不正确，请重新输入！", Toast.LENGTH_SHORT).show();
//                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            Toast.makeText(LoginActivity.this, errorinfo, Toast.LENGTH_SHORT).show();
        }

    }

    void checkPermission() {
        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.READ_PHONE_STATE);
            if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if ((checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.ACCESS_NETWORK_STATE);
            if ((checkSelfPermission(Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.CHANGE_NETWORK_STATE);
            if (permissionsList.size() != 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_PHONE_PERMISSIONS);
            }
        }
    }
}
