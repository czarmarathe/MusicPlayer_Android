package com.uic.sarthak_marathe.music_audio_server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.uic.sarthak_marathe.audioplayer_common.AudioPlayerControls;

import java.util.ArrayList;
import java.util.Arrays;

public class Music_Player extends Service {
    public Music_Player() {
    }

    //Variables used to maintain state of the system
    //Like object of MediaPlayer class used to start,pause,stop...s=audio clips and other variables to kee track of song selected
    //and played by the user. Notification Manager object used to notify changes in notification
    private MediaPlayer music_obj = null;
    private int music_id;
    public static int pause_pos;
    public int lastpos = -1;
    Notification.Builder notification;
    NotificationManager notificationManager;
    //Array List used to store and access the raw audio files . String arrray to store name of songs in notification
    public ArrayList<Integer> songs = new ArrayList<Integer>(Arrays.asList(R.raw.music1,
            R.raw.music2, R.raw.music3, R.raw.music4, R.raw.music5));
    public String songs_list [] = {"Rhythm of Heart Beats","Witcher III","Morning Mood","Temple","Limbo"};
    @Override
    public void onCreate() {
        super.onCreate();

        if (music_obj != null){
            music_obj.setLooping(false);

            music_obj.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    ManageNotification(3,lastpos);
                    stopSelf(music_id);
                }
            });
        }

        Log.i("IDD","Music Player Object Created");

    }

    //Create Stub object and overwrite the methods present in AIDL file interface
    AudioPlayerControls.Stub bindobj = new AudioPlayerControls.Stub() {
        @Override
        public void Play_Audio(int pos) throws RemoteException {

            //When MusicPlayer object has been created and there is change in selection of song by user
            if (music_obj != null && lastpos == pos){
                music_id = 0;
                Log.i("IDD","Play Button Pressed");
                Log.i("IDD","Song Number:"+pos);

                //If music is already playing just start the song from beginning on start button press for same song
                if (music_obj.isPlaying()){
                    music_obj.seekTo(0);
                    Log.i("IDD","Music already Playing");
                }
                //If music not playing, start the song selected
                else{
                    music_obj.start();
                    music_obj.seekTo(0);
                    Log.i("IDD","Music Started");
                }
            }
            //If MusicPlayer object has not been initialized
            else{
                if (music_obj != null && lastpos != pos){
                    music_obj.stop();
                    music_obj.release();
                    music_obj = null;
                }
                //Initialize MusicPlayer object and assign a raw audio file and start the song
                music_obj = MediaPlayer.create(Music_Player.this ,songs.get(pos));
                music_obj.start();
                Log.i("IDD","Music Object Recreated");
                Log.i("IDD","Music Started");
            }
            //Update the notification
            ManageNotification(1,pos);
            lastpos = pos ;
        }

        @Override
        public void Pause_Audio() throws RemoteException {
            //If music is playing , on pause button press store the current position in clip
            if (music_obj != null){
                if (music_obj.isPlaying() && music_obj != null){
                    music_obj.pause();
                    pause_pos = music_obj.getCurrentPosition();
                    Log.i("IDD","Song Paused");
                }
                else {
                    Log.i("IDD:","Pause Function : Music was not playing");
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Select a Song !!", Toast.LENGTH_SHORT).show();

            //Update the notification correspondingly
            ManageNotification(2,lastpos);
        }

        @Override
        public void Resume_Audio() throws RemoteException {
            //If MusicPlayer object is not null , on resume button push start the song from the point it was paused
            if (music_obj != null){
                if (!music_obj.isPlaying()){
                    music_obj.start();
                    music_obj.seekTo(pause_pos);
                    Log.i("IDD","Song Resumed");
                }
                else{
                    Log.i("IDD:","Resume : Music was already playing");
                    if (music_obj == null)
                        Toast.makeText(getApplicationContext(), "Select a Song !!", Toast.LENGTH_SHORT).show();
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Select a Song !!", Toast.LENGTH_SHORT).show();

            //Update the notification correpondingly
            ManageNotification(1,lastpos);
        }

        @Override
        public void Stop_Audio() throws RemoteException {
            //Stop the music currently being played or initialized in object by releasing the object
            if (music_obj != null){
                try {
                    music_obj.stop();
                    music_obj.release();
                    music_obj = null;
                    Log.i("IDD","Song Stopped");
                }
                catch (Exception E){
                    E.printStackTrace();
                    Log.i("IDD","Something Happened while Stopping");
                    Log.i("IDD",":"+E.toString());
                }

            }
            else{
                Log.i("IDD","Stop Null Problem");
                Toast.makeText(getApplicationContext(), "Select a Song !!", Toast.LENGTH_SHORT).show();
            }

            //Update the notification correspondingly
            ManageNotification(3,lastpos);
        }
    };

    //Method used to manage and update notifications
    public void ManageNotification(int func, int posit){

        notification = new Notification.Builder( getApplicationContext());
        if (func == 1){
            notification.setSmallIcon(android.R.drawable.ic_media_play);
            notification.setContentTitle("Playing Music");
        }
        else if (func == 2){
            notification.setSmallIcon(android.R.drawable.ic_media_pause);
            notification.setContentTitle("Paused Music");
        }
        else
        {
            notification.setSmallIcon(android.R.drawable.ic_media_next);
            notification.setContentTitle("Stopped Music");
        }

        notification.setOngoing(true).setAutoCancel(true)
                .setContentText(songs_list[posit]);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return bindobj;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("IDD","Service Unbound");
        stopSelf();

        return super.onUnbind(intent);
    }

    //When the service is destroyed stop the song being played ,release the object and remove notification
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (music_obj != null){
            music_obj.stop();
            music_obj.release();
            notificationManager.cancelAll();
        }


        Log.i("IDD","Service Destroyed");
    }
}
