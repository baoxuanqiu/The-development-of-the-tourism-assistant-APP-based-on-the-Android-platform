package com.example.diplomadesign;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.diplomadesign.account.Account;
import com.mob.MobSDK;

import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends AppCompatActivity {
    private LinearLayout returnLinear;
    private Button getPhoneCode;
    private EditText telephone;
    private EditText phonecode;//手机验证码
    private EditText name;
    private Button register;//注册
    private CheckBox provision;//协议

    //关于短信验证码的
    private EventHandler eventHandler;
    private static boolean phone_Code_right=false;//用户输入的正确的验证码
    private boolean flag_1=true;
    Thread thread=null;
    private boolean flag=true;
    private int i=60;
    private int flag_2=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏标题栏
        }
        returnLinear=(LinearLayout) findViewById(R.id.return_linear);
        getPhoneCode=(Button)findViewById(R.id.get_phone_code);
        telephone=(EditText)findViewById(R.id.telephone_text);
        name=(EditText)findViewById(R.id.accountname_text);
        register=(Button)findViewById(R.id.register);
        provision=(CheckBox) findViewById(R.id.provision);
        phonecode=(EditText)findViewById(R.id.phone_code_text);

        returnLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_1=new Intent(LoginActivity.this,MyActivity.class);
                startActivity(intent_1);
            }
        });

        telephone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String inputStr = telephone.getText().toString();
                if (isTelphoneValid(inputStr)){
                    telephone.setError(null);
                    telephone.setSelection(inputStr.length());//将光标移至文字末尾
                }else {
                    telephone.setError("手机号码输入不正确");
                    telephone.setSelection(inputStr.length());//将光标移至文字末尾

                }
            }
        });

        CompoundButton.OnCheckedChangeListener myListener=new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    final AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("服务协议和隐私政策");
                    builder.setMessage("请你务必审慎阅读、充分理解“服务协议和隐私政策”各条款，包括不限于：为了向你提供即时通讯" +
                            "、内容分享等服务，我们需要收集你的设备信息、操作日志等个人信息。如你同意，请点击“同意”开始接受我们的服务");
                    builder.setCancelable(false);
                    builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            builder.setCancelable(true);
                        }
                    });
                    builder.setNegativeButton("暂不使用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            provision.setChecked(false);
                        }
                    });
                    builder.show();
                }
            }
        };
        provision.setOnCheckedChangeListener(myListener);

        eventHandler=new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                // TODO 此处不可直接处理UI线程，处理后续操作需传到主线程中操作
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        //注册一个事件回调监听，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eventHandler);
        MobSDK.submitPolicyGrantResult(true, null);
