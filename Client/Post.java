/*
        Classe de l'objet Post qui contient les méthodes relatives au menu Ajouter/Modifier de l'application
        Auteur :  MATTEI Paul
        Dernière Modification : 23/06
*/


package com.example.androidapp;

import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;


public class Post {

    private boolean processing = false; // En cours de traitement ?
    public UserInput userInput; // Instance de l'objet userInput pour le traitement des champs de saisies
    private View view = MainActivity.mainAct.findViewById(R.id.loader2); // View du .gif qui apparait à l'écran lors de communication avec le serveur


    // Constructeur
    public Post()
    {
        view.setVisibility(View.VISIBLE);
        if(!TestConnexion.connectedToServer || !TestConnexion.dbOnline)
        {
            MainActivity.MakeToast("Le service est actuellement indisponible.", 400);
            return;
        }
        userInput = new UserInput();
        SetId();
    }


    //region Private

    // Envoie de la requête HTTP pour recevoir un ID valide
    private void SetId()
    {

        final RequestQueue queue = Volley.newRequestQueue(userInput.activity);
        final String url = MainActivity.host+"/GET/ID/"; // your URL

        view.setVisibility(View.INVISIBLE);
        queue.start();
        processing = true;
        String id = "";
        id += new Random().nextInt(10);
        id += new Random().nextInt(10);
        id += new Random().nextInt(10);
        id += new Random().nextInt(10);

        HashMap m = new HashMap();
        m.put("id", id);
        MainActivity.MakeToast("L'identifiant est unique et vous permet d'accéder à votre profil.", 310);
        String finalId = id;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(m), response ->
        {
            JSONObject reply, message = null;
            int count_index;
            try
            {
                reply = response.getJSONObject("reply");
                message = reply.getJSONObject("message");
                System.out.println(reply.optString("code") + "   " + message.optString("count"));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            count_index = Integer.parseInt(message.optString("count"));
            if(count_index > 0)
            {
                SetId();
            }
            else
            {
                userInput.getEdt_Id().setText(finalId);
                view.setVisibility(View.VISIBLE);
                processing = false;
            }

        }, error -> {processing = false; view.setVisibility(View.VISIBLE);;System.out.println("Erreur de communication vers le serveur:" + error);});
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsObjRequest);


    }

    // Envoie de la requête HTTP pour Ajouter/Modifier des données, et attente de la réponse
    private void PostAndGetReponse()
    {
        final RequestQueue queue = Volley.newRequestQueue(userInput.activity);
        final String url = MainActivity.host+"/POST/DATA/";

        MainActivity.mainAct.runOnUiThread(() -> view.setVisibility(View.INVISIBLE));
        queue.start();
        processing = true;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(userInput.input), response -> {
            JSONObject reply;
            String message, code;

            try
            {
                reply = response.getJSONObject("reply");
                code = reply.optString("code");
                message = reply.optString("message");
                MainActivity.mainAct.runOnUiThread(() -> view.setVisibility(View.VISIBLE));
                System.out.println(code + " " + message);
                MainActivity.MakeToast(message, 400);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            processing = false;

        }, error ->
             {
                 String mssg;
                 if(error.networkResponse != null) {
                     NetworkResponse networkResponse = error.networkResponse;
                     if (networkResponse.statusCode == 403)
                         mssg = "Compléter toutes les informations pour créer un nouvel utilisateur ou saisir un ID existant pour éditer un profil.";
                     else
                         mssg = "Communication avec le serveur impossible.";

                 }
                 else
                     mssg = "Erreur de connexion";

                 MainActivity.mainAct.runOnUiThread(() -> view.setVisibility(View.VISIBLE));
                 MainActivity.MakeToast(mssg, 400);
                 System.out.println("error");
                 processing = false;
            });

             jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                3500,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
             );

             queue.add(jsObjRequest);
    }

    //endregion


    // region Public

    // OnClick du boutton Envoyer du menu Ajouter/Modifier
    public void OnClick()
    {
        if(processing)
            return;
        if(!userInput.PutValueAndContinue())
        {
            String[] error = userInput.error.split("\\+");
            String mssg = "Champ(s) invalide(s):";
            for(String item : error)
            {
                mssg += " "+item;
            }

            MainActivity.MakeToast(mssg, 400);
            return;
        }
        if(!TestConnexion.connectedLocally)
        {
            MainActivity.MakeToast("Echec de la requête. \nConnexion à internet requise.", 400);
            return;
        }
        else if(!TestConnexion.connectedToServer) {
            MainActivity.MakeToast("Communication avec le serveur impossible.", 400);
            return;
        }
        processing = true;

        PostAndGetReponse();
    }

    // endregion
}
