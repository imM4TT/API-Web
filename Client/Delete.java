/*
        Classe Delete qui s'occupe et contient des/les éléments de suppression de profil
        Auteur :  MATTEI Paul
        Dernière Modification : 23/06
 */


package com.example.androidapp;

import android.app.Activity;

import android.view.Gravity;

import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import pl.droidsonroids.gif.GifImageView;

public class Delete {

    private Activity activity = MainActivity.mainAct;
    private EditText edtId = activity.findViewById(R.id.editText_id);
    private String id;
    private boolean process;
    private GifImageView gifImageView = MainActivity.mainAct.findViewById(R.id.loader3);


    // Constructeur
    public Delete()
    {
        gifImageView.setTranslationX(500); // Un de moyen les plus simple pour cacher un gif est de le sortir de l'écran ou d'afficher un autre layout par dessus, ici on décale le gif pour qu'il ne soit plus dans le champs visible de l'écran
    }


    // OnClick sur le boutton Supprimer du menu supprimer
    public void OnClick()
    {
        if(process)
            return;
        id = edtId.getText().length() > 3 ? edtId.getText().toString() : "";
        if(id == "")
        {
            MainActivity.MakeToast("ID Invalide", 400);
            return;
        }
        else if(!TestConnexion.connectedToServer || !TestConnexion.dbOnline)
        {
            MainActivity.MakeToast("Le service est actuellement indisponible.", 400);
            return;
        }
        process = true;
        gifImageView.setTranslationX(0);
        Suppression();
    }

    //  Méthode qui gère la suppresion
    private void Suppression()
    {
        final RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = MainActivity.host+"/DELETE/"; // your URL
        queue.start();
        JSONObject json = null;
        try
        {
            json = new JSONObject("{'id':'"+id+"'}");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, json, response -> {
            JSONObject reply;
            String message, code;

            try
            {
                reply = response.getJSONObject("reply");
                code = reply.optString("code");
                message = reply.optString("message");
                System.out.println(code + " " + message );
                Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            process = false;
            gifImageView.setTranslationX(500);

        }, error -> {
            Toast toast = Toast.makeText(activity, "Echec de la requête "+(id), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            System.out.println("error");
            gifImageView.setTranslationX(500);
            process = false;});
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        queue.add(jsObjRequest);
    }

}
