package me.temoa.expandabletextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

  private static final String sText = "一款基于 Arduino 的智能花盆系统，实现对盆栽植物的自动浇水，并且上传数据到 Yeelink 物联网平台，手机端可以随时查看盆栽植物当前环境温度、土壤湿度、水箱水量和获取天气信息，及时为盆栽做出适合天气的放置，并且可以手机控制浇水。系统的设计分成三个部分：对盆栽的植物的土壤湿度、环境温度进行检测和水泵的控制；网络模块对盆栽环境数据的上传；手机端（Android）对盆栽植物相关数据的查看和控制浇水系统。系统的主控为一块称为 Arduino Uno 的控制板，环境温度的监测主要以传感器 DHT11 实现，将监测到的空气温度数据传送给控制板，将其上传到物联网平台。土壤湿度监测完成后，将其上传到物联网平台并判断盆栽植物是否需要浇水，控制水泵的工作。Android 手机软件查看服务器返回的数据，及时做出应对和控制浇水系统。";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ExpandableTextView expandableTextView = findViewById(R.id.expandable_tv);
    expandableTextView.setText(sText, false);
    expandableTextView.setListener(new ExpandableTextView.OnTextExpandedListener() {
      @Override
      public void expanded(boolean expandable) {
        Log.d("Test", "expanded() called with: expandable = [" + expandable + "]");
      }

      @Override
      public void collapse() {
        Log.d("Test", "collapse() called");
      }
    });
  }
}
