package com.example.phoneauthentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class WelcomeActivity extends AppCompatActivity {

    private TextView dataText;
    private Button deleteBtn, uploadImage;
    private ImageView profileImage;


    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private StorageReference profileRefference;

    private static final int GALLERY = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        dataText = findViewById(R.id.dataTextId);
        deleteBtn = findViewById(R.id.deleteBtnId);
        uploadImage = findViewById(R.id.addImgId);
        profileImage = findViewById(R.id.imgViewId);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.keepSynced(true);
        profileRefference = FirebaseStorage.getInstance().getReference();

        String userId = currentUser.getUid();
        databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("profile image")) {

                    String imageUrl = dataSnapshot.child("profile image").getValue().toString();

                    Picasso.get().load(imageUrl).into(profileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                databaseReference.child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        Toast.makeText(WelcomeActivity.this, "Data has been deleted successfully", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });


        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);


                startActivityForResult(Intent.createChooser(gallery, "Select and Image"), GALLERY);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == GALLERY && resultCode == RESULT_OK) {


            Uri imageUri = data.getData();
            final String cUser = currentUser.getUid();

            final StorageReference filepath = profileRefference.child("profile_image").child(cUser + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    
                                    
                                    String url = uri.toString();
                                    
                                    databaseReference.child(cUser).child("profile image").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            
                                       if (task.isSuccessful()){

                                           Toast.makeText(WelcomeActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                       }
                                       
                                        }
                                    });
                                    
                                }
                            });

                    }
                }
            });


        }

    }
}
