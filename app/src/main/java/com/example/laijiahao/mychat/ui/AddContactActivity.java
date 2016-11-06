package com.example.laijiahao.mychat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.laijiahao.mychat.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.wayww.edittextfirework.FireworkView;

/**
 * Created by laijiahao on 16/9/24.
 */
public class AddContactActivity extends BaseActivity{

    private EditText editText;
    private Button searchBtn;
    private String toAddUsername;
    private RelativeLayout searchedUserLayout;
    private TextView nameText;
    private ProgressDialog progressDialog;
    private RelativeLayout title;
    private RelativeLayout rl_note;
    private FireworkView mFireworkView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        editText = (EditText) findViewById(R.id.edit_note);
        rl_note = (RelativeLayout) findViewById(R.id.rl_note);
        searchBtn = (Button) findViewById(R.id.search);
        searchedUserLayout = (RelativeLayout) findViewById(R.id.ll_user);
        nameText = (TextView) findViewById(R.id.name);
        title = (RelativeLayout) findViewById(R.id.title);
        mFireworkView = (FireworkView) findViewById(R.id.fire_work);
        mFireworkView.bindEditText(editText);
        Intent intent = getIntent();
        String data = intent.getStringExtra("extra_data");
        if(data!=null){
            rl_note.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            searchedUserLayout.setVisibility(View.VISIBLE);
            nameText.setText(data);
        }
    }

    /**
     * search contact
     * @param v
     */
    public void searchContact(View v) {
        final String name = editText.getText().toString();
        String saveText = searchBtn.getText().toString();


        if (getString(R.string.button_search).equals(saveText)) {
            toAddUsername = name;
            if(TextUtils.isEmpty(name)) {
                new EaseAlertDialog(this, R.string.Please_enter_a_username).show();
                return;
            }

            // TODO you can search the user from your app server here.

            //show the userame and add button if user exist
            searchedUserLayout.setVisibility(View.VISIBLE);
            nameText.setText(toAddUsername);

        }


    }

    /**
     *  add contact
     * @param view
     */
    public void addContact(View view){
        //不能添加自己
        if(EMClient.getInstance().getCurrentUser()
                .equals(nameText.getText().toString())){
            new EaseAlertDialog(this, R.string.not_add_myself).show();
            return;
        }

//        if(DemoHelper.getInstance().getContactList().containsKey(nameText.getText().toString())){
//
//            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
//            return;
//        }

        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMClient.getInstance().contactManager().addContact(nameText.getText().toString(), s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void back(View v) {
        finish();
    }
}
