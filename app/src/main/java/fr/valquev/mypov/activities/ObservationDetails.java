package fr.valquev.mypov.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;

import fr.valquev.mypov.ImagePicker;
import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.NoSwipeViewPager;
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
    private static final int NEW_LOCATION = 101;

    private Context mContext;
    private NoSwipeViewPager mViewPager;
    private TabLayout mTabLayout;
    private FloatingActionButton addCommentFAB;

    private Observation mObservation;
    private User mUser;

    private ObservationDetailsFragmentsAdapter adapter;
    private ObservationDetailsComments commentFrag;
    private ObservationDetailsContent descriptionFrag;

    private View dialogView;
    private ProgressDialog dialogUpdating;

    private LatLng newLocation;

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

        newLocation = new LatLng(mObservation.getLat(), mObservation.getLng());

        Bundle observation = new Bundle();
        observation.putParcelable("observation", mObservation);

        descriptionFrag = (ObservationDetailsContent) ObservationDetailsContent.instantiate(mContext, ObservationDetailsContent.class.getName(), observation);
        commentFrag = (ObservationDetailsComments) ObservationDetailsComments.instantiate(mContext, ObservationDetailsComments.class.getName(), observation);

        mViewPager = (NoSwipeViewPager) findViewById(R.id.observation_details_viewpager);
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
                builder.setTitle(mObservation.getNom());
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
                                        commentFrag.getComments(false);
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
        adapter = new ObservationDetailsFragmentsAdapter(getSupportFragmentManager());
        adapter.addFrag(descriptionFrag, getResources().getString(R.string.description_up));
        adapter.addFrag(commentFrag, getResources().getString(R.string.comments_up) + "(" + mObservation.getNb_commentaires() + ")");

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

    private void refreshCommentsTitle() {
        adapter.setCommentTitle(getResources().getString(R.string.comments_up) + "(" + mObservation.getNb_commentaires() + ")");
        adapter.notifyDataSetChanged();
        mTabLayout.setTabsFromPagerAdapter(adapter);
        mViewPager.setCurrentItem(1);
    }

    public void setNbCommentsTitle(int nbCommentsTitle) {
        mObservation.setNb_commentaires(nbCommentsTitle);
        refreshCommentsTitle();
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
                    Intent chooseImageIntent = ImagePicker.getPickImageIntent(mContext);
                    startActivityForResult(chooseImageIntent, PICK_IMAGE);
                } else if (item.getTitle().equals("Itinéraire")) {
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
                } else if(item.getTitle().equals("Supprimer l'observation")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                    builder.setTitle(mObservation.getNom());
                    builder.setMessage("Attention ! Il est impossible de restaurer une observation supprimée. Êtes-vous bien sûr de vouloir supprimer cette observation ?");
                    builder.setNegativeButton("Non", null);
                    builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final ProgressDialog dialogdel = ProgressDialog.show(mContext, "Suppression en cours", "Chargement, veuillez patienter...", true);
                            MyPOVClient.client.deleteObservation(mObservation.getId(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                                @Override
                                public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                    if (response.isSuccess()) {
                                        if (response.body().getStatus() == 0) {
                                            Toast.makeText(mContext, "Observation supprimée", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                            mUser.logout();
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(mContext, response.code() + " - " + response.raw().message(), Toast.LENGTH_LONG).show();
                                    }
                                    dialogdel.cancel();
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                    dialogdel.cancel();
                                }
                            });
                        }
                    });
                    builder.show();
                } else if(item.getTitle().equals("Modifier l'observation")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                    dialogView = getLayoutInflater().inflate(R.layout.dialog_modif_observation, null);
                    ((DatePicker) dialogView.findViewById(R.id.observation_modif_date)).setMaxDate(System.currentTimeMillis());
                    ((DatePicker) dialogView.findViewById(R.id.observation_modif_date)).setCalendarViewShown(false);
                    ((DatePicker) dialogView.findViewById(R.id.observation_modif_date)).setSpinnersShown(true);
                    dialogView.findViewById(R.id.observation_modif_localisation).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, NewLocation.class);
                            i.putExtra("observation", mObservation);
                            startActivityForResult(i, NEW_LOCATION);
                        }
                    });
                    ((EditText) dialogView.findViewById(R.id.observation_modif_name_text)).setText(mObservation.getNom());
                    ((EditText) dialogView.findViewById(R.id.observation_modif_description_text)).setText(mObservation.getDescription());
                    builder.setView(dialogView);
                    builder.setTitle("Modifier une observation");
                    builder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText nomET = (EditText) dialogView.findViewById(R.id.observation_modif_name_text);
                            EditText descriptionET = (EditText) dialogView.findViewById(R.id.observation_modif_description_text);
                            DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.observation_modif_date);

                            String nom = nomET.getText().toString();

                            if (nom.equals("")) {
                                nomET.setError("Vide");
                                return;
                            }

                            String description = descriptionET.getText().toString();

                            if (description.equals("")) {
                                descriptionET.setError("Vide");
                                return;
                            }

                            dialogUpdating = ProgressDialog.show(mContext, "Modification de l'observation", "Chargement, veuillez patienter...", true);
