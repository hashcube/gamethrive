package com.tealeaf.plugin.plugins;

import com.tealeaf.EventQueue;
import com.tealeaf.event.*;
import com.tealeaf.plugin.IPlugin;
import com.tealeaf.logger;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import com.tealeaf.util.HTTP;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;

import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.app.AlertDialog;

import com.gamethrive.GameThrive;
import com.gamethrive.GameThrive.*;

import com.gamethrive.NotificationOpenedHandler;

public class GameThrivePlugin implements IPlugin {
  // There should only be one GameThrive instance for your whole App across all activities.

  Context _ctx;
  Intent _intent;
  Activity _activity;

  private static final String TAG = "{{GameThrivePlugin}}";

  private static GameThrive gameThrive;

  public class gamethriveNotificationOpened extends com.tealeaf.event.Event {
    boolean failed;
    String notification_opened_on;

    public gamethriveNotificationOpened(String opened_on) {
      super("gamethriveNotificationOpened");
      this.failed = false;
      this.notification_opened_on = opened_on;
    }
  }

  public class gamethriveGotData extends com.tealeaf.event.Event {
    boolean failed;
    String noOpCount;

    public gamethriveGotData(String opCount) {
      super("gamethriveGotData");
      this.failed = false;
      this.noOpCount = opCount;
    }
  }


  public void onCreateApplication(Context applicationContext) {
    this._ctx = applicationContext;
  }

  public void onCreate(Activity activity, Bundle savedInstanceState) {
    String gProjectNumber = null, appID = null;

    this._activity = activity;
    PackageManager manager = activity.getPackageManager();

    logger.log("GAMETHRIVE INITIALIZED", TAG);
    try {
      if (gameThrive == null){
        Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;

        if (meta != null) {
          gProjectNumber = meta.get("googleProjectNo").toString();
          appID = meta.get("gameThriveAppID").toString();
        }

        logger.log(gProjectNumber, appID, TAG);

        if (appID != null && gProjectNumber != null) {
          gameThrive = new GameThrive(activity, gProjectNumber, appID, new SudokuNotificationOpenedHandler());
          logger.log("Gamethrive instance created", TAG);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPause() {
    // super.onPause();
    gameThrive.onPaused();
  }
  @Override
  public void onResume() {
    // super.onResume();
    gameThrive.onResumed();
  }
  //Send tags to gameThrive
  public void sendTags(String jsonData) {
    try {
      logger.log(TAG, "Send Tags : " , jsonData);
      JSONObject object = new JSONObject(jsonData);
      gameThrive.sendTags(object);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  //Get notification_opened_count, Other tags can be gotten in the same way
  public void getNotificationOpenedCount() {
    try {
      logger.log(TAG, "Get notification opened count");

      gameThrive.getTags(new GetTagsHandler() {
        @Override
        public void tagsAvailable(JSONObject rTags) {
          logger.log(TAG, "retrieved data : ", rTags.toString());
          try {
            EventQueue.pushEvent(new gamethriveGotData
                                 (rTags.get("notification_opened_count").toString()));
          }  catch (org.json.JSONException eJ){
            EventQueue.pushEvent(new gamethriveGotData("0"));
            logger.log(TAG, "Opening notification for the first time");
          }catch (Exception e) {
             e.printStackTrace();
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void onStart() {
  }

  public void onStop() {
  }

  public void onDestroy() {
  }

  public void onNewIntent(Intent intent) {

  }

  public void setInstallReferrer(String referrer) {

  }

  public void onActivityResult(Integer request, Integer result, Intent data) {

  }

  public boolean consumeOnBackPressed() {
    return true;
  }

  public void onBackPressed() {

  }


  // NotificationOpenedHandler is implemented in its own class instead of adding implements to MainActivity so we don't hold on to a reference of our first activity if it gets recreated.
  private class SudokuNotificationOpenedHandler implements NotificationOpenedHandler {
    /**
     * Callback to implement in your app to handle when a notification is opened from the Android status bar or
     * a new one comes in while the app is running.
     * This method is located in this activity as an example, you may have any class you wish implement NotificationOpenedHandler and define this method.
     *
     * @param message        The message string the user seen/should see in the Android status bar.
     * @param additionalData The additionalData key value pair section you entered in on gamethrive.com.
     * @param isActive       Was the app in the foreground when the notification was received.
     */
    @Override
    public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
      String messageTitle, opened_on, opened_count;

      Date opened_on_time = new Date();

      AlertDialog.Builder builder = null;
      if (additionalData != null) {
        if (additionalData.has("discount"))
          messageTitle = "Discount!";
        else if (additionalData.has("bonusCredits"))
          messageTitle = "Bonus Credits!";
        else
          messageTitle = "Other Extra Data";
        builder = new AlertDialog.Builder(_ctx)
          .setTitle(messageTitle)
          .setMessage(message + "\n\n" + additionalData.toString());
      }
      else if (isActive) // If a push notification is received when the app is being used it does not display in the notification bar so display in the app.
        builder = new AlertDialog.Builder(_ctx)
          .setTitle("GameThrive Message")
          .setMessage(message);
      // Add your game logic around this so the user is not interrupted during gameplay. Check GameThriveSDK
      logger.log("setCancelable running");

      EventQueue.pushEvent(new gamethriveNotificationOpened(opened_on_time.toString()));
    }
  }
}
