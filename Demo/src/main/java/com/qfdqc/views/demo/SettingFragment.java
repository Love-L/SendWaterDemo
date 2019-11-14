package com.qfdqc.views.demo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingFragment extends Fragment {

    ListView listView;
    private static final String[] settingList = {
            "检查更新",
            "意见反馈",
            "关于我们"
    };
    private List<Map<String, Object>> list = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_setting, container, false);
        listView = (ListView) view.findViewById(R.id.listView_setting);
        for(String s:settingList) {
            Map<String, Object> map = new HashMap<>();
            map.put("menuName",s);
            list.add(map);
        }
        ListAdapter listAdapter = new SimpleAdapter(
                getActivity(),
                list,
                R.layout.listview_setting_item,
                new String[]{"menuName"},
                new int[]{R.id.listView_setting_item_name}
        );
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        Toast.makeText(getActivity(),"你的已是最新版本",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:break;
                    case 2:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("关于本软件");
//                        builder.setMessage("        本软件用于查询图书馆座位, 它能够提供实时的座位的使用信息，为广 大读者的就座学习带来便利!");
                        builder.setMessage("        本软件是为珠江学院的学生提供订购饮用水的一个平台，学生能够以宿舍为单位进行订水!");
                        builder.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                        break;
                }
            }
        });
        return view;
    }
}
