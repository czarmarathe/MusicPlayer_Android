package com.uic.sarthak_marathe.music_player_client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.uic.sarthak_marathe.audioplayer_common.AudioPlayerControls;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Variables used to maintain session and keep track of changes and updates
    //Object of the AIDL fil class. ArrayList to keep track of requests made by the user in client app
    //String array to display names of song in List View and variables to keep track of songs selected and played
    private AudioPlayerControls playerobj;
    private boolean bounded = false;
    public ArrayList<String> request_list = new ArrayList<String>();
    public String song_list [] = {"1: Rhythm of Heart Beats","2: Witcher III","3: Morning Mood","4: Temple","5: Limbo"};
    public int song_pos = -1;
    public int song_playing = -1;
    ArrayAdapter listreq_adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Access to view of button present in UI
        final Button start_bt = (Button) findViewById(R.id.start);
        final Button pause_bt = (Button) findViewById(R.id.pause);
        final Button resume_bt = (Button) findViewById(R.id.resume);
        final Button stop_bt = (Button) findViewById(R.id.stop);

        //Define a list view and assign adapter to display name of songs for selection
        final ListView song_view = (ListView) findViewById(R.id.song_listview);
        ArrayAdapter list_adapter = new ArrayAdapter(this, R.layout.list_item, song_list);
        song_view.setAdapter(list_adapter);

        //Define a list view and assign adapter to displayed the requests made by user of client app
        final ListView request_view = (ListView) findViewById(R.id.request_listview);
        listreq_adapter = new ArrayAdapter(this, R.layout.list_item_request, request_list);
        request_view.setAdapter(listreq_adapter);

        //Executed everytime a user selects a song name from the ListView
        song_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                song_pos = i;
                song_view.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                song_view.setItemChecked(i,true);
            }
        });

        //On Click Listeners for all 4 buttons ( Play, Stop , Pause and Resume)
        start_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (song_pos < 0)
                    Toast.makeText(getApplicationContext(), "Select a song !!",Toast.LENGTH_SHORT).show();
                else if (song_pos > -1){
                    try {
                        //Call the corresponding method of service class that was overwritten in server app
                        playerobj.Play_Audio(song_pos);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    song_playing = song_pos;
                }
                else
                    Log.i("IDD","Something Wrong !");

                //Add the requests made by users to arraylist and continuously update the list view for display
                if (song_pos != -1){
                    request_list.add("Play Song :"+(song_pos+1));
                    listreq_adapter.notifyDataSetChanged();
                    resume_bt.setEnabled(false);
                    pause_bt.setEnabled(true);
                }

            }
        });

        stop_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (song_playing < 0)
                    Toast.makeText(getApplicationContext(), "Select a song and start it !!",Toast.LENGTH_SHORT).show();

                try {
                    //Call the corresponding method of service class that was overwritten in server app
                    playerobj.Stop_Audio();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (song_playing != -1){
                    request_list.add("Stop Song: "+(song_playing+1));
                    listreq_adapter.notifyDataSetChanged();
                    pause_bt.setEnabled(false);
                    resume_bt.setEnabled(false);
                }

            }

        });

        pause_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (song_playing < 0)
                    Toast.makeText(getApplicationContext(), "Select a song and start it !!",Toast.LENGTH_SHORT).show();

                try {
                    //Call the corresponding method of service class that was overwritten in server app
                    playerobj.Pause_Audio();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (song_playing != -1){
                    request_list.add("Pause Song: "+(song_playing+1));
                    listreq_adapter.notifyDataSetChanged();
                    resume_bt.setEnabled(true);
                    pause_bt.setEnabled(false);
                }

            }
        });

        resume_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (song_playing < 0)
                    Toast.makeText(getApplicationContext(), "Select a song and start it !!",Toast.LENGTH_SHORT).show();

                try {
                    //Call the corresponding method of service class that was overwritten in server app
                    playerobj.Resume_Audio();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (song_playing != -1){
                    request_list.add("Resume Song: "+(song_playing+1));
                    listreq_adapter.notifyDataSetChanged();
                    pause_bt.setEnabled(true);
                    resume_bt.setEnabled(false);
                }
            }
        });

    }

    //Create a ServiceConnection object to be used by the client app to bind and unbind from service
    private final ServiceConnection connobj = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("IDDD","Its Bounded");
            playerobj = AudioPlayerControls.Stub.asInterface(iBinder);
            bounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            playerobj = null;
            bounded = false;
            Log.i("IDDD","Its UnBounded");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("IDD","Activity Resumed:"+bounded);
        if (!bounded){

            boolean val = false;

            Intent intent_serv = new Intent(AudioPlayerControls.class.getName());

            ResolveInfo RInfo = getPackageManager().resolveService(intent_serv, Context.BIND_AUTO_CREATE);
            intent_serv.setComponent(new ComponentName(RInfo.serviceInfo.packageName, RInfo.serviceInfo.name));

            //Bind service by passing the intent with action to aidl class file, serviceConnection object and appropriate flags
            //Returns true if service bound
            val = bindService(intent_serv, this.connobj, BIND_AUTO_CREATE);

            if (val)
                Log.i("IDD","Bounded:"+val);
            else
                Log.i("IDD","Not Bounded");
        }

    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i("IDD","Activity Stopped:"+bounded);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("IDD","Activity Destroyed:"+bounded);
        Log.i("IDD","In Destroy");
        //When Activity destroyed by android or user unbind the service
        try{
            unbindService(this.connobj);
        }
        catch (Exception E){
            Log.i("IDD",":"+E.toString());
        }

        Log.i("IDD","Unbounded when destroyed");

    }
}
