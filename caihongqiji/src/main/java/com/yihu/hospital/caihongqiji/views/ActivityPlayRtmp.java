package com.yihu.hospital.caihongqiji.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.TIMElem;
import com.tencent.TIMMessage;
import com.tencent.TIMTextElem;
import com.tencent.TIMUserProfile;
import com.tencent.av.TIMAvManager;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.yihu.hospital.caihongqiji.adapters.ChatMsgListAdapter;
import com.yihu.hospital.caihongqiji.model.ChatEntity;
import com.yihu.hospital.caihongqiji.model.CurLiveInfo;
import com.yihu.hospital.caihongqiji.model.CustomMsgEntity;
import com.yihu.hospital.caihongqiji.model.LiveInfoJson;
import com.yihu.hospital.caihongqiji.model.MemberID;
import com.yihu.hospital.caihongqiji.model.MySelfInfo;
import com.yihu.hospital.caihongqiji.presenters.RtmpHelper;
import com.yihu.hospital.caihongqiji.presenters.UserServerHelper;
import com.yihu.hospital.caihongqiji.presenters.viewinface.LiveView;
import com.yihu.hospital.caihongqiji.utils.Constants;
import com.yihu.hospital.caihongqiji.utils.SxbLog;
import com.yihu.hospital.caihongqiji.views.customviews.BaseActivity;
import com.yihu.hospital.caihongqiji.views.customviews.CustomTextView;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityPlayRtmp extends BaseActivity implements ITXLivePlayListener {
    private final static String TAG = "ActivityPlayRtmp";
    private final static String RTMP_URL = "rtmp://4425.liveplay.myqcloud.com/live/4425_fb4eb728355611e791eae435c87f075e";
    private TXCloudVideoView txvvPlayerView;
    private TXLivePlayer mTxlpPlayer;

    private LinearLayout mStartView;
    private LinearLayout mIMView;
    private ImageButton mImageButtonStart;
    private ImageButton mImageButtonSwitch;
    private ImageButton mImageButtonFullScreen;
    private ImageButton mImageButtonExitFullScreen;
    private ListView mListViewMsg;
    private ImageView mImageViewBack, mImageViewSend;
    private EditText mEtMsg;
    private PopupWindow mPopupWindow;
    private ViewGroup mContactSelectGroup;
    private ListView mContactSelectListView;

    private InputMethodManager imm;
    private RtmpHelper mRtmpHelper;
    private Timer mHearBeatTimer;
    private HeartBeatTask mHeartBeatTask;//心跳

    private boolean mIsStart = false;
    private boolean mIsFullScreenMode = false;

    //TOP

    private ArrayList<ChatEntity> mTopArrayListChatEntity;
    private ChatMsgListAdapter mTopChatMsgListAdapter;
    private ArrayList<ChatEntity> mTopTmpChatList = new ArrayList<ChatEntity>();//缓冲队列
    private boolean mTopBoolNeedRefresh = false;
    private boolean mTopBoolRefreshLock = false;
    private TimerTask mTopTimerTask = null;
    private ListView mTopListViewMsgItems;

    /**
     * 直播心跳
     */
    private class HeartBeatTask extends TimerTask {
        @Override
        public void run() {
            UserServerHelper.getInstance().heartBeater(MySelfInfo.getInstance().getIdStatus());
            requestExpertList();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.yihu.hospital.caihongqiji.R.layout.activity_play_rtmp);

        initView();

        mTxlpPlayer = new TXLivePlayer(this);

        mTxlpPlayer.setPlayerView(txvvPlayerView);
        mTxlpPlayer.setConfig(new TXLivePlayConfig());
        mTxlpPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);

        mRtmpHelper = new RtmpHelper(this, new LiveView() {

            @Override
            public void enterRoomComplete(int id_status, boolean succ) {
                Log.i("wzw", "enterRoomComplete");
                if (succ == true) {
                    //主播心跳
                    mHearBeatTimer = new Timer(true);
                    mHeartBeatTask = new HeartBeatTask();
                    mHearBeatTimer.schedule(mHeartBeatTask, 100, 5 * 1000); //5秒重复上报心跳 拉取房间列表

                }
            }

            @Override
            public void quiteRoomComplete(int id_status, boolean succ, LiveInfoJson liveinfo) {
                Log.i("wzw", "quiteRoomComplete");
            }

            @Override
            public void showInviteDialog() {
                Log.i("wzw", "showInviteDialog");
            }

            @Override
            public void refreshText(String text, String name) {
                Log.i("wzw", "refreshText");
                if (text != null) {
                    refreshTextListView(name, text, Constants.TEXT_TYPE);
                }
            }

            @Override
            public void refreshTopText(String sequence, String text, String name) {
                Log.i("wzw", "refreshTopText");
                if (text != null) {
                    ChatEntity entity = new ChatEntity();
                    entity.setSenderName("置顶");
                    entity.setContext(text);
                    entity.setSequence(sequence);
                    entity.setType(Constants.TEXT_TYPE);
                    mTopBoolNeedRefresh = true;
                    mTopTmpChatList.add(entity);
                    if (mTopBoolRefreshLock) {
                        return;
                    } else {
                        doTopRefreshListView();
                    };
                    //mChatMsgListAdapter.notifyDataSetChanged();

                    mTopListViewMsgItems.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void cancelTopText(String sequence) {
                Log.i("wzw", "cancelTopText");
                for (ChatEntity entity : mTopArrayListChatEntity) {
                    if (entity.getSequence().equals(sequence)) {
                        mTopArrayListChatEntity.remove(entity);
                        mTopChatMsgListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void refreshThumbUp() {
                Log.i("wzw", "refreshThumbUp");
            }

            @Override
            public void refreshUI(String id) {
                Log.i("wzw", "refreshUI");
            }

            @Override
            public boolean showInviteView(String id) {
                return false;
            }

            @Override
            public void cancelInviteView(String id) {
                Log.i("wzw", "cancelInviteView");
            }

            @Override
            public void cancelMemberView(String id) {
                Log.i("wzw", "cancelMemberView");
            }

            @Override
            public void memberJoin(String id, String name) {
                Log.i("wzw", "memberJoin");
            }

            @Override
            public void hideInviteDialog() {
                Log.i("wzw", "hideInviteDialog");
            }

            @Override
            public void pushStreamSucc(TIMAvManager.StreamRes streamRes) {
                Log.i("wzw", "pushStreamSucc");
            }

            @Override
            public void stopStreamSucc() {
                Log.i("wzw", "stopStreamSucc");
            }

            @Override
            public void startRecordCallback(boolean isSucc) {
                Log.i("wzw", "startRecordCallback");
            }

            @Override
            public void stopRecordCallback(boolean isSucc, List<String> files) {
                Log.i("wzw", "stopRecordCallback");
            }

            @Override
            public void hostLeave(String id, String name) {
                Log.i("wzw", "refreshTopText");
            }

            @Override
            public void hostBack(String id, String name) {
                Log.i("wzw", "hostBack");
            }

            @Override
            public void refreshMember(ArrayList<MemberID> memlist) {
                Log.i("wzw", "refreshMember");
            }
        });

        mRtmpHelper.startEnterRoom();

        requestExpertList();
    }

    private void initTopView() {
        //wzw add for top
        mTopListViewMsgItems = (ListView) findViewById(com.yihu.hospital.caihongqiji.R.id.im_msg_top_listview);
        mTopArrayListChatEntity = new ArrayList<ChatEntity>();
        mTopChatMsgListAdapter = new ChatMsgListAdapter(this, mTopListViewMsgItems, mTopArrayListChatEntity) {
            @Override
            protected void setTextColor(CustomTextView sendContext) {
                sendContext.setTextColor(getResources().getColor(com.yihu.hospital.caihongqiji.R.color.colorRed));
            }

            @Override
            protected void setText(CustomTextView sendContext, ChatEntity item, SpannableString spanString) {
                sendContext.setText(item.getContext());
            }
        };
        mTopListViewMsgItems.setAdapter(mTopChatMsgListAdapter);
    }

    private void doTopRefreshListView() {
        if (mTopBoolNeedRefresh) {
            mTopBoolRefreshLock = true;
            mTopBoolNeedRefresh = false;
            mTopArrayListChatEntity.addAll(mTopTmpChatList);
            mTopTmpChatList.clear();
            mTopChatMsgListAdapter.notifyDataSetChanged();

            if (null != mTimerTask) {
                mTimerTask.cancel();
            }
            mTopTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(REFRESH_TOP_LISTVIEW);
                }
            };
            //mTimer.cancel();
            mTimer.schedule(mTopTimerTask, MINFRESHINTERVAL);
        } else {
            mTopBoolRefreshLock = false;
        }
    }

    private void requestExpertList() {
        UserServerHelper.getInstance().requestExpertList(MySelfInfo.getInstance().getToken(), CurLiveInfo.getRoomNum());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTxlpPlayer.setPlayListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("wzw", "play onPause");
        mTxlpPlayer.stopPlay(false);
        txvvPlayerView.onDestroy();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.i("wzw", "play onDestroy");
        mHeartBeatTask.cancel();
        mRtmpHelper.startExitRoom();
    }

    @Override
    public void onPlayEvent(int event, Bundle param) {
        if (TXLiveConstants.PLAY_EVT_PLAY_PROGRESS == event){       // 忽略process事件
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);
            return;
        }

        Log.v(TAG, "onPlayEvent->event: "+event+"|"+param.getString(TXLiveConstants.EVT_DESCRIPTION));
        //错误还是要明确的报一下
        if (event < 0) {
            Toast.makeText(getApplicationContext(), "连接失败，网络异常", Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }

//        if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
//            finish();
//        }
    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }

    private void initPopWindow() {
        Log.i("wzw", "wzw initPopWindow");
        initContactSelectLayout();
        mPopupWindow = new PopupWindow(mContactSelectGroup, 300, 240, true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    /**
     * 初始化专家选择布局
     */
    private void initContactSelectLayout() {

        Log.i("wzw", "wzw initContactSelectLayout");
        mContactSelectGroup = (ViewGroup) getLayoutInflater()
                .inflate(com.yihu.hospital.caihongqiji.R.layout.listview_layout, null, false);

        mContactSelectListView = (ListView)
                mContactSelectGroup.findViewById(com.yihu.hospital.caihongqiji.R.id.list_view);
        UserAdapter mUserAdapter = new UserAdapter(this, com.yihu.hospital.caihongqiji.R.layout.text_view, CurLiveInfo.getExpertList());
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
                    mEtMsg.setText("@" + s + ":");
                    mEtMsg.setSelection(mEtMsg.getText().length());
                    mPopupWindow.dismiss();
                }
            });

            return view;
        }
    }

    private boolean mTouchOver = false;
    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message message) {
            Log.i("wzw", "handleMessage onTouch:" + mTouchOver);
            mTouchOver = true;
            mImageButtonSwitch.setVisibility(View.GONE);
            if (mIsFullScreenMode) {
                // fullscreen mode
                mImageButtonExitFullScreen.setVisibility(View.GONE);
            } else {
                mImageButtonFullScreen.setVisibility(View.GONE);
            }
        }
    };

    private void OnClickDismissBtn() {
        mHandle.removeMessages(0);
        mHandle.sendEmptyMessageDelayed(0, 3000);
    }

    private void initView(){
        initPopWindow();
        initTopView();
        mStartView = (LinearLayout) findViewById(com.yihu.hospital.caihongqiji.R.id.start_view);
        mIMView = (LinearLayout) findViewById(com.yihu.hospital.caihongqiji.R.id.ll_im);

        mImageButtonStart = (ImageButton) findViewById(com.yihu.hospital.caihongqiji.R.id.ib_start);
        mImageButtonFullScreen = (ImageButton) findViewById(com.yihu.hospital.caihongqiji.R.id.ib_fullscreen);
        mImageButtonExitFullScreen = (ImageButton) findViewById(com.yihu.hospital.caihongqiji.R.id.ib_exit_fullscreen);

        mImageButtonSwitch = (ImageButton) findViewById(com.yihu.hospital.caihongqiji.R.id.ib_switch);
        txvvPlayerView = (TXCloudVideoView)findViewById(com.yihu.hospital.caihongqiji.R.id.txvv_play_view);
        mImageViewBack = (ImageView) findViewById(com.yihu.hospital.caihongqiji.R.id.iv_back);
        mImageViewSend = (ImageView) findViewById(com.yihu.hospital.caihongqiji.R.id.iv_send);
        mEtMsg = (EditText) findViewById(com.yihu.hospital.caihongqiji.R.id.et_msg);
        mListViewMsg = (ListView) findViewById(com.yihu.hospital.caihongqiji.R.id.list_view);
        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtMsg.getWindowToken(), 0);
        mImageButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTxlpPlayer.startPlay(RTMP_URL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP);
                mStartView.setVisibility(View.GONE);
                mIsStart = true;
                mHandle.sendEmptyMessageDelayed(0, 3000);
            }
        });
        mImageButtonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsStart) {
                    mTxlpPlayer.stopPlay(false);
                    mImageButtonSwitch.setImageResource(com.yihu.hospital.caihongqiji.R.drawable.round_pause_button);
                    mIsStart = false;
                } else {
                    mTxlpPlayer.startPlay(RTMP_URL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP);
                    mImageButtonSwitch.setImageResource(com.yihu.hospital.caihongqiji.R.drawable.round_play_button);
                    mIsStart = true;
                }
                OnClickDismissBtn();
            }
        });
        mImageButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fullscreen
                mIsFullScreenMode = true;
                mIMView.setVisibility(View.GONE);

                mImageButtonExitFullScreen.setVisibility(View.VISIBLE);
                mImageButtonFullScreen.setVisibility(View.GONE);
                mTxlpPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE);
                mTopListViewMsgItems.setVisibility(View.GONE);

                mImageButtonSwitch.setVisibility(View.GONE);

                OnClickDismissBtn();
            }
        });
        mImageButtonExitFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFullScreenMode = false;
                mIMView.setVisibility(View.VISIBLE);
                mImageButtonExitFullScreen.setVisibility(View.GONE);
                mImageButtonSwitch.setVisibility(View.VISIBLE);
                mImageButtonFullScreen.setVisibility(View.VISIBLE);
                mTopListViewMsgItems.setVisibility(View.VISIBLE);
                mTxlpPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);

                OnClickDismissBtn();
            }
        });
        mImageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtMsg.getText().length() > 0) {
                    // send msg
                    sendText("" + mEtMsg.getText());

                    imm.showSoftInput(mEtMsg, InputMethodManager.SHOW_FORCED);
                    imm.hideSoftInputFromWindow(mEtMsg.getWindowToken(), 0);
                } else {
                    Toast.makeText(ActivityPlayRtmp.this, "input can not be empty!", Toast.LENGTH_LONG).show();
                }
            }
        });

        txvvPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("wzw", "onTouch:" + mTouchOver);
                if (mTouchOver) {
                    mTouchOver = false;
                    if (mIsFullScreenMode) {
                        // fullscreen mode
                        mImageButtonExitFullScreen.setVisibility(View.VISIBLE);
                    } else {
                        mImageButtonSwitch.setVisibility(View.VISIBLE);
                        mImageButtonFullScreen.setVisibility(View.VISIBLE);
                    }
                    mHandle.sendEmptyMessageDelayed(0, 3000);
                }

                return false;
            }
        });

        // 设置文本内容变化监听器
        mEtMsg.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEtMsg.getText().toString().equals("@")) {
                    //TODO
                    if (CurLiveInfo.getExpertList() != null && !CurLiveInfo.getExpertList().isEmpty()) {
                        Log.i("wzw", "wzw mPopupWindow show");
                        mPopupWindow.showAsDropDown(mEtMsg);

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

        initListView();
    }

    private void sendText(String msg) {
        if (msg.length() == 0)
            return;
        try {
            byte[] byte_num = msg.getBytes("utf8");
            if (byte_num.length > 160) {
                Toast.makeText(ActivityPlayRtmp.this, "input message too long", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
                        // 符合@id:msg格式，去除@id:，保留msg
                        msg = msgs[1];
                        flag = true;
                        break;
                    }
                }
            }
        }

        TIMMessage Nmsg = new TIMMessage();
//        Nmsg.setSender(MySelfInfo.getInstance().getNickName());
        TIMTextElem elem = new TIMTextElem();
        if (flag) {
            msg = getMsgObject(CustomMsgEntity.Guest2LiveGuest, msg);
        } else {
            msg = getMsgObject(CustomMsgEntity.GuestGroupChat, msg);
        }
        elem.setText(msg);
        if (Nmsg.addElement(elem) != 0) {
            return;
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
                    String s = getMsgContent(CustomMsgEntity.Guest2LiveGuest, textElem.getText());
                    textElem.setText(s);
                    if (data.isSelf()) {
                        refreshText(textElem.getText(), MySelfInfo.getInstance().getNickName());
//                        handleTextMessage(elem, MySelfInfo.getInstance().getNickName());
                    } else {
                        TIMUserProfile sendUser = data.getSenderProfile();
                        String name;
                        if (sendUser != null) {
                            name = sendUser.getNickName();
                        } else {
                            name = data.getSender();
                        }
                        refreshText(textElem.getText(), name);
                    }
                }
                SxbLog.d(TAG, "sendGroupMessage->success");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(ActivityPlayRtmp.this, "send msg failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
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
                    String s = getMsgContent(CustomMsgEntity.GuestGroupChat, textElem.getText());
                    textElem.setText(s);
                    if (data.isSelf()) {
                        refreshText(textElem.getText(), MySelfInfo.getInstance().getNickName());
//                        handleTextMessage(elem, MySelfInfo.getInstance().getNickName());
                    } else {
                        TIMUserProfile sendUser = data.getSenderProfile();
                        String name;
                        if (sendUser != null) {
                            name = sendUser.getNickName();
                        } else {
                            name = data.getSender();
                        }
                        refreshText(textElem.getText(), name);
                    }
                }
                SxbLog.d(TAG, "sendGroupMessage->success");
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(ActivityPlayRtmp.this, "send msg failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMsgObject(String msgCmd, String msgContent) {
        JSONObject msg = new JSONObject();
        String msgObject = "";
        try {
            msg.put("cmd", msgCmd);
            JSONObject request = new JSONObject();
            request.put("msgtype", "");

            java.util.UUID uuid = java.util.UUID.randomUUID();
            request.put("sequence", uuid.toString());
            request.put("version", "");
            msg.put("request", request);
            JSONObject body = new JSONObject();
            body.put("msgContent", msgContent);
            body.put("sender", MySelfInfo.getInstance().getNickName());
            msg.put("msgbody", body);
            msgObject = msg.toString();
            Log.i("wzw", "wzw cmd:" + msgCmd + " content:" + " msg:" + msgObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msgObject;
    }

    private String getMsgContent(String type, String msgObject) {
        JSONTokener jsonParser = new JSONTokener(msgObject);
        Log.i("wzw", "wzw type:" + type + " msgObject:" + msgObject);
        JSONObject response = null;
        try {
            response = (JSONObject) jsonParser.nextValue();
            String cmd = response.getString("cmd");
            if (cmd.equals(type)) {
                JSONObject data = response.getJSONObject("msgbody");
                String msgContent = data.getString("msgContent");
                return msgContent;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void refreshText(String text, String name) {
        if (text != null) {
            refreshTextListView(name, text, Constants.TEXT_TYPE);
        }
    }

    private void refreshTextListView(String name, String context, int type) {
        // 清空 editText
        mEtMsg.setText("");

        ChatEntity entity = new ChatEntity();
        entity.setSenderName(name);
        entity.setContext(context);
        entity.setType(type);
        //mArrayListChatEntity.add(entity);
        notifyRefreshListView(entity);
        //mChatMsgListAdapter.notifyDataSetChanged();

        SxbLog.d(TAG, "refreshTextListView height " + mListViewMsg.getHeight());

        if (mListViewMsg.getCount() > 1) {
            if (true)
                mListViewMsg.setSelection(0);
            else
                mListViewMsg.setSelection(mListViewMsg.getCount() - 1);
        }
    }

    private void initListView() {

        mArrayListChatEntity = new ArrayList<ChatEntity>();
        mChatMsgListAdapter = new ChatMsgListAdapter(this, mListViewMsg, mArrayListChatEntity);
        mListViewMsg.setAdapter(mChatMsgListAdapter);
    }

    private static final int MINFRESHINTERVAL = 500;
    private boolean mBoolRefreshLock = false;
    private boolean mBoolNeedRefresh = false;
    private final Timer mTimer = new Timer();
    private ArrayList<ChatEntity> mTmpChatList = new ArrayList<ChatEntity>();//缓冲队列
    private TimerTask mTimerTask = null;
    private ArrayList<ChatEntity> mArrayListChatEntity;
    private ChatMsgListAdapter mChatMsgListAdapter;
    private static final int REFRESH_LISTVIEW = 5;
    private static final int REFRESH_TOP_LISTVIEW = 6;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_LISTVIEW:
                    doRefreshListView();
                    break;
                case REFRESH_TOP_LISTVIEW:
                    doTopRefreshListView();
                    break;
            }
            return false;
        }
    });

    /**
     * 通知刷新消息ListView
     */
    private void notifyRefreshListView(ChatEntity entity) {
        mBoolNeedRefresh = true;
        mTmpChatList.add(entity);
        if (mBoolRefreshLock) {
            return;
        } else {
            doRefreshListView();
        }
    }

    /**
     * 刷新ListView并重置状态
     */
    private void doRefreshListView() {
        if (mBoolNeedRefresh) {
            mBoolRefreshLock = true;
            mBoolNeedRefresh = false;
            mArrayListChatEntity.addAll(mTmpChatList);
            mTmpChatList.clear();
            mChatMsgListAdapter.notifyDataSetChanged();

            if (null != mTimerTask) {
                mTimerTask.cancel();
            }
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    SxbLog.v(TAG, "doRefreshListView->task enter with need:" + mBoolNeedRefresh);
                    mHandler.sendEmptyMessage(REFRESH_LISTVIEW);
                }
            };
            //mTimer.cancel();
            mTimer.schedule(mTimerTask, MINFRESHINTERVAL);
        } else {
            mBoolRefreshLock = false;
        }
    }
}
