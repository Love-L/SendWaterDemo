package com.qfdqc.views.demo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**定义菜单项类*/
class TuiCoolMenuItem {
    String menuTitle ;  //菜单项标题
    int menuIcon ;      //菜单项图标

    TuiCoolMenuItem(String menuTitle , int menuIcon ){
        this.menuTitle = menuTitle ;
        this.menuIcon = menuIcon ;
    }
}

/**自定义设置侧滑菜单ListView的Adapter*/
public class DrawerAdapter extends BaseAdapter {

    private List<TuiCoolMenuItem> MenuItems = new ArrayList<>( ) ;      //存储侧滑菜单中的各项的数据
    private Context context ;                                           //构造方法中传过来的activity

    DrawerAdapter( Context context ){
        this.context = context ;
        MenuItems.add(new TuiCoolMenuItem("个人信息", R.drawable.mine)) ;
//        MenuItems.add(new TuiCoolMenuItem("查找空位", R.drawable.find)) ;
        MenuItems.add(new TuiCoolMenuItem("订购水票", R.drawable.find)) ;
        MenuItems.add(new TuiCoolMenuItem("我的订单", R.drawable.oeder)) ;
        MenuItems.add(new TuiCoolMenuItem("应用设置", R.drawable.setting)) ;
        MenuItems.add(new TuiCoolMenuItem("退出登录", R.drawable.exit)) ;
    }

    @Override
    public int getCount() {
        return MenuItems.size();
    }

    @Override
    public TuiCoolMenuItem getItem(int position) {
        return MenuItems.get(position) ;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView ;
        Drawable drawable;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.menudrawer_item, parent, false);
            ((TextView) view).setText(getItem(position).menuTitle) ;
            //((TextView) view).setCompoundDrawablesWithIntrinsicBounds(getItem(position).menuIcon, 0, 0, 0) ;//似乎此方法不能设置图片大小
            drawable = context.getDrawable(getItem(position).menuIcon);
            drawable.setBounds(0,0,100,100);                                  //设置图片大小
            ((TextView) view).setCompoundDrawables(drawable,null,null,null);     //设置图片在左边
        }
        return view ;
    }
}