package com.example.thuctaptotnghiep.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thuctaptotnghiep.Activity.CommentActivity;
import com.example.thuctaptotnghiep.Activity.MainActivity;
import com.example.thuctaptotnghiep.Model.Comment;
import com.example.thuctaptotnghiep.Model.User;
import com.example.thuctaptotnghiep.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mComment;

    private FirebaseUser firebaseUser;

    int turn_reply = 0;


    public CommentAdapter(Context mContext, List<Comment> mComment) {
        this.mContext = mContext;
        this.mComment = mComment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item,parent,false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = mComment.get(position);
        holder.comment.setText(comment.getComment());
        holder.date.setText(formatTimeNgay(comment.getDatetime()));

        getUserInfo(holder.image_profile,holder.username,comment.getPublisher());

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid",comment.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid",comment.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turn_reply++;
                if(turn_reply%2!=0){
                    CommentActivity.mention_user.setVisibility(View.VISIBLE);
                    CommentActivity.text.setText(comment.getPublisher());
                    CommentActivity.mention_user.setText("@"+holder.username.getText().toString()+": ");
                }else{
                    CommentActivity.mention_user.setVisibility(View.GONE);
                    CommentActivity.text.setText("");
                    CommentActivity.mention_user.setText("");
                }

            }
        });

        holder.linear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                holder.linear_function.setVisibility(View.VISIBLE);
                return false;
            }
        });

        if(comment.getPublisher().equals(firebaseUser.getUid())){
            holder.edit.setVisibility(View.VISIBLE);
            holder.del.setVisibility(View.VISIBLE);
        }else{
            holder.edit.setVisibility(View.GONE);
            holder.del.setVisibility(View.GONE);
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editComment(comment.getDatetime(),comment.getPostid(),comment.getComment());
            }
        });

        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseUser.getUid()==comment.getPublisher()){
                    AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(mContext);
                    final AlertDialog alertDialog = alertDialog1.create();
                    alertDialog.setTitle("Do you want to delete it?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alertDialog.cancel();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                   for(int dem1 = 0; dem1 < 3 ; dem1++){
                                        FirebaseDatabase.getInstance().getReference().child("Comments").child(comment.getPostid()).child(String.valueOf(comment.getDatetime()+dem1)).removeValue();
                                    }
                                    for(int dem = 0; dem <= 5; dem++){
                                        FirebaseDatabase.getInstance().getReference().child("Notifications").child(comment.getPost_publisherid())
                                                .child(firebaseUser.getUid()+"comment "+String.valueOf(comment.getDatetime()+dem) +"time").removeValue();
                                        FirebaseDatabase.getInstance().getReference().child("Notifications").child(comment.getReply_cmt_pulisherid())
                                                .child(firebaseUser.getUid()+"reply_comment "+String.valueOf(comment.getDatetime()+dem) +"time").removeValue();
                                    }





                                }
                            });
                    alertDialog.show();
                }
                else {
                    Toast.makeText(mContext, "Can't delete other's comment..", Toast.LENGTH_SHORT).show();
                }
            }
        });







    }



    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile;
        public TextView username, comment, date, reply, edit, del;
        LinearLayout linear,linear_function;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            date = itemView.findViewById(R.id.date);
            reply = itemView.findViewById(R.id.reply);
            edit = itemView.findViewById(R.id.edit);
            del = itemView.findViewById(R.id.del);
            linear = itemView.findViewById(R.id.linear);
            linear_function = itemView.findViewById(R.id.linear_function);




        }
    }

    private void editComment(final Long datetime, final String postid, String comment){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Edit Comment");

        final EditText editText = new EditText(mContext);
        editText.setText(comment);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(lp);
        builder.setView(editText);


        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("comment",editText.getText().toString().trim());
                FirebaseDatabase.getInstance().getReference("Comments")
                        .child(postid).child(String.valueOf(datetime)).updateChildren(hashMap);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherid);
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

    // Hàm chuyển timestamp sang datetime
    private String formatTimeNgay(long timestamp) {
        //  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - EEE, dd MMM yyyy ");
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy ");
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
