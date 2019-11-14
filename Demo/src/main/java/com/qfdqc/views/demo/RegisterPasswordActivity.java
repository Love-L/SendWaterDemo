package com.qfdqc.views.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterPasswordActivity extends AppCompatActivity {
    private String register_username = "";
    private EditText register_password; // 定义一个输入用户名的编辑框组件
    private EditText register_enter_password; // 定义一个输入密码的编辑框组件
    private Handler handler;            // 定义一个android.os.Handler对象
    private String result = "";         // 定义一个代表显示内容的字符串
    //final String serverPath = "http://192.168.43.122:8888/";
    final String serverPath = "http://47.103.26.115:8888/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_password);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("register_data");
        register_username = bundle.getString("register_username");

        TextView show_username = (TextView) findViewById(R.id.show_register_number);
        show_username.setText(register_username);

        register_password = (EditText) findViewById(R.id.register_password);
        register_enter_password = (EditText) findViewById(R.id.register_enter_password);

        Button btn_Login = (Button) findViewById(R.id.register);
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(register_password.getText().toString().trim())) {
                    Toast.makeText(RegisterPasswordActivity.this, "请填写密码！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(register_password.getText().toString().trim()).equals(register_enter_password.getText().toString().trim())) {
                    Toast.makeText(RegisterPasswordActivity.this, "确认密码失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!NetUtil.isNetConnected(RegisterPasswordActivity.this)) {
                    Toast.makeText(RegisterPasswordActivity.this, "网络不可用", Toast.LENGTH_LONG).show();
                    return;
                }
                class  MyHandler extends Handler {
                    @Override
                    public void handleMessage(Message msg) {
                        if ("true".equals(result)) {
                            Toast.makeText(RegisterPasswordActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterPasswordActivity.this, LoginActivity.class);
                            RegisterUsernameActivity.mActivity.finish();
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(RegisterPasswordActivity.this, "请填写正确密码！", Toast.LENGTH_SHORT).show();
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
        });
    }

    public void send() {
        String target = serverPath + "admin/isRegister.do";    //要提交的服务器地址
        URL url;
        try {
            url = new URL(target);              //创建URL对象
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();                               // 创建一个HTTP连接

            urlConn.setRequestMethod("POST");   // 指定使用POST请求方式
            urlConn.setDoInput(true);           // 向连接中写入数据
            urlConn.setDoOutput(true);          // 从连接中读取数据
            urlConn.setUseCaches(false);        // 禁止缓存
            urlConn.setInstanceFollowRedirects(true);                       //自动执行HTTP重定向

            urlConn.setRequestProperty("Connection","Keep-Alive");
            urlConn.setRequestProperty("Charset","UTF-8");
            urlConn.setRequestProperty("Content-Type","application/json;charset=UTF-8");// 设置文件类型:
            // 设置接收类型, 否则返回415错误
            //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
            urlConn.setRequestProperty("accept","application/json");

            JSONObject JsonObj = new JSONObject();
            JsonObj.put("username",register_username);
            JsonObj.put("password",register_password.getText().toString());
            String JsonStr = JsonObj.toString();
            if (!TextUtils.isEmpty(JsonStr)) {
                byte[] writeBytes = JsonStr.getBytes();
                urlConn.setRequestProperty("Content-Length", String.valueOf(writeBytes.length));
                OutputStream out = urlConn.getOutputStream();
                out.write(JsonStr.getBytes());
                out.flush();
                out.close();
            }

            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {   //判断是否响应成功
                InputStreamReader in = new InputStreamReader(urlConn.getInputStream());// 获得读取的内容(urlConn的getInputStream()方法必须在setRequestProperty()方法之后)
                BufferedReader buffer = new BufferedReader(in);             // 获取输入流对象
                String inputLine;
                result = "";
                while ((inputLine = buffer.readLine()) != null) {           //通过循环逐行读取输入流中的内容
                    result += inputLine;
                }
                in.close();                     //关闭字符输入流
            }
            urlConn.disconnect();               //断开连接
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}