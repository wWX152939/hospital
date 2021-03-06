package com.yihu.hospital.caihongqiji.views;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.tencent.TIMMessage;
import com.tencent.TIMUserProfile;
import com.tencent.av.TIMAvManager;
import com.tencent.av.sdk.AVView;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILivePushOption;
import com.tencent.ilivesdk.core.ILiveRecordOption;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.tools.quality.ILiveQualityData;
import com.tencent.ilivesdk.tools.quality.LiveInfo;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.ilivesdk.view.AVVideoView;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVText;
import com.yihu.hospital.caihongqiji.R;
import com.yihu.hospital.caihongqiji.adapters.ChatMsgListAdapter;
import com.yihu.hospital.caihongqiji.model.ChatEntity;
import com.yihu.hospital.caihongqiji.model.CurLiveInfo;
import com.yihu.hospital.caihongqiji.model.LiveInfoJson;
import com.yihu.hospital.caihongqiji.model.MemberID;
import com.yihu.hospital.caihongqiji.model.MySelfInfo;
import com.yihu.hospital.caihongqiji.model.PPTEntity;
import com.yihu.hospital.caihongqiji.model.RoomInfoJson;
import com.yihu.hospital.caihongqiji.presenters.LiveHelper;
import com.yihu.hospital.caihongqiji.presenters.LiveListViewHelper;
import com.yihu.hospital.caihongqiji.presenters.UserServerHelper;
import com.yihu.hospital.caihongqiji.presenters.viewinface.LiveListView;
import com.yihu.hospital.caihongqiji.presenters.viewinface.LiveView;
import com.yihu.hospital.caihongqiji.presenters.viewinface.PPTListView;
import com.yihu.hospital.caihongqiji.presenters.viewinface.ProfileView;
import com.yihu.hospital.caihongqiji.utils.Constants;
import com.yihu.hospital.caihongqiji.utils.GlideCircleTransform;
import com.yihu.hospital.caihongqiji.utils.LogConstants;
import com.yihu.hospital.caihongqiji.utils.SxbLog;
import com.yihu.hospital.caihongqiji.utils.UIUtils;
import com.yihu.hospital.caihongqiji.views.customviews.BaseActivity;
import com.yihu.hospital.caihongqiji.views.customviews.CustomTextView;
import com.yihu.hospital.caihongqiji.views.customviews.HeartLayout;
import com.yihu.hospital.caihongqiji.views.customviews.InputTextMsgDialog;
import com.yihu.hospital.caihongqiji.views.customviews.MembersDialog;
import com.yihu.hospital.caihongqiji.views.customviews.PPTInfoDialog;
import com.yihu.hospital.caihongqiji.views.customviews.SpeedTestDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;


/**
 * Live直播类
 */
public class LiveActivity extends BaseActivity implements LiveView, View.OnClickListener, ProfileView, LiveListView , PPTListView {
    private static final String TAG = LiveActivity.class.getSimpleName();
    private static final int GETPROFILE_JOIN = 0x200;

    //private EnterLiveHelper mEnterRoomHelper;
    //private OldLiveHelper mOldLiveHelper;
    private LiveHelper mLiveHelper;
    private LiveListViewHelper mLiveListHelper;

    private ArrayList<ChatEntity> mArrayListChatEntity;
    private ChatMsgListAdapter mChatMsgListAdapter;

    //wzw add for top msg

    private ArrayList<ChatEntity> mTopArrayListChatEntity;
    private ChatMsgListAdapter mTopChatMsgListAdapter;
    private ArrayList<ChatEntity> mTopTmpChatList = new ArrayList<ChatEntity>();//缓冲队列
    private boolean mTopBoolNeedRefresh = false;
    private boolean mTopBoolRefreshLock = false;
    private TimerTask mTopTimerTask = null;

    private static final int MINFRESHINTERVAL = 500;
    private static final int UPDAT_WALL_TIME_TIMER_TASK = 1;
    private static final int TIMEOUT_INVITE = 2;
    private boolean mBoolRefreshLock = false;
    private boolean mBoolNeedRefresh = false;
    private final Timer mTimer = new Timer();
    private ArrayList<ChatEntity> mTmpChatList = new ArrayList<ChatEntity>();//缓冲队列
    private TimerTask mTimerTask = null;
    private static final int REFRESH_LISTVIEW = 5;
    private static final int REFRESH_PPT_LISTVIEW = 11;
    private static final int REFRESH_TOP_LISTVIEW = 13;
    private Dialog mMemberDg, inviteDg;
    private HeartLayout mHeartLayout;
    private TextView mLikeTv;
    private HeartBeatTask mHeartBeatTask;//心跳
    private ImageView mHeadIcon;
    private TextView mHostNameTv;
    private LinearLayout mHostLayout, mHostLeaveLayout;
    private final int REQUEST_PHONE_PERMISSIONS = 0;
    private long mSecond = 0;
    private String formatTime;
    private Timer mHearBeatTimer, mVideoTimer;
    private VideoTimerTask mVideoTimerTask;//计时器
    private TextView mVideoTime;
    private ObjectAnimator mObjAnim;
    private ImageView mRecordBall;
    private ImageView mQualityCircle;
    private TextView mQualityText;
    private TextView roomId;
    private int thumbUp = 0;
    private long admireTime = 0;
    private int watchCount = 0;
    private static boolean mBeatuy = false;
    private static boolean mWhite = true;
    private boolean bCleanMode = false;
    private boolean mProfile;
    private boolean bInAvRoom = false, bSlideUp = false, bDelayQuit = false;
    private boolean bReadyToChange = false;
    private boolean bHLSPush = false;

    private String backGroundId;

    private TextView tvMembers;
    private TextView tvAdmires;
    private AVRootView mRootView;

    private Dialog mDetailDialog;

