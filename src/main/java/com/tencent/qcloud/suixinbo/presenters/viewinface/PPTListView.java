package com.tencent.qcloud.suixinbo.presenters.viewinface;

import com.tencent.qcloud.suixinbo.model.PPTEntity;
import com.tencent.qcloud.suixinbo.presenters.UserServerHelper;

import java.util.ArrayList;


/**
 *  列表页面回调
 */
public interface PPTListView extends MvpView{


    void showPPTList(UserServerHelper.RequestBackInfo result, ArrayList<PPTEntity> pptList);
}