//datePicker.getCalendarView().getDate()
                            MyPOVClient.client.setObservation(mObservation.getId(), newLocation.latitude, newLocation.longitude, nom, description, System.currentTimeMillis() / 1000, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<Observation>>() {
                                @Override
                                public void onResponse(Response<MyPOVResponse<Observation>> response, Retrofit retrofit) {
                                    if (response.isSuccess()) {
                                        if (response.body().getStatus() == 0) {
                                            mObservation = response.body().getObject();
                                            Intent intent = new Intent(mContext, ObservationDetails.class);
                                            intent.putExtra("observation", mObservation);
                                            startActivity(intent);
                                            finish();
                                            Toast.makeText(mContext, "Modification effectuée", Toast.LENGTH_LONG).show();
                                            dialogUpdating.cancel();
                                        } else {
                                            dialogUpdating.cancel();
                                            Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                            mUser.logout();
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(mContext, response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                                        dialogUpdating.cancel();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                    dialogUpdating.cancel();
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Annuler", null);
                    builder.show();
                }
                break;

            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mObservation.getObservateur().getId_user() == mUser.getId_user()) {
            menu.add("Modifier l'observation");
            menu.add("Ajouter une photo");
            menu.add("Supprimer l'observation");
        }
        menu.add("Itinéraire");
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case PICK_IMAGE:
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, intent);
                    uploadImage(bitmap);
                    break;

                case NEW_LOCATION:
                    newLocation = (LatLng) intent.getExtras().get("location");
                    Toast.makeText(mContext, "Nouvelle localisation enregistrée", Toast.LENGTH_SHORT).show();

                default:
                    break;
            }
        }
    }

    private void uploadImage(Bitmap bitmap) {
        final Bitmap userPic = ImagePicker.getResizedBitmap(bitmap, 512);

        double size = (userPic.getByteCount() / 8) / 1000000;

        if(size < 10D) {
            final ProgressDialog dialog = ProgressDialog.show(mContext, "Envoi en cours", "Chargement, veuillez patienter...", true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            userPic.compress(Bitmap.CompressFormat.JPEG, 100, out);

            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), out.toByteArray());

            MyPOVClient.client.addPhotoObservation(requestBody, mObservation.getId(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                @Override
                public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        if (response.body().getStatus() == 0) {
                            descriptionFrag.addImageToFlipper(userPic);
                            descriptionFrag.setFlipperToLastPic();
                        } else {
                            Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            mUser.logout();
                            finish();
                        }
                    } else {
                        Toast.makeText(mContext, response.code() + " - " + response.raw().message(), Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    dialog.cancel();
                }
            });
        } else {
            Toast.makeText(mContext, "L'image doit faire moins de 10mo", Toast.LENGTH_LONG).show();
        }
    }

    public void next() {
        descriptionFrag.next();
    }

    public void previous() {
        descriptionFrag.previous();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return descriptionFrag.onTouchEvent(event);
    }
}