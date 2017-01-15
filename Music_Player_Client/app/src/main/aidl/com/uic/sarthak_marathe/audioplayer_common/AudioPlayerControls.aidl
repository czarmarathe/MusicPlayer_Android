// AudioPlayerControls.aidl
//package audioplayer_common;
package com.uic.sarthak_marathe.audioplayer_common;

// Declare any non-default types here with import statements

interface AudioPlayerControls {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void Play_Audio(int pos);

    void Pause_Audio();

    void Resume_Audio();

    void Stop_Audio();
}
