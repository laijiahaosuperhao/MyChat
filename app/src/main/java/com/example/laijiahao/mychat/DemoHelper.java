package com.example.laijiahao.mychat;

import android.content.Context;
import android.util.Log;

import com.example.laijiahao.mychat.db.InviteMessgeDao;
import com.example.laijiahao.mychat.db.UserDao;
import com.example.laijiahao.mychat.domain.InviteMessage;
import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.model.EaseNotifier;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.hyphenate.chat.EMGCMListenerService.TAG;


public class DemoHelper {

    private static DemoHelper instance = null;
    private String username;
    private DemoModel demoModel = null;
    private Context appContext;
    private Map<String, EaseUser> contactList;

    private InviteMessgeDao inviteMessgeDao;
    private UserDao userDao;

    private boolean isContactListenerRegisted;
    private EaseUI easeUI;


    public synchronized static DemoHelper getInstance() {
        if (instance == null) {
            instance = new DemoHelper();
        }
        return instance;
    }


    /**
     * init helper
     *
     * @param context
     *            application context
     */
    public void init(Context context) {
        demoModel = new DemoModel(context);
        registerContactListener();
        initDbDao();

    }


    private void initDbDao() {
        inviteMessgeDao = new InviteMessgeDao(appContext);
        userDao = new UserDao(appContext);
    }
    /**
     * register group and contact listener, you need register when login
     */
    public void registerContactListener(){
        if(!isContactListenerRegisted){
            EMClient.getInstance().contactManager().setContactListener(new MyContactListener());
            isContactListenerRegisted = true;
        }

    }


    /**
     * if ever logged in
     *
     * @return
     */
    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();

    }

    /**
     * set current username
     * @param username
     */
    public void setCurrentUserName(String username){
        this.username = username;
        demoModel.setCurrentUserName(username);
    }

    /**
     * get current user's id
     */
    public String getCurrentUsernName(){
        if(username == null){
            username = demoModel.getCurrentUsernName();
        }
        return username;
    }

    /**
     * get contact list
     *
     * @return
     */
    public Map<String, EaseUser> getContactList() {
        if (isLoggedIn() && contactList == null) {
            contactList = demoModel.getContactList();
        }

        // return a empty non-null object to avoid app crash
        if(contactList == null){
            return new Hashtable<String, EaseUser>();
        }

        return contactList;
    }

    /***
     * 好友变化listener
     *
     */
    public class MyContactListener implements EMContactListener {

        @Override
        public void onContactAdded(String username) {

            // save contact
            Map<String, EaseUser> localUsers = getContactList();
            Map<String, EaseUser> toAddUsers = new HashMap<String, EaseUser>();
            EaseUser user = new EaseUser(username);

            if (!localUsers.containsKey(username)) {
                userDao.saveContact(user);
            }
            toAddUsers.put(username, user);
            localUsers.putAll(toAddUsers);

        }

        @Override
        public void onContactDeleted(String username) {
            Map<String, EaseUser> localUsers = DemoHelper.getInstance().getContactList();
            localUsers.remove(username);
            userDao.deleteContact(username);
            inviteMessgeDao.deleteMessage(username);

        }

        @Override
        public void onContactInvited(String username, String reason) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();

            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
                    inviteMessgeDao.deleteMessage(username);
                }
            }
            // save invitation as message
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            Log.d(TAG, username + "apply to be your friend,reason: " + reason);
            // set invitation status
            msg.setStatus(InviteMessage.InviteMesageStatus.BEINVITEED);
            notifyNewInviteMessage(msg);
        }

        @Override
        public void onContactAgreed(String username) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            // save invitation as message
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            Log.d(TAG, username + "accept your request");
            msg.setStatus(InviteMessage.InviteMesageStatus.BEAGREED);
            notifyNewInviteMessage(msg);
        }

        @Override
        public void onContactRefused(String username) {
            // your request was refused
            Log.d(username, username + " refused to your request");
        }
    }

    /**
     * save and notify invitation message
     * @param msg
     */
    private void notifyNewInviteMessage(InviteMessage msg){
        if(inviteMessgeDao == null){
            inviteMessgeDao = new InviteMessgeDao(appContext);
        }
        inviteMessgeDao.saveMessage(msg);
        //increase the unread message count
        inviteMessgeDao.saveUnreadMessageCount(1);
        // notify there is new message
        getNotifier().vibrateAndPlayTone(null);
    //    getNotifier().vibrateAndPlayTone(EMMessage.createReceiveMessage(EMMessage.Type.CMD));
    }

    /**
     * get instance of EaseNotifier
     *
     * @return
     */
    public EaseNotifier getNotifier() {
        return easeUI.getNotifier();
    }




}