package com.zxs.yihaobeta.submitview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.zxs.yihaobeta.submitview.view.SubmitView;

/**
 * Created by yihaobeta on 2017/10/25.
 */

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final SubmitView submitView = findViewById(R.id.submitView);
    Button start = findViewById(R.id.btn_start);
    Button end = findViewById(R.id.btn_end);
    submitView.setText("提交");
    submitView.setTextSize(25);

    submitView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        submitView.submit();
      }
    });
    start.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        submitView.cancel();
      }
    });

    end.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        submitView.submitCompleted();
      }
    });
  }
}
