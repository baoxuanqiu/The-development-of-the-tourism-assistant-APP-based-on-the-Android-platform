package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.diplomadesign.account.GetAccountInfo;

public class AccountInfoActivity extends AppCompatActivity {
    private TextView my_name;
    private TextView my_tele;
    private ImageView my_image;
    private LinearLayout back_linear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }

        my_name=(TextView)findViewById(R.id.user_name);
        my_tele=(TextView)findViewById(R.id.user_tel);
        my_image=(ImageView)findViewById(R.id.user_head_image);
        GetAccountInfo getAccountInfo=new GetAccountInfo();
        String real_name=getAccountInfo.getAccount_name();
        my_name.setText(real_name);
        String real_tele=getAccountInfo.getAccount_tele();
        my_tele.setText(real_tele);
        Bitmap real_image=getAccountInfo.getAccount_image();
        my_image.setImageBitmap(real_image);
        back_linear=(LinearLayout)findViewById(R.id.return_linear);
        back_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AccountInfoActivity.this,MyActivity.class);
                startActivity(intent);
            }
        });
    }
}
