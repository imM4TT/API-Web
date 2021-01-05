/*
        Classe UserInput qui se charge de récupérer les données saisies par l'utilisateur, vérifie si il y'a des erreur
        Auteur :  MATTEI Paul
        Dernière Modification : 24/06
 */

package com.example.androidapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.HashMap;

public class UserInput {

    private int index_layout;
    public Activity activity;

    private String nom = "";
    private String prenom = "";
    private String email = "";
    private String telephone = "";
    private String promo = "";
    private String td = "";
    private String tp = "";
    private String id = "";
    private String image = "";
    private boolean aStudent = true;

    private EditText edt_nom, edt_prenom, edt_email, edt_telephone, edt_activite, edt_id;
    public RadioGroup rg_status, rg_td, rg_tp;
    private Spinner spinner_promo;



    private TextView label_act, label_td, label_tp;

    public HashMap<String, String> input = new HashMap<>();

    //region Image Button
    public ImageView imageView;
    public File file = null;
    public Button btnImg;
    //endregion

    public String error = "";

    UserInput()
    {
        index_layout = MainActivity.index;
        activity = MainActivity.mainAct;
        Init();
    }




    //region Main Attribut

    public void PutNom()
    {
        String n = edt_nom.getText().toString();
        if(n.length() > 2 & n.length() < 15)
        {
            nom = n;
            input.put("nom", n);
        }
    }
    public void PutPrenom()
    {
        String p = edt_prenom.getText().toString();
        if(p.length() > 2 && p.length() < 20)
        {
            prenom = p;
            input.put("prenom", p);
        }
    }
    public void PutEmail()
    {
        String e = edt_email.getText().toString();
        if(e.length() > 2 && e.length() < 40)
        {
            email = e;
            input.put("email", e);
        }
    }
    public void PutTelephone()
    {
        String t = edt_telephone.getText().toString();
        if(t.length() > 9 && t.length() < 20)
        {
            telephone = t;
            input.put("telephone", t);
        }
    }
    public void PutPromo()
    {
        try
        {
            promo = spinner_promo.getSelectedItem().toString();
            if(promo.contains("."))
            {
                return;
            }
            input.put("promo", promo);
        }
        catch (NumberFormatException e)
        {
        }
        catch (NullPointerException e)
        {
        }
    }
    public void PutActivite()
    {
        String a = edt_activite.getText().toString();
        if(a.length() > 0 && a.length() < 20)
        {
            if(aStudent)
            {
                input.put("etude", a);
                input.put("emploi", "");
            }
            else
            {
                input.put("emploi", a);
                input.put("etude", "");
            }
        }
    }
    public void PutTp()
    {
        if(!aStudent)
        {
            input.put("tp", "");
            return;
        }
        int index = 0;
        try
        {
            index = rg_tp.getCheckedRadioButtonId();
            RadioButton rb = MainActivity.mainAct.findViewById(index);
            tp = rb.getText().toString();
            input.put("tp", tp);
        }
        catch (NullPointerException e)
        {
            //error += "tp";
            return;
        }
    }
    public void PutTd()
    {
        if(!aStudent)
        {
            input.put("td", "");
            return;
        }
        int index = 0;
        try
        {
            index = rg_td.getCheckedRadioButtonId();
            RadioButton rb = MainActivity.mainAct.findViewById(index);
            td = rb.getText().toString();
            input.put("td", td);
        }
        catch (NullPointerException e)
        {
            //error += "td";
            return;
        }
    }
    public void PutId()
    {
        String i = edt_id.getText().toString();
        if(i.length() > 2 && i.length() < 20)
        {
            id = i;
            input.put("id", i);
        }
    }
    public void PutImage() {
        if(image == "") {
            System.out.println("NUL");
            return;
        }
        System.out.println("PAS NUL");
        if(image.length() > 2)
        {
            input.put("photo", image);
        }
        System.out.println("OK FIN POUR L'INSERTION DE L'IMAGE DANS LA REQUETE");
    }

    // endregion


    // region Accesseurs
    // region Edit Text
    private void SetEdt()
    {
        SetEdt_Nom();
        SetEdt_Prenom();
        SetEdt_Email();
        SetEdt_Telephone();
        SetEdt_Activite();
        SetEdt_Id();
    }
    private void SetEdt_Nom()
    {
        if(index_layout == 1)
            edt_nom = activity.findViewById(R.id.editText_nom);

        edt_nom.setText("");
    }
    private void SetEdt_Prenom()
    {
        if(index_layout == 1)
            edt_prenom = activity.findViewById(R.id.editText_prenom);

        edt_prenom.setText("");
    }
    private void SetEdt_Email()
    {
        if(index_layout == 1)
            edt_email = activity.findViewById(R.id.editText_email);

        edt_email.setText("");
    }
    private void SetEdt_Telephone()
    {
        if(index_layout == 1)
            edt_telephone = activity.findViewById(R.id.editText_tel);

        edt_telephone.setText("");
    }
    private void SetEdt_Activite()
    {
        if(index_layout == 1)
            edt_activite = activity.findViewById(R.id.editText_activite);

        edt_activite.setText("");
    }
    private void SetEdt_Id()
    {
        if(index_layout == 1)
            edt_id = activity.findViewById(R.id.editTextId);

        edt_id.setText("0000");
    }

