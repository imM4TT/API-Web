/*
        Classe Get du menu consulter qui contient les méthode relatives à la partie Read du modèle CRUD
        Auteur :  MATTEI Paul
        Dernière Modification : 23/06
 */


package com.example.androidapp;

import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

public class Get {


    public boolean setData;
    private View view = MainActivity.mainAct.findViewById(R.id.loader);
    public DynamicLayout dynamicLayout;

    //Constructeur
    public Get()
    {
        view.setVisibility(View.VISIBLE);
        if(!TestConnexion.connectedToServer || !TestConnexion.dbOnline) // connexion indisponible
        {
            MainActivity.MakeToast("Le service est actuellement indisponible.", 400);
            return;
        }
        view.setVisibility(View.INVISIBLE);
        GetData();
    }

    // Obtention des données
    private void GetData()
    {
        final RequestQueue queue = Volley.newRequestQueue(MainActivity.mainAct);
        final String url = MainActivity.host+"/GET/DATA"; // your URL

        queue.start();
        setData = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response ->{

            JSONObject reply, message;
            JSONArray usersJ;
            String code;

            try
            {
                reply = new JSONObject(response).optJSONObject("reply");
                code = reply.optString("code");
                message = reply.optJSONObject("message");
                usersJ = message.optJSONArray("users");
                System.out.println(reply);
                ArrayList<User> users = new ArrayList<>();
                int max = usersJ.length();

                for (int i = 0; i < max; i++)
                {
                    JSONObject jsonobject = usersJ.getJSONObject(i);
                    String nom = jsonobject.optString("nom");
                    String prenom = jsonobject.optString("prenom");
                    String photo = jsonobject.optString("photo");
                    String contact = jsonobject.getString("contact");
                    String etudiant = jsonobject.getString("etudiant");
                    String ancien = jsonobject.getString("ancien");
                    users.add(new User(nom, prenom, photo, contact, etudiant, ancien));
                }

                dynamicLayout = new DynamicLayout(users);
                setData = false;
                view.setVisibility(View.VISIBLE);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }, error ->view.setVisibility(View.VISIBLE));
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        queue.add(stringRequest);
    }

}