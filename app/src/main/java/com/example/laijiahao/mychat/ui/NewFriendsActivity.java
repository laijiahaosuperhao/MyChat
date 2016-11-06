package com.example.laijiahao.mychat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.adapter.NewFriendsMsgAdapter;
import com.example.laijiahao.mychat.db.InviteMessgeDao;
import com.example.laijiahao.mychat.domain.InviteMessage;

import java.util.List;

/**
 * Created by laijiahao on 16/9/29.
 */
public class NewFriendsActivity extends BaseActivity {

    private ListView listView;
    List<InviteMessage> msgs;
    InviteMessgeDao dao;
    NewFriendsMsgAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends);
        listView = (ListView) findViewById(R.id.listview);

        dao = new InviteMessgeDao(this);
        msgs = dao.getMessagesList();
        // 设置adapter
        adapter = new NewFriendsMsgAdapter(this, 1,msgs);
        listView.setAdapter(adapter);
        dao.saveUnreadMessageCount(0);
    }


    public void back(View v) {
        finish();
    }
}
