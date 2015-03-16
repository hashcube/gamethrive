package com.gamethrive;

import com.tealeaf.logger;
import com.tealeaf.EventQueue;
import com.tealeaf.event.Event;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import java.util.Date;

public class GameBroadcastReceiver extends BroadcastReceiver {
  private static Date received_on_time = null;
  private static Integer received_count = 0;
  private static final String TAG = "{{GameThrivePlugin}}";

  //Singleton implementation
  //public final static GameBroadcastReceiver INSTANCE = new GameBroadcastReceiver();
  // private GameBroadcastReceiver() {
    // Exists only to defeat instantiation.
  //}

  // You may consider adding a wake lock here if you need to make sure the devices doesn't go to sleep while processing.
  // We recommend starting a service of your own here if your doing any async calls or doing any heavy processing.
  @Override
  public void onReceive(Context context, Intent intent) {
    received_on_time = new Date();
    received_count++;
    logger.log("Notification received on : ", received_on_time.toString(), TAG);
  }

  public Date getReceiveDate()
  {
    return received_on_time;
  }

  public Integer getReceiveCount()
  {
    Integer rc;
    rc = received_count;
    received_count = 0;
    return rc;
  }
}
