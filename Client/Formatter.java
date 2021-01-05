/*
        Classe qui contient certaines méthodes statique de formattage d'une image (JPG vers PNG et compression notamment)
        Auteur :  MATTEI Paul
        Dernière Modification : 24/06
 */

package com.example.androidapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Formatter {


    // Méthode qui convertit un String64 en Bitmap
    public static Bitmap str64ToBmp(String t)
    {

        byte[] imageAsBytes = Base64.decode(t.getBytes(), Base64.DEFAULT);
        Bitmap b = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

        return b;
    }


    // Méthode qui convertit un Bitmap en Str64
    public static String bmpToStr64(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte [] arr = bos.toByteArray();
        String result=Base64.encodeToString(arr, Base64.DEFAULT);

        return result;
    }


}