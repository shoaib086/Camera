package com.example.shoaib.camera;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;


import com.example.shoaib.camera.motiondetection.MotionDetector;
import com.example.shoaib.camera.motiondetection.MotionDetectorCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/////

public class ActivityFragment extends Fragment {


    private static final String TAG = "MainActivity";


    private static final String VIDEO_TRACK_ID = "video1";
    private static final String AUDIO_TRACK_ID = "audio1";
    private static final String LOCAL_STREAM_ID = "stream1";


    private PeerConnectionFactory peerConnectionFactory;
    private VideoSource localVideoSource;
    private MediaStream localMediaStream;
    private VideoRenderer otherPeerRenderer;
    public static final int RequestPermissionCode = 7;
    private MotionDetector motionDetector;


    GLSurfaceView videoView;


    private Boolean exit = false;

    private Boolean myCheck = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_activity, container, false);
        videoView = (GLSurfaceView) v.findViewById(R.id.glview_call);

        if (Build.VERSION.SDK_INT < 23) {
            //Do not need to check the permission
            initializePeerConnection();
        } else {
            if (CheckingPermissionIsEnabledOrNot()) {
                initializePeerConnection();
            } else {
                RequestMultiplePermission();
            }
        }

        return v;
    }


    private void initializePeerConnection() {




        motionDetector = new MotionDetector(getActivity(), videoView);



        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        PeerConnectionFactory.initializeAndroidGlobals(
                getActivity(),  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true,  // Hardware Acceleration Enabled
                null); // Render EGL Context

        peerConnectionFactory = new PeerConnectionFactory();

        VideoCapturerAndroid vc = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfBackFacingDevice(), null);

        // VideoCapturerAndroid vc = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfFrontFacingDevice(), null);


        localVideoSource = peerConnectionFactory.createVideoSource(vc, new MediaConstraints());
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
        localVideoTrack.setEnabled(true);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);

        localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID);
        localMediaStream.addTrack(localVideoTrack);
        localMediaStream.addTrack(localAudioTrack);


        VideoRendererGui.setView(videoView, null);
        try {
            otherPeerRenderer = VideoRendererGui.createGui(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            VideoRenderer renderer = VideoRendererGui.createGui(50, 50, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            localVideoTrack.addRenderer(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {

            @Override
            public void onMotionDetected() {
                Log.e(TAG, "onMotionDetected is called");
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
               // v.vibrate(500);
                showToast("Motion detected!");
            }

            @Override
            public void onTooDark() {
                showToast("Too dark here");
            }
        });


    }

    private void showToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    private void RequestMultiplePermission() {

        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(getActivity(), new String[]
                {
                        CAMERA,
                        RECORD_AUDIO,
                        WRITE_EXTERNAL_STORAGE,
                }, RequestPermissionCode);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ExternalStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && ExternalStoragePermission) {

                        Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_LONG).show();
                        initializePeerConnection();

                    } else {
                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }

                break;
        }
    }


    /*
    * CheckingPermissionIsEnabledOrNot() method is used to check permission is already enable or not
    * It basically check whether the user has already checked all the permission or not in order to start the application
    * If all permission are granted it return true is returned otherwise false.
    * */
    public boolean CheckingPermissionIsEnabledOrNot() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), RECORD_AUDIO);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE);


        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume is called");

        if (!myCheck) {
            motionDetector.onResume();
            if (motionDetector.checkCameraHardware()) {
                showToast("Camera found");
                Log.e(TAG, "Camera found");

            } else {
                Log.e(TAG, "no camera avaiable");
                showToast("No camera available");
            }
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        Log.e(TAG, "onPause is called");

        motionDetector.onPause();

    }



    /*
    * This function load the refresh gallery after a video is saved
    * */
    private void loadToGallery() {
        MediaScannerConnection.scanFile(getActivity(),
                new String[]{Environment.getExternalStorageDirectory().toString()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    /*
    * This function is triggered when user press back button in the application and it let the user know
     * the recording of screen is in process if he still want to exit the recording of screen will be stopped and when
     * user do press back button again at second time the recording of screen is stopped and application is closed.
    * */

}