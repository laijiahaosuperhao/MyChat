package com.example.laijiahao.mychat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.ui.BaiduMapActivity;
import com.example.laijiahao.mychat.ui.ChooseAreaActivity;
import com.example.laijiahao.mychat.ui.FriendCircleActivity;
import com.example.laijiahao.mychat.ui.YaoyiyaoActivity;
import com.xys.libzxing.zxing.activity.CaptureActivity;

public class FindFragment extends Fragment implements View.OnClickListener{
	private RelativeLayout yaoyiyao;
	private RelativeLayout saoyisao;
	private RelativeLayout re_friends;
	private RelativeLayout myweather;
	private RelativeLayout re_fujin;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.fragment_find, container,false);
		yaoyiyao = (RelativeLayout) view.findViewById(R.id.re_yaoyiyao);
		saoyisao = (RelativeLayout) view.findViewById(R.id.re_qrcode);
		re_friends = (RelativeLayout) view.findViewById(R.id.re_friends);
		myweather = (RelativeLayout) view.findViewById(R.id.myweather);
		re_fujin = (RelativeLayout) view.findViewById(R.id.re_fujin);
		yaoyiyao.setOnClickListener(this);
		saoyisao.setOnClickListener(this);
		re_friends.setOnClickListener(this);
		myweather.setOnClickListener(this);
		re_fujin.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.re_qrcode:
				//打开扫描界面扫描条形码或二维码
				Intent openCameraIntent = new Intent(getActivity(), CaptureActivity.class);
				startActivityForResult(openCameraIntent, 0);
				break;
			case R.id.re_yaoyiyao:
				Intent intent = new Intent(getActivity(),YaoyiyaoActivity.class);
				startActivity(intent);
				break;
			case R.id.re_friends:
				Intent friendCircle = new Intent(getActivity(),FriendCircleActivity.class);
				startActivity(friendCircle);
				break;
			case R.id.myweather:
				Intent myWeather = new Intent(getActivity(),ChooseAreaActivity.class);
				startActivity(myWeather);
				break;
			case R.id.re_fujin:
				Intent mySite = new Intent(getActivity(),BaiduMapActivity.class);
				startActivity(mySite);
				break;

		}
	}
}
