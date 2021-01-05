/*
        Classe TestConnexion qui permet d'établir le statut de la connexion client-serveur
        Auteur :  MATTEI Paul
        Dernière Modification : 24/06
 */


package com.example.androidapp;

import android.app.Activity;
import android.graphics.Color;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class TestConnexion {

    public static volatile int timeToWait = 10000; // 10 secondes
    private Activity activity = MainActivity.mainAct;
    public TextView statusServer; // Textview qui correspond au statut du serveur

    public static boolean connectedLocally = false; // variables de statut de connexion
    public static volatile boolean connectedToServer = false; // ..
    public static boolean dbOnline = false;

    // Constructeur
    public TestConnexion()
    {
        System.out.println("1");
        statusServer = activity.findViewById(R.id.textView_state);
        if(!connectedToInternet())
        {
            System.out.println("2");
            activity.runOnUiThread(() ->
            {
                statusServer.setText(R.string.NoConnexion);
                statusServer.setTextColor(Color.RED);
                timeToWait = 2000;
            });
            connectedToServer = false;
            connectedLocally = false;
            return;
        }
        connectedLocally = true;
        Test();
    }

    // Méthode pour tester la connexion
    private void Test()
    {
        System.out.println("TEST DE CONNEXION");
            final RequestQueue queue = Volley.newRequestQueue(activity);
            final String url = MainActivity.host+"/GET/STATUS/"; // your URL
System.out.println(url);
            queue.start();

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response ->
            {
                JSONObject reply;
                String message, code;
                System.out.println("Tentative de communication avec le serveur. . .");

                try
                {
                    reply = new JSONObject(response).optJSONObject("reply");
                    code = reply.optString("code");
                    message = reply.optString("message");
                    System.out.println(code + "    " + message);
                    if(!message.contains("Base de données offline"))
                    {
                        dbOnline = true;
                    }
                    else
                        dbOnline = false;
                    if(reply.optString("code").contains("200"))
                        connectedToServer = true;
                    else
                        connectedToServer = false;
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }


                String finalMessage = connectedToServer && dbOnline ? "  Connecté au serveur" : (connectedToServer ? "  Base de données inaccessible" : "Serveur déconnecté");
                boolean finalDbOnline = dbOnline;
                activity.runOnUiThread(() ->
                {
                    if (connectedToServer && finalDbOnline) {
                        statusServer.setTextColor(Color.GREEN);
                    } else {
                        statusServer.setTextColor(Color.RED);
                    }
                    statusServer.setText(finalMessage);
                });
                timeToWait = 20000;
                queue.getCache().clear();
            }, error -> {
                String finalMessage = "  Serveur déconnecté";
                System.out.println(error);
                activity.runOnUiThread(() ->
                {
                    connectedToServer = false;
                    statusServer.setText(finalMessage);
                    statusServer.setTextColor(Color.RED);
                });
                timeToWait = 10000;
            });

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    2000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
    }

    // Méthode pour vérifier la connexion internet
    public boolean connectedToInternet()
    {
         try
        {
            try (Socket soc = new Socket())
            {
                soc.connect(new InetSocketAddress("www.google.com", 80), 500);
            }
            return true;
        } catch (IOException ex)
        {
            return false;
        }

    }
}
