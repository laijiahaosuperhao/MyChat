package com.example.laijiahao.mychat.fragment;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.constant.Constant;
import com.example.laijiahao.mychat.receiver.MyAdminDeviceReceiver;
import com.example.laijiahao.mychat.ui.ChatDemoActivity;
import com.example.laijiahao.mychat.ui.CompassActivity;
import com.example.laijiahao.mychat.ui.PreWalletActivity;
import com.example.laijiahao.mychat.ui.ProfileActivity;
import com.example.laijiahao.mychat.utils.MyConnectionListener;
import com.hyphenate.chat.EMClient;

public class MeFragment extends Fragment implements View.OnClickListener{

	private DevicePolicyManager dpm;
	private RelativeLayout locknow;
	private RelativeLayout zhinanzhen;
	private RelativeLayout re_myinfo;
	private RelativeLayout re_wallet;
	private RelativeLayout re_setting;
	private TextView tv_name;
	private TextView tv_id;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.fragment_profile, container,false);
		dpm = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
		locknow = (RelativeLayout) view.findViewById(R.id.locknow);
		zhinanzhen = (RelativeLayout) view.findViewById(R.id.zhinanzhen);
		re_myinfo = (RelativeLayout) view.findViewById(R.id.re_myinfo);
		re_wallet = (RelativeLayout) view.findViewById(R.id.re_wallet);
		tv_id = (TextView) view.findViewById(R.id.tv_id);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		re_setting = (RelativeLayout) view.findViewById(R.id.re_setting);


		String nick = getActivity().getSharedPreferences(Constant.USERINFO_FILENAME, Context.MODE_PRIVATE).getString("nick", "laijiahaosuperhao");
		tv_name.setText(nick);
		String tel = getActivity().getSharedPreferences(Constant.USERINFO_FILENAME, Context.MODE_PRIVATE).getString("tel", "laijiahaosuperhao");
		tv_id.setText(tel);

		EMClient.getInstance().addConnectionListener(new MyConnectionListener(getActivity()));
		locknow.setOnClickListener(this);
		zhinanzhen.setOnClickListener(this);
		re_myinfo.setOnClickListener(this);
		re_wallet.setOnClickListener(this);
		re_setting.setOnClickListener(this);
		return view;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()){
			/**
			 * 实现一键锁屏
			 * 1.激活设备管理程序
			 * 2.完成锁屏等操作
			 */
			case R.id.locknow:
				//判断是否有激活，如果没有激活，就直接激活设备
				ComponentName who = new ComponentName(this.getActivity(), MyAdminDeviceReceiver.class);
				//如果有激活，直接锁屏
				if(dpm.isAdminActive(who)){
					dpm.lockNow();
				}else{
					// return false - don't update checkbox until we're really active
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, who);
					intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"设备管理员");
					startActivity(intent);
				}
				break;
			/**
			 * 实现指南针
			 */
			case R.id.zhinanzhen:
				Intent intent = new Intent(getActivity(),CompassActivity.class);
				startActivity(intent);
				break;

			case R.id.re_myinfo:
				startActivityForResult(new Intent(getActivity(), ProfileActivity.class),0);
				break;

			case R.id.re_wallet:
				Intent intent1 = new Intent(getActivity(),PreWalletActivity.class);
				startActivity(intent1);
				break;
			case R.id.re_setting:
				Intent intent2 = new Intent(getActivity(),ChatDemoActivity.class);
				startActivity(intent2);
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode== Activity.RESULT_OK){
			initView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initView() {
	}

}
