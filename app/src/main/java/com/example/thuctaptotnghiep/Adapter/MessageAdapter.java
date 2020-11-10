package com.example.thuctaptotnghiep.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thuctaptotnghiep.Model.Chat;
import com.example.thuctaptotnghiep.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MessageAdapter extends  RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    public static final  int MSG_TYPE_LEFT = 0;
    public static final  int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Chat> listChat;
    private String imageurl;

    int Click = 0;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> listChat, String imageurl) {
        this.context = context;
        this.listChat = listChat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {
        final Chat chat = listChat.get(position);
        holder.show_message.setText(chat.getMessage());
        holder.datetime.setText(formatTimeNgayGio(chat.getDatetime())+"");
        Glide.with(context).load(imageurl).into(holder.profile_image);

        // isSeen Message
        if(listChat.get(position).getIsSeen().equals("true")){
            holder.isSeen.setText("Seen");
        }else{
            holder.isSeen.setText("Delivered");
        }

       
       holder.show_message.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Click++;
               if(Click%2==0){
                   holder.datetime.setVisibility(View.GONE);
                   holder.isSeen.setVisibility(View.GONE);
               }
               else {
                   holder.datetime.setVisibility(View.VISIBLE);
                   holder.isSeen.setVisibility(View.VISIBLE);
               }

           }
       });

        holder.show_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to revoke this message");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RevokeMessage(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });

    }

    private void  RevokeMessage(int position){
        final String myId = FirebaseAuth.getInstance().getUid();

        Long messTime = listChat.get(position).getDatetime();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = reference.orderByChild("datetime").equalTo(messTime);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("sender").getValue().equals(myId)){
                     //  snapshot.getRef().removeValue();
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("message","This message was revoked...");
                        snapshot.getRef().updateChildren(hashMap);



                        Toast.makeText(context, "message revoked ...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Can't revoke other's comment..", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message, isSeen,datetime;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            isSeen = itemView.findViewById(R.id.isSeen);
            datetime = itemView.findViewById(R.id.datetime);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(listChat.get(position).getSender().equals(firebaseUser.getUid())){
            return  MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    // Hàm chuyển timestamp sang datetime
    private String formatTimeNgayGio(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - EEE, dd MMM yyyy ");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
