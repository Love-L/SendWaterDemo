package com.qfdqc.views.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegisterUsernameActivity extends AppCompatActivity {
    private EditText register_username;
    private Handler handler;
    private String result = "";
    public static AppCompatActivity mActivity;
    //final String serverPath = "http://192.168.43.122:8888/";
    final String serverPath = "http://47.103.26.115:8888/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_register_username);
        register_username = (EditText) findViewById(R.id.register_username);
        Button enter_username = (Button) findViewById(R.id.enter_username);      //获取用于登录的按钮控件
        enter_username.setOnClickListener(new View.OnClickListener() {  //实现单击登录按钮，发送信息与服务器交互
            @Override
            public void onClick(View v) {
                String username = register_username.getText().toString();
                if ("".equals(username.trim())) {
                    Toast.makeText(RegisterUsernameActivity.this, "请填写手机号码！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!username.replaceAll("[^\\d]+", "").trim().equals(username) || username.length() != 11) {
                    Toast.makeText(RegisterUsernameActivity.this, "请填正确写手机号码！", Toast.LENGTH_SHORT).show();
                    return;                                             //狠狠的手机号码数据验证
                }
                if (!NetUtil.isNetConnected(RegisterUsernameActivity.this)) {
                    Toast.makeText(RegisterUsernameActivity.this, "网络不可用", Toast.LENGTH_LONG).show();
                    return;
                }
                class  MyHandler extends Handler {      //如果服务器返回值为“true”证明用户名密码正确并跳转登录后界面否则给出相应的提示信息
                    @Override
                    public void handleMessage(Message msg) {
                        if ("false".equals(result)) {
                            Intent in = new Intent(RegisterUsernameActivity.this, RegisterPasswordActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("register_username",register_username.getText().toString().trim());
                            in.putExtra("register_data",bundle);
                            startActivity(in);
                        } else if("true".equals(result)) {
                            Toast.makeText(RegisterUsernameActivity.this, "此用户已经存在", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterUsernameActivity.this, "网络有问题或输入有误", Toast.LENGTH_SHORT).show();//用户名、密码错误的提示信息
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

    public void send() {
        String target = serverPath + "admin/isExist.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();                               // 创建一个HTTP连接
            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向
            urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");                     // 设置内容类型
            DataOutputStream out = new DataOutputStream( urlConn.getOutputStream());                            // 获取输出流
            String param =  "username=" + URLEncoder.encode(register_username.getText().toString(), "utf-8");  //连接要提交的数据
            out.writeBytes(param);              //将要传递的数据写入数据输出流
            out.flush();                        //输出缓存
            out.close();                        //关闭数据输出流
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {   //判断是否响应成功
                InputStreamReader in = new InputStreamReader(urlConn.getInputStream());                          // 获得读取的内容
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