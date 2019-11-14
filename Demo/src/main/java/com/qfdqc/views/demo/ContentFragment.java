package com.qfdqc.views.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.qfdqc.views.seattable.Library;
import com.qfdqc.views.seattable.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ContentFragment extends Fragment {
    public static final String SELECTED_ITEM = "selected_item" ;

    private SharedPreferences.Editor editor;    //在退出本Fragment页面时会用它把当前选中的图书馆id和楼层id保存到本地，以方便下次进入页面时的访问
    private boolean isCreateView;               //进入页面后，用它来判断是否已经初始化页面了，以确保不会再次更新控件的数据，以提高性能
    private Handler handler;                    //使用网络请求数据时需要用到的 线程
    Spinner library_spinner;                    //图书馆下拉列表
    Spinner floor_spinner;                      //楼层下拉列表
    Spinner room_spinner;                       //区域下拉列表
    Button btn_find;                            //查找座位按钮

    public List<Library> libraries;             //图书馆数据集合
    public List<Room> rooms;                    //区域数据集合
    public String [] libraries_name;            //用于显示在的图书馆名称数组
    public String[] floors_name;                //用于显示的楼层名称数组
    public String [] rooms_name;                //用于显示的区域名称数组
    public final String[] all_floor_name = {"一楼","二楼","三楼","四楼","五楼","六楼","七楼","八楼","九楼","十楼","十一楼","十二楼"};//开始时的哪些楼层是否有座位区域是未确定的
    public int[] floor_list;                    //用于保存从服务器获取的有座位区域的楼层，以确定要在下拉控件上显示的楼层名称
    public int floor_id;                        //保存当前选中的楼层的id
    public String library_id;                   //保存当前选中的图书馆的id
    static public String room_id;               //保存当前选中的区域的id
    static public String room_data;             //是从服务器获取到的 也是用户已经确定选中要查看的区域，它是Json类型数据的字符串，将会在座位表页面被解释
    ArrayAdapter<String> librariesAdapter;      //这三个都是下拉列表控件用于匹配数据的适配器
    ArrayAdapter<String> floorsAdapter;
    ArrayAdapter<String> roomsAdapter;

    private String result;                      //保存从服务器接收到的字符串数据
    //final String serverPath = "http://192.168.43.122:8888/";
    final String serverPath = "http://47.103.26.115:8888/";

    @Override
    public void onDestroyView() {               //在页面将要退出时，要保存当前选中的图书馆id和楼层id
        editor.putString("libraryId",library_id);
        editor.putInt("floorId",floor_id);
        editor.apply();
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content, container, false);
        isCreateView = true;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("state",getActivity().MODE_PRIVATE);
        editor = sharedPreferences.edit();
        library_id = sharedPreferences.getString("libraryId","");        //取本地数据, 取出上次选中的图书馆id
        floor_id = sharedPreferences.getInt("floorId",0);                 //取出选中的楼层id
        editor.apply();
        library_spinner = (Spinner) view.findViewById(R.id.library_spinner);    //获取页面控件
        floor_spinner = (Spinner) view.findViewById(R.id.floor_spinner);
        room_spinner = (Spinner) view.findViewById(R.id.room_spinner);
        getLibraryData();                                                       //从服务器获取图书馆数据
        btn_find = (Button) view.findViewById(R.id.start_find);
        btn_find.setOnClickListener(new View.OnClickListener() {                //设置按钮的事件监听器
            @Override
            public void onClick(View view) {
                getSeatInfo();                                                  //点击按钮就调用 getSeatInfo()方法 从服务器中获取座位表数据
            }
        });
        return view ;
    }

    public void initView() {                                                    // initView() 方法在每次进入页面时只会被调用一次, 可以被多次调用, 但没必要
        for(int i = 0;i < libraries.size(); i++) {                              // 遍历图书馆数据集合, 找到跟上次选中的 图书馆id 相匹配的下拉选中, 设置其被选中
            if(libraries.get(i).getLibrary_id().equals(library_id)) {
                library_spinner.setSelection(i,true);                   // 设置该项被选中
                break;                                                          // 当找到了符合的那一项之后就使用 break; 退出遍历循环, 以提高性能
            }
        }
        for(int i = 0;i < floor_list.length; i++) {                             // 同上
            if(floor_list[i] == floor_id) {
                floor_spinner.setSelection(i,true);
                break;
            }
        }
        library_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {       /* 当选择图书馆时 调用查找 floors 以得到 floor_id */
                library_id = libraries.get(position).getLibrary_id();           // 获取选中项的 图书馆id ,以作记录
                getFloorData();                                                 // 选择了图书馆之后, 当然是要从服务器获取其相关的楼层数据
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        floor_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                floor_id = floor_list[position];                                // 获取选中项的 楼层id ,以作记录
                getRoomData();                                                  // 选择了楼层之后, 接下来就是从服务器获取其相关的区域数据
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        room_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                room_id = rooms.get(position).getId();                          // 选中区域之后,先用room_id记录选中的区域id,以用于从服务器获取其区域的座位表数据
                if (!NetUtil.isNetConnected(getActivity())) {
                    Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_LONG).show();
                    return;                                                     // 没用网络时, 弹出提示并终止查询
                }
                class  MyHandler extends Handler {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                    }
                }
                handler = new MyHandler();
                new Thread(new Runnable() {
                    public void run() {
                        getRoomInfo();
                        Message m = handler.obtainMessage();
                        handler.sendMessage(m);
                    }
                }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void handlerLibraryJsonData(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            libraries = new ArrayList<>();
            Library library;
            for(int i = 0;i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                library = new Library();
                library.setLibrary_id(jsonObject.getString("library_id"));
                library.setSchool(jsonObject.getString("school"));
                library.setLibrary_name(jsonObject.getString("library_name"));
                library.setLocalx(Double.parseDouble(jsonObject.getString("localx")));
                library.setLocaly(Double.parseDouble(jsonObject.getString("localy")));
                library.setFloors(Integer.parseInt(jsonObject.getString("floors")));
                libraries.add(library);
            }
            libraries_name = new String[libraries.size()];
            for(int i = 0; i < libraries.size(); i++) {
                libraries_name[i] = "";
                libraries_name[i] = libraries.get(i).getLibrary_name();
            }
            librariesAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,libraries_name);
            librariesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            library_spinner.setAdapter(librariesAdapter);
            if("".equals(library_id)) {
                library_id = libraries.get(0).getLibrary_id();      /* 如果 library_id 还是空的 就用刚从服务器中获取到的第一个来给它赋值 */
                Log.e("---->>>","当 library_id 为空的时候 library_id = " + library_id);
            }
            Log.e("---->>>","这里的library_id不应该是个空值 library_id = " + library_id);
            getFloorData();          /* library_id 中有了值，就可以用它来 向服务器中获取相应的 floor_list 有座位表的区域列表 */
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getLibraryData() {
        if (!NetUtil.isNetConnected(getActivity())) {
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_LONG).show();
            return;
        }
        class  MyHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                if(!"".equals(result)) {
                    handlerLibraryJsonData(result);
                }
                super.handleMessage(msg);
            }
        }
        handler = new MyHandler();
        new Thread(new Runnable() {
            public void run() {
                getLibraries();
                Message m = handler.obtainMessage();
                handler.sendMessage(m);
            }
        }).start();
    }

    public void handlerFloorJsonData(String result) {
        Log.e("---->>>","处理从数据库获取到的Floor数据时候 library_id = " + library_id);
        Log.e("---->>>","result = " + result);
        try {
            JSONArray jsonArray = new JSONArray(result);
            floor_list = new int[jsonArray.length()];
            for(int i = 0;i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                floor_list[i] = Integer.parseInt(jsonObject.getString("flooer"));
            }
            floors_name = new String[floor_list.length];         /* 用从服务器获取到的 floors 的数据来 给floors数组赋值 */
            for(int i = 0; i < floor_list.length; i++) {
                floors_name[i] = all_floor_name[floor_list[i] - 1];
            }
            floorsAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,floors_name);
            floorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            floor_spinner.setAdapter(floorsAdapter);
            if(floor_id == 0) {
                Log.e("---->>>","floor_id = " + floor_id);
            }
            if(floor_list.length > 0) {
                Log.e("---->>>","floor_list.length = " + floor_list.length);
            }
            if(floor_id == 0 && floor_list.length > 0) {
                floor_id = floor_list[0];
            }
            if(isCreateView) {
                Log.e("---->>>","第一次初始化页面控件此时的 library_id = " + library_id + "; floor_id = " + floor_id);
                getRoomData();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getFloorData() {
        if (!NetUtil.isNetConnected(getActivity())) {
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_LONG).show();
            return;
        }
        class  MyHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                if(!"".equals(result)) {
                    handlerFloorJsonData(result);
                }
                super.handleMessage(msg);
            }
        }
        handler = new MyHandler();
        new Thread(new Runnable() {
            public void run() {
                getFloors();
                Message m = handler.obtainMessage();
                handler.sendMessage(m);
            }
        }).start();
    }

    public void handlerRoomJsonData(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            rooms = new ArrayList<>();
            Room room;
            for(int i = 0;i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                room = new Room();
                room.setId(jsonObject.getString("room_id"));
                room.setName(jsonObject.getString("room_name"));
                room.setNo(Integer.parseInt(jsonObject.getString("room_no")));
                room.setFlooer(Integer.parseInt(jsonObject.getString("flooer")));
                room.setCountx(Integer.parseInt(jsonObject.getString("countx")));
                room.setCounty(Integer.parseInt(jsonObject.getString("county")));
                rooms.add(room);
            }
            rooms_name = new String[rooms.size()];
            for(int i = 0; i < rooms.size(); i++) {
                rooms_name[i] = rooms.get(i).getName();
            }
            roomsAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,rooms_name);
            roomsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            room_spinner.setAdapter(roomsAdapter);
            if(isCreateView) {
                initView();
                isCreateView = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getRoomData() {
        if (!NetUtil.isNetConnected(getActivity())) {
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_LONG).show();
            return;
        }
        class  MyHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                if(!"".equals(result)) {
                    handlerRoomJsonData(result);
                }
                super.handleMessage(msg);
            }
        }
        handler = new MyHandler();
        new Thread(new Runnable() {
            public void run() {
                getRooms();
                Message m = handler.obtainMessage();
                handler.sendMessage(m);
            }
        }).start();
    }

    public void getSeatInfo() {      //进入座位表Activity前，先从数据库中取得其中的数据
        if ("".equals(room_id.trim())) {
            Toast.makeText(getContext(), "请选择区域！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!NetUtil.isNetConnected(getContext())) {
            Toast.makeText(getContext(), "网络不可用", Toast.LENGTH_LONG).show();
            return;
        }
        class  MyHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                if (!"".equals(result)) {
                    Intent intent = new Intent(getContext(), WeChatPayActivity.class);
                    startActivity(intent);
//                    Intent in = new Intent(getContext(), SeatInfoActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("result",result);
//                    bundle.putString("roomData",room_data);
//                    in.putExtra("seatsData",bundle);
//                    startActivity(in);
                } else {
                    Toast.makeText(getContext(), "网络出错！", Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        }
        handler = new MyHandler();
        new Thread(new Runnable() {     // 创建一个新线程，用于从网络上获取文件
            public void run() {
                getSeatTable();                 //调用send()方法，用于发送用户名、密码到Web服务器
                Message m = handler.obtainMessage();              // 获取一个Message
                handler.sendMessage(m); // 发送消息
            }
        }).start();                     // 开启线程
    }

    public void getLibraries() {
        String target = serverPath + "admin/getLibraries.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();           // 创建一个HTTP连接
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setConnectTimeout(5000);
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");      // 设置内容类型
            DataOutputStream out = new DataOutputStream( urlConn.getOutputStream());                            // 获取输出流
            //String param =  "school=" + URLEncoder.encode(room_id, "utf-8");  //连接要提交的数据
            //out.writeBytes(param);              //将要传递的数据写入数据输出流
            out.flush();                        //输出缓存
            out.close();                        //关闭数据输出流
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {   //判断是否响应成功
                InputStreamReader in = new InputStreamReader(
                        urlConn.getInputStream());                          // 获得读取的内容
                BufferedReader buffer = new BufferedReader(in);             // 获取输入流对象
                String inputLine;
                result = "";
                while ((inputLine = buffer.readLine()) != null) {           //通过循环逐行读取输入流中的内容
                    result += inputLine;
                }
                in.close();                     //关闭字符输入流
            }
            urlConn.disconnect();               //断开连接
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFloors() {
        String target = serverPath + "admin/getFloors.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();           // 创建一个HTTP连接
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setConnectTimeout(5000);
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");      // 设置内容类型
            DataOutputStream out = new DataOutputStream( urlConn.getOutputStream());                            // 获取输出流
            String param =  "library=" + URLEncoder.encode(library_id, "utf-8");  //连接要提交的数据
            out.writeBytes(param);              //将要传递的数据写入数据输出流
            out.flush();                        //输出缓存
            out.close();                        //关闭数据输出流
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {   //判断是否响应成功
                InputStreamReader in = new InputStreamReader(
                        urlConn.getInputStream());                          // 获得读取的内容
                BufferedReader buffer = new BufferedReader(in);             // 获取输入流对象
                String inputLine;
                result = "";
                while ((inputLine = buffer.readLine()) != null) {           //通过循环逐行读取输入流中的内容
                    result += inputLine;
                }
                in.close();                     //关闭字符输入流
            }
            urlConn.disconnect();               //断开连接
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getRooms() {
        String target = serverPath + "admin/getRooms.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();           // 创建一个HTTP连接
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setConnectTimeout(5000);
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");      // 设置内容类型
            DataOutputStream out = new DataOutputStream( urlConn.getOutputStream());                            // 获取输出流
            String param =  "library=" + URLEncoder.encode(library_id, "utf-8") +  //连接要提交的数据
                    "&floor=" + URLEncoder.encode(floor_id+"", "utf-8");
            out.writeBytes(param);              //将要传递的数据写入数据输出流
            out.flush();                        //输出缓存
            out.close();                        //关闭数据输出流
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {   //判断是否响应成功
                InputStreamReader in = new InputStreamReader(
                        urlConn.getInputStream());                          // 获得读取的内容
                BufferedReader buffer = new BufferedReader(in);             // 获取输入流对象
                String inputLine;
                result = "";
                while ((inputLine = buffer.readLine()) != null) {           //通过循环逐行读取输入流中的内容
                    result += inputLine;
                }
                in.close();                     //关闭字符输入流
            }
            urlConn.disconnect();               //断开连接
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getRoomInfo() {
        String target = serverPath + "admin/getRoomData.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();           // 创建一个HTTP连接
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setConnectTimeout(5000);
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");      // 设置内容类型
            DataOutputStream out = new DataOutputStream( urlConn.getOutputStream());                            // 获取输出流
            String param =  "room=" + URLEncoder.encode(room_id, "utf-8");  //连接要提交的数据
            out.writeBytes(param);              //将要传递的数据写入数据输出流
            out.flush();                        //输出缓存
            out.close();                        //关闭数据输出流
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {   //判断是否响应成功
                InputStreamReader in = new InputStreamReader(
                        urlConn.getInputStream());                          // 获得读取的内容
                BufferedReader buffer = new BufferedReader(in);             // 获取输入流对象
                String inputLine;
                room_data = "";
                while ((inputLine = buffer.readLine()) != null) {           //通过循环逐行读取输入流中的内容
                    room_data += inputLine;
                }
                in.close();                     //关闭字符输入流
            }
            urlConn.disconnect();               //断开连接
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getSeatTable() {
        String target = serverPath + "admin/getSeatsData.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();           // 创建一个HTTP连接
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setConnectTimeout(5000);
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");      // 设置内容类型
            DataOutputStream out = new DataOutputStream( urlConn.getOutputStream());                            // 获取输出流
            String param =  "room=" + URLEncoder.encode(room_id, "utf-8");  //连接要提交的数据
            out.writeBytes(param);              //将要传递的数据写入数据输出流
            out.flush();                        //输出缓存
            out.close();                        //关闭数据输出流
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {   //判断是否响应成功
                InputStreamReader in = new InputStreamReader(
                        urlConn.getInputStream());                          // 获得读取的内容
                BufferedReader buffer = new BufferedReader(in);             // 获取输入流对象
                String inputLine;
                result = "";
                while ((inputLine = buffer.readLine()) != null) {           //通过循环逐行读取输入流中的内容
                    result += inputLine;
                }
                in.close();                     //关闭字符输入流
            }
            urlConn.disconnect();               //断开连接
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}