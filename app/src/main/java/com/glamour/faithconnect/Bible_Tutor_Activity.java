package com.glamour.faithconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class Bible_Tutor_Activity extends AppCompatActivity {
RecyclerView recyclerView;
EditText editMessage;
ImageView btnSend;
List<Message> messageList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible_tutor);

recyclerView=findViewById(R.id.chatList);
btnSend=findViewById(R.id.message_send);
editMessage=findViewById(R.id.editText);

btnSend.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        messageList = new ArrayList<>();

    }
});

    }
}