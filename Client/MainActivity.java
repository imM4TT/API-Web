/*
        Classe mère qui lance l'application et définit les fonctions du menu principale
        Auteur :  MATTEI Paul
        Dernière Modification : 23/06
*/

package com.example.androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;

import android.content.Context;

import android.content.Intent;

import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;

import android.os.StrictMode;

import android.view.Gravity;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity{

    private Post post;     // Objet post pour ajouter ou modifier des ressources
    private Get get;       // Objet get pour consulter des ressources
    private Delete delete; // Objet delete pour supprimer des ressources
    private RelativeLayout[] layout = new RelativeLayout[4];
    public static int index = 0; // 0 => menu // 1 => layout send // 2 => layout set // 3 => layout del

    public static Activity mainAct;
    public static Thread thread1 = null; // Thread secondaire pour le statut de la connexion

    private boolean exitThread1 = false;
    private boolean disableOnResume = true;

    public static String host = "http://androscope.serveousercontent.com"; //"https://m4tt-5250f063.localhost.run"

    public static boolean isPicking = false;


    //region Changement d'état de l'applciation (Pause, Reprise, Création)
    @Override
    protected void onPause()
    {
        System.out.println("PAUSE");
        super.onPause();

        if(isPicking)
            return;



        exitThread1 = true;
        thread1.interrupt();
    }

    @Override
    protected void onResume()
    {
        System.out.println("REPRISE");
        super.onResume();

        if(isPicking) {
            isPicking = false;
            return;
        }

        if(disableOnResume)
        {
            disableOnResume = false;
            return;
        }

        Restart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println(Build.VERSION.SDK_INT);
        System.out.println(Build.VERSION.RELEASE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Init();
    }

    //endregion


    // region OnClick Menu Principal

    public void OnClickAjouter(View v)
    {
        index = 1; // definition de l'index
        post = new Post(); // nouveau Post
        SetLayout(); // organisation des layout
    }

    public void OnClickConsulter(View v)
    {
        index = 2;
        get = new Get();
        SetLayout();
    }

    public void OnClickSupprimer(View v)
    {
        index = 3;
        delete = new Delete();
        SetLayout();
    }

    // endregion


    // region Buttons Submit & Back

    public void OnClickSubmit(View v)// Se produit lors d'un click sur le bouton submit d'une des sections principale ( send, get, delete )
    {
        if(index == 1)// click sur le bouton submit du MENU POST
        {
            new Thread(() -> post.OnClick()).start();
        }
        else if(index == 3)// click sur le bouton submit du MENU DELETE
        {
            delete.OnClick();
        }
    }

    public void OnClickBack(View v) // OnClick Retour
    {
        if(index == 2)
        {
            if(get.setData)
                return;
            get.dynamicLayout.Refresh();
        }
        HideKeyboard();
        index = 0;
        SetLayout();
    }

    //endregion


    //region Private

    // Initialise l'application
    private void Init()
    {
        CheckConnexion();
        mainAct = this;

        layout[0] = findViewById(R.id.layout_menu);
        layout[1] = findViewById(R.id.layout_send);
        layout[2] = findViewById(R.id.layout_get);
        layout[3] = findViewById(R.id.layout_del);
    }

    // Réorganise l'ordre d'affichage des menus en fonction de l'index
    private void SetLayout()
    {
        for (int i = 0; i < layout.length; i++)
        {
            if(i == index)
            {
                layout[i].setVisibility(View.VISIBLE);
                continue;
            }
            layout[i].setVisibility(View.INVISIBLE);
        }
        if(index != 0)
        {
            TestConnexion.timeToWait = 15000;
        }
        else
        {
            TestConnexion.timeToWait = 8000;
        }
    }

    // Permet de cacher le clavier numérique à certain moment (lorsqu'on est sur le menu principal, il n'y a pas besoin de clavier numérique)
    private void HideKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Permet de restaurer la configuration de base lors d'un changement de statut d'activité
    private void Restart()
    {
        index = 0;
        exitThread1 = false;

        SetLayout();
        CheckConnexion();
    }

    // Vérifie l'état de la connexion en arrière plan
    private void CheckConnexion()
    {
        thread1 = new Thread(() ->
        {System.out.println("new th1");
            while (!exitThread1)
            {System.out.println("exec th");
                new TestConnexion();
                try
                {
                    thread1.sleep(TestConnexion.timeToWait);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            try
            {
                thread1.sleep(200);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            thread1.interrupt();
        });
        thread1.start();
    }

    // endregion


    //region Static

    // Faire un Toast en fonction d'un message et d'une position de manière statique
    public static void MakeToast(String mssg, int y)
    {
        mainAct.runOnUiThread(() -> {
            Toast toast = Toast.makeText(MainActivity.mainAct, mssg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, y);
            toast.getView().setBackgroundColor(Color.LTGRAY);
            toast.show();
        });

    }
    //endregion


    //region ImagePicker

    // Se produit lors d'un clic sur le boutton "imagepicker" du menu Ajouter/Modifier
    public void OnClickImagePicker(View v)
    {
        isPicking = true;
        ImagePicker.with(this)
                .setToolbarColor("#FFA500")
                .setFolderMode(true)
                .setFolderTitle("Album")
                .setRootDirectoryName("/")
                .setDirectoryName("Cliché_Androscope")
                .setMultipleMode(false)
                .setDoneTitle("Ok")
                .setShowNumberIndicator(true)
                .setRequestCode(100)
                .start();
    }

    // Se produit lorsque la selection de l'image est aboutie
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (ImagePicker.shouldHandleResult(requestCode, resultCode, data, 100))
        {
            ArrayList<Image> images = ImagePicker.getImages(data);
            post.userInput.SetImageViewContent(new File(images.get(0).getPath()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //endregion

}
