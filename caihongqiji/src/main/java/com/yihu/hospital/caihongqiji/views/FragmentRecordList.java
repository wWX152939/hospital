package com.yihu.hospital.caihongqiji.views;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yihu.hospital.caihongqiji.adapters.RecordAdapter;
import com.yihu.hospital.caihongqiji.model.RecordInfo;
import com.yihu.hospital.caihongqiji.presenters.RecListViewHelper;
import com.yihu.hospital.caihongqiji.presenters.viewinface.RecListView;

import java.util.ArrayList;

/**
 * Created by xkazerzhang on 2016/12/22.
 */
public class FragmentRecordList extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RecListView {
    private ListView mLiveList;
    private RecListViewHelper recHelper;
    private RecordAdapter adapterRecord;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AlertDialog mWaitDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.yihu.hospital.caihongqiji.R.layout.fragment_record_list, container, false);
        mLiveList = (ListView) view.findViewById(com.yihu.hospital.caihongqiji.R.id.record_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(com.yihu.hospital.caihongqiji.R.id.swipe_refresh_layout_list);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright), getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light), getResources().getColor(android.R.color.holo_red_light));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        recHelper = new RecListViewHelper(this);
        freshRecordList();
        return view;
    }

    private void freshRecordList() {
        showWaitDlalog(getString(com.yihu.hospital.caihongqiji.R.string.str_loading_record));
        recHelper.refresh();
    }

    private void showWaitDlalog(String strMsg) {
        if (null == mWaitDialog) {
            mWaitDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(strMsg)
                    .create();
            mWaitDialog.setCanceledOnTouchOutside(false);
            mWaitDialog.show();
        }else{
            mWaitDialog.setMessage(strMsg);
            mWaitDialog.show();
        }
    }

    private void dimissWaitDialog() {
        if (null != mWaitDialog) {
            mWaitDialog.dismiss();
            mWaitDialog = null;
        }
    }

    @Override
    public void onRefresh() {
        freshRecordList();
    }

    @Override
    public void onUpdateRecordList(ArrayList<RecordInfo> list) {
        dimissWaitDialog();
        mSwipeRefreshLayout.setRefreshing(false);
        adapterRecord = new RecordAdapter(getActivity(), com.yihu.hospital.caihongqiji.R.layout.item_record, list);
        if (list != null)
            mLiveList.setAdapter(adapterRecord);
    }
}