    private ArrayList<String> mRenderUserList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 不锁屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_live);

        checkPermission();

        mLiveHelper = new LiveHelper(this, this);
        mLiveListHelper = new LiveListViewHelper(this);

        initView();
        backGroundId = CurLiveInfo.getHostID();

        requestExpertList();
        initPPTListView();
        //进入房间流程
        mLiveHelper.startEnterRoom();
        //初始化社会化分享组件
        ShareSDK.initSDK(this);
    }

    private void requestExpertList() {
        Log.i("wzw", "wzw requestExpertList");
        UserServerHelper.getInstance().requestExpertList(MySelfInfo.getInstance().getToken(), CurLiveInfo.getRoomNum());
        Log.i("wzw", "wzw out requestExpertList");
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDAT_WALL_TIME_TIMER_TASK:
                    updateWallTime();
                    break;
                case REFRESH_LISTVIEW:
                    doRefreshListView();
                    break;
                case REFRESH_PPT_LISTVIEW:
                    mUserAdapter.addAll(mPPTList);
                    break;
                case REFRESH_TOP_LISTVIEW:
                    doTopRefreshListView();
                    break;
                case TIMEOUT_INVITE:
                    String id = "" + msg.obj;
                    cancelInviteView(id);
                    mLiveHelper.sendGroupCmd(Constants.AVIMCMD_MULTI_HOST_CANCELINVITE, id);
                    break;
            }
            return false;
        }
    });

    /**
     * 时间格式化
     */
    private void updateWallTime() {
        String hs, ms, ss;

        long h, m, s;
        h = mSecond / 3600;
        m = (mSecond % 3600) / 60;
        s = (mSecond % 3600) % 60;
        if (h < 10) {
            hs = "0" + h;
        } else {
            hs = "" + h;
        }

        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }

        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        if (hs.equals("00")) {
            formatTime = ms + ":" + ss;
        } else {
            formatTime = hs + ":" + ms + ":" + ss;
        }

        if (Constants.HOST == MySelfInfo.getInstance().getIdStatus() && null != mVideoTime) {
            SxbLog.i(TAG, " refresh time ");
            mVideoTime.setText(formatTime);
        }
    }

    /**
     * 初始化UI
     */
    private TextView BtnBack, BtnInput, Btnflash, BtnSwitch, BtnBeauty, BtnWhite, BtnMic, BtnScreen, BtnHeart, BtnNormal, mVideoChat, BtnCtrlVideo, BtnCtrlMic, BtnHungup, mBeautyConfirm;

    //wzw add
    private TextView BtnPPT;
    private ListView mListViewPPT;
    private TextView inviteView1, inviteView2, inviteView3;
    private ListView mListViewMsgItems;
    private ListView mTopListViewMsgItems;
    private LinearLayout mHostCtrView, mNomalMemberCtrView, mVideoMemberCtrlView, mBeautySettings;
    private FrameLayout mFullControllerUi, mBackgound;
    private SeekBar mBeautyBar;
    private int mBeautyRate, mWhiteRate;
    private TextView pushBtn, recordBtn, speedBtn;

    private void showHeadIcon(ImageView view, String avatar) {
        if (TextUtils.isEmpty(avatar)) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
            Bitmap cirBitMap = UIUtils.createCircleImage(bitmap, 0);
            view.setImageBitmap(cirBitMap);
        } else {
            SxbLog.d(TAG, "load icon: " + avatar);
            RequestManager req = Glide.with(this);
            req.load(avatar).transform(new GlideCircleTransform(this)).into(view);
        }
    }

    /**
     * 初始化界面
     */
    private void initView() {
        mHostCtrView = (LinearLayout) findViewById(R.id.host_bottom_layout);
        mNomalMemberCtrView = (LinearLayout) findViewById(R.id.member_bottom_layout);
        mVideoMemberCtrlView = (LinearLayout) findViewById(R.id.video_member_bottom_layout);
        mHostLeaveLayout = (LinearLayout) findViewById(R.id.ll_host_leave);
        mVideoChat = (TextView) findViewById(R.id.video_interact);
        BtnPPT = (TextView) findViewById(R.id.ppt_list);
        mListViewPPT = (ListView) findViewById(R.id.list_view_ppt);
        mHeartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        mVideoTime = (TextView) findViewById(R.id.broadcasting_time);
        mHeadIcon = (ImageView) findViewById(R.id.head_icon);
        mVideoMemberCtrlView.setVisibility(View.INVISIBLE);
        mHostNameTv = (TextView) findViewById(R.id.host_name);
        tvMembers = (TextView) findViewById(R.id.member_counts);
        tvAdmires = (TextView) findViewById(R.id.heart_counts);
        mQualityText = (TextView) findViewById(R.id.quality_text);
        speedBtn = (TextView) findViewById(R.id.speed_test_btn);
        speedBtn.setOnClickListener(this);
        mQualityCircle = (ImageView) findViewById(R.id.quality_circle);
        BtnCtrlVideo = (TextView) findViewById(R.id.camera_controll);
        BtnCtrlMic = (TextView) findViewById(R.id.mic_controll);
        BtnHungup = (TextView) findViewById(R.id.close_member_video);
        BtnCtrlVideo.setOnClickListener(this);
        BtnCtrlMic.setOnClickListener(this);
        BtnHungup.setOnClickListener(this);
        BtnPPT.setOnClickListener(this);
        roomId = (TextView) findViewById(R.id.room_id);

        //for 测试用
        TextView paramVideo = (TextView) findViewById(R.id.param_video);
        paramVideo.setOnClickListener(this);
        tvTipsMsg = (TextView) findViewById(R.id.qav_tips_msg);
        tvTipsMsg.setTextColor(Color.BLACK);
        paramTimer.schedule(task, 1000, 1000);


        if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {
            mHostCtrView.setVisibility(View.VISIBLE);
            mNomalMemberCtrView.setVisibility(View.GONE);
            mRecordBall = (ImageView) findViewById(R.id.record_ball);
            Btnflash = (TextView) findViewById(R.id.flash_btn);
            BtnSwitch = (TextView) findViewById(R.id.switch_cam);
            BtnBeauty = (TextView) findViewById(R.id.beauty_btn);
            BtnWhite = (TextView) findViewById(R.id.white_btn);
            BtnMic = (TextView) findViewById(R.id.mic_btn);
            BtnScreen = (TextView) findViewById(R.id.fullscreen_btn);
            mVideoChat.setVisibility(View.VISIBLE);
            Btnflash.setOnClickListener(this);
            BtnSwitch.setOnClickListener(this);
            BtnBeauty.setOnClickListener(this);
            BtnWhite.setOnClickListener(this);
            BtnMic.setOnClickListener(this);
            BtnScreen.setOnClickListener(this);
            mVideoChat.setOnClickListener(this);
            inviteView1 = (TextView) findViewById(R.id.invite_view1);
            inviteView2 = (TextView) findViewById(R.id.invite_view2);
            inviteView3 = (TextView) findViewById(R.id.invite_view3);
            inviteView1.setOnClickListener(this);
            inviteView2.setOnClickListener(this);
            inviteView3.setOnClickListener(this);

            tvAdmires.setVisibility(View.VISIBLE);

            initBackDialog();
            initDetailDailog();


            mMemberDg = new MembersDialog(this, R.style.floag_dialog, this);
            startRecordAnimation();
            showHeadIcon(mHeadIcon, MySelfInfo.getInstance().getAvatar());
            mBeautySettings = (LinearLayout) findViewById(R.id.qav_beauty_setting);
            mBeautyConfirm = (TextView) findViewById(R.id.qav_beauty_setting_finish);
            mBeautyConfirm.setOnClickListener(this);
            mBeautyBar = (SeekBar) (findViewById(R.id.qav_beauty_progress));
            mBeautyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    SxbLog.d("SeekBar", "onStopTrackingTouch");
                    if (mProfile == mBeatuy) {
                        Toast.makeText(LiveActivity.this, "beauty " + mBeautyRate + "%", Toast.LENGTH_SHORT).show();//美颜度
                    } else {
                        Toast.makeText(LiveActivity.this, "white " + mWhiteRate + "%", Toast.LENGTH_SHORT).show();//美白度
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    SxbLog.d("SeekBar", "onStartTrackingTouch");
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    Log.i(TAG, "onProgressChanged " + progress);
                    if (mProfile == mBeatuy) {
                        mBeautyRate = progress;
                        ILiveSDK.getInstance().getAvVideoCtrl().inputBeautyParam(getBeautyProgress(progress));//美颜
                    } else {
                        mWhiteRate = progress;
                        ILiveSDK.getInstance().getAvVideoCtrl().inputWhiteningParam(getBeautyProgress(progress));//美白
                    }
                }
            });
        } else {
            LinearLayout llRecordTip = (LinearLayout) findViewById(R.id.record_tip);
            llRecordTip.setVisibility(View.GONE);
            mHostNameTv.setVisibility(View.VISIBLE);
            initInviteDialog();
            mNomalMemberCtrView.setVisibility(View.VISIBLE);
            mHostCtrView.setVisibility(View.GONE);
            BtnInput = (TextView) findViewById(R.id.message_input);
            BtnInput.setOnClickListener(this);
            mLikeTv = (TextView) findViewById(R.id.member_send_good);
            mLikeTv.setOnClickListener(this);
            mVideoChat.setVisibility(View.GONE);
            BtnScreen = (TextView) findViewById(R.id.clean_screen);

            List<String> ids = new ArrayList<>();
            ids.add(CurLiveInfo.getHostID());
            showHeadIcon(mHeadIcon, CurLiveInfo.getHostAvator());
            mHostNameTv.setText(UIUtils.getLimitString(CurLiveInfo.getHostID(), 10));

            mHostLayout = (LinearLayout) findViewById(R.id.head_up_layout);
            mHostLayout.setOnClickListener(this);
            BtnScreen.setOnClickListener(this);
        }
        BtnNormal = (TextView) findViewById(R.id.normal_btn);
        BtnNormal.setOnClickListener(this);
        mFullControllerUi = (FrameLayout) findViewById(R.id.controll_ui);

        pushBtn = (TextView) findViewById(R.id.push_btn);
        pushBtn.setOnClickListener(this);

        recordBtn = (TextView) findViewById(R.id.record_btn);

        // wzw recordbtn gone
        recordBtn.setVisibility(View.GONE);
        recordBtn.setOnClickListener(this);

        initPushDialog();
        initRecordDialog();

        BtnBack = (TextView) findViewById(R.id.btn_back);
        BtnBack.setOnClickListener(this);

        mListViewMsgItems = (ListView) findViewById(R.id.im_msg_listview);
        mArrayListChatEntity = new ArrayList<ChatEntity>();
        mChatMsgListAdapter = new ChatMsgListAdapter(this, mListViewMsgItems, mArrayListChatEntity);
        mListViewMsgItems.setAdapter(mChatMsgListAdapter);

        //wzw add for top
        mTopListViewMsgItems = (ListView) findViewById(R.id.im_msg_top_listview);
        mTopArrayListChatEntity = new ArrayList<ChatEntity>();
        mTopChatMsgListAdapter = new ChatMsgListAdapter(this, mTopListViewMsgItems, mTopArrayListChatEntity) {
            @Override
            protected void setTextColor(CustomTextView sendContext) {
                sendContext.setTextColor(getResources().getColor(R.color.colorRed));
            }

            @Override
            protected void setText(CustomTextView sendContext, ChatEntity item, SpannableString spanString) {
                sendContext.setText(item.getContext());
            }
        };
        mTopListViewMsgItems.setAdapter(mTopChatMsgListAdapter);


        tvMembers.setText("" + CurLiveInfo.getMembers());
        tvAdmires.setText("" + CurLiveInfo.getAdmires());

        //TODO 获取渲染层
        mRootView = (AVRootView) findViewById(R.id.av_root_view);
        //TODO 设置渲染层
        ILVLiveManager.getInstance().setAvVideoView(mRootView);


        mRootView.setGravity(AVRootView.LAYOUT_GRAVITY_RIGHT);
        mRootView.setSubMarginY(getResources().getDimensionPixelSize(R.dimen.small_area_margin_top));
        mRootView.setSubMarginX(getResources().getDimensionPixelSize(R.dimen.small_area_marginright));
        mRootView.setSubPadding(getResources().getDimensionPixelSize(R.dimen.small_area_marginbetween));
        mRootView.setSubWidth(getResources().getDimensionPixelSize(R.dimen.small_area_width));
        mRootView.setSubHeight(getResources().getDimensionPixelSize(R.dimen.small_area_height));
        mRootView.setSubCreatedListener(new AVRootView.onSubViewCreatedListener() {
            @Override
            public void onSubViewCreated() {
                for (int i = 1; i < ILiveConstants.MAX_AV_VIDEO_NUM; i++) {
                    final int index = i;
                    AVVideoView avVideoView = mRootView.getViewByIndex(index);
                    avVideoView.setGestureListener(new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            mRootView.swapVideoView(0, index);
                            backGroundId = mRootView.getViewByIndex(0).getIdentifier();
//                            updateHostLeaveLayout();
                            backGroundId = mRootView.getViewByIndex(0).getIdentifier();
                            if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {//自己是主播
                                if (backGroundId.equals(MySelfInfo.getInstance().getId())) {//背景是自己
                                    mHostCtrView.setVisibility(View.VISIBLE);
                                    mVideoMemberCtrlView.setVisibility(View.INVISIBLE);
                                } else {//背景是其他成员
                                    mHostCtrView.setVisibility(View.INVISIBLE);
                                    mVideoMemberCtrlView.setVisibility(View.VISIBLE);
                                }
                            } else {//自己成员方式
                                if (backGroundId.equals(MySelfInfo.getInstance().getId())) {//背景是自己
                                    mVideoMemberCtrlView.setVisibility(View.VISIBLE);
                                    mNomalMemberCtrView.setVisibility(View.INVISIBLE);
                                } else if (backGroundId.equals(CurLiveInfo.getHostID())) {//主播自己
                                    mVideoMemberCtrlView.setVisibility(View.INVISIBLE);
                                    mNomalMemberCtrView.setVisibility(View.VISIBLE);
                                } else {
                                    mVideoMemberCtrlView.setVisibility(View.INVISIBLE);

                                    //wzw 底部按钮一直显示
                                    mNomalMemberCtrView.setVisibility(View.VISIBLE);
                                }

                            }

                            return super.onSingleTapConfirmed(e);
                        }
                    });
                }

                mRootView.getViewByIndex(0).setGestureListener(new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        if(e1.getY()-e2.getY() > 20 && Math.abs(velocityY) > 10){
                            bSlideUp = true;
                        }else if(e2.getY()-e1.getY() > 20 && Math.abs(velocityY) > 10){
                            bSlideUp = false;
                        }
                        // wzw don't switch
//                        switchRoom();

                        return false;
                    }
                });
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        ILiveRoomManager.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ILiveRoomManager.getInstance().onPause();
    }



    /**
     * 直播心跳
     */
    private class HeartBeatTask extends TimerTask {
        @Override
        public void run() {
            String host = CurLiveInfo.getHostID();
            SxbLog.i(TAG, "HeartBeatTask " + host);
            if (MySelfInfo.getInstance().getId().equals(CurLiveInfo.getHostID()))
                UserServerHelper.getInstance().heartBeater(1);
            else
                UserServerHelper.getInstance().heartBeater(MySelfInfo.getInstance().getIdStatus());
            mLiveHelper.pullMemberList();
        }
    }

    /**
     * 记时器
     */
    private class VideoTimerTask extends TimerTask {
        public void run() {
            SxbLog.i(TAG, "timeTask ");
            ++mSecond;
            if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST)
                mHandler.sendEmptyMessage(UPDAT_WALL_TIME_TIMER_TASK);
        }
    }

    @Override
    protected void onDestroy() {
        watchCount = 0;
        super.onDestroy();
        if (null != mHearBeatTimer) {
            mHearBeatTimer.cancel();
            mHearBeatTimer = null;
        }
        if (null != mVideoTimer) {
            mVideoTimer.cancel();
            mVideoTimer = null;
        }
        if (null != paramTimer) {
            paramTimer.cancel();
            paramTimer = null;
        }


        inviteViewCount = 0;
        thumbUp = 0;
        CurLiveInfo.setMembers(0);
        CurLiveInfo.setAdmires(0);
        CurLiveInfo.setCurrentRequestCount(0);
        mLiveHelper.onDestory();

		ShareSDK.stopSDK(this);
    }


    /**
     * 点击Back键
     */
    @Override
    public void onBackPressed() {
        if (bInAvRoom) {
            bDelayQuit = false;
            quiteLiveByPurpose();
        } else {
            clearOldData();
            finish();
        }
    }

    /**
     * 主动退出直播
     */
    private void quiteLiveByPurpose() {
        if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {
            if (backDialog.isShowing() == false)
                backDialog.show();


        } else {
            mLiveHelper.startExitRoom();

        }
    }


    private Dialog backDialog;

    private void initBackDialog() {
        backDialog = new Dialog(this, R.style.dialog);
        backDialog.setContentView(R.layout.dialog_end_live);
        TextView tvSure = (TextView) backDialog.findViewById(R.id.btn_sure);
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ILVCustomCmd cmd = new ILVCustomCmd();
                cmd.setCmd(Constants.AVIMCMD_EXITLIVE);
                cmd.setType(ILVText.ILVTextType.eGroupMsg);
                ILVLiveManager.getInstance().sendCustomCmd(cmd, new ILiveCallBack<TIMMessage>() {
                    @Override
                    public void onSuccess(TIMMessage data) {
                        //如果是直播，发消息
                        if (null != mLiveHelper) {
                            mLiveHelper.startExitRoom();
                            if (isPushed) {
                                mLiveHelper.stopPush();
                            }
                        }
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {

                    }

                });
                backDialog.dismiss();
            }
        });
        TextView tvCancel = (TextView) backDialog.findViewById(R.id.btn_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backDialog.cancel();
            }
        });
    }


    /**
     * 完成进出房间流程
     */
    @Override
    public void enterRoomComplete(int id_status, boolean isSucc) {

//        Toast.makeText(LiveActivity.this, "EnterRoom  " + id_status + " isSucc " + isSucc, Toast.LENGTH_SHORT).show();
        //必须得进入房间之后才能初始化UI
        mRootView.getViewByIndex(0).setRotate(true);
//        mRootView.getViewByIndex(0).setDiffDirectionRenderMode(AVVideoView.ILiveRenderMode.BLACK_TO_FILL);
        bInAvRoom = true;
        bDelayQuit = true;
        bReadyToChange = true;
        roomId.setText("" + CurLiveInfo.getRoomNum());
        if (isSucc == true) {
            //主播心跳
            mHearBeatTimer = new Timer(true);
            mHeartBeatTask = new HeartBeatTask();
            mHearBeatTimer.schedule(mHeartBeatTask, 100, 5 * 1000); //5秒重复上报心跳 拉取房间列表

            //直播时间
            mVideoTimer = new Timer(true);
            mVideoTimerTask = new VideoTimerTask();
            mVideoTimer.schedule(mVideoTimerTask, 1000, 1000);
            //IM初始化
            if (id_status == Constants.HOST) {//主播方式加入房间成功
                mHostNameTv.setText(MySelfInfo.getInstance().getId());
                //开启摄像头渲染画面
                SxbLog.i(TAG, "createlive enterRoomComplete isSucc" + isSucc);
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putBoolean("living", true);
                editor.apply();
            } else {
                //发消息通知上线
                mLiveHelper.sendGroupCmd(Constants.AVIMCMD_ENTERLIVE, "");
            }


        }
    }


    @Override
    public void quiteRoomComplete(int id_status, boolean succ, LiveInfoJson liveinfo) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                UserServerHelper.getInstance().reportMe(MySelfInfo.getInstance().getIdStatus(), 1);//通知server 我下线了
            }
        }.start();

        if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {
            if ((getBaseContext() != null) && (null != mDetailDialog) && (mDetailDialog.isShowing() == false)) {
                SxbLog.d(TAG, LogConstants.ACTION_HOST_QUIT_ROOM + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "quite room callback"
                        + LogConstants.DIV + LogConstants.STATUS.SUCCEED + LogConstants.DIV + "id status " + id_status);
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putBoolean("living", false);
                editor.apply();
                mDetailTime.setText(formatTime);
                mDetailAdmires.setText("" + CurLiveInfo.getAdmires());
                mDetailWatchCount.setText("" + watchCount);
                mDetailDialog.show();


            }
        } else {
            clearOldData();
            finish();
        }

        //发送

        bInAvRoom = false;
    }


    private TextView mDetailTime, mDetailAdmires, mDetailWatchCount;

    private void initDetailDailog() {
        mDetailDialog = new Dialog(this, R.style.dialog);
        mDetailDialog.setContentView(R.layout.dialog_live_detail);
        mDetailTime = (TextView) mDetailDialog.findViewById(R.id.tv_time);
        mDetailAdmires = (TextView) mDetailDialog.findViewById(R.id.tv_admires);
        mDetailWatchCount = (TextView) mDetailDialog.findViewById(R.id.tv_members);

        mDetailDialog.setCancelable(false);

        TextView tvCancel = (TextView) mDetailDialog.findViewById(R.id.btn_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDetailDialog.dismiss();
                finish();
            }
        });
