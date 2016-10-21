package com.example.laijiahao.mychat.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.laijiahao.mychat.DemoHelper;
import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.db.InviteMessgeDao;
import com.example.laijiahao.mychat.ui.NewFriendsActivity;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseContactListFragment;

import java.util.Hashtable;
import java.util.Map;

public class ContactFragment extends EaseContactListFragment implements View.OnClickListener {

	private TextView tvUnread;
	private InviteMessgeDao inviteMessgeDao;


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
	public void refresh() {
		Map<String, EaseUser> m = DemoHelper.getInstance().getContactList();
		if (m instanceof Hashtable<?, ?>) {
			m = (Map<String, EaseUser>) ((Hashtable<String, EaseUser>)m).clone();
		}
		setContactsMap(m);
		super.refresh();
		if(inviteMessgeDao == null){
			inviteMessgeDao = new InviteMessgeDao(getActivity());
		}
		if(inviteMessgeDao.getUnreadMessagesCount() > 0){
			tvUnread.setVisibility(View.VISIBLE);
		}else{
			tvUnread.setVisibility(View.GONE);
		}
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
