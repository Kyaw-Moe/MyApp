package com.myapp.myapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectivityReceiver extends BroadcastReceiver {





    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if(activeNetwork!=null&& activeNetwork.isConnectedOrConnecting())
        {
            if(activeNetwork.getType()==ConnectivityManager.TYPE_WIFI)
            {
                Toast.makeText(context,"Wifi Connected",Toast.LENGTH_LONG).show();

            }
            else if(activeNetwork.getType()==ConnectivityManager.TYPE_MOBILE)
            {

                Toast.makeText(context,"Mobile Data Connected",Toast.LENGTH_LONG).show();
            }
        }
       else
        {
            Toast.makeText(context,"Offline",Toast.LENGTH_LONG).show();
        }

    }



}
