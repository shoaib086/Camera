package com.example.shoaib.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.example.shoaib.camera.listeners.ServiceCallBack;
import com.example.shoaib.camera.utils.AppConstants;
import com.example.shoaib.camera.utils.VolleyRequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.example.shoaib.camera.utils.AppConstants.cameraemail;
import static com.example.shoaib.camera.utils.AppConstants.loginemail;

public class StreamActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener, ServiceCallBack {
    TextView navEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
try{
        JSONObject requestObject = new JSONObject();
        requestObject.put("name", loginemail);

        VolleyRequestHandler postRequestHandler = new VolleyRequestHandler();
        postRequestHandler.makeJsonRequest(this, Request.Method.POST, AppConstants.emailcamera_URL, AppConstants.POST_TAG,this, requestObject);

    } catch (JSONException e) {
        e.printStackTrace();
    }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
       TextView navUsername = (TextView) headerView.findViewById(R.id.textView);
        navEmail = (TextView) headerView.findViewById(R.id.textView2);
        navUsername.setText(loginemail);


        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction fragmentTransaction =getSupportFragmentManager().beginTransaction();
        livefragment fragment = new livefragment();
        fragmentTransaction.replace(R.id.content_frame, fragment);

        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
            finish();
        }
        if (id == R.id.nav_activity) {

            FragmentTransaction fragmentTransaction =getSupportFragmentManager().beginTransaction();
            ActivityFragment fragment = new ActivityFragment();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        if (id == R.id.nav_face) {

            FragmentTransaction fragmentTransaction =getSupportFragmentManager().beginTransaction();
            livefragment fragment = new livefragment();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onSuccess(String requestTag, Object data) throws UnsupportedEncodingException {
        Toast.makeText(this, requestTag + ">>>" + data, Toast.LENGTH_LONG).show();
        Log.d("done",data.toString());
        try {
            JSONObject jsonObject = new JSONObject(data.toString());
            String message = jsonObject.getString("Message");
            JSONArray Users = jsonObject.getJSONArray("Users");
            JSONObject obj2= Users.getJSONObject(0);
            String email = obj2.getString("email");


            //JSONArray obj=Users.getJSONArray(1);

            Log.d("shoaib", message);


                navEmail.setText(email);
                cameraemail=email;
                Log.d("done",Users.toString());
                Log.d("done",obj2.toString());
           // Log.d("done",obj.toString());
            //Log.d("done",email);



              //  Toast.makeText(this, "Please enter correct email and password", Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(String requestTag, String message) {

        Toast.makeText(this, requestTag + ">>>" + message, Toast.LENGTH_LONG).show();
    }
}
