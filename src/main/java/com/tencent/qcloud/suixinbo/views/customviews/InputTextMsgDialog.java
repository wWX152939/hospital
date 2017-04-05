package com.tencent.qcloud.suixinbo.views.customviews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.TIMElem;
import com.tencent.TIMMessage;
import com.tencent.TIMTextElem;
import com.tencent.TIMUserProfile;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.qcloud.suixinbo.R;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MemberID;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.utils.SxbLog;
import com.tencent.qcloud.suixinbo.views.LiveActivity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


/**
 * 文本输入框
 */
public class InputTextMsgDialog extends Dialog {
    // wzw add
    private PopupWindow mPopupWindow;
    private ViewGroup mContactSelectGroup;
    private ListView mContactSelectListView;


    private TextView confirmBtn;
    private EditText messageTextView;
    private static final String TAG = InputTextMsgDialog.class.getSimpleName();
    private Context mContext;
    private LiveActivity mVideoPlayActivity;
    private InputMethodManager imm;
    private RelativeLayout rlDlg;
    private int mLastDiff = 0;
    private final String reg = "[`~@#$%^&*()-_+=|{}':;,/.<>￥…（）—【】‘；：”“’。，、]";
    private Pattern pattern = Pattern.compile(reg);

    public InputTextMsgDialog(Context context, int theme, LiveActivity activity) {
        super(context, theme);
        mContext = context;
        mVideoPlayActivity = activity;
        setContentView(R.layout.input_text_dialog);
        messageTextView = (EditText) findViewById(R.id.input_message);
        confirmBtn = (TextView) findViewById(R.id.confrim_btn);
//        rlDlg = (RelativeLayout) findViewById(R.id.rl_dlg);
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageTextView.getText().length() > 0) {
                    sendText("" + messageTextView.getText());
                    imm.showSoftInput(messageTextView, InputMethodManager.SHOW_FORCED);
                    imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
                    dismiss();
                } else {
                    Toast.makeText(mContext, "input can not be empty!", Toast.LENGTH_LONG).show();
                }
            }
        });

        initContactSelectLayout();
        initPopWindow();

        // 设置文本内容变化监听器
        messageTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (messageTextView.getText().toString().equals("@")) {
                    //TODO
                    if (CurLiveInfo.getExpertList() != null && !CurLiveInfo.getExpertList().isEmpty()) {
                        Log.i("wzw", "wzw mPopupWindow show");
                        mPopupWindow.showAsDropDown(messageTextView);

                    } else {
                        mPopupWindow.dismiss();
                    }
                } else {
                    mPopupWindow.dismiss();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                mPopupWindow.setFocusable(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                mPopupWindow.setFocusable(true);
            }
        });
        messageTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() != KeyEvent.ACTION_UP) {   // 忽略其它事件
                    return false;
                }

                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        if (messageTextView.getText().length() > 0) {
                            sendText("" + messageTextView.getText());
                            imm.showSoftInput(messageTextView, InputMethodManager.SHOW_FORCED);
                            imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
                            dismiss();
                        } else {
                            Toast.makeText(mContext, "input can not be empty!", Toast.LENGTH_LONG).show();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

        final LinearLayout rldlgview = (LinearLayout) findViewById(R.id.rl_inputdlg_view);
//        rldlgview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Rect r = new Rect();
//                //获取当前界面可视部分
//                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
//                //获取屏幕的高度
//                int screenHeight = getWindow().getDecorView().getRootView().getHeight();
//                //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
//                int heightDifference = screenHeight - r.bottom;
//
//                if (heightDifference <= 0 && mLastDiff > 0) {
//                    imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
//                    dismiss();
//                }
//                mLastDiff = heightDifference;
//            }
//        });
//        rldlgview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
//                dismiss();
//            }
//        });
    }

    /**
     * 初始化弹出窗口
     */
    @SuppressWarnings("deprecation")
    private void initPopWindow() {
        Log.i("wzw", "wzw initPopWindow");
        mPopupWindow = new PopupWindow(mContactSelectGroup, 300, 240, true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    /**
     * 初始化专家选择布局
     */
    private void initContactSelectLayout() {

        Log.i("wzw", "wzw initContactSelectLayout");
        mContactSelectGroup = (ViewGroup) getLayoutInflater()
                .inflate(R.layout.listview_layout, null, false);

        mContactSelectListView = (ListView)
                mContactSelectGroup.findViewById(R.id.list_view);
        UserAdapter mUserAdapter = new UserAdapter(mVideoPlayActivity, R.layout.text_view, CurLiveInfo.getExpertList());
        mContactSelectListView.setAdapter(mUserAdapter);
    }

    class UserAdapter extends ArrayAdapter<MemberID> {
        private int mResourceId;
        private final LayoutInflater mInflater;

        public UserAdapter(Context context, int textViewResourceId, List<MemberID> idList) {
            super(context, textViewResourceId, idList);
            this.mResourceId = textViewResourceId;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView == null) {
                view = mInflater.inflate(mResourceId, parent, false);
            } else {
                view = convertView;
            }
            TextView text = (TextView) view;
            final CharSequence s = (CharSequence) getItem(position).getId();
            Log.i("wzw", "wzw s:" + s);
            text.setText(s);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageTextView.setText("@" + s + ":");
                    messageTextView.setSelection(messageTextView.getText().length());
                    mPopupWindow.dismiss();
                }
            });

            return view;
        }
    }

    /**
     * add message text
     */
    public void setMessageText(String strInfo) {
        messageTextView.setText(strInfo);
        messageTextView.setSelection(strInfo.length());
    }

    @Override
    public void dismiss() {
        super.dismiss();
//        mVideoPlayActivity.refreshViewAfterDialog();
    }

    @Override
    public void cancel() {
        super.cancel();
    }


    private void sendText(String msg) {
        if (msg.length() == 0)
            return;
        try {
            byte[] byte_num = msg.getBytes("utf8");
            if (byte_num.length > 160) {
                Toast.makeText(mContext, "input message too long", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        TIMMessage Nmsg = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(msg);
        if (Nmsg.addElement(elem) != 0) {
            return;
        }

        //TODO
        boolean flag = false;
        String id = "";
        if (msg.startsWith("@")) {
            Log.i("wzw", "wzw start with @");
            String[] msgs = msg.split(":");
            Log.i("wzw", "wzw msgs:" + msgs.toString() + " len:" + msgs.length);
            if (msgs.length != 0) {
                id = msgs[0].substring(1, msgs[0].length());
                Log.i("wzw", "id:" + id + " curList:" + CurLiveInfo.getExpertList());
                for (int i = 0; i < CurLiveInfo.getExpertList().size(); i ++) {
                    if (CurLiveInfo.getExpertList().get(i).getId().contains(id)) {
                        flag = true;
                        break;
                    }
                }
            }
        }

        if (flag) {
            Log.i("wzw", "wzw send c2c msg");
            setC2CMsg(id, Nmsg);
        } else {
            Log.i("wzw", "wzw send group msg");
            setGroupMsg(Nmsg);
        }
    }

    private void setC2CMsg(String id, TIMMessage Nmsg) {
        ILiveRoomManager.getInstance().sendC2CMessage(id, Nmsg, new ILiveCallBack<TIMMessage>() {
            @Override
            public void onSuccess(TIMMessage data) {
                //发送成回显示消息内容
                for (int j = 0; j < data.getElementCount(); j++) {
                    TIMElem elem = (TIMElem) data.getElement(0);
                    TIMTextElem textElem = (TIMTextElem) elem;
                    if (data.isSelf()) {
                        if (mVideoPlayActivity != null)
                            mVideoPlayActivity.refreshText(textElem.getText(), MySelfInfo.getInstance().getNickName());
//                        handleTextMessage(elem, MySelfInfo.getInstance().getNickName());
                    } else {
                        TIMUserProfile sendUser = data.getSenderProfile();
                        String name;
                        if (sendUser != null) {
                            name = sendUser.getNickName();
                        } else {
                            name = data.getSender();
                        }
                        if (mVideoPlayActivity != null)
                            mVideoPlayActivity.refreshText(textElem.getText(), name);
                    }
                }
                SxbLog.d(TAG, "sendGroupMessage->success");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(mContext, "send msg failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGroupMsg(TIMMessage Nmsg) {
        ILiveRoomManager.getInstance().sendGroupMessage(Nmsg, new ILiveCallBack<TIMMessage>() {
            @Override
            public void onSuccess(TIMMessage data) {
                //发送成回显示消息内容
                for (int j = 0; j < data.getElementCount(); j++) {
                    TIMElem elem = (TIMElem) data.getElement(0);
                    TIMTextElem textElem = (TIMTextElem) elem;
                    if (data.isSelf()) {
                        if (mVideoPlayActivity != null)
                            mVideoPlayActivity.refreshText(textElem.getText(), MySelfInfo.getInstance().getNickName());
//                        handleTextMessage(elem, MySelfInfo.getInstance().getNickName());
                    } else {
                        TIMUserProfile sendUser = data.getSenderProfile();
                        String name;
                        if (sendUser != null) {
                            name = sendUser.getNickName();
                        } else {
                            name = data.getSender();
                        }
                        if (mVideoPlayActivity != null)
                            mVideoPlayActivity.refreshText(textElem.getText(), name);
                    }
                }
                SxbLog.d(TAG, "sendGroupMessage->success");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(mContext, "send msg failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager inputManager = (InputMethodManager) messageTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(messageTextView, 0);
            }

        }, 500);
    }
}
