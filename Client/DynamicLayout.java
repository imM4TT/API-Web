/*
        Classe DynamicLayout qui permet d'instancier un nombre de layout en fonction du nombre de profil présent dans la réponse de la requête du menu consulter
        Auteur :  MATTEI Paul
        Dernière Modification : 24/06
 */

package com.example.androidapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;

import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class DynamicLayout {

    private Activity activity = MainActivity.mainAct;
    private ArrayList<User> users;
    private ArrayList<View> views = new ArrayList<>();
    private int size;

    private LinearLayout layoutContainer;
    public boolean process;

    // Constructeur
    public DynamicLayout(ArrayList<User> Users)
    {
        process = true;
        layoutContainer = activity.findViewById(R.id.layout_get_container);
        users = Users;
        size = Users.size();
        Instantiate();
    }

    // Création d'un layout, affichage du layout, assignation des valeurs du layout, pour chaque utilisateur
    public void Instantiate()
    {
        int ressources = R.layout.layout_template;
        if(layoutContainer.getChildCount() > 0)
        {
            layoutContainer.removeAllViews();
        }
        for(int i = 0; i < size; i++)
        {
            activity.getLayoutInflater().inflate(ressources, layoutContainer); // 1) création des layout
            InsertRows((ViewGroup) layoutContainer.getChildAt(i)); // 2) création de la liste qui contient toute les views
            if(i == size-1)
            {
                layoutContainer.getChildAt(i).setPadding(0,0,0,150);
            }
        }
        SetRowsValue(); // 3) Assignation des valeurs aux views
    }

    private void InsertRows(ViewGroup parent)
    {
        ViewGroup subroot = (ViewGroup) parent.getChildAt(0);
        int max = subroot.getChildCount();// nombre maximum d'element enfant
        for(int i = 0; i < max; i++)
        {
            View v;
            if(i == 0)//on accède au nom & prénom & photo qui sont dans le même viewgroup
            {
                ViewGroup vc = ((ViewGroup)subroot.getChildAt(i));
                v = vc.getChildAt(0);
                views.add(v);
                v = vc.getChildAt(1);
                views.add(v);
                v = vc.getChildAt(2);
            }
            else//on accède à tous les autres éléments (contat, promo, image ...)
                v = subroot.getChildAt(i);

            views.add(v);//on ajoute tous les élément de chaque layout model dans cette liste
        }
    }

    private void SetRowsValue()
    {
        int max = views.size();
        int user_i = 0;
        int item_i = 0;
        View v;
        String str = null;

        for(int i=0; i<max; i++)//toutes les views
        {
            v = views.get(i);
            if(i != 0 && i % 6 == 0)//tous les 7 items on change d'utilisateur
            {
                user_i ++;
                item_i = 0;
            }
            str = users.get(user_i).items[item_i];

            if( v instanceof TextView )
            {
                ((TextView) v).setText(str); //assignement des valeurs string
            }
            else
            {
                CircleImageView im = (CircleImageView) v;
                im.setBorderWidth(5); im.setBorderColor(Color.rgb(255,87,34));
                Bitmap bmp = Formatter.str64ToBmp(str);
                if(bmp != null)
                {
                    System.out.println(bmp.getByteCount());
                    activity.runOnUiThread(() -> im.setImageBitmap(bmp));
                }

            }
            item_i ++;
        }
    }

    public void Refresh()
    {
        if(layoutContainer.getChildCount() > 0)
        {
            layoutContainer.removeAllViews();
        }
    }
}
