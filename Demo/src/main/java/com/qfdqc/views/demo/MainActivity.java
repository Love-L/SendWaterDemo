package com.qfdqc.views.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Timer timer;                //计时器
    private TextView textView;  //显示倒数秒数的文本框控件
    private int i = 5;          //设置倒数的最大秒数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tv1);     //获取显示倒数秒数的文本框控件
        timer = new Timer();                              //初始化计时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();          //初始化message
                message.what = 1;
                if (i >= 0) {
                    handler.sendMessage(message);         //发送消息给handler
                }
                if (i == 0) {
                    cancel();                             //清除计时器
                }
            }
        },0, 1000);                         //每个1秒执行一次该方法
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();                           //点击跳过时，要把计时器取消，不然它依然在执行，执行完之后还是会打开LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {                                //消息为1
                i--;                                            //秒数倒数-1
                String txt = "跳过(" + i + ")";
                textView.setText(txt);                          //将秒数显示在右上角文本框中
                if (i == 0) {                                   //秒数为零时跳转
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);                      //启动跳转
                    finish();
                }
            }
            return false;
        }
    });

    public boolean onKeyDown(int keyCode, KeyEvent event) {        //不执行父类点击事件
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);         //继续执行父类其他点击事件
    }
}