//        mDetailDialog.show();
    }

    /**
     * 成员状态变更
     */
    @Override
    public void memberJoin(String id, String name) {
        SxbLog.d(TAG, LogConstants.ACTION_VIEWER_ENTER_ROOM + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "on member join" +
                LogConstants.DIV + "join room " + id);
        watchCount++;
        refreshTextListView(TextUtils.isEmpty(name) ? id : name, "join live", Constants.MEMBER_ENTER);

    }


    @Override
    public void hostLeave(String id, String name) {
        refreshTextListView("host", "leave for a while", Constants.HOST_LEAVE);
    }

    @Override
    public void hostBack(String id, String name) {
        refreshTextListView(TextUtils.isEmpty(name) ? id : name, "is back", Constants.HOST_BACK);
    }

    @Override
    public void refreshMember(ArrayList<MemberID> memlist) {
        // 定时器，五秒一次
        if (memlist != null && tvMembers != null) {
            tvMembers.setText("" + memlist.size());
            UserServerHelper.getInstance().requestExpertList(MySelfInfo.getInstance().getToken(), CurLiveInfo.getRoomNum());
        }
    }


    /**
     * 红点动画
     */
    private void startRecordAnimation() {
        mObjAnim = ObjectAnimator.ofFloat(mRecordBall, "alpha", 1f, 0f, 1f);
        mObjAnim.setDuration(1000);
        mObjAnim.setRepeatCount(-1);
        mObjAnim.start();
    }

    private float getBeautyProgress(int progress) {
        SxbLog.d("shixu", "progress: " + progress);
        return (9.0f * progress / 100.0f);
    }


    @Override
    public void showInviteDialog() {
        if ((inviteDg != null) && (getBaseContext() != null) && (inviteDg.isShowing() != true)) {
            inviteDg.show();
        }
    }

    @Override
    public void hideInviteDialog() {
        if ((inviteDg != null) && (inviteDg.isShowing() == true)) {
            inviteDg.dismiss();
        }
    }


    @Override
    public void refreshText(String text, String name) {
        if (text != null) {
            refreshTextListView(name, text, Constants.TEXT_TYPE);
        }
    }

    @Override
    public void refreshTopText(String sequence, String text, String name) {
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

    @Override
    public void cancelTopText(String sequence) {
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
        CurLiveInfo.setAdmires(CurLiveInfo.getAdmires() + 1);
        if (!bCleanMode) {      // 纯净模式下不播放飘星动画
            mHeartLayout.addFavor();
        }
        tvAdmires.setText("" + CurLiveInfo.getAdmires());
    }

    @Override
    public void refreshUI(String id) {
        //当主播选中这个人，而他主动退出时需要恢复到正常状态
        if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST)
            if (!backGroundId.equals(CurLiveInfo.getHostID()) && backGroundId.equals(id)) {
                backToNormalCtrlView();
            }
    }


    private int inviteViewCount = 0;

    @Override
    public boolean showInviteView(String id) {
        SxbLog.d(TAG, LogConstants.ACTION_VIEWER_SHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "invite up show" +
                LogConstants.DIV + "id " + id);
        int index = mRootView.findValidViewIndex();
        if (index == -1) {
            Toast.makeText(LiveActivity.this, "the invitation's upper limit is 3", Toast.LENGTH_SHORT).show();
            return false;
        }
        int requetCount = index + inviteViewCount;
        if (requetCount > 3) {
            Toast.makeText(LiveActivity.this, "the invitation's upper limit is 3", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (hasInvited(id)) {
            Toast.makeText(LiveActivity.this, "it has already invited", Toast.LENGTH_SHORT).show();
            return false;
        }
        switch (requetCount) {
            case 1:
                inviteView1.setText(id);
                inviteView1.setVisibility(View.VISIBLE);
                inviteView1.setTag(id);

                break;
            case 2:
                inviteView2.setText(id);
                inviteView2.setVisibility(View.VISIBLE);
                inviteView2.setTag(id);
                break;
            case 3:
                inviteView3.setText(id);
                inviteView3.setVisibility(View.VISIBLE);
                inviteView3.setTag(id);
                break;
        }
        mLiveHelper.sendC2CCmd(Constants.AVIMCMD_MUlTI_HOST_INVITE, "", id);
        inviteViewCount++;
        //30s超时取消
        Message msg = new Message();
        msg.what = TIMEOUT_INVITE;
        msg.obj = id;
        mHandler.sendMessageDelayed(msg, 30 * 1000);
        return true;
    }


    /**
     * 判断是否邀请过同一个人
     */
    private boolean hasInvited(String id) {
        if (id.equals(inviteView1.getTag())) {
            return true;
        }
        if (id.equals(inviteView2.getTag())) {
            return true;
        }
        if (id.equals(inviteView3.getTag())) {
            return true;
        }
        return false;
    }

    @Override
    public void cancelInviteView(String id) {
        if ((inviteView1 != null) && (inviteView1.getTag() != null)) {
            if (inviteView1.getTag().equals(id)) {
            }
            if (inviteView1.getVisibility() == View.VISIBLE) {
                inviteView1.setVisibility(View.INVISIBLE);
                inviteView1.setTag("");
                inviteViewCount--;
            }
        }

        if (inviteView2 != null && inviteView2.getTag() != null) {
            if (inviteView2.getTag().equals(id)) {
                if (inviteView2.getVisibility() == View.VISIBLE) {
                    inviteView2.setVisibility(View.INVISIBLE);
                    inviteView2.setTag("");
                    inviteViewCount--;
                }
            } else {
                Log.i(TAG, "cancelInviteView inviteView2 is null");
            }
        } else {
            Log.i(TAG, "cancelInviteView inviteView2 is null");
        }

        if (inviteView3 != null && inviteView3.getTag() != null) {
            if (inviteView3.getTag().equals(id)) {
                if (inviteView3.getVisibility() == View.VISIBLE) {
                    inviteView3.setVisibility(View.INVISIBLE);
                    inviteView3.setTag("");
                    inviteViewCount--;
                }
            } else {
                Log.i(TAG, "cancelInviteView inviteView3 is null");
            }
        } else {
            Log.i(TAG, "cancelInviteView inviteView3 is null");
        }


    }

    @Override
    public void cancelMemberView(String id) {
        if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {
        } else {
            //TODO 主动下麦 下麦；
            SxbLog.d(TAG, LogConstants.ACTION_VIEWER_UNSHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "start unShow" +
                    LogConstants.DIV + "id " + id);
            mLiveHelper.downMemberVideo();
        }
        mLiveHelper.sendGroupCmd(Constants.AVIMCMD_MULTI_CANCEL_INTERACT, id);
        mRootView.closeUserView(id, AVView.VIDEO_SRC_TYPE_CAMERA, true);
        backToNormalCtrlView();
    }


    private void showReportDialog() {
        final Dialog reportDialog = new Dialog(this, R.style.report_dlg);
        reportDialog.setContentView(R.layout.dialog_live_report);

        TextView tvReportDirty = (TextView) reportDialog.findViewById(R.id.btn_dirty);
        TextView tvReportFalse = (TextView) reportDialog.findViewById(R.id.btn_false);
        TextView tvReportVirus = (TextView) reportDialog.findViewById(R.id.btn_virus);
        TextView tvReportIllegal = (TextView) reportDialog.findViewById(R.id.btn_illegal);
        TextView tvReportYellow = (TextView) reportDialog.findViewById(R.id.btn_yellow);
        TextView tvReportCancel = (TextView) reportDialog.findViewById(R.id.btn_cancel);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    default:
                        reportDialog.cancel();
                        break;
                }
            }
        };

        tvReportDirty.setOnClickListener(listener);
        tvReportFalse.setOnClickListener(listener);
        tvReportVirus.setOnClickListener(listener);
        tvReportIllegal.setOnClickListener(listener);
        tvReportYellow.setOnClickListener(listener);
        tvReportCancel.setOnClickListener(listener);

        reportDialog.setCanceledOnTouchOutside(true);
        reportDialog.show();
    }

    private void showHostDetail() {
        Dialog hostDlg = new Dialog(this, R.style.host_info_dlg);
        hostDlg.setContentView(R.layout.host_info_layout);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Window dlgwin = hostDlg.getWindow();
        WindowManager.LayoutParams lp = dlgwin.getAttributes();
        dlgwin.setGravity(Gravity.TOP);
        lp.width = (int) (display.getWidth()); //设置宽度

        hostDlg.getWindow().setAttributes(lp);
        hostDlg.show();

        TextView tvHost = (TextView) hostDlg.findViewById(R.id.tv_host_name);
        tvHost.setText(CurLiveInfo.getHostName());
        ImageView ivHostIcon = (ImageView) hostDlg.findViewById(R.id.iv_host_icon);
        showHeadIcon(ivHostIcon, CurLiveInfo.getHostAvator());
        TextView tvLbs = (TextView) hostDlg.findViewById(R.id.tv_host_lbs);
        tvLbs.setText(UIUtils.getLimitString(CurLiveInfo.getAddress(), 6));
        ImageView ivReport = (ImageView) hostDlg.findViewById(R.id.iv_report);
        ivReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog();
            }
        });
    }

    private boolean checkInterval() {
        if (0 == admireTime) {
            admireTime = System.currentTimeMillis();
            return true;
        }
        long newTime = System.currentTimeMillis();
        if (newTime >= admireTime + 1000) {
            admireTime = newTime;
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_back) {
            quiteLiveByPurpose();

        } else if (i == R.id.message_input) {
            inputMsgDialog();

        } else if (i == R.id.member_send_good) {// 添加飘星动画
            mHeartLayout.addFavor();
            if (checkInterval()) {
                mLiveHelper.sendGroupCmd(Constants.AVIMCMD_PRAISE, "");
                CurLiveInfo.setAdmires(CurLiveInfo.getAdmires() + 1);
                tvAdmires.setText("" + CurLiveInfo.getAdmires());
            } else {
                //Toast.makeText(this, getString(R.string.text_live_admire_limit), Toast.LENGTH_SHORT).show();
            }

        } else if (i == R.id.flash_btn) {
            switch (ILiveRoomManager.getInstance().getCurCameraId()) {
                case ILiveConstants.FRONT_CAMERA:
                    Toast.makeText(LiveActivity.this, "this is front cam", Toast.LENGTH_SHORT).show();
                    break;
                case ILiveConstants.BACK_CAMERA:
                    mLiveHelper.toggleFlashLight();
                    break;
                default:
                    Toast.makeText(LiveActivity.this, "camera is not open", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if (i == R.id.switch_cam) {
            ILiveRoomManager.getInstance().enableCamera((ILiveRoomManager.getInstance().getCurCameraId() + 1)%2, true);
//            switch (ILiveRoomManager.getInstance().getCurCameraId()) {
//
//                case ILiveConstants.FRONT_CAMERA:
//                    ILiveRoomManager.getInstance().switchCamera(ILiveConstants.BACK_CAMERA);
//                    break;
//                case ILiveConstants.BACK_CAMERA:
//                    ILiveRoomManager.getInstance().switchCamera(ILiveConstants.FRONT_CAMERA);
//                    break;
//            }
        } else if (i == R.id.mic_btn) {
            if (mLiveHelper.isMicOn()) {
                BtnMic.setBackgroundResource(R.drawable.icon_mic_close);
            } else {
                BtnMic.setBackgroundResource(R.drawable.icon_mic_open);
            }
            mLiveHelper.toggleMic();
        } else if (i == R.id.head_up_layout) {
            // wzw dont show
//            showHostDetail();

        } else if (i == R.id.clean_screen || i == R.id.fullscreen_btn) {
            bCleanMode = true;
            mFullControllerUi.setVisibility(View.INVISIBLE);
            BtnNormal.setVisibility(View.VISIBLE);

        } else if (i == R.id.normal_btn) {
            bCleanMode = false;
            mFullControllerUi.setVisibility(View.VISIBLE);
            BtnNormal.setVisibility(View.GONE);

        } else if (i == R.id.video_interact) {
            mMemberDg.setCanceledOnTouchOutside(true);
            mMemberDg.show();

        } else if (i == R.id.ppt_list) {
            switchPPTListView();
        } else if (i == R.id.camera_controll) {
            Toast.makeText(LiveActivity.this, "切换" + backGroundId + "camrea 状态", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "onClick: hostid " + ILiveRoomManager.getInstance().getHostId() + " myself " + MySelfInfo.getInstance().getId());
            if (MySelfInfo.getInstance().getId().equals(backGroundId)) {//自己关闭自己
                mLiveHelper.switchCamera();
            } else {
                mLiveHelper.sendC2CCmd(Constants.AVIMCMD_MULTI_HOST_CONTROLL_CAMERA, backGroundId, backGroundId);
            }

        } else if (i == R.id.mic_controll) {
            Toast.makeText(LiveActivity.this, "切换" + backGroundId + "mic 状态", Toast.LENGTH_SHORT).show();
            if (ILiveRoomManager.getInstance().getHostId().equals(MySelfInfo.getInstance().getId())) {//自己关闭自己
                mLiveHelper.toggleMic();
            } else {
                mLiveHelper.sendC2CCmd(Constants.AVIMCMD_MULTI_HOST_CONTROLL_MIC, backGroundId, backGroundId);//主播关闭自己
            }

        } else if (i == R.id.close_member_video) {
            cancelMemberView(backGroundId);

        } else if (i == R.id.beauty_btn) {
            Log.i(TAG, "onClick " + mBeautyRate);

            mProfile = mBeatuy;
            if (mBeautySettings != null) {
                if (mBeautySettings.getVisibility() == View.GONE) {
                    mBeautySettings.setVisibility(View.VISIBLE);
                    mFullControllerUi.setVisibility(View.INVISIBLE);
                    mBeautyBar.setProgress(mBeautyRate);
                } else {
                    mBeautySettings.setVisibility(View.GONE);
                    mFullControllerUi.setVisibility(View.VISIBLE);
                }
            } else {
                SxbLog.i(TAG, "beauty_btn mTopBar  is null ");
            }

        } else if (i == R.id.white_btn) {
            Log.i(TAG, "onClick " + mWhiteRate);
            mProfile = mWhite;
            if (mBeautySettings != null) {
                if (mBeautySettings.getVisibility() == View.GONE) {
                    mBeautySettings.setVisibility(View.VISIBLE);
                    mFullControllerUi.setVisibility(View.INVISIBLE);
                    mBeautyBar.setProgress(mWhiteRate);
                } else {
                    mBeautySettings.setVisibility(View.GONE);
                    mFullControllerUi.setVisibility(View.VISIBLE);
                }
            } else {
                SxbLog.i(TAG, "beauty_btn mTopBar  is null ");
            }

        } else if (i == R.id.qav_beauty_setting_finish) {
            mBeautySettings.setVisibility(View.GONE);
            mFullControllerUi.setVisibility(View.VISIBLE);

        } else if (i == R.id.invite_view1) {
            inviteView1.setVisibility(View.INVISIBLE);
            mLiveHelper.sendGroupCmd(Constants.AVIMCMD_MULTI_CANCEL_INTERACT, "" + inviteView1.getTag());

        } else if (i == R.id.invite_view2) {
            inviteView2.setVisibility(View.INVISIBLE);
            mLiveHelper.sendGroupCmd(Constants.AVIMCMD_MULTI_CANCEL_INTERACT, "" + inviteView2.getTag());

        } else if (i == R.id.invite_view3) {
            inviteView3.setVisibility(View.INVISIBLE);
            mLiveHelper.sendGroupCmd(Constants.AVIMCMD_MULTI_CANCEL_INTERACT, "" + inviteView3.getTag());

        } else if (i == R.id.param_video) {
            showTips = !showTips;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    UserServerHelper.getInstance().getRoomPlayUrl(MySelfInfo.getInstance().getMyRoomNum());//通知server 我下线了
                    UserServerHelper.getInstance().getPlayUrlList(0, 10);
                }
            }.start();

        } else if (i == R.id.push_btn) {
            pushStream();

        } else if (i == R.id.record_btn) {
            if (!mRecord) {
                if (recordDialog != null)
                    recordDialog.show();
            } else {
                mLiveHelper.stopRecord();
            }

        } else if (i == R.id.speed_test_btn) {
            new SpeedTestDialog(this).start();

        }
    }

    //for 测试获取测试参数
    private boolean showTips = false;
    private TextView tvTipsMsg;
    Timer paramTimer = new Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (showTips) {
                        mQualityCircle.setVisibility(View.VISIBLE);
                        mQualityText.setVisibility(View.VISIBLE);
                        if (tvTipsMsg != null && ILiveSDK.getInstance().getAVContext() != null &&
                                ILiveSDK.getInstance().getAVContext().getRoom() != null) {
                            //String tips =getQualityTips();
                            String tips = "\n\n";
                            ILiveQualityData qData = ILiveRoomManager.getInstance().getQualityData();
                            if (null != qData){
                                tips += "FPS:\t"+qData.getUpFPS()+"\n\n";
                                tips += "Send:\t"+qData.getSendKbps()+"Kbps\t";
                                tips += "Recv:\t"+qData.getRecvKbps()+"Kbps\n\n";
                                tips += "SendLossRate:\t"+qData.getSendLossRate()+"%\t";
                                tips += "RecvLossRate:\t"+qData.getRecvLossRate()+"%\n\n";
                                tips += "AppCPURate:\t"+qData.getAppCPURate()+"%\t";
                                tips += "SysCPURate:\t"+qData.getSysCPURate()+"%\n\n";
                                Map<String, LiveInfo> userMaps = qData.getLives();
                                for (Map.Entry<String, LiveInfo> entry : userMaps.entrySet()){
                                    tips += "\t"+entry.getKey()+"-"+entry.getValue().getWidth()+"*"+entry.getValue().getHeight()+"\n";
                                }
                            }

                            tips += '\n';
                            tips += getQualityTips(ILiveSDK.getInstance().getAVContext().getRoom().getQualityTips());
                            tvTipsMsg.getBackground().setAlpha(125);
                            tvTipsMsg.setText(tips);
                            tvTipsMsg.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tvTipsMsg.setText("");
                        tvTipsMsg.setVisibility(View.INVISIBLE);
                        mQualityCircle.setVisibility(View.GONE);
                        mQualityText.setVisibility(View.GONE);
                    }
                }
            });
        }
    };


    private void backToNormalCtrlView() {
        if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {
            backGroundId = CurLiveInfo.getHostID();
            mHostCtrView.setVisibility(View.VISIBLE);
            mVideoMemberCtrlView.setVisibility(View.GONE);
        } else {
            backGroundId = CurLiveInfo.getHostID();
            mNomalMemberCtrView.setVisibility(View.VISIBLE);
            mVideoMemberCtrlView.setVisibility(View.GONE);
        }
    }


    private void switchPPTListView() {
        Log.i("wzw", "wzw switchPPTListView:" + mListViewPPT.getVisibility());
        if (mListViewPPT.getVisibility() == View.VISIBLE) {
            mListViewPPT.setVisibility(View.GONE);
        } else {
            mListViewPPT.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showPPTList(UserServerHelper.RequestBackInfo result, ArrayList<PPTEntity> pptList) {
        mPPTList = pptList;
        mHandler.sendEmptyMessage(REFRESH_PPT_LISTVIEW);
        Log.i("wzw", "wzw pptList:" + pptList);
    }

    UserAdapter mUserAdapter;
    ArrayList<PPTEntity> mPPTList;
    private void initPPTListView() {
        Log.i("wzw", "wzw initPPTListView");
        mUserAdapter = new UserAdapter(this, R.layout.text_view);
        mListViewPPT.setAdapter(mUserAdapter);
        UserServerHelper.getInstance().requestPPTList(this, MySelfInfo.getInstance().getToken());
    }

    class UserAdapter extends ArrayAdapter<PPTEntity> {
        private int mResourceId;
        private final LayoutInflater mInflater;

        public UserAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
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
            final CharSequence s = (CharSequence) getItem(position).getName();
            text.setText(s);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("wzw", "wzw onClick s:" + s);
                    PPTInfoDialog(s);
                }
            });

            return view;
        }
    }

    private void PPTInfoDialog(CharSequence s) {
        //TODO
        String url = "";
        for (PPTEntity ppt : mPPTList) {
            if (ppt.getName().equals(s)) {
                url = ppt.getCustomer_url();
                break;
            }
        }
        PPTInfoDialog pptInfoDialog = new PPTInfoDialog(this, R.style.dialog, this, url);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = pptInfoDialog.getWindow().getAttributes();

        lp.width = (int) (display.getWidth()); //设置宽度
        pptInfoDialog.getWindow().setAttributes(lp);
        pptInfoDialog.setCancelable(true);
        pptInfoDialog.show();
    }


    /**
     * 发消息弹出框
     */
    private void inputMsgDialog() {
        InputTextMsgDialog inputMsgDialog = new InputTextMsgDialog(this, R.style.inputdialog, this);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = inputMsgDialog.getWindow().getAttributes();

        lp.width = (int) (display.getWidth()); //设置宽度
        inputMsgDialog.getWindow().setAttributes(lp);
        inputMsgDialog.setCancelable(true);
        inputMsgDialog.show();
    }


    /**
     * 主播邀请应答框
     */
    private void initInviteDialog() {
        inviteDg = new Dialog(this, R.style.dialog);
        inviteDg.setContentView(R.layout.invite_dialog);
        TextView hostId = (TextView) inviteDg.findViewById(R.id.host_id);
        hostId.setText(CurLiveInfo.getHostID());
        TextView agreeBtn = (TextView) inviteDg.findViewById(R.id.invite_agree);
        TextView refusebtn = (TextView) inviteDg.findViewById(R.id.invite_refuse);
        agreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mVideoMemberCtrlView.setVisibility(View.VISIBLE);
//                mNomalMemberCtrView.setVisibility(View.INVISIBLE);
                //上麦 ；TODO 上麦 上麦 上麦 ！！！！！；
                mLiveHelper.sendC2CCmd(Constants.AVIMCMD_MUlTI_JOIN, "", CurLiveInfo.getHostID());
                mLiveHelper.upMemberVideo();
                inviteDg.dismiss();
            }
        });

        refusebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLiveHelper.sendC2CCmd(Constants.AVIMCMD_MUlTI_REFUSE, "", CurLiveInfo.getHostID());
                inviteDg.dismiss();
            }
        });

        Window dialogWindow = inviteDg.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }


    /**
     * 消息刷新显示
     *
     * @param name    发送者
     * @param context 内容
     * @param type    类型 （上线线消息和 聊天消息）
     */
    public void refreshTextListView(String name, String context, int type) {
        ChatEntity entity = new ChatEntity();
        entity.setSenderName(name);
        entity.setContext(context);
        entity.setType(type);
        //mArrayListChatEntity.add(entity);
        notifyRefreshListView(entity);
        //mChatMsgListAdapter.notifyDataSetChanged();

        mListViewMsgItems.setVisibility(View.VISIBLE);
        SxbLog.d(TAG, "refreshTextListView height " + mListViewMsgItems.getHeight());

        if (mListViewMsgItems.getCount() > 1) {
            if (true)
                mListViewMsgItems.setSelection(0);
            else
                mListViewMsgItems.setSelection(mListViewMsgItems.getCount() - 1);
        }
    }


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

    @Override
    public void updateProfileInfo(TIMUserProfile profile) {

    }

    @Override
    public void updateUserInfo(int requestCode, List<TIMUserProfile> profiles) {
        if (null != profiles) {
            switch (requestCode) {
                case GETPROFILE_JOIN:
                    for (TIMUserProfile user : profiles) {
                        tvMembers.setText("" + CurLiveInfo.getMembers());
                        SxbLog.w(TAG, "get nick name:" + user.getNickName());
                        SxbLog.w(TAG, "get remark name:" + user.getRemark());
                        SxbLog.w(TAG, "get avatar:" + user.getFaceUrl());
                        if (!TextUtils.isEmpty(user.getNickName())) {
                            refreshTextListView(user.getNickName(), "join live", Constants.MEMBER_ENTER);
                        } else {
                            refreshTextListView(user.getIdentifier(), "join live", Constants.MEMBER_ENTER);
                        }
                    }
                    break;
            }

        }
    }

    //旁路直播
    private static boolean isPushed = false;

    /**
     * 旁路直播 退出房间时必须退出推流。否则会占用后台channel。
     */
    public void pushStream() {
        if (!isPushed) {
            bHLSPush = false;
            if (mPushDialog != null)
                mPushDialog.show();
        } else {
            mLiveHelper.stopPush();
        }
    }

    private Dialog mPushDialog;

    private void initPushDialog() {
        mPushDialog = new Dialog(this, R.style.dialog);
        mPushDialog.setContentView(R.layout.push_dialog_layout);
        final EditText pushfileNameInput = (EditText) mPushDialog.findViewById(R.id.push_filename);
        final RadioGroup radgroup = (RadioGroup) mPushDialog.findViewById(R.id.push_type);


        Button recordOk = (Button) mPushDialog.findViewById(R.id.btn_record_ok);
        recordOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ILivePushOption option = new ILivePushOption();
                if (pushfileNameInput.getText().toString().equals("")) { // 推流名字为空
                    Toast.makeText(LiveActivity.this, "name can't be empty", Toast.LENGTH_SHORT);
                    return;
                } else {
                    option.channelName(pushfileNameInput.getText().toString());
                }

                if (radgroup.getCheckedRadioButtonId() == R.id.hls) {//默认格式
                    option.encode(TIMAvManager.StreamEncode.HLS);
                    bHLSPush = true;
                } else {
                    option.encode(TIMAvManager.StreamEncode.RTMP);
                }
                mLiveHelper.startPush(option);//开启推流
                mPushDialog.dismiss();
            }
        });


        Button recordCancel = (Button) mPushDialog.findViewById(R.id.btn_record_cancel);
        recordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPushDialog.dismiss();
            }
        });

        Window dialogWindow = mPushDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
        mPushDialog.setCanceledOnTouchOutside(false);
    }

    private void showPushUrl(final String url){
        ILiveLog.d("ILVBX", "showPushUrl->entered:"+url);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.str_push_title)
                .setMessage(url)
                .setPositiveButton(getString(R.string.str_push_copy), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager cmb = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", url);
                        cmb.setPrimaryClip(clipData);
                        Toast.makeText(getApplicationContext(), "Copy Success", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null);
        if (bHLSPush){
            builder.setNeutralButton(getString(R.string.str_push_share), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showShareDlg(url);
                }
            });
        }
        builder.show();
    }

    private void showShareDlg(String url ){
        //分享到社交平台
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        SxbLog.i("TAG", "pushStreamSucc->title:"+CurLiveInfo.getTitle());
        SxbLog.i("TAG", "pushStreamSucc->url:"+url);
        oks.setTitle(CurLiveInfo.getTitle());
        String coverUrl = CurLiveInfo.getCoverurl();
        if(coverUrl == null || coverUrl.length() == 0){//用户未选择封面时，使用默认封面
            coverUrl = "https://zhaoyang21cn.github.io/ilivesdk_help/readme_img/cover_default.png";
        }
        oks.setImageUrl(coverUrl);
        oks.setText("走过路过，不要错过~快来观看直播吧！");
        oks.setUrl(url);

        // 启动分享GUI
        oks.show(this);
    }


    /**
     * 推流成功
     */
    @Override
    public void pushStreamSucc(TIMAvManager.StreamRes streamRes) {
        List<TIMAvManager.LiveUrl> liveUrls = streamRes.getUrls();
        isPushed = true;
        pushBtn.setText(R.string.live_btn_stop_push);
        int length = liveUrls.size();
        String url = null;
        String url2 = null;
        if (length == 1) {
            TIMAvManager.LiveUrl avUrl = liveUrls.get(0);
            url = avUrl.getUrl();
        } else if (length == 2) {
            TIMAvManager.LiveUrl avUrl = liveUrls.get(0);
            url = avUrl.getUrl();
            TIMAvManager.LiveUrl avUrl2 = liveUrls.get(1);
            url2 = avUrl2.getUrl();
        }

        showPushUrl(url);
    }

    private Dialog recordDialog;
    private String filename = "";
    private boolean mRecord = false;
    private EditText filenameEditText;

    private void initRecordDialog() {
        recordDialog = new Dialog(this, R.style.dialog);
        recordDialog.setContentView(R.layout.record_layout);

        filenameEditText = (EditText) recordDialog.findViewById(R.id.record_filename);

        if (filename.length() > 0) {
            filenameEditText.setText(filename);
        }
        filenameEditText.setText("" + CurLiveInfo.getRoomNum());

        Button videoRecord = (Button) recordDialog.findViewById(R.id.btn_record_video);
        videoRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ILiveRecordOption option = new ILiveRecordOption();
                filename = filenameEditText.getText().toString();
                option.fileName("sxb_" + ILiveLoginManager.getInstance().getMyUserId() + "_" + filename);

                option.classId(123);
                option.recordType(TIMAvManager.RecordType.VIDEO);
                mLiveHelper.startRecord(option);
                mLiveHelper.notifyNewRecordInfo(filename);
                recordDialog.dismiss();
            }
        });
        Button audioRecord = (Button) recordDialog.findViewById(R.id.btn_record_audio);
        audioRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ILiveRecordOption option = new ILiveRecordOption();
                filename = filenameEditText.getText().toString();
                option.fileName("sxb_" + ILiveLoginManager.getInstance().getMyUserId() + "_" + filename);

                option.classId(123);
                option.recordType(TIMAvManager.RecordType.AUDIO);
                mLiveHelper.startRecord(option);
                recordDialog.dismiss();
                recordDialog.dismiss();
            }
        });
        Window dialogWindow = recordDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
        recordDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 停止推流成功
     */
    @Override
    public void stopStreamSucc() {
        isPushed = false;
        pushBtn.setText(R.string.live_btn_push);
    }

    @Override
    public void startRecordCallback(boolean isSucc) {
        mRecord = true;
        recordBtn.setText(R.string.live_btn_stop_record);
    }

    @Override
    public void stopRecordCallback(boolean isSucc, List<String> files) {
        if (isSucc == true) {
            mRecord = false;
            recordBtn.setText(R.string.live_btn_record);
        }
    }

    void checkPermission() {
        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.CAMERA);
            if ((checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.RECORD_AUDIO);
            if ((checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.WAKE_LOCK);
            if ((checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            if (permissionsList.size() != 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_PHONE_PERMISSIONS);
            }
        }
    }

    // 清除老房间数据
    private void clearOldData() {
        mArrayListChatEntity.clear();
        mBoolNeedRefresh = true;
        if (mBoolRefreshLock) {
            return;
        } else {
            doRefreshListView();
        }

        // Top
        mTopArrayListChatEntity.clear();
        mTopBoolNeedRefresh = true;
        if (mTopBoolRefreshLock) {
            return;
        } else {
            doTopRefreshListView();
        }

        mRootView.clearUserView();
    }


    @Override
    public void showRoomList(UserServerHelper.RequestBackInfo reqinfo,ArrayList<RoomInfoJson> livelist) {
        if(reqinfo.getErrorCode()!=0){
            Toast.makeText(this, "error" + reqinfo.getErrorCode()+" info " +reqinfo.getErrorInfo() , Toast.LENGTH_SHORT).show();
            return;


        }
        int index = 0, oldPos = 0;
        for (; index<livelist.size(); index++){
            if (livelist.get(index).getInfo().getRoomnum() == CurLiveInfo.getRoomNum()){
                oldPos = index;
                index ++;
                break;
            }
        }
        if (bSlideUp){
            index -= 2;
        }
        RoomInfoJson info = livelist.get((index+livelist.size())%livelist.size());

        if (null != info){
            MySelfInfo.getInstance().setIdStatus(Constants.MEMBER);
            MySelfInfo.getInstance().setJoinRoomWay(false);
            CurLiveInfo.setHostID(info.getHostId());
            CurLiveInfo.setHostName("");
            CurLiveInfo.setHostAvator("");
            CurLiveInfo.setRoomNum(info.getInfo().getRoomnum());
            CurLiveInfo.setMembers(info.getInfo().getMemsize()); // 添加自己
            CurLiveInfo.setAdmires(info.getInfo().getThumbup());

            backGroundId = CurLiveInfo.getHostID();

            showHeadIcon(mHeadIcon, CurLiveInfo.getHostAvator());
            if (!TextUtils.isEmpty(CurLiveInfo.getHostName())) {
                mHostNameTv.setText(UIUtils.getLimitString(CurLiveInfo.getHostName(), 10));
            }else{
                mHostNameTv.setText(UIUtils.getLimitString(CurLiveInfo.getHostID(), 10));
            }
            tvMembers.setText("" + CurLiveInfo.getMembers());
            tvAdmires.setText("" + CurLiveInfo.getAdmires());

            clearOldData();
            //进入房间流程
            mLiveHelper.switchRoom();
        }else{
            bReadyToChange = true;
        }
    }

    private void switchRoom(){
        if (bReadyToChange) {
            mLiveListHelper.getPageData();
        }
    }

    private static String getValue(String src, String param, String sep){
        int idx = src.indexOf(param);
        if (-1 != idx) {
            idx += param.length() + 1;
            if (-1 != sep.indexOf(src.charAt(idx))){
                idx ++;
            }
            for (int i = idx; i < src.length(); i++) {
                if (-1 != sep.indexOf(src.charAt(i))) {
                    return src.substring(idx, i).trim();
                }
            }
        }

        return "";
    }

    public String getQualityTips(String qualityTips) {
        String strTips = "";
        String sep = "[](),\n";

        strTips += "AVSDK版本号: " + getValue(qualityTips, "sdk_version", sep) + "\n";
        strTips += "房间号: " + getValue(qualityTips, "RoomID", sep) + "\n";
        strTips += "角色: " + getValue(qualityTips, "ControlRole", sep) + "\n";
        strTips += "权限: " + getValue(qualityTips, "Authority", sep) + "\n";
        String tmpStr = getValue(qualityTips, "视频采集", "\n");
        if (!TextUtils.isEmpty(tmpStr))
            strTips += "采集信息: " + getValue(qualityTips, "视频采集", "\n") + "\n";
        strTips += "麦克风: " + getValue(qualityTips, "Mic", sep) + "\n";
        strTips += "扬声器: " + getValue(qualityTips, "Spk", sep) + "\n";

        return strTips;
    }
}
