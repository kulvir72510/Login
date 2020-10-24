package com.kulvir72510.logindata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import static android.widget.Toast.LENGTH_LONG;

public class signup extends AppCompatActivity {
    EditText namesignup,branchsignup,yearsignup,possignup,phonesignup,emailsignup,passsignup;
    ImageView dp;
    Button savebut;

    public Uri imageUri;
    User user=new User();
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference dreff;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        namesignup=findViewById(R.id.namesignup);
        branchsignup=findViewById(R.id.branchsignup);
        yearsignup=findViewById(R.id.yearsignup);
        possignup=findViewById(R.id.possignup);
        phonesignup=findViewById(R.id.phonesignup);
        emailsignup=findViewById(R.id.emailsignup);
        passsignup=findViewById(R.id.passsignup);
        dp=(ImageView)findViewById(R.id.dp);
        savebut=findViewById(R.id.savebut);

        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        dreff=FirebaseDatabase.getInstance().getReference().child("Data");

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosepicture();
            }
        });

        savebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imageid;
                imageid=System.currentTimeMillis()+","+getExtension(imageUri);
                int y=Integer.parseInt(yearsignup.getText().toString().trim());
                long p=Long.parseLong(phonesignup.getText().toString().trim());

                user.setName(namesignup.getText().toString().trim());
                user.setBranch(branchsignup.getText().toString().trim());
                user.setYear(y);
                user.setPosition(possignup.getText().toString().trim());
                user.setPhone(p);
                user.setEmail(emailsignup.getText().toString().trim());
                user.setPass(passsignup.getText().toString().trim());
                user.setImageid(imageid);
                dreff.push().setValue(user);
                mAuth.createUserWithEmailAndPassword(emailsignup.getText().toString().trim(),passsignup.getText().toString().trim()).addOnCompleteListener(signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(signup.this,"data  not saved successfully",Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(signup.this,"data saved successfully",Toast.LENGTH_LONG).show();
                            Intent intent=new Intent(signup.this,MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });



            }
        });
    }

    private void choosepicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==RESULT_OK && data != null && data.getData()!=null){
            imageUri=data.getData();
            dp.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {

        final ProgressDialog pd =new ProgressDialog(this);
        pd.setTitle("Uploading Image");
        pd.show();

        final String randomkey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/"+randomkey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content),"image uploaded",Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"failed to uploaded", LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progressPercent= (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        pd.setMessage("Percentage: "+(int) progressPercent+"%");
                    }
                });
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
}