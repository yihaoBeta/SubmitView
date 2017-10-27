package com.zxs.yihaobeta.submitview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.zxs.yihaobeta.submitview.view.SubmitView;
import com.zxs.yihaobeta.submitview.view.SubmitWithProgressView;

/**
 * Created by yihaobeta on 2017/10/25.
 */

public class MainActivity extends AppCompatActivity {

  private static final int MSG_SUBMIT_COMPLETE = 0x01;
  private static final int MSG_SUBMIT_UPDATE_PROGRESS = 0x02;
  private SubmitView mSubmitView;
  private SubmitWithProgressView mSubmitWithProgressView;
  private Handler mHandler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_SUBMIT_COMPLETE: {
          if (mSubmitView != null) mSubmitView.submitCompleted();
          break;
        }
        case MSG_SUBMIT_UPDATE_PROGRESS: {
          if (mSubmitWithProgressView == null) {
            return false;
          }

          mSubmitWithProgressView.setProgressValue(msg.arg1);
          if (msg.arg1 <= 100) {
            msg.arg1++;
            mHandler.sendMessageDelayed(Message.obtain(msg),30);
          } break;
        }
      } return true;
    }
  });

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSubmitView = findViewById(R.id.submitView);
    mSubmitWithProgressView = findViewById(R.id.submitWithProgressView);

    mSubmitView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mSubmitView.submit();
        Message message = new Message();
        message.what = MSG_SUBMIT_COMPLETE;
        mHandler.sendMessageDelayed(message, 2000);
      }
    });

    mSubmitWithProgressView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mSubmitWithProgressView.submit();
        Message message = new Message();
        message.what = MSG_SUBMIT_UPDATE_PROGRESS;
        message.arg1 = 0;
        mHandler.sendMessageDelayed(message, 30);
      }
    });
  }
}
