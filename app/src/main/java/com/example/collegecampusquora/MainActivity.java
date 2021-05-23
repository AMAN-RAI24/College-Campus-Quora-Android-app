package com.example.collegecampusquora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.collegecampusquora.model.Post;
import com.example.collegecampusquora.model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnNoteListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DOWNLOAD_BATCH_SIZE = 10;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private PostAdapter adapter;
    private List<Post> itemList;
    private List<String> list;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser current_user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userref= FirebaseFirestore.getInstance().collection("Users");
    private CollectionReference postref= FirebaseFirestore.getInstance().collection("Posts");
    private DocumentSnapshot lastVisible=null;

    private boolean isScrolling = false;
    private boolean allPostsLoaded = false;
    private QueryUtils queryUtils = new QueryUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("tag  ", "onCreate Called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        itemList=new ArrayList<>();
        list=new ArrayList<String>();

        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(),NewPostActivity.class));
            }
        });
//        setUpRecyclerView();
        linearLayoutManager = new LinearLayoutManager(this);
    }

    protected void onStart() {
        super.onStart();
        //Log.v(LOG_TAG, "onStart Called");

        current_user = mAuth.getCurrentUser();
        if(current_user == null) {
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
        }
        setUpRecyclerView();
        Toast.makeText(getApplicationContext(),"Complete Your Profile Under Settings",Toast.LENGTH_LONG).show();

    }
    protected void onPause(){
        super.onPause();
    }
    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        if(current_user == null) {
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
        }
        itemList.clear();
        getPostsFromFirestore();
        adapter = new PostAdapter(itemList,current_user,this,this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int currentItems = linearLayoutManager.getChildCount();
                int totalItems = linearLayoutManager.getItemCount();
                int scrolledItems = linearLayoutManager.findFirstVisibleItemPosition();

                if(isScrolling && currentItems + scrolledItems == totalItems) {
                    isScrolling = false;
                    getPostsFromFirestore();
                }
            }
        });
    }

    private void getPostsFromFirestore() {
        if(allPostsLoaded) return;
        Query query;
        if(lastVisible != null) {
            query = postref.orderBy("postTime", Query.Direction.ASCENDING).startAfter(lastVisible).limit(DOWNLOAD_BATCH_SIZE);
        } else {
            query = postref.orderBy("postTime").limit(DOWNLOAD_BATCH_SIZE);
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(querySnapshot != null) {
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            Object tempObject = documentSnapshot.get("Heading");
                            String postHeading = null;
                            if (tempObject != null) {
                                postHeading = tempObject.toString();
                            }
                            tempObject = documentSnapshot.get("Text");
                            //tempObject = documentSnapshot.get("UserID").;
                           String postText = null;
                            if (tempObject != null) {
                                postText = tempObject.toString();
                            }
                            itemList.add(new Post(documentSnapshot.getId(), postHeading, postText,
                                    documentSnapshot.getLong("Likes"),
                                    documentSnapshot.getLong("Dislikes"),
                                    documentSnapshot.getLong("NumberOfComments"),
                                    documentSnapshot.getLong("postTime"),
                                    documentSnapshot.getString("imageURL")));
                        }
                        if (querySnapshot.getDocuments().size() > 0) {
                            lastVisible = querySnapshot.getDocuments().get(task.getResult().getDocuments().size() - 1);
                        } else {
                            lastVisible = null;
                            allPostsLoaded = true;
                        }
                    }
                    adapter.notifyDataSetChanged();
                    adapter.filterList(itemList);

                }
                else{
                    String error= Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.sign_out){
            signOut();
        }
        if(item.getItemId() == R.id.settings){
            finish();
            startActivity(new Intent(this,ProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        mAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    @Override
    public void onNoteClick(Post it) {
        Toast.makeText(MainActivity.this, "Okay", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpvoteClick(Post it) {
          queryUtils.onUpvoteClick(this,postref,it,current_user,userref,adapter);
    }

    @Override
    public void onDownvoteClick(Post it) {
        queryUtils.onDownvoteClick(this, postref, it, current_user, userref, adapter);
    }
}