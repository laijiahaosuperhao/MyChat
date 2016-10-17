package com.example.laijiahao.mychat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.laijiahao.mychat.DemoHelper;
import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.constant.Constant;
import com.example.laijiahao.mychat.utils.MyConnectionListener;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.wayww.edittextfirework.FireworkView;


/**
 * Login screen
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private EditText et_usertel;
    private EditText et_password;
    private boolean autoLogin = false;
    private Button btn_login;
    private Button btn_qtlogin;
    private Button tv_wenti;
    private FireworkView mFireworkView_et_usertel;
    private FireworkView mFireworkView_et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DemoHelper.getInstance().isLoggedIn()) {
            autoLogin = true;
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        //注册一个监听连接状态的listener---------监听网络状态+账户是否在别处登录+请求服务器失败等.
        EMClient.getInstance().addConnectionListener(new MyConnectionListener(this));

        setContentView(R.layout.activity_login);
        et_usertel = (EditText) findViewById(R.id.et_usertel);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_qtlogin = (Button) findViewById(R.id.btn_qtlogin);
        tv_wenti = (Button) findViewById(R.id.tv_wenti);

        mFireworkView_et_usertel = (FireworkView) findViewById(R.id.fire_work_et_usertel);
        mFireworkView_et_password = (FireworkView) findViewById(R.id.fire_work_et_password);
        mFireworkView_et_usertel.bindEditText(et_usertel);
        mFireworkView_et_password.bindEditText(et_password);
        // 监听多个输入框
        TextChange textChange = new TextChange();
        et_usertel.addTextChangedListener(textChange);
        et_password.addTextChangedListener(textChange);
        // if user changed, clear the password
        et_usertel.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
             //   mFireworkView_et_password.setEnabled(false);
             //   et_password.setText(null);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        TODO 此处可预置上次登陆的手机号
//        		if (DemoHelper.getInstance().getCurrentUsernName() != null) {
//        			et_usertel.setText(DemoHelper.getInstance().getCurrentUsernName());
//        		}


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginInSever(et_usertel.getText().toString(), et_password.getText().toString());
            }
        });

        btn_qtlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        tv_wenti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,LocalloginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginInSever(final String tel, String password) {
        final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在登录...");
        pd.show();

        if (TextUtils.isEmpty(tel) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "用户名和密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "EMClient.getInstance().login");
        EMClient.getInstance().login(tel, password, new EMCallBack() {
            /**
             * 登陆成功的回调
             */
            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();

                        // 加载所有会话到内存
                        EMClient.getInstance().chatManager().loadAllConversations();
                        // 加载所有群组到内存，如果使用了群组的话
                        // EMClient.getInstance().groupManager().loadAllGroups();
                        /**
                         * 保存用户名
                         */
                        getSharedPreferences(Constant.USERINFO_FILENAME, MODE_PRIVATE).edit().putString("tel", tel).commit();

                        // 登录成功跳转界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.d(TAG, "login: onProgress");
            }

            /**
             * 登陆错误的回调
             * @param i
             * @param s
             */
            @Override
            public void onError(final int i, final String s) {
                Log.d(TAG, "login: onError: " + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        Log.d("lzan13", "登录失败 Error code:" + i + ", message:" + s);
                        /**
                         * 关于错误码可以参考官方api详细说明
                         * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1_e_m_error.html
                         */
                        switch (i) {
                            // 网络异常 2
                            case EMError.NETWORK_ERROR:
                                Toast.makeText(LoginActivity.this, "网络错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的用户名 101
                            case EMError.INVALID_USER_NAME:
                                Toast.makeText(LoginActivity.this, "无效的用户名 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无效的密码 102
                            case EMError.INVALID_PASSWORD:
                                Toast.makeText(LoginActivity.this, "无效的密码 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户认证失败，用户名或密码错误 202
                            case EMError.USER_AUTHENTICATION_FAILED:
                                Toast.makeText(LoginActivity.this, "用户认证失败，用户名或密码错误 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 用户不存在 204
                            case EMError.USER_NOT_FOUND:
                                Toast.makeText(LoginActivity.this, "用户不存在 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 无法访问到服务器 300
                            case EMError.SERVER_NOT_REACHABLE:
                                Toast.makeText(LoginActivity.this, "无法访问到服务器 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 等待服务器响应超时 301
                            case EMError.SERVER_TIMEOUT:
                                Toast.makeText(LoginActivity.this, "等待服务器响应超时 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 服务器繁忙 302
                            case EMError.SERVER_BUSY:
                                Toast.makeText(LoginActivity.this, "服务器繁忙 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            // 未知 Server 异常 303 一般断网会出现这个错误
                            case EMError.SERVER_UNKNOWN_ERROR:
                                Toast.makeText(LoginActivity.this, "未知的服务器异常 code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(LoginActivity.this, "ml_sign_in_failed code: " + i + ", message:" + s, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
            }
        });


    }


    // EditText监听器
    class TextChange implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {

            boolean Sign2 = et_usertel.getText().length() > 0;
            boolean Sign3 = et_password.getText().length() > 0;

            if (Sign2 & Sign3) {
                btn_login.setEnabled(true);
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                btn_login.setEnabled(false);
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (autoLogin) {
            return;
        }
    }

    public void back(View view) {
        finish();
    }
}