    public EditText getEdt_Id()
    {
        return edt_id;
    }
    // endregion

    // region Radio
    int t = 0;
    private void SetRadio() {
        SetRg_Status();
        SetRg_Td();
        SetRg_Tp();

        rg_status.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = activity.findViewById(checkedId);
            if(checkedId == -1)
            {
                return;
            }

            if(rb.getText().toString().equals("Ancien"))
            {
                rg_tp.setVisibility(View.INVISIBLE);
                rg_td.setVisibility(View.INVISIBLE);
                label_tp.setVisibility(View.INVISIBLE);
                label_td.setVisibility(View.INVISIBLE);
                label_act.setText(R.string.emploi);
            }
            else
            {
                rg_tp.setVisibility(View.VISIBLE);
                rg_td.setVisibility(View.VISIBLE);
                label_tp.setVisibility(View.VISIBLE);
                label_td.setVisibility(View.VISIBLE);
                label_act.setText(R.string.poursuite);
            }
        });
    }

    private void SetRg_Status()
    {
        rg_status = activity.findViewById(R.id.rg_status);
        rg_status.check(R.id.rbE);
    }
    private void SetRg_Td()
    {
        rg_td = activity.findViewById(R.id.rg_td);
        rg_td.setVisibility(View.VISIBLE);
        rg_td.clearCheck();
    }
    private void SetRg_Tp()
    {
        rg_tp = activity.findViewById(R.id.rg_tp);
        rg_tp.setVisibility(View.VISIBLE);
        rg_tp.clearCheck();
    }

    // endregion

    //region Image

    public void SetImageViewContent(File f)
    {
        file = f;
        final Target target = new Target()
        {
            @Override
            public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from) {
                if(bitmap.getByteCount() > 2000000)
                {
                    MainActivity.MakeToast("Image trop lourde.",400);
                    imageView.setImageResource(android.R.color.transparent);
                    image = "";
                    return;
                }
                System.out.println("OK image loaded");

                imageView.setImageBitmap(bitmap);

                Bitmap b = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
                image = Formatter.bmpToStr64(b);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                MainActivity.MakeToast("Erreur lors du chargement de l'image",400);
                image = "";
                imageView.setImageResource(android.R.color.transparent);System.out.println("3");
                return;
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}


        };
        imageView.setTag(target);
        Picasso.get().load(f).resize(180, 180).centerCrop().into(target);

    }
    //endregion

    //region Label

    private void SetLabel()
    {
        label_act = activity.findViewById(R.id.textView_activite);
        label_td = activity.findViewById(R.id.textView_td);
        label_tp = activity.findViewById(R.id.textView_tp);
        label_act.setText(R.string.poursuite);
        label_td.setVisibility(View.VISIBLE);
        label_tp.setVisibility(View.VISIBLE);
    }


    // endregion

    //region Spinner
    private void SetSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity.getApplicationContext(), R.array.array_promo, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner_promo = activity.findViewById(R.id.spinner_promo);
        spinner_promo.setAdapter(adapter);
    }
    //endregion

    //region Button
    private void SetButton()
    {
        btnImg = activity.findViewById(R.id.button_img);
    }
    //endregion

    //region ImageView

    private void SetImgView()
    {
        imageView = activity.findViewById(R.id.imgview);
        imageView.setImageResource(android.R.color.transparent);
    }

    //endregion

    // endregion


    //region Public
    public void SetStudent()
    {
        int index = 0;
        try
        {
            index = rg_status.getCheckedRadioButtonId();
            RadioButton rb = MainActivity.mainAct.findViewById(index);
            String s = rb.getText().toString();
            if(s.contains("Ancien"))
                aStudent=false;
            else
                aStudent=true;
        }
        catch (NullPointerException e)
        {
            error += "student";
            return;
        }
    }
    //endregion


    // region Private

    private void Init()
    {
        SetLabel();
        SetEdt();
        SetRadio();
        SetSpinner();
        SetButton();
        SetImgView();
        Reset();
    }

    public boolean PutValueAndContinue()
    {
        Reset();
        SetStudent();
        PutNom();
        PutPrenom();
        PutEmail();
        PutTelephone();
        PutPromo();
        PutActivite();
        PutTp();
        PutTd();
        PutId();
        CheckEdtInput();//verif des champs edit text, les autres inputs sont vérifié à la volé ( radio, spinner )
        PutImage();

        if(error.isEmpty())
            System.out.println(error + " Pas d'erreur de saisi input");
        else
            System.out.println(error.split("\\+").length + "Input(s) invalide(s) " + error);

        return error.length() <= 0;
    }

    private void CheckEdtInput()
    {
        if(edt_nom.length() != 0 && edt_nom.length() < 4)
        {
            error += "Nom+";
        }
        if(edt_prenom.length() != 0 && edt_prenom.length() < 2)
        {
            error += "Prénom+";
        }
        if(edt_email.length() != 0 && edt_email.length() < 8)
        {
            error += "Email+";
        }
        if(edt_telephone.length() != 0 && edt_telephone.length() < 10)
        {
            error += "Telephone+";
        }
        if(edt_activite.length() != 0 && edt_activite.length() < 3)
        {
            error += "Activité+";
        }
        if(edt_id.length() < 4)
        {
            error += "ID+";
        }
    }

    private void Reset()
    {
        error = "";
        input.clear();
    }


    // endregion



}
