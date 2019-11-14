package com.qfdqc.views.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyPasswordActivity extends AppCompatActivity implements TextWatcher {

    String old_password = "";
    EditText edit_old_password;
    EditText edit_new_password;
    EditText edit_new_password_again;
    Button modify_button;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("session_user_password");
        old_password = bundle.getString("password");

        edit_old_password = (EditText) findViewById(R.id.old_password);
        edit_new_password = (EditText) findViewById(R.id.new_password);
        edit_new_password_again = (EditText) findViewById(R.id.new_password_again);
        modify_button = (Button) findViewById(R.id.modify_password);

        edit_old_password.addTextChangedListener(this);
        edit_new_password.addTextChangedListener(this);
        edit_new_password_again.addTextChangedListener(this);

        modify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edit_new_password.getText().toString().trim().equals(edit_new_password_again.getText().toString().trim())) {
                    Toast.makeText(ModifyPasswordActivity.this,"两次输入的密码不一致",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!old_password.equals(edit_old_password.getText().toString().trim())) {
                    Toast.makeText(ModifyPasswordActivity.this,"原密码不正确",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putString("newPassword",edit_new_password.getText().toString().trim());
                intent.putExtras(bundle);
                setResult(103,intent);
                finish();
            }
        });
        modify_button.setClickable(false);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if("".equals(charSequence.toString().trim())
                || "".equals(edit_old_password.getText().toString().trim())
                || "".equals(edit_new_password.getText().toString().trim())
                || "".equals(edit_new_password_again.getText().toString().trim())) {
            modify_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_bg_gray));
            modify_button.setClickable(false);
        } else {
            modify_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_bg));
            modify_button.setClickable(true);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

}