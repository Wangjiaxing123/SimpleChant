package com.newtrekwang.simplechant;

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

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity>>>>>>>>>>";
    Button btn_connect,btn_send;
    EditText editText_ip,editText_message;
    TextView textView_message;
    private boolean isConnected=false;
    private Socket socket=null;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
           switch (msg.what){
               case 100:
                   String mess= (String) msg.obj;
                   textView_message.append(mess+"\n");
                   break;
               case 101:
                   String sendMsg= (String) msg.obj;
                   textView_message.append("已发送："+sendMsg+"\n");
                   editText_message.setText("");
                   break;
               case 103:
                   editText_ip.setFocusable(true);
                   btn_connect.setClickable(true);
                   Toast.makeText(MainActivity.this, "连接异常！", Toast.LENGTH_SHORT).show();
                   break;
               case 104:
                   Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_SHORT).show();
                   editText_ip.setFocusable(false);
                   btn_connect.setClickable(false);
                   break;
               case 105:
                   editText_ip.setFocusable(true);
                   btn_connect.setClickable(true);
                   Toast.makeText(MainActivity.this, "连接超时！", Toast.LENGTH_SHORT).show();
                   break;
               default:
                   break;
           }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_connect= (Button) findViewById(R.id.btn_connect);
        btn_send= (Button) findViewById(R.id.btn_send);
        textView_message= (TextView) findViewById(R.id.messageTextView);

        editText_ip= (EditText) findViewById(R.id.et_ip);
        editText_message= (EditText) findViewById(R.id.et_messge);

        btn_send.setOnClickListener(this);
        btn_connect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_connect:
                String ip=editText_ip.getText().toString();
                if (!TextUtils.isEmpty(ip)){
                    new ConnectThread(ip).start();
                }
                break;
            case R.id.btn_send:
                if (isConnected&&socket!=null){
                    String message=editText_message.getText().toString();
                    if (!TextUtils.isEmpty(message)){
                       write(message);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void write( String str){
        final String messageStr=str;

        new Thread(){
            @Override
            public void run() {
                try {
                    OutputStream outputStream=socket.getOutputStream();
                    outputStream.write(messageStr.getBytes());
                    outputStream.flush();

                    Message message=new Message();
                    message.what=101;
                    message.obj=messageStr;
                    handler.sendMessage(message);

                } catch (IOException e) {
                    e.printStackTrace();
                    isConnected=false;
                    socket=null;
                    handler.sendEmptyMessage(103);
                }
            }
        }.start();
    }


    private class ConnectThread extends Thread{
        private String ip;
        ConnectThread(String ip){
            this.ip=ip;
        }
        @Override
        public void run() {
            try {
                socket=new Socket(ip,9999);

                if (socket.isConnected()){
                    isConnected=true;
                    handler.sendEmptyMessage(104);

                    new ReadThread(handler,socket).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof ConnectException){
                    handler.sendEmptyMessage(105);
                    return;
                }
                isConnected=false;
                socket=null;
               handler.sendEmptyMessage(103);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
