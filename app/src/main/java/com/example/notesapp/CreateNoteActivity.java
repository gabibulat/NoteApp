package com.example.notesapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class CreateNoteActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION=1, REQUEST_CODE_SELECT_IMAGE =2, REQUEST_CODE_ADD_NOTE=1;
    private ImageView imageRemoveImage,imageNote;
    public EditText inputNoteTitle, inputNote;
    private TextView textDateTime;
    private String noteId, imagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        this.getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

        inputNoteTitle= findViewById(R.id.inputNoteTitle);
        inputNote = findViewById(R.id.inputNote);
        textDateTime=findViewById(R.id.textDateTime);
        imageNote= findViewById(R.id.imageNote);
        imageRemoveImage=findViewById(R.id. imageRemoveImage);

        //Popunjavanja polja za edit note jer je u istom activity-u i create i edit
        if(getIntent().getStringExtra("title")!=null) {
            inputNoteTitle.setText(getIntent().getStringExtra("title"));
            inputNote.setText(getIntent().getStringExtra("text"));
            textDateTime.setText(getIntent().getStringExtra("date"));
            imageNote.setImageBitmap(BitmapFactory.decodeFile(getIntent().getStringExtra("image")));
            noteId=getIntent().getStringExtra("noteId");

            if(getIntent().getStringExtra("image")!=null) {
                imageNote.setVisibility(View.VISIBLE);
                imageRemoveImage.setVisibility(View.VISIBLE);
            }

        }

        //Stavljanje datuma nakon popunjavanja za edit, tako da kad se uredi ide novi datum zadnjeg uređivanja
        textDateTime.setText(new SimpleDateFormat("EEEE, dd.MM.yyyy. HH:mm a", Locale.getDefault()).format(new Date()));

        findViewById(R.id.imageReminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReminder();
            }
        });

        findViewById(R.id.imageSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveNote()==1){
                    v.setOnClickListener(null);
                }
            }
        });

        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else{
                    selectImage();
                }

            }
        });

        findViewById(R.id.imageBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePath=null;
                imageNote.setVisibility(View.GONE);
                imageRemoveImage.setVisibility(View.GONE);

            }
        });
    }

    private void showReminder() {
        AlertDialog.Builder reminder = new AlertDialog.Builder(this, R.style.CustomDialog);
        AlertDialog alert = reminder.create();

        LayoutInflater inflater = this.getLayoutInflater();
        View rem = inflater.inflate(R.layout.activity_set_reminder_acitvity, null);
        alert.setView(rem);
        alert.show();
        Button ok=(Button) rem.findViewById(R.id.textOk);
        Button cancel= (Button) rem.findViewById(R.id.textCancel);
        DatePicker datePicker = rem.findViewById(R.id.datePicker);
        TimePicker timePicker = rem.findViewById(R.id.timePicker);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(datePicker.getVisibility()== android.view.View.VISIBLE){
                    Toast.makeText(CreateNoteActivity.this,String.valueOf(datePicker.getDayOfMonth()),Toast.LENGTH_SHORT).show();
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.VISIBLE);
                    cancel.setText("Back");

                }else{

                    if(timePicker.getCurrentHour()>Calendar.getInstance().get(Calendar.HOUR_OF_DAY) |
                       timePicker.getCurrentHour()==Calendar.getInstance().get(Calendar.HOUR_OF_DAY) &
                       timePicker.getCurrentMinute()>Calendar.getInstance().get(Calendar.MINUTE)){


                       /*  Intent intent = new Intent(CreateNoteActivity.this,AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(CreateNoteActivity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager =(AlarmManager) getSystemService(ALARM_SERVICE);
 Calendar myAlarmDate = Calendar.getInstance();
                       / myAlarmDate.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        myAlarmDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                        myAlarmDate.set(Calendar.SECOND, 0);
                        myAlarmDate.set(Calendar.MILLISECOND, 0);
                        long millis = myAlarmDate.getTimeInMillis();

                        myAlarmDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());



                        alarmManager.set(AlarmManager.RTC_WAKEUP, myAlarmDate.getTimeInMillis(),pendingIntent);*/
                        alert.cancel();
                    }



                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cancel.getText()=="Back"){
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.GONE);
                    cancel.setText("Cancel");
                }
                else{

                    alert.cancel();
                }
            }
        });

    }



    private int saveNote() {
        String nTitle =inputNoteTitle.getText().toString(),
                nText =inputNote.getText().toString(),
                nDate =textDateTime.getText().toString();

        if(nTitle.trim().isEmpty()){
            Toast.makeText(this,"Note title can't be empty!",Toast.LENGTH_SHORT).show();
            return 0;
        }

        FirebaseFirestore fstore= FirebaseFirestore.getInstance();
        ProgressBar progressBarSave = findViewById(R.id.progressBar);
        progressBarSave.setVisibility(View.VISIBLE);

        //kad se edit radi: briše staru verziju i sprema ponovo sa novim datumom-zadnje uređivanje
       if(noteId !=null){
           fstore.collection("notes").document(noteId).delete();
            noteId=null;
       }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docref = fstore.collection("notes").document(user.getUid()).collection("mynotes").document();
        Map<String, Object> note = new HashMap<>();
        note.put("title", nTitle);
        note.put("content", nText);
        note.put("date", nDate);
        note.put("image",imagePath);

        docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreateNoteActivity.this,"Saved.", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateNoteActivity.this,"Error, try again.", Toast.LENGTH_SHORT).show();

            }
        });
        return 1;
    }

    //region Dopuštenje za dohvaćanje slika, biranje, prikazivanje odabrane slike u CreateNoteActivity-ju i spremanje putanje(za bazu)
    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage(){
        Intent intent=new Intent(Intent.ACTION_PICK,  android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        selectImage();
                    }else{
                        Toast.makeText(this, "Permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
            }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                        imagePath=getPathFromUri(selectedImageUri);

                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    }
                }
            }
        }

    private String getPathFromUri (Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri,null,null,null,null);
        if(cursor==null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath=cursor.getString(index);
            cursor.close();

        }
        return filePath;
    }
    //endregion

    @Override
    public void onBackPressed() {
        startActivityForResult(
                new Intent(new Intent(getApplicationContext(), MainActivity.class)),
                REQUEST_CODE_ADD_NOTE
        );
    }

}

