package com.tencent.qcloud.suixinbo.presenters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.util.Log;

import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MemberID;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.model.PPTEntity;
import com.tencent.qcloud.suixinbo.model.RecordInfo;
import com.tencent.qcloud.suixinbo.model.RoomInfoJson;
import com.tencent.qcloud.suixinbo.presenters.viewinface.PPTListView;
import com.tencent.qcloud.suixinbo.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 网络请求类
 */
public class UserServerHelper {
    private static final String TAG = UserServerHelper.class.getSimpleName();
    private static UserServerHelper instance = null;
//    public static final String BASE_URL = "https://sxb.qcloud.com/sxb/index.php?";
    // 注册 登录 创建房间 上报创建房结果 拉去直播房间列表 上报进入房间信息 拉取房间成员列表
    public static final String BASE_URL = "http://www.yihucloud.com/liver/index.php?";

    // 请求专家列表
    public static final String REQUEST_EXPERT = BASE_URL + "svc=live&cmd=roomexpertslist";

    // 请求PPT列表
    public static final String REQUEST_PPT = BASE_URL + "svc=ppt&cmd=list";

    // 注册
    public static final String REGISTER = BASE_URL + "svc=account&cmd=regist";

    // 验证邀请码
    public static final String CHECK_YQM = BASE_URL + "svc=code&cmd=check";

    //登录
    public static final String LOGIN = BASE_URL + "svc=account&cmd=login";
    public static final String LOGOUT = BASE_URL + "svc=account&cmd=logout";

    //创建房间
    public static final String APPLY_CREATE_ROOM = BASE_URL + "svc=live&cmd=create";

    // 上报房间结果
    public static final String REPORT_ROOM_INFO = BASE_URL + "svc=live&cmd=reportroom";

    public static final String HEART_BEAT = BASE_URL + "svc=live&cmd=heartbeat";
    public static final String STOP_ILIVE = BASE_URL + "svc=live&cmd=exitroom";

    // 拉取直播房间列表
    public static final String GET_ROOMLIST = BASE_URL + "svc=live&cmd=roomlist";

    //上报进入房间信息
    public static final String REPORT_ME = BASE_URL + "svc=live&cmd=reportmemid";

    //拉取房间成员列表
    public static final String GET_MEMLIST = BASE_URL + "svc=live&cmd=roomidlist";

    public static final String REPORT_RECORD ="https://sxb.qcloud.com/sxb/index.php?svc=live&cmd=reportrecord";
    public static final String GET_REOCORDLIST ="https://sxb.qcloud.com/sxb/index.php?svc=live&cmd=recordlist";
    public static final String GET_PLAYERLIST = BASE_URL + "svc=live&cmd=livestreamlist";
    public static final String GET_ROOM_PLAYURL ="https://sxb.qcloud.com/sxb/index.php?svc=live&cmd=getroomplayurl";
    public static final String GET_COS_SIG = "https://sxb.qcloud.com/sxb/index.php?svc=cos&cmd=get_sign";


    private String token = ""; //后续使用唯一标示
    private String Sig = ""; //登录唯一标示
//    private int avRoom;
//    private String groupID;

    public class RequestBackInfo {

        int errorCode;
        String errorInfo;

        RequestBackInfo(int code, String bad) {
            errorCode = code;
            errorInfo = bad;
        }

        public int getErrorCode() {
            return errorCode;
        }


        public String getErrorInfo() {
            return errorInfo;
        }

    }


    public static UserServerHelper getInstance() {
        if (instance == null) {
            instance = new UserServerHelper();
        }
        return instance;
    }



    private ArrayList<RoomInfoJson> roomList;

