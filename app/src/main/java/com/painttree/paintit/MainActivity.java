package com.painttree.paintit;

import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;
    private Boolean mProcessLike=false;
    private DatabaseReference mDatabaseLike;
    private CircleImageView navCircleImageView;
    private TextView navUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth=FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        mBlogList=(RecyclerView)findViewById(R.id.blog_list);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View mView=navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
        mDatabase=FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabase.keepSynced(true);
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        navCircleImageView = (CircleImageView) mView.findViewById(R.id.Navigation_image);
        navUsername = (TextView) mView.findViewById(R.id.Navigation_User);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        checkUserExist();
        setNavImage();



    }

    private void setNavImage() {
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String ref=(String)dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("image").getValue();
                Log.d("DATA",ref);
                Picasso.with(getApplicationContext()).load(ref).placeholder(R.drawable.fb_avatar).fit().networkPolicy(NetworkPolicy.OFFLINE).into(navCircleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplicationContext()).load(ref).into(navCircleImageView);

                    }
                });
                String name=(String)dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("name").getValue();
                navUsername.setText(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class, R.layout.blog_row,BlogViewHolder.class,mDatabase
        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, Blog model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setProfile(getApplicationContext(),model.getProfileImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setTime(DateUtils.getRelativeTimeSpanString(model.getTime()));
                viewHolder.setLikeBtn(post_key);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleBlogIntent=new Intent(MainActivity.this,BlogSingleActivity.class);
                        singleBlogIntent.putExtra("blog_id",post_key);
                        startActivity(singleBlogIntent);

                    }
                });
                viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike=true;

                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (mProcessLike) {
                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                            mProcessLike = false;
                                        } else {

                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                            mProcessLike = false;
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });



                    }
                });
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }

    private void checkUserExist() {

        if(mAuth.getCurrentUser()!=null) {

            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }

                }

                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

    }
    public static class BlogViewHolder extends RecyclerView.ViewHolder{
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        View mView;
        DatabaseReference mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
        ImageButton mLikeBtn;


        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mLikeBtn = (ImageButton)mView.findViewById(R.id.like_btn);
        }
        public void setLikeBtn(final String post_key){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                        mLikeBtn.setImageResource(R.drawable.ic_like);
                    }
                    else{
                        mLikeBtn.setImageResource(R.drawable.ic_second);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        public void setTitle(String title){

          TextView post_title=(TextView) mView.findViewById(R.id.post_title);
                post_title.setText(title);
        }
        public void setDesc(String desc){
            TextView post_desc=((TextView) mView.findViewById(R.id.post_desc));
            post_desc.setText(desc);

        }
        public void setTime(CharSequence time){
            TextView post_time=(TextView)mView.findViewById(R.id.tv_time);
            post_time.setText(time);
        }
        public void setUsername(String username){
            TextView post_username= (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }
        public void setImage(final Context ctx,final String image){
           final ImageView post_image= (ImageView) mView.findViewById(R.id.post_image);
            // Picasso.with(ctx).load(image).into(post_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                public void onSuccess() {
                }

                public void onError() {
                    Picasso.with(ctx).load(image).into(post_image);
                }
            });

        }
        public void setProfile(final Context ctx, final String profile){

            final CircleImageView profile_Image=(CircleImageView)mView.findViewById(R.id.iv_post_owner_image);

            Picasso.with(ctx).load(profile).placeholder(R.drawable.fb_avatar).fit().networkPolicy(NetworkPolicy.OFFLINE).into(profile_Image, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                  Picasso.with(ctx).load(profile).placeholder(R.drawable.fb_avatar).fit().into(profile_Image);
                }
            });

        }



    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(this, PostActivity.class));
        }
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if(item.getItemId()==R.id.action_setting){
            startActivity(new Intent(getApplicationContext(),About.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_help) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
