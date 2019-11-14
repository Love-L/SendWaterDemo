package com.qfdqc.views.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyPhoneActivity extends AppCompatActivity  {

    String old_phone = "";
    TextView tv_old_phone;
    TextView tv_new_phone;
    EditText edit_old_phone;
    EditText edit_new_phone;
    Button submit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_phone);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("session_user_phone");
        old_phone = bundle.getString("phone");

        tv_old_phone = (TextView) findViewById(R.id.tv_old_phone);
        tv_new_phone = (TextView) findViewById(R.id.tv_new_phone);
        edit_old_phone = (EditText) findViewById(R.id.old_phone);
        edit_new_phone = (EditText) findViewById(R.id.new_phone);
        submit_button = (Button) findViewById(R.id.modify_password);

        edit_old_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    tv_old_phone.setText("原手机号");
                    edit_old_phone.setHint("");
                } else {
                    if("".equals(edit_old_phone.getText().toString().trim())) {
                        tv_old_phone.setText(" ");
                    }
                    edit_old_phone.setHint("原手机号");
                }
            }
        });
        edit_new_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    tv_new_phone.setText("新手机号");
                    edit_new_phone.setHint("");
                } else {
                    if("".equals(edit_new_phone.getText().toString().trim())) {
                        tv_new_phone.setText(" ");
                    }
                    edit_new_phone.setHint("新手机号");
                }
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String old_phone_value = edit_old_phone.getText().toString();
                String new_phone_value = edit_new_phone.getText().toString();
                if ("".equals(old_phone_value.trim())) {
                    Toast.makeText(ModifyPhoneActivity.this, "请填写手机号码！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!old_phone_value.replaceAll("[^\\d]+", "").trim().equals(old_phone_value) || old_phone_value.length() != 11) {
                    Toast.makeText(ModifyPhoneActivity.this, "请填正确写手机号码！", Toast.LENGTH_SHORT).show();
                    return;                                             //狠狠的手机号码数据验证
                }
                if (!new_phone_value.replaceAll("[^\\d]+", "").trim().equals(new_phone_value) || new_phone_value.length() != 11) {
                    Toast.makeText(ModifyPhoneActivity.this, "请填正确写手机号码！", Toast.LENGTH_SHORT).show();
                    return;                                             //狠狠的手机号码数据验证
                }


                if(!old_phone.equals(edit_old_phone.getText().toString().trim())) {
                    Toast.makeText(ModifyPhoneActivity.this,"原手机号匹配",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putString("newPhone",edit_new_phone.getText().toString().trim());
                intent.putExtras(bundle);
                setResult(104,intent);
                finish();
            }
        });
    }

}