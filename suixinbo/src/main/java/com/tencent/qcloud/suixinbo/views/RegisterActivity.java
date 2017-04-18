package com.tencent.qcloud.suixinbo.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.suixinbo.QavsdkApplication;
import com.tencent.qcloud.suixinbo.R;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.LoginHelper;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LoginView;
import com.tencent.qcloud.suixinbo.utils.Validator;
import com.tencent.qcloud.suixinbo.views.customviews.BaseActivity;

/**
 * 注册账号类
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener, LoginView {
    private EditText mUserName, mPassword, mRepassword, mEmail;
    private TextView mBtnRegister;
    private ImageButton mBtnBack;
    QavsdkApplication mMyApplication;
    LoginHelper mLoginHeloper;
    private static final String TAG = RegisterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_independent_register);
        mUserName = (EditText) findViewById(R.id.username);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mRepassword = (EditText) findViewById(R.id.repassword);
        mBtnRegister = (TextView) findViewById(R.id.btn_register);
        mBtnBack = (ImageButton) findViewById(R.id.back);
        mBtnBack.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
        mMyApplication = (QavsdkApplication) getApplication();
        mLoginHeloper = new LoginHelper(this, this);
    }

    @Override
    protected void onDestroy() {
        mLoginHeloper.onDestory();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_register) {
            String userId = mUserName.getText().toString();
            String userPW = mPassword.getText().toString();
            String userEmail = mEmail.getText().toString();
            String userPW2 = mRepassword.getText().toString();


            if (userId.length() < 4 || userId.length() > 24) {
                Log.i(TAG, "onClick " + userId.length());
                Toast.makeText(RegisterActivity.this, "用户名不符合格式", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Validator.isEmail(userEmail)) {
                Toast.makeText(RegisterActivity.this, "邮箱格式不合法", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId.length() == 0 || userPW.length() == 0 || userPW2.length() == 0) {
                Toast.makeText(RegisterActivity.this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!userPW.equals(userPW2)) {
                Toast.makeText(RegisterActivity.this, "两次密码输入密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userPW.length() < 8) {
                Toast.makeText(RegisterActivity.this, "密码的长度不能小于8个字符", Toast.LENGTH_SHORT).show();
                return;
            }

            //注册一个账号
            mLoginHeloper.standardRegister(userId, mPassword.getText().toString(), userEmail);
        }
        if (view.getId() == R.id.back) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void loginSucc() {
        Toast.makeText(RegisterActivity.this, "" + MySelfInfo.getInstance().getId() + " login ", Toast.LENGTH_SHORT).show();
        jumpIntoHomeActivity();
    }

    @Override
    public void loginFail(String module, int errCode, String errMsg) {
        if (errCode == 30003) {
            // wzw 登录成功判断邀请码
            final EditText et = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("请输入邀请码")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(et)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            mLoginHeloper.checkYQM(mUserName.getText().toString(), mPassword.getText().toString(), et.getText().toString());
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
                            startLoginActivity();
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, errMsg , Toast.LENGTH_SHORT).show();
            startLoginActivity();
        }

    }

    public static final String ID = "user_id";
    public static final String NAME = "user_name";
    private void startLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra(ID, mUserName.getText().toString());
        intent.putExtra(NAME, mPassword.getText().toString());
        startActivity(intent);
        finish();
    }

    /**
     * 直接跳转主界面
     */
    private void jumpIntoHomeActivity() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

}
