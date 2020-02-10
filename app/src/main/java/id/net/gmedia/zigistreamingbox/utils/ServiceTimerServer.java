package id.net.gmedia.zigistreamingbox.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import id.net.gmedia.zigistreamingbox.RemoteUtils.ServiceUtils;


public class ServiceTimerServer extends Service{

        private static Timer timer = new Timer();
        private Context ctx;
        private int timerTtime = 1 * 60 * 1000; // 1 min

        public IBinder onBind(Intent arg0)
        {
            return null;
        }

        public void onCreate()
        {
            super.onCreate();
            ctx = this;
            startService();
        }

        private void startService()
        {
            timer.scheduleAtFixedRate(new mainTask(), 0, timerTtime);
        }

        private class mainTask extends TimerTask
        {
            public void run()
            {
                ServiceUtils.lockedClient = "";
            }
        }

        public void onDestroy()
        {
            super.onDestroy();
            //Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
        }

}
