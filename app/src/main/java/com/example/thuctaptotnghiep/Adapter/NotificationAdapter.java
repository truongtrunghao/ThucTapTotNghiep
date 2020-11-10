package com.example.thuctaptotnghiep.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thuctaptotnghiep.Fragment.PostDetailFragment;
import com.example.thuctaptotnghiep.Fragment.ProfileFragment;
import com.example.thuctaptotnghiep.Model.Chat;
import com.example.thuctaptotnghiep.Model.Notification;
import com.example.thuctaptotnghiep.Model.Post;
import com.example.thuctaptotnghiep.Model.User;
import com.example.thuctaptotnghiep.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item,parent,false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Notification notification = mNotification.get(position);
        holder.text.setText(notification.getText());
        holder.date.setText(formatTimeNgay(notification.getDatetime()));
        getUser(holder.image_profile,holder.username,notification.getUserid());

        if(notification.isIspost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPost(holder.post_image,notification.getPostid());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(notification.isIspost()){
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                    editor.putString("postid",notification.getPostid());
                    editor.apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new PostDetailFragment()).commit();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Notification notification = snapshot.getValue(Notification.class);
                                if(notification.getDatetime().equals(mNotification.get(position).getDatetime())){
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("isSeen","true");
                                    snapshot.getRef().updateChildren(hashMap);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                    editor.putString("profileid",notification.getUserid());
                    editor.apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Notification notification = snapshot.getValue(Notification.class);
                                if(notification.getDatetime().equals(mNotification.get(position).getDatetime())){
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("isSeen","true");
                                    snapshot.getRef().updateChildren(hashMap);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        if(notification.getIsSeen().equals("false")){
            holder.itemView.setBackgroundResource(R.drawable.background_comment);
        }else{
            holder.itemView.setBackgroundResource(R.color.white);
        }
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, post_image;
        public TextView username, text, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.text);
            date = itemView.findViewById(R.id.date);
        }
    }

    private void getUser(final ImageView imageView, final TextView username, String publisher){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisher);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPost(final ImageView imageView, final String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Glide.with(mContext).load(post.getPostimage()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Hàm chuyển timestamp sang datetime
    private String formatTimeNgay(long timestamp) {
        //  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - EEE, dd MMM yyyy ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
    private String formatTimeGio(long timestamp) {
        //  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - EEE, dd MMM yyyy ");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
