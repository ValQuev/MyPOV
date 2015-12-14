package fr.valquev.mypov.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.R;
import fr.valquev.mypov.User;
import fr.valquev.mypov.adapters.ObservationDetailsFragmentsAdapter;
import fr.valquev.mypov.fragments.ObservationDetailsComments;
import fr.valquev.mypov.fragments.ObservationDetailsContent;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by ValQuev on 27/09/15.
 */
public class ObservationDetails extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;

    private Context mContext;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private FloatingActionButton addCommentFAB;

    private Observation mObservation;
    private User mUser;

    private ObservationDetailsComments commentFrag;
    private ObservationDetailsContent descriptionFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_details);

        mContext = this;
        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            finish();
        }

        mObservation = getIntent().getParcelableExtra("observation");

        Bundle observation = new Bundle();
        observation.putParcelable("observation", mObservation);

        descriptionFrag = (ObservationDetailsContent) ObservationDetailsContent.instantiate(mContext, ObservationDetailsContent.class.getName(), observation);
        commentFrag = (ObservationDetailsComments) ObservationDetailsComments.instantiate(mContext, ObservationDetailsComments.class.getName(), observation);

        mViewPager = (ViewPager) findViewById(R.id.observation_details_viewpager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.observation_details_toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.observation_details_tabs);
        addCommentFAB = (FloatingActionButton) findViewById(R.id.fab_observation_details_add_comment);
        addCommentFAB.hide();

        toolbar.setTitle(mObservation.getNom() + " par " + mObservation.getObservateur().getPseudo());
        setSupportActionBar(toolbar);

        addCommentFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                final View v = getLayoutInflater().inflate(R.layout.observation_details_comments_write, null);
                builder.setView(v);
                builder.setMessage(mObservation.getNom());
                builder.setPositiveButton("Commenter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText commentET = (EditText) v.findViewById(R.id.observation_details_add_comment_text);

                        String comment = commentET.getText().toString();

                        if (comment.equals("")) {
                            commentET.setError("Vide");
                            return;
                        }

                        MyPOVClient.client.addComment(mObservation.getId(), comment, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                            @Override
                            public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                if (response.isSuccess()) {
                                    if (response.body().getStatus() == 0) {
                                        commentFrag.getComments();
                                    } else {
                                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                        mUser.logout();
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(mContext, response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Annuler", null);
                builder.show();
            }
        });

        setAB();

        setupViewPager();
    }

    private void setupViewPager() {
        ObservationDetailsFragmentsAdapter adapter = new ObservationDetailsFragmentsAdapter(getSupportFragmentManager());
        adapter.addFrag(descriptionFrag, getResources().getString(R.string.description_up));
        adapter.addFrag(commentFrag, getResources().getString(R.string.comments_up));

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(0);

        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        addCommentFAB.hide();
                        break;

                    case 1:
                        addCommentFAB.show();
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setAB() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case 0:
                if (item.getTitle().equals("Ajouter une photo")) {
                    Intent intent = new Intent();
                    // Show only images, no videos or anything else
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // Always show the chooser (if there are multiple options available)
                    startActivityForResult(Intent.createChooser(intent, "Sélectionnez une image"), PICK_IMAGE);
                } else {
                    try {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+ mObservation.getLat() +","+ mObservation.getLng());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    } catch (Exception e) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps"));
                        startActivity(i);
                        Toast.makeText(mContext, "Veuillez installer Google Maps", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case 1:
                Uri gmmIntentUri = Uri.parse("geo:"+ mObservation.getLat() +","+ mObservation.getLng());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;

            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //if (mObservation.getObservateur().getId_user() == mUser.getId_user()) {
            menu.add("Ajouter une photo");
            menu.add("Itinéraire");
        //}
        menu.add("Itinéraire");
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case PICK_IMAGE:
                    addImage(intent);
                    break;

                default:
                    break;
            }
        }
    }

    private void addImage(Intent intent) {
        try {
            final Uri selectedImage = intent.getData();
            final Bitmap userPic = decodeUri(selectedImage);

            double size = (userPic.getByteCount() / 8) / 1000000;

            if(size < 3.0D) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                userPic.compress(Bitmap.CompressFormat.PNG, 100, out);
                byte[] myByteArray = out.toByteArray();

                final File pic = new File(getApplicationContext().getFileStreamPath("test.jpg").getPath());
                pic.createNewFile();
                out.writeTo(new FileOutputStream(pic));

                Log.v("TEST", "TEST " + pic.getPath());

                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), pic);

                MyPOVClient.client.addPhotoObservation(requestBody, mObservation.getId(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                    @Override
                    public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            if (response.body().getStatus() == 0) {
                                Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                mUser.logout();
                                finish();
                            }
                            pic.delete();
                        } else {
                            Toast.makeText(mContext, response.code() + " - " + response.raw().message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(mContext, "L'image doit faire moins de 3mo", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 512;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(selectedImage), null, o2);

    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getFileNameByUri(Uri uri)
    {
        String fileName="unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content")==0)
        {
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                fileName = filePathUri.getLastPathSegment().toString();
            }
        }
        else if (uri.getScheme().compareTo("file")==0)
        {
            fileName = filePathUri.getLastPathSegment().toString();
        }
        else
        {
            fileName = fileName+"_"+filePathUri.getLastPathSegment();
        }
        return fileName;
    }
}
