package com.yihu.hospital.caihongqiji.presenters.viewinface;

import com.yihu.hospital.caihongqiji.model.PPTEntity;
import com.yihu.hospital.caihongqiji.presenters.UserServerHelper;

import java.util.ArrayList;


/**
 *  列表页面回调
 */
public interface PPTListView extends MvpView{


    void showPPTList(UserServerHelper.RequestBackInfo result, ArrayList<PPTEntity> pptList);
}
