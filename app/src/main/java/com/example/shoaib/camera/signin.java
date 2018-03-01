package com.example.shoaib.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.example.shoaib.camera.listeners.ServiceCallBack;
import com.example.shoaib.camera.utils.AppConstants;
import com.example.shoaib.camera.utils.VolleyRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.example.shoaib.camera.utils.AppConstants.loginemail;

public class signin extends Fragment implements View.OnClickListener, ServiceCallBack {
    EditText email;
    EditText password;
    public  static String email_st,password_st;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signin2, container, false);

        TextView textView = (TextView) v.findViewById(R.id.textView3);
        TextView textView2 = (TextView) v.findViewById(R.id.textView);
        email = (EditText) v.findViewById(R.id.editText);
        password = (EditText) v.findViewById(R.id.editText2);
        Button btnPost = (Button) v.findViewById(R.id.button);
        btnPost.setOnClickListener(this);
        textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                signup fragment = new signup();
                fragmentTransaction.replace(R.id.fragment, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();



            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                forgetpassword_fragment fragment = new forgetpassword_fragment();
                fragmentTransaction.replace(R.id.fragment, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();



            }
        });

        return v;
    }

   @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                try {

                    email_st = email.getText().toString();
                    password_st= password.getText().toString();
                    JSONObject requestObject = new JSONObject();
                    requestObject.put("name", email_st);
                    requestObject.put("password", password_st);
                    VolleyRequestHandler postRequestHandler = new VolleyRequestHandler();
                    postRequestHandler.makeJsonRequest(getActivity(), Request.Method.POST, AppConstants.signin_URL, AppConstants.POST_TAG,this, requestObject);
                  loginemail =email_st;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onSuccess(String requestTag, Object data) throws UnsupportedEncodingException {
       // Toast.makeText(getActivity(), requestTag + ">>>" + data, Toast.LENGTH_LONG).show();

        try {
            JSONObject jsonObject = new JSONObject(data.toString());
            String message = jsonObject.getString("Message");
            String error = jsonObject.getString("Error");
            Log.d("shoaib", String.valueOf(message));
            if(message=="true") {
                Intent intent = new Intent(getActivity(), StreamActivity.class);

                startActivity(intent);
                getActivity().finish();

            }
           else  {

                Toast.makeText(getActivity(), "Please enter correct email and password", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onFailure(String requestTag, String message) {
        Toast.makeText(getActivity(), "Please turn on your internet Connection", Toast.LENGTH_LONG).show();
    }

}



