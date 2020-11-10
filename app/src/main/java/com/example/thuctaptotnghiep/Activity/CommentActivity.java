package com.example.thuctaptotnghiep.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.thuctaptotnghiep.Adapter.CommentAdapter;
import com.example.thuctaptotnghiep.Model.Comment;
import com.example.thuctaptotnghiep.Model.Notification;
import com.example.thuctaptotnghiep.Model.User;
import com.example.thuctaptotnghiep.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;


    public static EditText addcomment;
    public static ImageView image_profile;
    public static TextView post, text,mention_user;

    String postid;
    String publisherid;

    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this,commentList);
        recyclerView.setAdapter(commentAdapter);

        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        text = findViewById(R.id.text);
        mention_user = findViewById(R.id.mention_user);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addcomment.getText().toString().equals("")){
                    Toast.makeText(CommentActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();

                    if(!firebaseUser.getUid().equals(publisherid)){
                        addNotification();
                    }else if(firebaseUser.getUid().equals(publisherid)&&!mention_user.getText().toString().equals("")&&!text.getText().toString().equals(publisherid)){
                        addNotification();
                    }

                    text.setText("");
                    mention_user.setText("");
                    mention_user.setVisibility(View.GONE);
                    addcomment.setText("");
                }
            }
        });

        getImage();
        readComments();

    }

    private void addNotification(){
        if(text.getText().toString().equals("")){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userid",firebaseUser.getUid());
            hashMap.put("text","commented in your post");
            hashMap.put("postid",postid);
            hashMap.put("ispost",true);
            hashMap.put("isSeen","false");
            hashMap.put("datetime",System.currentTimeMillis());

            //reference.push().setValue(hashMap);
            reference.child(firebaseUser.getUid()+"comment " + System.currentTimeMillis()+"time").setValue(hashMap);
        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(text.getText().toString().trim());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userid",firebaseUser.getUid());
            hashMap.put("text","mentioned you in a comment");
            hashMap.put("postid",postid);
            hashMap.put("ispost",true);
            hashMap.put("isSeen","false");
            hashMap.put("datetime",System.currentTimeMillis());

            //reference.push().setValue(hashMap);
            reference.child(firebaseUser.getUid()+"reply_comment " + System.currentTimeMillis()+"time").setValue(hashMap);
        }

    }

    private  void addComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        if(mention_user.getText().toString().equals("")){
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("comment",mention_user.getText().toString()+addcomment.getText().toString());
            hashMap.put("publisher",firebaseUser.getUid());
            hashMap.put("datetime",System.currentTimeMillis());
            hashMap.put("postid",postid);
            hashMap.put("post_publisherid",publisherid);
            hashMap.put("reply_cmt_pulisherid","");

            reference.child(String.valueOf(System.currentTimeMillis())).setValue(hashMap);
        } else{
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("comment",mention_user.getText().toString()+addcomment.getText().toString());
            hashMap.put("publisher",firebaseUser.getUid());
            hashMap.put("datetime",System.currentTimeMillis());
            hashMap.put("postid",postid);
            hashMap.put("post_publisherid",publisherid);
            hashMap.put("reply_cmt_pulisherid",text.getText().toString().trim());

            reference.child(String.valueOf(System.currentTimeMillis())).setValue(hashMap);
            // reference.push().setValue(hashMap);
        }


    }

    private void getImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user  = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}