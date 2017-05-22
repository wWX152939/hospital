package com.yihu.hospital.caihongqiji.presenters.viewinface;

import com.yihu.hospital.caihongqiji.model.MemberInfo;

import java.util.ArrayList;


/**
 * 成员列表回调
 */
public interface MembersDialogView extends MvpView {

    void showMembersList(ArrayList<MemberInfo> data);

}
