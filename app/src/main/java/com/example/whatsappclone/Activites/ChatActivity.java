package com.example.whatsappclone.Activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Adapters.MessagesAdapter;
import com.example.whatsappclone.Models.Message;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;
    String senderUid;
    String receiverUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        messages = new ArrayList<>();


        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");


        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.profile);

        binding.imageView2.setOnClickListener(v -> finish());

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(v -> {
            String messageTxt = binding.messageBox.getText().toString();

            Date date = new Date();
            Message message = new Message(messageTxt, senderUid, date.getTime());
            binding.messageBox.setText("");

            String randomKey = database.getReference().push().getKey();

            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg", message.getMessage());
            lastMsgObj.put("lastMsgTime", date.getTime());

            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomKey)
                    .setValue(message).addOnSuccessListener(aVoid -> database.getReference().child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message));

        });

        binding.attachment.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25) {
            if(data != null) {
                if(data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(task -> {
                        dialog.dismiss();
                        if(task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String filePath = uri.toString();

                                String messageTxt = binding.messageBox.getText().toString();

                                Date date = new Date();
                                Message message = new Message(messageTxt, senderUid, date.getTime());
                                message.setMessage("photo");
                                message.setImageUrl(filePath);
                                binding.messageBox.setText("");

                                String randomKey = database.getReference().push().getKey();

                                HashMap<String, Object> lastMsgObj = new HashMap<>();
                                lastMsgObj.put("lastMsg", message.getMessage());
                                lastMsgObj.put("lastMsgTime", date.getTime());

                                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                database.getReference().child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(aVoid -> database.getReference().child("chats")
                                                .child(receiverRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(aVoid1 -> {

                                                }));

                            });
                        }
                    });
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}