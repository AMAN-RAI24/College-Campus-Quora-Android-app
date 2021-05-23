package com.example.collegecampusquora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postref = db.collection("Posts");
    private EditText heading;
    private EditText text;
    private Button button_post;
    private Button button_choose;
    private Date date;
    private Long ptime;
    private ArrayList<String> tags;
    private ImageView imageView;
    private String uid;
    private final int PICK_IMAGE_REQUEST = 71;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth mauth;
    private FirebaseUser user;
    private static final String TAG = NewPostActivity.class.getSimpleName();

    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mauth=FirebaseAuth.getInstance();
        user=mauth.getCurrentUser();

        heading = findViewById(R.id.heading);
        text = findViewById(R.id.text);
        button_post = findViewById(R.id.button_post);
        button_choose = findViewById(R.id.button_choose);
        date = new Date();
        ptime = date.getTime();
        tags = new ArrayList<>();
        uid= user.getUid();
        //uid = "fdgjjhh654876";
        imageView = findViewById(R.id.imageView);
        button_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
                //dispatchTakePictureIntent();
            }
        });

            //Toast.makeText(getApplicationContext(),"heading and text can't be empty",Toast.LENGTH_SHORT).show();
        button_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Map<String,Object> post = new HashMap<>();
                    post.put("Heading",heading.getText().toString());
                    post.put("Text",text.getText().toString());
                    post.put("Likes",0);
                    post.put("Dislikes",0);
                    post.put("UserID",uid);
                    post.put("postTime",ptime);
                    QueryUtils queryUtils = new QueryUtils();
                    final String postId = queryUtils.generateUniqueId();
                    Bitmap bitmap;
                    try {
                        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    } catch (Exception e) {
                        bitmap = null;
                    }
                    if(bitmap != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        final StorageReference ref = storage.getReference().child("images/" + postId + ".jpg");
                        UploadTask uploadTask = ref.putBytes(data);
                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return ref.getDownloadUrl();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageURL = null;
                                if (uri != null) {
                                    imageURL = uri.toString();
                                }
                                post.put("imageURL", imageURL);
                                postref.document(postId).set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(NewPostActivity.this, "Error Adding Document", Toast.LENGTH_SHORT).show();
                                        Log.w(TAG, "Error adding document", e);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NewPostActivity.this, "Error Adding Document", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                    } else {
                        postref.document(postId).set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NewPostActivity.this, "Error Adding Document", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                    }
                }
            });
    }

   private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    // following method is for camera integration
//    private void dispatchTakePictureIntent() {
//    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//    try {
//        startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST);
//    } catch (ActivityNotFoundException e) {
//        // display error state to the user
//        e.printStackTrace();
//    }
//}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}