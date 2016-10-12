package com.example.laijiahao.mychat.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.ui.NewFriendsActivity;
import com.hyphenate.easeui.ui.EaseContactListFragment;

public class ContactFragment extends EaseContactListFragment implements View.OnClickListener {

	private TextView tvUnread;


	@Override
	protected void initView() {
		super.initView();
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.item_contact_list_header, null);
		this.titleBar.setVisibility(View.GONE);
		getView().findViewById(R.id.search_bar_view).setVisibility(View.GONE);
		listView.addHeaderView(headerView);
		registerForContextMenu(listView);

		headerView.findViewById(R.id.re_newfriends).setOnClickListener(this);
		tvUnread = (TextView) headerView.findViewById(R.id.tv_unread);

	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.re_newfriends:
				// 进入申请与通知页面
				startActivity(new Intent(getActivity(), NewFriendsActivity.class));
				break;
		}
	}
}
