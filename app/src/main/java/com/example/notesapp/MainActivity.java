package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.makeramen.roundedimageview.RoundedImageView;


import android.content.Intent;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;



import static com.example.notesapp.R.*;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE=1;
    private FirestoreRecyclerAdapter<Note,NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);


        this.getWindow().setStatusBarColor(this.getResources().getColor(color.colorPrimaryDark));

        findViewById(R.id.imageLogOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivityForResult(
                        new Intent(new Intent(getApplicationContext(), LoginActivity.class)),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

   
        findViewById(id.imageAddNoteMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(new Intent(getApplicationContext(), CreateNoteActivity.class)),
                        REQUEST_CODE_ADD_NOTE
                );
            }

        });


        FirebaseFirestore fstore= FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();

        Query query = fstore.collection("notes").document(user.getUid()).collection("mynotes").orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query, Note.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int position, @NonNull Note note) {
                String title=note.getTitle() , text=note.getContent(), date=note.getDate(), imagePath=note.getImage(),docId ;
                noteViewHolder.noteTitle.setText(title);
                noteViewHolder.noteContent.setText(text);
                noteViewHolder.noteDateTime.setText(date);
                docId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                noteViewHolder.imageNoteView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                noteViewHolder.imageNoteView.setVisibility(View.VISIBLE);

                noteViewHolder.imageDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fstore.collection("notes").document(user.getUid()).collection("mynotes").document(docId).delete();
                    }
                });

                noteViewHolder.imageEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(
                                    new Intent(new Intent(getApplicationContext(), CreateNoteActivity.class)),
                                    REQUEST_CODE_ADD_NOTE
                            );
                            Intent i = new Intent(MainActivity.this, CreateNoteActivity.class);
                            i.putExtra("title",title);
                            i.putExtra("text",text);
                            i.putExtra("date",date);
                            i.putExtra("image",imagePath);
                            i.putExtra("noteId",docId);
                            startActivity(i);
                      }
                    });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note,parent,false);
                return new NoteViewHolder(view);
            }
        };

        RecyclerView noteList = findViewById(R.id.notesRecyclerView);
        noteList.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList.setAdapter(noteAdapter);
    }


    public static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent, noteDateTime;
        ImageView imageEdit, imageDelete;
        RoundedImageView imageNoteView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteContent = itemView.findViewById(id.noteContent);
            noteDateTime = itemView.findViewById(R.id.noteDateTime);
            imageEdit = itemView.findViewById(R.id.imageEdit);
            imageDelete = itemView.findViewById(id.imageDelete);
            imageNoteView = itemView.findViewById(id.imageNoteView);


        }
    }


    @Override
    public void onBackPressed() { }

    @Override
    protected void onStart(){
        super.onStart();
        noteAdapter.startListening();
    }
}