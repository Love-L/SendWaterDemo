package com.qfdqc.views.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.qfdqc.views.seattable.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class MineFragment extends Fragment {

    ListView listView;
    private static final String[] userInfoList = {
            "用户名",
            "登录密码",
            "姓名",
            "性别",
            "手机号码"
    };
    private List<Map<String, Object>> list = new ArrayList<>();

    String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/seat/cache/image/";
    protected static final int SUCCESS = 1;   //成功获取到图片的状态码
    protected static final int ERROR = 2;     //失败的状态码
    private Handler handler;
    private ImageView user_image;
    private String imageUriPath;
    private String result;
    //final String serverPath = "http://192.168.43.122:8888/";
    final String serverPath = "http://47.103.26.115:8888/";

    AlertDialog modifyNameDialog;
    private int singleChecked = 0;
    final String[] items = new String[]{"男", "女"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        user_image = (ImageView) view.findViewById(R.id.user_image);
        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  //调用系统相册, 开始上传图片
                intent.setType("image/*");
                startActivityForResult(intent, 101);
            }
        });
        File imageFile = new File(dir + ContentActivity.session_user.getImage());
        if (imageFile.exists()) {
            user_image.setImageURI(Uri.fromFile(new File(dir + ContentActivity.session_user.getImage())));      //本地有图片就读取本地的图片
        } else {
            getUserImage();     //本地没有就从网络读取
        }
        String[] userInfoValue = {
                ContentActivity.session_user.getUsername(),
                "修改",
                ContentActivity.session_user.getName(),
                ContentActivity.session_user.getSex(),
                ContentActivity.session_user.getPhone()
        };
        String[] userInfoArrow = {"  ", " >", " >", " >", " >"};
        listView = (ListView) view.findViewById(R.id.listView_user_info);
        for (int i = 0; i < userInfoList.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", userInfoList[i]);
            map.put("value", userInfoValue[i]);
            map.put("arrow", userInfoArrow[i]);
            list.add(map);
        }
        ListAdapter listAdapter = new SimpleAdapter(
                getActivity(),
                list,
                R.layout.listview_user_info_item,
                new String[]{"key", "value", "arrow"},
                new int[]{R.id.listView_user_info_item_key, R.id.listView_user_info_value, R.id.listView_user_info_arrow}
        );
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        startModifyPasswordActivity();
                        break;
                    case 2:
                        openModifyNameDialog();
                        break;
                    case 3:
                        openSelectSexDialog();
                        break;
                    case 4:
                        startModifyPhoneActivity();
                        break;
                }
            }
        });
        singleChecked = ContentActivity.session_user.getSex().equals("男")?0:1;
        return view;
    }

    public void getUserImage() {
        class MyHandler extends Handler {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == SUCCESS) {
                    Bitmap bitmap = (Bitmap) msg.obj;
                    user_image.setImageBitmap(bitmap);
                } else if (msg.what == ERROR) {
                    Toast.makeText(getActivity(), "显示图片错误", Toast.LENGTH_SHORT).show();
                }
            }
        }
        handler = new MyHandler();
        final String path = serverPath + "/image/" + ContentActivity.session_user.getImage();
        new Thread() {
            private HttpURLConnection conn;
            private Bitmap bitmap;

            public void run() {
                try {
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setUseCaches(true);
                    conn.setConnectTimeout(5000);
                    int state = conn.getResponseCode();
                    if (state == 200) {                             //请求网络成功，获取输入流
                        InputStream in = conn.getInputStream();   //将流转换为Bitmap对象
                        bitmap = BitmapFactory.decodeStream(in);//告诉消息处理器显示图片
                        Message msg = new Message();
                        msg.what = SUCCESS;
                        msg.obj = bitmap;
                        saveUserImage(bitmap);                  //把图片保存到本地
                        handler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = ERROR;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = ERROR;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    public void saveUserImage(Bitmap mBitmap) {
        String state = Environment.getExternalStorageState();   //获取内部存储状态  
        if (!state.equals(Environment.MEDIA_MOUNTED)) {         // 如果状态不是已挂载 mounted, 则无法读写
            return;
        }
        try {
            File file = new File(dir + ContentActivity.session_user.getImage());
            FileOutputStream out = new FileOutputStream(file);                        //需要读写权限
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DeleteCutOutImage() {
        File file = new File(imageUriPath);
        if (file.isFile() && file.exists()) {
            if (file.delete()) {
                Log.e("--->>>", "MineFragment.java--->>>DeleteCutOutImage()--->>>file.delete()--->>>delete successfully");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {          //选择图片成功, 开始调用系统的图片裁剪
                Uri uri = data.getData();
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(uri, "image/*");         //设置要缩放的图片Uri和类型
                intent.putExtra("aspectX", 1024);       //宽度比
                intent.putExtra("aspectY", 1024);       //高度比
                intent.putExtra("outputX", 1024);       //输出图片的宽度
                intent.putExtra("outputY", 1024);       //输出图片的高度
                intent.putExtra("scale", true);         //缩放
                intent.putExtra("return-data", false);  //当为true的时候就返回缩略图, false 就不返回，需要通过Uri
                intent.putExtra("noFaceDetection", false);//前置摄像头
                startActivityForResult(intent, 102);    //打开剪裁Activity
            }
        }
        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                user_image.setImageURI(uri);
                imageUriPath = uri.getPath();
                Upload upload = new Upload(new File(uri.getPath()));
                upload.execute(serverPath + "admin/uploadImage.do");
                /* 不可以在此次就调用 updateUserInfo()方法,因为异步请求的问题,只能把 updateUserInfo()方法的调用放到上传头像完成之后,以确保线性同步 */
            }
        }
        if(requestCode == 103 && resultCode == 103) {
            Bundle bundle = data.getExtras();
            String newPassword = bundle.getString("newPassword");
            Log.e("--->>>","newPassword= = " + newPassword);
            ContentActivity.session_user.setPassword(newPassword);
            updateUserInfo();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new MineFragment()).commit();
            Toast.makeText(getActivity(),"密码修改成功",Toast.LENGTH_SHORT).show();
        }
        if(requestCode == 104 && resultCode == 104) {
            Bundle bundle = data.getExtras();
            String newPhone = bundle.getString("newPhone");
            Log.e("--->>>","newPhone = " + newPhone);
            ContentActivity.session_user.setPhone(newPhone);
            updateUserInfo();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new MineFragment()).commit();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class Upload extends AsyncTask<String, Void, String> {
        File file;

        public Upload(File file) {
            this.file = file;
        }

        @Override
        protected String doInBackground(String... strings) {
            return UploadImg.uploadFile(file, strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                Toast.makeText(getActivity().getBaseContext(), "更改成功", Toast.LENGTH_SHORT).show();
                updateUserInfo();
                DeleteCutOutImage();
            }
        }
    }

    public void updateUserInfo() {
        if (!NetUtil.isNetConnected(getActivity())) {
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_LONG).show();
            return;
        }
        class MyHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                if ("false".equals(result)) {
                    Toast.makeText(getActivity(), "更改失败！", Toast.LENGTH_SHORT).show();
                } else {
                    UpdateSessionUser(result);
                }
                super.handleMessage(msg);
            }
        }
        handler = new MyHandler();
        new Thread(new Runnable() {
            public void run() {
                send();
                Message m = handler.obtainMessage();
                handler.sendMessage(m);
            }
        }).start();
    }

    public void send() {
        String target = serverPath + "admin/updateUserInfo.do";
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setConnectTimeout(5000);
            urlConn.setInstanceFollowRedirects(true); //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");     // 设置内容类型
            DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());                            // 获取输出流
            String param = "id=" + URLEncoder.encode(ContentActivity.session_user.getId(), "utf-8") +
                    "&username=" + URLEncoder.encode(ContentActivity.session_user.getUsername(), "utf-8") +
                    "&password=" + URLEncoder.encode(ContentActivity.session_user.getPassword(), "utf-8") +
                    "&name=" + URLEncoder.encode(ContentActivity.session_user.getName(), "utf-8") +
                    "&phone=" + URLEncoder.encode(ContentActivity.session_user.getPhone(), "utf-8") +
                    "&sex=" + URLEncoder.encode(ContentActivity.session_user.getSex(), "utf-8") +
                    "&image=" + URLEncoder.encode(ContentActivity.session_user.getImage(), "utf-8");  //连接要提交的数据
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

    public void UpdateSessionUser(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ContentActivity.session_user = new User();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentActivity.session_user.setId(jsonObject.getString("id"));
                ContentActivity.session_user.setUsername(jsonObject.getString("username"));
                ContentActivity.session_user.setPassword(jsonObject.getString("PASSWORD"));
                ContentActivity.session_user.setName(jsonObject.getString("NAME"));
                ContentActivity.session_user.setPhone(jsonObject.getString("phone"));
                ContentActivity.session_user.setSex(jsonObject.getString("sex"));
                ContentActivity.session_user.setImage(jsonObject.getString("image"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startModifyPasswordActivity() {
        Intent intent = new Intent(getActivity(),ModifyPasswordActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("password",ContentActivity.session_user.getPassword());
        intent.putExtra("session_user_password",bundle);
        startActivityForResult(intent, 103);
    }

    public void openModifyNameDialog() {
        View layout = View.inflate(getContext(),R.layout.modify_user_name_dialog,null);
        modifyNameDialog = new AlertDialog.Builder(getContext()).create();
        modifyNameDialog.setView(layout);
        modifyNameDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText updateName = (EditText) modifyNameDialog.findViewById(R.id.dialog_name);
                String name = updateName.getText().toString();
                if(!"".equals(name)) {
                    ContentActivity.session_user.setName(name);
                    updateUserInfo();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new MineFragment()).commit();
                }
            }
        });
        modifyNameDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        modifyNameDialog.show();
    }

    public void openSelectSexDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择性别");
        builder.setSingleChoiceItems(
                items,
                singleChecked,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        singleChecked = which;
                    }
                }
        );
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentActivity.session_user.setSex(items[singleChecked]);
                updateUserInfo();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new MineFragment()).commit();
            }
        });
        builder.show();
    }

    public void startModifyPhoneActivity() {

        Intent intent = new Intent(getActivity(),ModifyPhoneActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("phone",ContentActivity.session_user.getPhone());
        intent.putExtra("session_user_phone",bundle);
        startActivityForResult(intent, 104);
    }

}