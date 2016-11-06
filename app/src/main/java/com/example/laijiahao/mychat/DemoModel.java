package com.example.laijiahao.mychat;

import android.content.Context;

import com.example.laijiahao.mychat.db.UserDao;
import com.example.laijiahao.mychat.utils.PreferenceManager;
import com.hyphenate.easeui.domain.EaseUser;

import java.util.List;
import java.util.Map;


public class DemoModel {

    protected Context context = null;

    
    public DemoModel(Context ctx){
        context = ctx;
        PreferenceManager.init(context);
    }


    /**
     * save current username
     * @param username
     */
    public void setCurrentUserName(String username){
        PreferenceManager.getInstance().setCurrentUserName(username);
    }
    public String getCurrentUsernName(){
        return PreferenceManager.getInstance().getCurrentUsername();
    }

    public boolean saveContactList(List<EaseUser> contactList) {
        UserDao dao = new UserDao(context);
        dao.saveContactList(contactList);
        return true;
    }

    public Map<String, EaseUser> getContactList() {
        UserDao dao = new UserDao(context);
        return dao.getContactList();
    }



}
