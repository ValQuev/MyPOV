package fr.valquev.mypov.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import java.util.List;

import fr.valquev.mypov.MyPOV;
import fr.valquev.mypov.MyPOVClient;
import fr.valquev.mypov.MyPOVResponse;
import fr.valquev.mypov.Note;
import fr.valquev.mypov.Notes;
import fr.valquev.mypov.Observation;
import fr.valquev.mypov.ObservationPhoto;
import fr.valquev.mypov.R;
import fr.valquev.mypov.SwipeGestureListener;
import fr.valquev.mypov.User;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by ValQuev on 28/09/15.
 */
public class ObservationDetailsContent extends BaseFragment {

    private Context mContext;
    private Observation mObservation;
    private ViewFlipper mViewFlipper;
    private SwipeGestureListener swipeGestureListener;

    private View view;

    private TextView affirmation;
    private TextView infirmation;

    private User mUser;

    private int page = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mUser = new User(mContext);
        mObservation = getArguments().getParcelable("observation");
        return inflater.inflate(R.layout.observation_details_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;

        mViewFlipper = (ViewFlipper) view.findViewById(R.id.annonce_vf);

        TextView date = (TextView) view.findViewById(R.id.observation_content_date);
        affirmation = (TextView) view.findViewById(R.id.tv_affirmation);
        infirmation = (TextView) view.findViewById(R.id.tv_infirmation);
        TextView texte = (TextView) view.findViewById(R.id.observation_content_text);

        date.setText(String.format("%s", mObservation.getSuperbDate()));

        Notes notes = mObservation.getNotes();
        affirmation.setText(String.format("%d", notes.getNbAffirmations()));
        infirmation.setText(String.format("%d", notes.getNbInfirmations()));

        texte.setText(String.format("%s", mObservation.getDescription()));

        getMyNotes();

        ImageView map = new ImageView(mContext);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:"+ mObservation.getLat() +","+ mObservation.getLng() +"?q="+ mObservation.getLat() +","+ mObservation.getLng() +"(" + mObservation.getNom() + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        String pos = mObservation.getLat() + "," + mObservation.getLng();
        Picasso.with(mContext).load("https://maps.googleapis.com/maps/api/staticmap?center=" + pos + "&zoom=13&markers=" + pos + "&size=640x480&scale=2").centerCrop().fit().into(map);
        mViewFlipper.addView(map);

        for (final ObservationPhoto photo : mObservation.getPhotos()) {
            final ImageView imageView = new ImageView(mContext);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                    builder.setTitle("Supprimer cette image");
                    builder.setMessage("Voulez-vous vraiment supprimer définitivement cette image ?");
                    builder.setNegativeButton("Non", null);
                    builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final ProgressDialog dialogdel = ProgressDialog.show(mContext, "Suppression en cours", "Chargement, veuillez patienter...", true);
                            MyPOVClient.client.deletePic(photo.getId(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                                @Override
                                public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                    if (response.isSuccess()) {
                                        if (response.body().getStatus() == 0) {
                                            mViewFlipper.removeView(imageView);
                                            Toast.makeText(mContext, "Photo supprimée", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                                            mUser.logout();
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
                }
            });
            Picasso.with(mContext).load("https://mypov.fr/uploads/observations/" + mObservation.getId() + "/" + photo.getId() + "." + photo.getFormat()).centerCrop().fit().into(imageView);
            mViewFlipper.addView(imageView);
        }

        swipeGestureListener = new SwipeGestureListener(mContext);

        mViewFlipper.setOnTouchListener(swipeGestureListener);

        if (mViewFlipper.getChildCount() > 1) {
            view.findViewById(R.id.show_annonce_left).setVisibility(View.VISIBLE);
            view.findViewById(R.id.show_annonce_right).setVisibility(View.VISIBLE);
            view.findViewById(R.id.show_annonce_left).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    previous();
                }
            });
            view.findViewById(R.id.show_annonce_right).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    next();
                }
            });
        }
    }

    private void getMyNotes() {
        MyPOVClient.client.getNoteObservation(mObservation.getId(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<List<Note>>>() {
            @Override
            public void onResponse(Response<MyPOVResponse<List<Note>>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    if (response.body().getStatus() == 0) {
                        if (response.body().getObject() != null) {
                            int note = -1;

                            for (Note n : response.body().getObject()) {
                                if (n.getId_user() == mUser.getId_user()) {
                                    note = n.getNote();
                                }
                            }

                            if (note == 0) {
                                ((ImageView) view.findViewById(R.id.infirmer)).setColorFilter(Color.parseColor("#a50000"));
                                view.findViewById(R.id.observation_note_infirmer).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MyPOVClient.client.delNoteObservation(mObservation.getId(), mUser.getId_user(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                                            @Override
                                            public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                                ((ImageView) view.findViewById(R.id.infirmer)).setColorFilter(Color.parseColor("#000000"));
                                                int newnote = Integer.parseInt(infirmation.getText().toString());
                                                newnote--;
                                                infirmation.setText(String.format("%d", newnote));
                                                getMyNotes();
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
                                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                                view.findViewById(R.id.observation_note_affirmer).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MyPOVClient.client.setNoteObservation(mObservation.getId(), mUser.getId_user(), 1, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                                            @Override
                                            public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                                int newnote = Integer.parseInt(infirmation.getText().toString());
                                                newnote--;
                                                infirmation.setText(String.format("%d", newnote));
                                                int newnote2 = Integer.parseInt(affirmation.getText().toString());
                                                newnote2++;
                                                affirmation.setText(String.format("%d", newnote2));
                                                ((ImageView) view.findViewById(R.id.infirmer)).setColorFilter(Color.parseColor("#000000"));
                                                ((ImageView) view.findViewById(R.id.affirmer)).setColorFilter(Color.parseColor("#298900"));
                                                getMyNotes();
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
                                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                            } else if (note == 1) {
                                ((ImageView) view.findViewById(R.id.affirmer)).setColorFilter(Color.parseColor("#298900"));
                                view.findViewById(R.id.observation_note_infirmer).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MyPOVClient.client.setNoteObservation(mObservation.getId(), mUser.getId_user(), 0, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                                            @Override
                                            public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                                int newnote = Integer.parseInt(infirmation.getText().toString());
                                                newnote++;
                                                infirmation.setText(String.format("%d", newnote));
                                                int newnote2 = Integer.parseInt(affirmation.getText().toString());
                                                newnote2--;
                                                affirmation.setText(String.format("%d", newnote2));
                                                ((ImageView) view.findViewById(R.id.infirmer)).setColorFilter(Color.parseColor("#a50000"));
                                                ((ImageView) view.findViewById(R.id.affirmer)).setColorFilter(Color.parseColor("#000000"));
                                                getMyNotes();
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
                                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                                view.findViewById(R.id.observation_note_affirmer).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MyPOVClient.client.delNoteObservation(mObservation.getId(), mUser.getId_user(), mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                                            @Override
                                            public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                                                int newnote2 = Integer.parseInt(affirmation.getText().toString());
                                                newnote2--;
                                                affirmation.setText(String.format("%d", newnote2));
                                                ((ImageView) view.findViewById(R.id.affirmer)).setColorFilter(Color.parseColor("#000000"));
                                                getMyNotes();
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
                                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                            } else {
                                noNotes();
                            }
                        } else {
                            noNotes();
                        }
                    } else {
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                        mUser.logout();
                        ((MyPOV) mContext).finish();
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

    private void noNotes() {
        view.findViewById(R.id.observation_note_infirmer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyPOVClient.client.addNoteObservation(mObservation.getId(), mUser.getId_user(), 0, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                    @Override
                    public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                        int newnote = Integer.parseInt(infirmation.getText().toString());
                        newnote++;
                        infirmation.setText(String.format("%d", newnote));
                        ((ImageView) view.findViewById(R.id.infirmer)).setColorFilter(Color.parseColor("#a50000"));
                        ((ImageView) view.findViewById(R.id.affirmer)).setColorFilter(Color.parseColor("#000000"));
                        getMyNotes();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        view.findViewById(R.id.observation_note_affirmer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyPOVClient.client.addNoteObservation(mObservation.getId(), mUser.getId_user(), 1, mUser.getMail(), mUser.getPassword()).enqueue(new Callback<MyPOVResponse<String>>() {
                    @Override
                    public void onResponse(Response<MyPOVResponse<String>> response, Retrofit retrofit) {
                        int newnote2 = Integer.parseInt(affirmation.getText().toString());
                        newnote2++;
                        affirmation.setText(String.format("%d", newnote2));
                        ((ImageView) view.findViewById(R.id.infirmer)).setColorFilter(Color.parseColor("#000000"));
                        ((ImageView) view.findViewById(R.id.affirmer)).setColorFilter(Color.parseColor("#298900"));
                        getMyNotes();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void previous() {
        if (mViewFlipper.getChildCount() > 1) {
            if (page > 0) {
                page--;
            } else {
                page = mViewFlipper.getChildCount() - 1;
            }
            mViewFlipper.setInAnimation(mContext, R.anim.right_in);
            mViewFlipper.setOutAnimation(mContext, R.anim.right_out);
            mViewFlipper.setDisplayedChild(page);
        }
    }

    public void next() {
        if (mViewFlipper.getChildCount() > 1) {
            if (page < mViewFlipper.getChildCount() - 1) {
                page++;
            } else {
                page = 0;
            }
            mViewFlipper.setInAnimation(mContext, R.anim.left_in);
            mViewFlipper.setOutAnimation(mContext, R.anim.left_out);
            mViewFlipper.setDisplayedChild(page);
        }
    }

    public void addImageToFlipper(Bitmap bitmap) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mViewFlipper.addView(imageView);
    }

    public void setFlipperToLastPic() {
        page = mViewFlipper.getChildCount() - 1;
        mViewFlipper.setDisplayedChild(page);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return swipeGestureListener.getDetector().onTouchEvent(event);
    }
}
