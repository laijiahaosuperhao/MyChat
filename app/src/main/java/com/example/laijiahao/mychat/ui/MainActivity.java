package com.example.laijiahao.mychat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.fragment.ContactFragment;
import com.example.laijiahao.mychat.fragment.FindFragment;
import com.example.laijiahao.mychat.fragment.MeFragment;
import com.example.laijiahao.mychat.runtimepermissions.PermissionsManager;
import com.example.laijiahao.mychat.runtimepermissions.PermissionsResultAction;
import com.example.laijiahao.mychat.utils.ActivityCollector;
import com.example.laijiahao.mychat.widget.ChangeColorIconWithText;
import com.example.laijiahao.mychat.utils.MyConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.easeui.ui.EaseConversationListFragment;
import com.hyphenate.exceptions.HyphenateException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements OnClickListener,
        OnPageChangeListener {

    private ViewPager mViewPager;
    private List<Fragment> mTabs = new ArrayList<Fragment>();

    private FragmentPagerAdapter mAdapter;

    private List<ChangeColorIconWithText> mTabIndicators = new ArrayList<ChangeColorIconWithText>();

    private List<String> usertels;

    private ContactFragment contactFragment;

    private static EMMessageListener emMessageListener;

    EaseConversationListFragment conversationListFragment;

    private int index;
    private int currentTabIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        // 判断sdk是否登录成功过，并没有退出和被踢，否则跳转到登陆界面
        if (!EMClient.getInstance().isLoggedInBefore()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener(this));

        setContentView(R.layout.activity_main);
        setOverflowButtonAlways();
        //去除显示的图标
        getActionBar().setDisplayShowHomeEnabled(false);

        initView();
        initDatas();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(4);
        initEvent();
        initEmMessageListener();
        runtimepermissions();

    }

    private void initEmMessageListener() {
        emMessageListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息----刷新一下当前页面喽
                conversationListFragment.refresh();
                EMClient.getInstance().chatManager().importMessages(messages);//保存到数据库
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }

            @Override
            public void onMessageReadAckReceived(List<EMMessage> messages) {
                //收到已读回执
            }

            @Override
            public void onMessageDeliveryAckReceived(List<EMMessage> message) {
                //收到已送达回执
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
            }
        };
        EMClient.getInstance().chatManager().addMessageListener(emMessageListener);
    }

    /**
     * 初始化所有事件
     */
    private void initEvent() {

        mViewPager.addOnPageChangeListener(this);

    }


    private void initDatas() {

   //     SettingActivity chatFragment = new SettingActivity();
        conversationListFragment = new EaseConversationListFragment();
        contactFragment = new ContactFragment();
        //	contactListFragment.setContactsMap(getContacts());
//        contactListFragment = new EaseContactListFragment();
        new Thread() {//需要在子线程中调用
                @Override
                public void run() {
                    //需要设置联系人列表才能启动fragment
                    Log.d("map", String.valueOf(getContacts()));
                    contactFragment.setContactsMap(getContacts());
                }
            }.start();
        FindFragment findFragment = new FindFragment();
        MeFragment meFragment = new MeFragment();

        contactFragment.setContactListItemClickListener(new EaseContactListFragment.EaseContactListItemClickListener() {

            @Override
            public void onListItemClicked(EaseUser user) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, user.getUsername()));
            }
        });

        conversationListFragment.setConversationListItemClickListener(new EaseConversationListFragment.EaseConversationListItemClickListener() {

            @Override
            public void onListItemClicked(EMConversation conversation) {
                //进入聊天页面
                startActivity(new Intent(MainActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, conversation.getUserName()));
            }
        });

        mTabs.add(conversationListFragment);
        mTabs.add(contactFragment);
        mTabs.add(findFragment);
        mTabs.add(meFragment);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mTabs.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mTabs.get(position);
            }
        };
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);

        ChangeColorIconWithText one = (ChangeColorIconWithText) findViewById(R.id.id_indicator_one);
        mTabIndicators.add(one);
        ChangeColorIconWithText two = (ChangeColorIconWithText) findViewById(R.id.id_indicator_two);
        mTabIndicators.add(two);
        ChangeColorIconWithText three = (ChangeColorIconWithText) findViewById(R.id.id_indicator_three);
        mTabIndicators.add(three);
        ChangeColorIconWithText four = (ChangeColorIconWithText) findViewById(R.id.id_indicator_four);
        mTabIndicators.add(four);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);


        one.setIconAlpha(1.0f);

        setContactListener();

    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        // Log.e("TAG", "position = " + position + " ,positionOffset =  "
        // + positionOffset);
        if (positionOffset > 0) {
            ChangeColorIconWithText left = mTabIndicators.get(position);
            ChangeColorIconWithText right = mTabIndicators.get(position + 1);
            left.setIconAlpha(1 - positionOffset);
            right.setIconAlpha(positionOffset);
        }

    }

    @Override
    public void onPageSelected(int position) {


    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub

    }




    private void setContactListener() {
        EMClient.getInstance().contactManager().setContactListener(new EMContactListener() {

            @Override
            public void onContactAgreed(String username) {
                Log.d("Listen","好友请求被同意了"+username);
                //好友请求被同意
                new Thread() {//需要在子线程中调用
                    @Override
                    public void run() {
                        //需要设置联系人列表才能启动fragment
                        contactFragment.setContactsMap(getContacts());
                        contactFragment.refresh();
                        Log.d("gggggggggggg", String.valueOf(getContacts()));
                    }
                }.start();

            }

            @Override
            public void onContactRefused(String username) {
                //好友请求被拒绝

            }

            @Override
            public void onContactInvited(final String username, String reason) {
                //收到好友邀请

            }

            @Override
            public void onContactDeleted(String username) {
                Log.d("Listen","好友被删除了"+username);
                //被删除时回调此方法

                new Thread() {//需要在子线程中调用
                    @Override
                    public void run() {
                        //需要设置联系人列表才能启动fragment
                        contactFragment.setContactsMap(getContacts());
                        contactFragment.refresh();
                    }
                }.start();
            }


            @Override
            public void onContactAdded(String username) {
                //增加了联系人时回调此方法
                Log.d("Listen","添加好友了"+username);

                new Thread() {//需要在子线程中调用
                    @Override
                    public void run() {
                        //需要设置联系人列表才能启动fragment
                        contactFragment.setContactsMap(getContacts());
                        contactFragment.refresh();
                    }
                }.start();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_friend:
                Intent intent = new Intent(this, AddContactActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    //反射，动态把ViewConfiguration里面的sHasPermanentMenuKey设置为false，
    // 让它能出现的OverflowButton一直出现，并且点击menu时，改变菜单显示的位置
    private void setOverflowButtonAlways() {
        try {
            //含有很多配置信息
            ViewConfiguration config = ViewConfiguration.get(this);
            //显示overflowbutton的Field
            Field menuKey = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            menuKey.setAccessible(true);
            menuKey.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置menu显示icon
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {

        //根据id判断是否是actionbar和判断menu是否是空的
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            //menu是一个接口，实际上的类是MenuBuilder
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    //找到MenuBuilder的setOptionalIconsVisible方法
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    //通过反射，设置可调用，然后设置为true
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onClick(View v) {
        clickTab(v);

    }

    /**
     * 点击Tab按钮
     *
     * @param v
     */
    private void clickTab(View v) {
        resetOtherTabs();

        switch (v.getId()) {
            case R.id.id_indicator_one:
                index = 0;
                mTabIndicators.get(0).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(0, false);
                break;
            case R.id.id_indicator_two:
                index = 1;
                mTabIndicators.get(1).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(1, false);
                break;
            case R.id.id_indicator_three:
                index = 2;
                mTabIndicators.get(2).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(2, false);
                break;
            case R.id.id_indicator_four:
                index = 3;
                mTabIndicators.get(3).setIconAlpha(1.0f);
                mViewPager.setCurrentItem(3, false);
                break;
        }
        currentTabIndex = index;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        EMClient.getInstance().chatManager().removeMessageListener(emMessageListener);
    }

    /**
     * 重置其他的TabIndicator的颜色
     */
    private void resetOtherTabs() {
        for (int i = 0; i < mTabIndicators.size(); i++) {
            mTabIndicators.get(i).setIconAlpha(0);
        }
    }


    /**
     * prepared users
     *
     * @return
     */

////	List<String> usertels = EMClient.getInstance().contactManager().getAllContactsFromServer();
//    private Map<String, EaseUser> getContact() {
//        Map<String, EaseUser> contacts = new HashMap<String, EaseUser>();
//
//        for (int i = 0; i < usertels.size(); i++) {
//            String usertel = "";
////			EaseUser user = new EaseUser("easeuitest" + i);
////			contacts.put("easeuitest" + i, user);
//            EaseUser user = new EaseUser(usertels.get(i));
//            contacts.put(usertel, user);
//        }
//        return contacts;
//    }

    /**
     * 获取联系人
     * @return
     */
    private Map<String, EaseUser> getContacts() {
        Map<String, EaseUser> map = new HashMap<>();
        try {
            List<String> userNames = EMClient.getInstance().contactManager().getAllContactsFromServer();
            Log.d("......有几个好友:" , String.valueOf(userNames.size()));
            for (String userId : userNames) {
                Log.d("好友列表中有 : " , userId);
                map.put(userId, new EaseUser(userId));
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            Intent intent = new Intent(this,AddContactActivity.class);
            intent.putExtra("extra_data",scanResult);
            startActivity(intent);
        }
    }

    private void runtimepermissions() {
        /**
         * 请求所有必要的权限----
         */
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//				Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