    public ArrayList<RoomInfoJson> getRoomListData(){
        return roomList;
    }


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();


    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            return "";
        }
    }

    public void requestExpertList(final String token, final int roomNum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("wzw", "wzw token:" + token + " roomNum:" + roomNum);
                    JSONObject jasonPacket = new JSONObject();
                    jasonPacket.put("token", token);
                    jasonPacket.put("roomnum", roomNum);
                    String json = jasonPacket.toString();
                    String res = post(REQUEST_EXPERT, json);
                    JSONTokener jsonParser = new JSONTokener(res);
                    JSONObject response = (JSONObject) jsonParser.nextValue();
                    int code = response.getInt("errorCode");
                    String errorInfo = response.getString("errorInfo");

                    Log.i("wzw", "wzw code:" + code);
                    if(code == 0){
                        JSONObject data = response.getJSONObject("data");
                        JSONArray record = data.getJSONArray("idlist");
                        Type listType = new TypeToken<ArrayList<MemberID>>() {}.getType();
                        ArrayList<MemberID> result = new Gson().fromJson(record.toString(), listType);
                        ILiveLog.i(TAG,"size"+result.size());
                        CurLiveInfo.setExpertList(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void requestPPTList(PPTListView pptInterface, String token) {
        requestPPTList(pptInterface, token, 0, 10);
    }

    public void requestPPTList(final PPTListView pptInterface, final String token, final int index, final int size) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("wzw", "wzw requestPPTList token:" + token);
                    JSONObject jasonPacket = new JSONObject();
                    jasonPacket.put("token", token);
                    jasonPacket.put("index", index);
                    jasonPacket.put("size", size);
                    String json = jasonPacket.toString();
                    String res = post(REQUEST_PPT, json);
                    JSONTokener jsonParser = new JSONTokener(res);
                    JSONObject response = (JSONObject) jsonParser.nextValue();
                    int code = response.getInt("errorCode");
                    String errorInfo = response.getString("errorInfo");

                    Log.i("wzw", "wzw requestPPTList code:" + code);
                    if(code == 0){
                        JSONObject data = response.getJSONObject("data");
                        JSONArray record = data.getJSONArray("pptlist");
                        Type listType = new TypeToken<ArrayList<PPTEntity>>() {}.getType();
                        ArrayList<PPTEntity> result = new Gson().fromJson(record.toString(), listType);
                        ILiveLog.i(TAG,"size"+result.size());
                        Log.i("wzw", "wzw showPPTList ");
                        pptInterface.showPPTList(new RequestBackInfo(code, errorInfo), result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 注册ID （独立方式）
     */
    public RequestBackInfo registerId(String id, String password, String email) {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("id", id);
            jasonPacket.put("pwd", password);
            jasonPacket.put("email", email);
            String json = jasonPacket.toString();
            String res = post(REGISTER, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");

            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证邀请码
     */
    public RequestBackInfo checkyqm(String id, String password, String code) {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("id", id);
            jasonPacket.put("pwd", password);
            jasonPacket.put("code", code);
            String json = jasonPacket.toString();
            String res = post(CHECK_YQM, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int ret_code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");

            if (ret_code == 0) {
                JSONObject data = response.getJSONObject("data");

                Sig = data.getString("userSig");
                token = data.getString("token");
                Integer role = data.getInt("role");
                MySelfInfo.getInstance().setId(id);
                MySelfInfo.getInstance().setPwd(password);
                MySelfInfo.getInstance().setUserSig(Sig);
                MySelfInfo.getInstance().setToken(token);
                MySelfInfo.getInstance().setRole(role);

            }

            return new RequestBackInfo(ret_code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 登录ID （独立方式）
     */
    public RequestBackInfo loginId(String id, String password) {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("id", id);
            jasonPacket.put("pwd", password);
            String json = jasonPacket.toString();
            String res = post(LOGIN, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();

            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            if (code == 0) {
                JSONObject data = response.getJSONObject("data");

                Sig = data.getString("userSig");
                token = data.getString("token");
                Integer role = data.getInt("role");
                MySelfInfo.getInstance().setId(id);
                MySelfInfo.getInstance().setPwd(password);
                MySelfInfo.getInstance().setUserSig(Sig);
                MySelfInfo.getInstance().setToken(token);
                MySelfInfo.getInstance().setRole(role);

            }
            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 登出ID （独立方式）
     */
    public RequestBackInfo logoutId(String id) {
        try {
            JSONObject jasonPacket = new JSONObject();
            // liqiang 登出不需要id
//            jasonPacket.put("id", id);
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            String json = jasonPacket.toString();
            String res = post(LOGOUT, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();

            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            return new RequestBackInfo(code, errorInfo);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * 申请创建房间
     */
    public RequestBackInfo applyCreateRoom() {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("type", "live");
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            String json = jasonPacket.toString();
            String res = post(APPLY_CREATE_ROOM, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            if (code == 0) {
                JSONObject data = response.getJSONObject("data");
                int avRoom = data.getInt("roomnum");
                MySelfInfo.getInstance().setMyRoomNum(avRoom);
                CurLiveInfo.setRoomNum(avRoom);
                String groupID = data.getString("groupid");
            }
            return new RequestBackInfo(code, errorInfo);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 上报房间信息
     */
    public RequestBackInfo reporNewtRoomInfo(String inputJson) {
        try {

            String res = post(REPORT_ROOM_INFO, inputJson);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 上报录制信息
     */
    public RequestBackInfo reporNewtRecordInfo(String inputJson) {
        try {
            Log.v(TAG, "reporNewtRecordInfo->"+inputJson);
            String res = post(REPORT_RECORD, inputJson);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            RequestBackInfo ret =  new RequestBackInfo(code, errorInfo);
            Log.v(TAG, "reporNewtRecordInfo->rsp:"+ret.errorCode+"|"+ret.getErrorInfo());
            return ret;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 心跳上报
     */
    public RequestBackInfo heartBeater (int role) {
        try {
            JSONObject jasonPacket = new JSONObject();

            // liqiang 1 主播 0 观众 2 上麦观众
            jasonPacket.put("role", role);
//            jasonPacket.put("role", 0);
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            jasonPacket.put("roomnum", MySelfInfo.getInstance().getMyRoomNum());

            //点赞数
            jasonPacket.put("thumbup",CurLiveInfo.getAdmires());
            String json = jasonPacket.toString();
            String res = post(HEART_BEAT, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取房间列表
     */
    public RequestBackInfo getRoomList() {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            jasonPacket.put("type", "live");
            jasonPacket.put("index", 0);
            jasonPacket.put("size", 20);
            jasonPacket.put("appid", Constants.SDK_APPID);
            String json = jasonPacket.toString();
            String res = post(GET_ROOMLIST, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            if(code ==0){
                JSONObject data = response.getJSONObject("data");
                JSONArray record = data.getJSONArray("rooms");
                Type listType = new TypeToken<ArrayList<RoomInfoJson>>() {}.getType();
                roomList= new Gson().fromJson(record.toString(), listType);
            }
            String errorInfo = response.getString("errorInfo");
            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通知UserServer结束房间
     */
    public RequestBackInfo notifyCloseLive() {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            jasonPacket.put("roomnum", MySelfInfo.getInstance().getMyRoomNum());
            jasonPacket.put("type", "live");
            String json = jasonPacket.toString();
            String res = post(STOP_ILIVE, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 上报成员
     */
    public RequestBackInfo reportMe(int role, int action) {
        try {
            JSONObject jasonPacket = new JSONObject();

            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            jasonPacket.put("roomnum", CurLiveInfo.getRoomNum());

            // liqiang 不需要该字段
//            jasonPacket.put("id", MySelfInfo.getInstance().getId());

            // 李强，修改role  主播1 成员0 上麦成员2
            jasonPacket.put("role", role);
//            jasonPacket.put("role", 0);

            //0 进入房间 1 退出房间上报
            jasonPacket.put("operate", action);

            String json = jasonPacket.toString();
            String res = post(REPORT_ME, json);
            ILiveLog.i(TAG,"reportMe "+role+" action " + action);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取房间内成员
     */
    public ArrayList<MemberID> getMemberList() {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            jasonPacket.put("roomnum", CurLiveInfo.getRoomNum());
            jasonPacket.put("index",0);
            jasonPacket.put("size", 40);

            String json = jasonPacket.toString();
            String res = post(GET_MEMLIST, json);
               JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            if(code ==0){
                JSONObject data = response.getJSONObject("data");
                JSONArray record = data.getJSONArray("idlist");
                Type listType = new TypeToken<ArrayList<MemberID>>() {}.getType();
                ArrayList<MemberID> result = new Gson().fromJson(record.toString(), listType);
                ILiveLog.i(TAG,"size"+result.size());
                return result;
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//
//    /**
//     * 上报录制视频URL
//     */
//    public RequestBackInfo reportRecord(String videoid,String videoUrl,int type,String cover) {
//        try {
//            JSONObject jasonPacket = new JSONObject();
//            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
//            jasonPacket.put("videoid", videoid);
//            jasonPacket.put("playurl",videoUrl);
//            jasonPacket.put("type", type);
//            jasonPacket.put("cover",cover);
//
//            String json = jasonPacket.toString();
//            String res = post(REPORT_RECORD, json);
//            JSONTokener jsonParser = new JSONTokener(res);
//            JSONObject response = (JSONObject) jsonParser.nextValue();
//            int code = response.getInt("errorCode");
//            String errorInfo = response.getString("errorInfo");
//            return new RequestBackInfo(code, errorInfo);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//
//
//
//    /**
//     * 获取点播列表
//     */
//    public RequestBackInfo getRecordList() {
//        try {
//            JSONObject jasonPacket = new JSONObject();
//            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
//            jasonPacket.put("type", 0);
//            jasonPacket.put("index",0);
//            jasonPacket.put("size", 10);
//            String json = jasonPacket.toString();
//            String res = post(GET_REOCORDLIST, json);
//            JSONTokener jsonParser = new JSONTokener(res);
//            JSONObject response = (JSONObject) jsonParser.nextValue();
//            int code = response.getInt("errorCode");
//            String errorInfo = response.getString("errorInfo");
//            return new RequestBackInfo(code, errorInfo);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }



    /**
     * 拉取录制列表
     */
    public ArrayList<RecordInfo> getRecordList (int page,int size) {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
			jasonPacket.put("type", Constants.VOD_MODE);
            jasonPacket.put("index", page);
            jasonPacket.put("size",size);
            String json = jasonPacket.toString();
            Log.v(TAG, "getRecordList->request: "+json);
            String res = post(GET_REOCORDLIST, json);
            Log.v(TAG, "getRecordList->ret: "+res);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            if(code ==0){
                JSONObject data = response.getJSONObject("data");
                JSONArray record = data.getJSONArray("videos");
                ArrayList<RecordInfo> recList = new ArrayList<>();
                for (int i=0; i<record.length(); i++){
                    recList.add(new RecordInfo(record.getJSONObject(i)));
                }
                ILiveLog.i(TAG,"size"+recList.size());
                return recList;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取播放列表
     */
    public RequestBackInfo getPlayUrlList (int page, int size) {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            jasonPacket.put("index", page);
            jasonPacket.put("size",size);
            String json = jasonPacket.toString();
            String res = post(GET_PLAYERLIST, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            String errorInfo = response.getString("errorInfo");
            return new RequestBackInfo(code, errorInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 获取房间回放地址
     */
    public RequestBackInfo getRoomPlayUrl (int room) {
        try {
            JSONObject jasonPacket = new JSONObject();
            jasonPacket.put("token", MySelfInfo.getInstance().getToken());
            jasonPacket.put("roomnum", room);
            String json = jasonPacket.toString();
            String res = post(GET_ROOM_PLAYURL, json);
            JSONTokener jsonParser = new JSONTokener(res);
            JSONObject response = (JSONObject) jsonParser.nextValue();
            int code = response.getInt("errorCode");
            if (code == 0) {
                JSONObject data = response.getJSONObject("data");
                String address = data.getString("address");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCosSig() {
        try {
            String response = UserServerHelper.getInstance().post(GET_COS_SIG, "");
            JSONTokener jsonParser = new JSONTokener(response);
            JSONObject reg_response = (JSONObject) jsonParser.nextValue();
            int ret = reg_response.getInt("errorCode");
            if (ret == 0) {
                JSONObject data = reg_response.getJSONObject("data");
                String sign = data.getString("sign");
                return sign;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
