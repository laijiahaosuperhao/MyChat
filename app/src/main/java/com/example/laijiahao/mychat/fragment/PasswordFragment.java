package com.example.laijiahao.mychat.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.ui.WalletActivity;
import com.example.laijiahao.mychat.utils.LockPatternView;


/**
 * 密码碎片
 */
public class PasswordFragment extends Fragment implements LockPatternView.OnPatterChangeListerer, View.OnClickListener {
    public static final String TYPE_SETTING = "setting";
    public static final String TYPE_CHECK = "check";

    private static final String ARG_TYPE = "type";
    private TextView lockHint;
    private LockPatternView lockPatternView;
    private LinearLayout btnLayout;
    private String passwordStr;
    private Button btn;
    private int errornum = 0;

    public static PasswordFragment newInstance(String typeStr) {
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, typeStr);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_password, container, false);
        lockHint = (TextView) contentView.findViewById(R.id.fragment_password_lock_hint);
        lockPatternView = (LockPatternView) contentView.findViewById(R.id.fragment_password_lock);
        btnLayout = (LinearLayout) contentView.findViewById(R.id.fragment_password_btn_layout);
        btn = (Button) contentView.findViewById(R.id.fragment_password_btn_commit);
        //设置密码
        if (getArguments() != null) {
            if (TYPE_SETTING.equals(getArguments().getString(ARG_TYPE))) {


                btnLayout.setVisibility(View.VISIBLE);
            }
        }
        lockPatternView.setPatterChangeListerer(this);
        btnLayout.setOnClickListener(this);
        btn.setOnClickListener(this);
        return contentView;
    }


    @Override
    public void onPatterChange(String passwordStr) {
        this.passwordStr = passwordStr;
        if (!TextUtils.isEmpty(passwordStr)) {
            lockHint.setText(passwordStr);
            //密码检查
            if (getArguments() != null) {
                if (TYPE_CHECK.equals(getArguments().getString(ARG_TYPE))) {
                    SharedPreferences sp = getActivity().getSharedPreferences("sp", Context.MODE_PRIVATE);
                    //检查成功
                    if (passwordStr.equals(sp.getString("password", ""))) {
                        getActivity().startActivity(new Intent(getActivity(), WalletActivity.class));
                        getActivity().finish();
                    } else {
                        //检查失败
                        lockHint.setText("密码错误");
                        errornum++;
                        lockPatternView.resetPoint();
                        if (errornum == 3) {
                            Intent intent = new Intent("com.example.broadcastbestpractice.FORCE_OFFLINE");
                            getActivity().sendBroadcast(intent);
                        }
                    }
                }
            }
        } else {
            lockHint.setText("至少5个图案");
        }
    }


    @Override
    public void onPatterStart(boolean isStart) {
        if (isStart) {
            lockHint.setText("请绘制图案");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_password_btn_commit:
                Log.d("99999", "sdddd");

                SharedPreferences sp = getActivity().getSharedPreferences("sp", Context.MODE_PRIVATE);
                sp.edit().putString("password", passwordStr).commit();
                getActivity().finish();

                break;
        }
    }
}
