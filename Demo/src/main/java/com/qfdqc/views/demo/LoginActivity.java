package com.qfdqc.views.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qfdqc.views.seattable.User;

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

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private EditText edit_Username; // 定义一个输入用户名的编辑框组件
    private EditText edit_Password; // 定义一个输入密码的编辑框组件
    private Handler handler;        // 定义一个android.os.Handler对象
    private String result = "";     // 定义一个代表显示内容的字符串
    private long exitTime = 0;
    ProgressBar mProgressBar;
    private User session_user;
    //final String serverPath = "http://192.168.43.122:8888/";
    final String serverPath = "http://47.103.26.115:8888/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences("state",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        edit_Username = (EditText) findViewById(R.id.username);     //获取用于输入用户名的编辑框组件
        edit_Password = (EditText)findViewById(R.id.password);      //获取用于输入密码的编辑框组件
        edit_Username.setText(sharedPreferences.getString("userName",""));
        edit_Password.setText(sharedPreferences.getString("userPassword",""));
        attemptGetUser();       //第一次隐性登录

        TextView text_forget = (TextView) findViewById(R.id.forget_password);   //忘记密码
        TextView text_register = (TextView) findViewById(R.id.to_register);     //注册账号
        Button btn_Login = (Button) findViewById(R.id.login);       //获取用于登录的按钮控件
        text_forget.setOnClickListener(new View.OnClickListener() { //忘记密码 Activity 还没开发
            @Override
            public void onClick(View view) {

            }
        });
        text_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterUsernameActivity.class));//跳转登录后界面
            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        btn_Login.setOnClickListener(new View.OnClickListener() {  //实现单击登录按钮，发送信息与服务器交互
            @Override
            public void onClick(View v) {
                if ("".equals(edit_Username.getText().toString().trim()) || "".equals(edit_Password.getText().toString().trim())) {
                    Toast.makeText(LoginActivity.this, "请填写用户名或密码！", Toast.LENGTH_SHORT).show();//当用户名、密码为空时给出相应提示
                    return;
                }
                if (!NetUtil.isNetConnected(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "网络不可用", Toast.LENGTH_LONG).show();
                    return;
                }
                if(session_user != null) {          /*点击时进行判断，看看是否需要再次登录/////////////////////////////*/
                    if(session_user.getUsername().equals(edit_Username.getText().toString().trim())
                    && session_user.getPassword().equals(edit_Password.getText().toString().trim())) {
                        Intent in = new Intent(LoginActivity.this, ContentActivity.class);//跳转登录后界面
                        Bundle bundle = userBundle();
                        in.putExtra("session_user",bundle);
                        startActivity(in);
                        finish();
                        return;
                    }
                }
                mProgressBar.setVisibility(View.VISIBLE);   //显示等待动画
                class  MyHandler extends Handler {      //如果服务器返回值不为空证明用户名密码正确并跳转登录后界面否则给出相应的提示信息
                    @Override
                    public void handleMessage(Message msg) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        if (!"".equals(result)) {                    //如果服务器返回值为“admin”，证明用户名、密码输入正确
                            initSessionUser(result);
                            Bundle bundle = userBundle();
                            Intent in = new Intent(LoginActivity.this, ContentActivity.class);//跳转登录后界面
                            in.putExtra("session_user",bundle);
                            startActivity(in);
                            Log.d("--->>>","二次登录");
                            editor.putString("userName",edit_Username.getText().toString());
                            editor.putString("userPassword",edit_Password.getText().toString());
                            editor.apply();
                            finish();                                //为了登录之后不返回登录页面，所以就直接 onDestroy()
                        } else {
                            Toast.makeText(LoginActivity.this, "请填写正确的用户名和密码！", Toast.LENGTH_SHORT).show();//用户名、密码错误的提示信息
                        }
                        super.handleMessage(msg);
                    }
                }
                handler = new MyHandler();
                new Thread(new Runnable() {     // 创建一个新线程，用于从网络上获取文件
                    public void run() {
                        send();                 //调用send()方法，用于发送用户名、密码到Web服务器
                        Message m = handler.obtainMessage();              // 获取一个Message
                        handler.sendMessage(m); // 发送消息
                    }
                }).start();                     // 开启线程
            }
        });
    }

    public void initSessionUser(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            session_user = new User();
            for(int i = 0;i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                session_user.setId(jsonObject.getString("id"));
                session_user.setUsername(jsonObject.getString("username"));
                session_user.setPassword(jsonObject.getString("PASSWORD"));
                session_user.setName(jsonObject.getString("NAME"));
                session_user.setPhone(jsonObject.getString("phone"));
                session_user.setSex(jsonObject.getString("sex"));
                session_user.setImage(jsonObject.getString("image"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bundle userBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("id",session_user.getId());
        bundle.putString("username",session_user.getUsername());
        bundle.putString("password",session_user.getPassword());
        bundle.putString("name",session_user.getName());
        bundle.putString("phone",session_user.getPhone());
        bundle.putString("sex",session_user.getSex());
        bundle.putString("image",session_user.getImage());
        return bundle;
    }

    public void attemptGetUser() {      //没有用户名的信息或没有网络都会直接结束此方法
        if("".equals(edit_Username.getText().toString().trim())) {
            return;
        }
        if (!NetUtil.isNetConnected(LoginActivity.this)) {
            return;
        }
        class  MyHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                if (!"".equals(result)) {
                    initSessionUser(result);
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
        String target = serverPath + "admin/isLogin.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();                               // 创建一个HTTP连接
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setConnectTimeout(5000);
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");                     // 设置内容类型
            DataOutputStream out = new DataOutputStream( urlConn.getOutputStream());                            // 获取输出流
            String param =  "username=" + URLEncoder.encode(edit_Username.getText().toString(), "utf-8") +
                    "&password=" + URLEncoder.encode(edit_Password.getText().toString(), "utf-8");  //连接要提交的数据
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if((System.currentTimeMillis() - exitTime) >2000) {
            Toast.makeText(LoginActivity.this,"再按一次退出程序", Toast.LENGTH_LONG).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor.putString("userName",edit_Username.getText().toString());
        editor.putString("userPassword",edit_Password.getText().toString());
        editor.commit();
        Log.d("--->>>","onStop()");
    }

    @Override
    protected void onPause() {
        Log.d("--->>>","onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("--->>>","onDestroy()");
        super.onDestroy();
    }
}