//        getPhoneCode.setClickable(true);
        getPhoneCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gtelephone=telephone.getText().toString();
                if (isTelphoneValid(gtelephone)){
                    SMSSDK.getVerificationCode("86",gtelephone);
                    flag=true;
                    phonecode.requestFocus();
                }else {
                    Toast.makeText(LoginActivity.this, "手机号码输入错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aname=name.getText().toString();
                String atelephone=telephone.getText().toString();
                String aphoneCode=phonecode.getText().toString();
                if(!provision.isChecked()){
                    Toast.makeText(LoginActivity.this,"请先接受服务协议与隐私政策",Toast.LENGTH_SHORT).show();
                }else if (aname.length()==0){
                    Toast.makeText(LoginActivity.this,"请输入昵称",Toast.LENGTH_SHORT).show();
                } else if (aphoneCode.length()!=6){
                    Toast.makeText(LoginActivity.this,"验证码输入错误",Toast.LENGTH_LONG).show();
                    phonecode.requestFocus();
                } else{
                    SMSSDK.submitVerificationCode("86",atelephone,aphoneCode);
                    flag=false;
                }
            }
        });
    }

    private boolean initAccount(String name,String telephone){
        Account account=new Account();
        account.setName(name);
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.drawable.head);
        account.setHeadshot(img(bmp));//刚开始注册成功设置默认头像

        List<Account> accounts=LitePal.findAll(Account.class);
        if (accounts.isEmpty()){
            account.setTelephone(telephone);
            account.save();
            return true;
        }else {
            List<Account> accounts_1=LitePal.select("telephone")
                    .where("telephone=?",telephone)
                    .find(Account.class);
            if (accounts_1.size()==0){
                account.setTelephone(telephone);
                account.save();
                return true;
            }else {
                return false;
            }
        }
    }


    //将Bitmap图片转化为字节
    private byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    //校验手机号码输入是否正确
    private boolean isTelphoneValid(String account) {
        if (account == null) {
            return false;
        }
        // 首位为1, 第二位为3-9, 剩下九位为 0-9, 共11位数字
        String pattern = "^[1]([3-9])[0-9]{9}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(account);
        return m.matches();
    }

    /**
     * 使用Handler来分发Message对象到主线程中，处理事件
     */
    Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event=msg.arg1;
            int result=msg.arg2;
            Object data=msg.obj;
            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                changeBtnGetCode();
                if(result == SMSSDK.RESULT_COMPLETE) {
                    boolean smart = (Boolean)data;
                    if(smart) {
                        Toast.makeText(getApplicationContext(),"该手机号已经注册过，请重新输入",
                                Toast.LENGTH_LONG).show();
                        telephone.requestFocus();
                        return;
                    }
                }
            }
            if(result==SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    String aname = name.getText().toString();
                    String atelephone = telephone.getText().toString();
                    boolean isCreate = initAccount(aname, atelephone);
                    if (isCreate) {
                        Intent intent_2 = new Intent(LoginActivity.this, CreateSuccessActivity.class);
                        startActivity(intent_2);
                        Intent intent_3 = new Intent("com.example.broadcasttest.GET_ACCOUNT_TELE");
                        intent_3.setPackage(getPackageName());
                        intent_3.putExtra("AccountTele", atelephone);
                        sendBroadcast(intent_3, null);
                    } else {
                        Toast.makeText(LoginActivity.this, "该手机号已经创建了账户", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getApplicationContext(), "验证码输入正确",
                            Toast.LENGTH_LONG).show();
                }
            }else {
                if (flag) {
                    Toast.makeText(getApplicationContext(), "验证码获取失败请重新获取", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "验证码输入错误哦", Toast.LENGTH_LONG).show();
                }
            }
        }

    };
    class LocalReceiver extends BroadcastReceiver{
        private String t="false_1";
        @Override
        public void onReceive(Context context, Intent intent) {
            this.t=intent.getStringExtra("isCodeValid");
        }
        public String getT() {
            return t;
        }
    }

    // 使用完EventHandler需注销，否则可能出现内存泄漏
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    //若已经获取短信验证码，则button显示倒数计时；若未获得或获得已结束，则button显示获取
    private void changeBtnGetCode() {
        thread = new Thread() {       //创建线程
            @Override
            public void run() {  //业务逻辑
                if (flag_1) {  //flag_1为true，表示可以点击获取验证码。flag_1为false，表示已发送验证码，点击无效
                    flag_1=false;
                    while (i > 0) {  //i一开始为60，即从60倒数计时
                        i--;
                        if (LoginActivity.this == null) {
                            break;
                        }
                        LoginActivity.this.runOnUiThread(new Runnable() { //把更新ui的代码创建在Runnable中
                                    @Override
                                    public void run() {
                                        getPhoneCode.setText("获取验证码(" + i + ")");
                                        getPhoneCode.setClickable(false);//点击无效
                                    }
                                });
                        try {
                            Thread.sleep(1000);//睡眠1000毫秒
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
                i = 60;
                flag_1 = true;
                if (LoginActivity.this != null) {
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getPhoneCode.setText("获取验证码");
                            getPhoneCode.setClickable(true);//点击有效
                        }
                    });
                }
            };
        };
        thread.start();
        phone_Code_right=false;
    }

}
