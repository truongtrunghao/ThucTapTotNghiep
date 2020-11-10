package com.example.thuctaptotnghiep.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.thuctaptotnghiep.Fragment.HomeFragment;
import com.example.thuctaptotnghiep.Model.Story;
import com.example.thuctaptotnghiep.Model.User;
import com.example.thuctaptotnghiep.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int count = 0;
    long pressTime = 0;
    long limit = 500;

    StoriesProgressView storiesProgressView;
    ImageView image, story_photo;
    TextView story_username;

    List<String> images;
    List<String> storyids;
    String userid;

    LinearLayout linear_seen;
    TextView count_seen;
    ImageView story_del;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        linear_seen = findViewById(R.id.linear_seen);
        count_seen = findViewById(R.id.count_seen);
        story_del = findViewById(R.id.story_del);

        linear_seen.setVisibility(View.GONE);
        story_del.setVisibility(View.GONE);


        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image);
        story_photo = findViewById(R.id.story_photo);
        story_username = findViewById(R.id.story_username);

        userid = getIntent().getStringExtra("userid");


        if(userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            linear_seen.setVisibility(View.VISIBLE);
            story_del.setVisibility(View.VISIBLE);
        }



        getStori(userid);
        userInfo(userid);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);


        linear_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this,FollowersActivity.class);
                intent.putExtra("id",userid);
                intent.putExtra("storyid",storyids.get(count));
                intent.putExtra("title","views");
                startActivity(intent);
            }
        });

        story_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.pause();
                AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(StoryActivity.this);
                final AlertDialog alertDialog = alertDialog1.create();
                alertDialog.setTitle("Do you want to delete it?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog.cancel();
                                storiesProgressView.resume();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                FirebaseDatabase.getInstance().getReference("Story").child(userid).child(storyids.get(count))
                                        .child("del").setValue(true);
                                FirebaseDatabase.getInstance().getReference("Story").child(userid).child(storyids.get(count))
                                        .child("timeend").setValue(System.currentTimeMillis()+140);
                                Intent intent = new Intent(StoryActivity.this, MainActivity.class);
                                startActivity(intent);
                               dialogInterface.dismiss();

                            }
                        });
                alertDialog.show();





            }
        });


    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++count)).into(image);

        addView(storyids.get(count));
        CountSeen(storyids.get(count));
    }

    @Override
    public void onPrev() {
        if((count-1)<0) return;
        Glide.with(getApplicationContext()).load(images.get(--count)).into(image);
        CountSeen(storyids.get(count));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStori(String userid){
        images = new ArrayList<>();
        storyids = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyids.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();
                    if(timecurrent>story.getTimestart() && timecurrent<story.getTimeend()){
                        images.add(story.getImageurl());
                        storyids.add(story.getStoryid());
                    }
                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(count);

                Glide.with(getApplicationContext()).load(images.get(count)).into(image);

                 addView(storyids.get(count));
                CountSeen(storyids.get(count));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void  userInfo(final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(story_photo);
                story_username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void addView(String storyid){
        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(storyid).child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
    }

    private void CountSeen(String storyid){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).child(storyid).child("views");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count_seen.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}