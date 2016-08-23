package com.onesignal;

import com.tealeaf.logger;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;

import java.util.Date;

public class GameBroadcastReceiver extends BroadcastReceiver {
  private static Date received_on_time = null;
  private static Integer received_count = 0;
  private static String title = null, message = null, segment_name = null;

  // You may consider adding a wake lock here if you need to make sure the devices doesn't go to sleep while processing.
  // We recommend starting a service of your own here if your doing any async calls or doing any heavy processing.
  @Override
  public void onReceive(Context context, Intent intent) {
    received_on_time = new Date();
    received_count++;
    Bundle data = intent.getExtras(); 
    title = data.getString("title");
    message = data.getString("alert");
    logger.log("Notification received on : ", received_on_time.toString(), "{{Onesignal}}");
    try {
      JSONObject customJSON = new JSONObject(data.getString("custom"));
      if (customJSON.has("a")) {
        JSONObject additionalData = customJSON.getJSONObject("a");
        segment_name = additionalData.getString("segment_name");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    logger.log(title, segment_name, message, "{{onesignal}} data received");
  }

  public Date getReceiveDate()
  {
    Date rt;
    rt = received_on_time;
    received_on_time = null;
    return rt;
  }

  public Integer getReceiveCount()
  {
    Integer rc;
    rc = received_count;
    received_count = 0;
    return rc;
  }
  
  public String getReceiveData(String item)
  {
    String temp = null;
    if(item == "title") {
      temp = title;
      title = null;
    } else if(item == "message") {
      temp = message;
      message = null;
    } else if(item == "segment_name") {
      temp = segment_name;
      segment_name = null;
    }
    return temp;
  } 
}
