package com.qfdqc.views.demo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.qfdqc.views.seattable.User;

public class ContentActivity extends AppCompatActivity {

    ListView menuDrawer ;                   // 侧滑菜单视图
    DrawerAdapter menuDrawerAdapter ;       // 侧滑菜单ListView的Adapter
    DrawerLayout mDrawerLayout ;            // DrawerLayout组件
    String currentContentTitle ;            // 当前的内容视图下（即侧滑菜单关闭状态下），ActionBar的标题,
    ActionBarDrawerToggle mDrawerToggle ;   // 侧滑菜单状态监听器
    ActionBar supportActionBar;

    static public User session_user;
    final int REQUEST_WRITE_EXTERNAL_STORAGE = 12134;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        currentContentTitle = getResources().getString(R.string.find);     //开始时显示全局标题“查找”

        menuDrawer = (ListView) findViewById(R.id.left_drawer);        //为侧滑菜单设置适配器Adapter，并为ListView添加单击事件监听器
        menuDrawerAdapter = new DrawerAdapter(this);
        menuDrawer.setAdapter(menuDrawerAdapter);
        menuDrawer.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new DrawerMenuToggle(this, mDrawerLayout, R.mipmap.ic_launcher, R.string.drawer_open, R.string.drawer_close) ;
        mDrawerLayout.addDrawerListener(mDrawerToggle);     //为DrawerLayout注册状态监听器

        supportActionBar = getSupportActionBar();
        supportActionBar.setTitle(currentContentTitle);     //设置ActionBar上的标题
        supportActionBar.setHomeButtonEnabled(true);        //设置ActionBar的指示图标可见，设置ActionBar上的应用图标位置处可以被单击
        supportActionBar.setDisplayShowHomeEnabled(true);   //隐藏ActionBar上的应用图标，只显示文字标签
        supportActionBar.setDisplayHomeAsUpEnabled(true);   //设置返回按钮

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.setHomeAsUpIndicator(R.mipmap.ic_launcher);//改变图标
        mDrawerToggle.syncState();                          //如果你想改变图标的话，这句话要去掉。这个会使用默认的三杠图标

        Bundle bd = new Bundle() ;
        bd.putString(ContentFragment.SELECTED_ITEM,menuDrawerAdapter.getItem(1).menuTitle);
        new ContentFragment().setArguments(bd);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new ContentFragment()).commit();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("session_user");
        session_user = getSessionUser(bundle);

        checkPermission();      //检查是否已经授权 (对内存的读写权限)
    }

    private void checkPermission(){
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);//申请权限
        }
    }

    public User getSessionUser(Bundle bundle) {
        User user = new User();
        user.setId(bundle.getString("id"));
        user.setUsername(bundle.getString("username"));
        user.setPassword(bundle.getString("password"));
        user.setName(bundle.getString("name"));
        user.setPhone(bundle.getString("phone"));
        user.setSex(bundle.getString("sex"));
        user.setImage(bundle.getString("image"));
        return user;
    }

    /**侧滑菜单单击事件监听器*/
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        private void selectItem(int position){

            //为内容视图加载新的Fragment
            Bundle bd = new Bundle() ;
            bd.putString(ContentFragment.SELECTED_ITEM,menuDrawerAdapter.getItem(position).menuTitle);
            ContentFragment contentFragment = new ContentFragment() ;
            contentFragment.setArguments(bd);

            FragmentManager fragmentManager =getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (position) {
                case 0:
                    transaction.replace(R.id.content_frame, new MineFragment());
                    break;
                case 1:
                    transaction.replace(R.id.content_frame, new ContentFragment());
                    break;
                case 2:
                    AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("敬请期待");
                    builder.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                    break;
                case 3:
                    transaction.replace(R.id.content_frame, new SettingFragment());
                    break;
                case 4:
                    LogoutDialog();
                    return;
            }
            transaction.commit();

            menuDrawer.setItemChecked(position, true);            //将选中的菜单项置为高亮
            setTitle(menuDrawerAdapter.getItem(position).menuTitle);    //将ActionBar中标题更改为选中的标题项
            mDrawerLayout.closeDrawer(menuDrawer);                      //将当前的侧滑菜单关闭，调用DrawerLayout的closeDrawer（）方法即可
        }

        public void setTitle( String title ){
            currentContentTitle = title ;                               //更改当前的CurrentContentTitle标题内容
            supportActionBar.setTitle(title);
        }
    }

    /**侧滑菜单状态监听器（开、关），通过继承ActionBarDrawerToggle实现*/
    private class DrawerMenuToggle extends ActionBarDrawerToggle {
        /**
         * @param drawerLayout ：就是加载的DrawerLayout容器组件
         * @param drawerImageRes ： 要使用的ActionBar左上角的指示图标
         * @param openDrawerContentDescRes 、closeDrawerContentDescRes：开启和关闭的两个描述字段，没有太大的用处
         * */
        private DrawerMenuToggle(AppCompatActivity activity, DrawerLayout drawerLayout, int drawerImageRes,
                                 int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes,closeDrawerContentDescRes);
        }

        /** 当侧滑菜单达到完全关闭的状态时，回调这个方法 */
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            //当侧滑菜单关闭后，显示ListView选中项的标题，如果并没有点击ListView中的任何项，那么显示原来的标题
            supportActionBar.setTitle(currentContentTitle);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        /** 当侧滑菜单完全打开时，这个方法被回调 */
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            //supportActionBar.setTitle(R.string.global_title); //当侧滑菜单打开时ActionBar显示全局标题
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }

    /**为了能够让ActionBarDrawerToggle监听器
     * 能够在Activity的整个生命周期中都能够以正确的逻辑工作
     * 需要添加下面两个方法*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**最后做一些菜单上处理*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);     //不使用菜单
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**每次调用 invalidateOptionsMenu() ，下面的这个方法就会被回调*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // 如果侧滑菜单的状态监听器在侧滑菜单打开和关闭时都调用了invalidateOptionsMenu()方法，
        //当侧滑菜单打开时将ActionBar上的某些菜单图标隐藏起来，使得这时仅显示“推酷”这个全局标题
        //本应用中是将ActiongBar上的action菜单项隐藏起来

        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(menuDrawer);      //判定当前侧滑菜单的状态
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);     //菜单上的图标
        return super.onPrepareOptionsMenu(menu);
    }

    /**当用户按下了"手机上的返回功能按键"的时候会回调这个方法*/
    @Override
    public void onBackPressed() {
        boolean drawerState =  mDrawerLayout.isDrawerOpen(menuDrawer);
        if (drawerState) {
            mDrawerLayout.closeDrawers();   //当按下返回功能键的时候，不是直接对Activity进行弹栈，而是先将菜单视图关闭
            return;
        }
        exitDialog();
        //super.onBackPressed();            //如果不注释此行，那么退出提示框也不会再显示了，它直接就退出应用程序了
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void LogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ContentActivity.this, LoginActivity.class));//跳转登录后界面
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setTitle("退出登录");
        builder.setMessage("确定要退出登录吗?");
        builder.create();
        builder.show();
    }

    public void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setTitle("退出提示");
        builder.setMessage("确定要退出吗?");
        builder.create();
        builder.show();
    }
}