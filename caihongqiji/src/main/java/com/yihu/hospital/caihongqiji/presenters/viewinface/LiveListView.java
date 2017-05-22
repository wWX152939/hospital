package com.yihu.hospital.caihongqiji.presenters.viewinface;

import com.yihu.hospital.caihongqiji.model.RoomInfoJson;
import com.yihu.hospital.caihongqiji.presenters.UserServerHelper;

import java.util.ArrayList;


/**
 *  列表页面回调
 */
public interface LiveListView extends MvpView{


    void showRoomList(UserServerHelper.RequestBackInfo result, ArrayList<RoomInfoJson> roomlist);
}
