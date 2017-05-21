package com.tencent.qcloud.suixinbo.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

/**
 * 自己的状态数据
 */
public class MySelfInfo {
    private static final String TAG = MySelfInfo.class.getSimpleName();
    private String id;

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    private String pwd;
    private String userSig;
    private String nickName;    // 呢称
    private String avatar;      // 头像
    private String sign;      // 签名

    // liqiang 0 普通用户 10 专家 100 管理员
    private Integer role;
    private String CosSig;
    private static boolean isCreateRoom = false;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String token;

    private boolean bLiveAnimator;  // 渐隐动画
    private SxbLog.SxbLogLevel logLevel;           // 日志等级

    private int id_status;

    private int myRoomNum = -1;

    private static MySelfInfo ourInstance = new MySelfInfo();

    public static MySelfInfo getInstance() {

        return ourInstance;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserSig() {
        return userSig;
    }

    public void setUserSig(String userSig) {
        this.userSig = userSig;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {

        Log.i("wzw", "setNickName nickname:" + nickName);
        this.nickName = nickName;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getMyRoomNum() {
        return myRoomNum;
    }

    public void setMyRoomNum(int myRoomNum) {
        this.myRoomNum = myRoomNum;
    }

    public String getCosSig() {
        return CosSig;
    }

    public void setCosSig(String cosSig) {
        CosSig = cosSig;
    }

    public boolean isbLiveAnimator() {
        return bLiveAnimator;
    }

    public void setbLiveAnimator(boolean bLiveAnimator) {
        this.bLiveAnimator = bLiveAnimator;
    }

    public SxbLog.SxbLogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(SxbLog.SxbLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void writeToCache(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_ID, id);
        editor.putString(Constants.USER_PWD, pwd);
        editor.putString(Constants.USER_SIG, userSig);
        editor.putString(Constants.USER_TOKEN, token);
        editor.putString(Constants.USER_NICK, nickName);
        editor.putString(Constants.USER_AVATAR, avatar);
        editor.putString(Constants.USER_SIGN, sign);
        editor.putInt(Constants.USER_ROOM_NUM, myRoomNum);
        editor.putBoolean(Constants.LIVE_ANIMATOR, bLiveAnimator);
        editor.putInt(Constants.LOG_LEVEL, logLevel.ordinal());
        editor.commit();
    }

    public void clearCache(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    public void getCache(Context context) {
        SharedPreferences sharedata = context.getSharedPreferences(Constants.USER_INFO, 0);
        id = sharedata.getString(Constants.USER_ID, null);
        pwd = sharedata.getString(Constants.USER_PWD, "");
        userSig = sharedata.getString(Constants.USER_SIG, null);
        token = sharedata.getString(Constants.USER_TOKEN,null);
        myRoomNum = sharedata.getInt(Constants.USER_ROOM_NUM, -1);
        nickName = sharedata.getString(Constants.USER_NICK, null);
        avatar = sharedata.getString(Constants.USER_AVATAR, null);
        sign = sharedata.getString(Constants.USER_SIGN, null);
        bLiveAnimator = sharedata.getBoolean(Constants.LIVE_ANIMATOR, false);
        int level = sharedata.getInt(Constants.LOG_LEVEL, SxbLog.SxbLogLevel.INFO.ordinal());
        if (level < SxbLog.SxbLogLevel.OFF.ordinal() || level > SxbLog.SxbLogLevel.INFO.ordinal()) {
            logLevel = SxbLog.SxbLogLevel.INFO;
        } else {
            logLevel = SxbLog.SxbLogLevel.values()[level];
        }
        SxbLog.setLogLevel(logLevel);
        SxbLog.i(TAG, " getCache id: " + id);
    }

    public int getIdStatus() {
        return id_status;
    }

    public void setIdStatus(int id_status) {
        this.id_status = id_status;
    }

    public boolean isCreateRoom() {
        return isCreateRoom;
    }

    public void setJoinRoomWay(boolean isCreateRoom) {
        this.isCreateRoom = isCreateRoom;
    }
}