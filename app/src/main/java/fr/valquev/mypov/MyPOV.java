package fr.valquev.mypov;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.activities.ObservationDetails;
import fr.valquev.mypov.fragments.ListeObservations;
import fr.valquev.mypov.fragments.Map;
import fr.valquev.mypov.fragments.Settings;

public class MyPOV extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener, PositionMapCenter, OnObservationListItemClickListener {

    private static final long DRAWER_CLOSE_DELAY_MS = 0;
    private static final String NAV_ITEM_ID = "navItemId";
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    public static final int ASK_COARSE_LOCATION_PERMISSION = 1;
    public static final int ASK_FINE_LOCATION_PERMISSION = 2;
    public static final int OBSERVATION_CLICK = 666;

    private final Handler mDrawerActionHandler = new Handler();
    private final Handler mDrawerActionHandlerMap = new Handler();
    private ActionBarDrawerToggle mDrawerToggle;
    private View headerView;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private User mUser;
    private CharSequence mTitle;
    private int mNavItemId;

    private NavigationView navigationView;
    public SwitchCompat switchCompat;

    private LocationManager locationManager;

    private Map mapFragment;
    private ListeObservations listeObservationFragment;
    private Fragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypov);

        mContext = this;

        mUser = new User(mContext);

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            finish();
            return;
        }

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        mapFragment = (Map) Map.instantiate(mContext, Map.class.getName());
        listeObservationFragment = (ListeObservations) ListeObservations.instantiate(mContext, ListeObservations.class.getName());
        settingsFragment = Settings.instantiate(mContext, Settings.class.getName());

        if(savedInstanceState == null) {
            mNavItemId = R.id.drawer_map;
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }

        navigationView = (NavigationView) findViewById(R.id.mypov_navigation);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().findItem(mNavItemId).setChecked(true);
        mTitle = navigationView.getMenu().findItem(mNavItemId).getTitle();

        headerView = getLayoutInflater().inflate(R.layout.mypov_header, navigationView);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                (Toolbar) findViewById(R.id.main_toolbar),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.drawer_satellite);
        View actionView = MenuItemCompat.getActionView(menuItem);
        switchCompat = (SwitchCompat) actionView.findViewById(R.id.nav_switch_view);
        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.toggleView();
            }
        });

        navigate(mNavItemId);

        mapFragment.setPositionMapCenter(this);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        if (menuItem != navigationView.getMenu().findItem(R.id.drawer_satellite)) {
            menuItem.setChecked(true);
            mNavItemId = menuItem.getItemId();
            mTitle = menuItem.getTitle();

            mDrawerLayout.closeDrawer(GravityCompat.START);
            mDrawerActionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigate(menuItem.getItemId());
                }
            }, DRAWER_CLOSE_DELAY_MS);

            return true;
        } else {
            //switchCompat.setChecked(!switchCompat.isChecked());
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavItemId == R.id.drawer_observations) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_filter:
                listeObservationFragment.openFilterDialog();
                break;

            default:
                return mDrawerToggle.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigate(int itemId) {
        Fragment newFragment;
        mNavItemId = itemId;
        supportInvalidateOptionsMenu();
        switch (itemId) {
            case R.id.drawer_map:
                if (mUser.getLastLatLng().latitude == 0.0 && mUser.getLastLatLng().longitude == 0.0) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ASK_FINE_LOCATION_PERMISSION);
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                }
                newFragment = mapFragment;
                break;

            case R.id.drawer_observations:
                newFragment = listeObservationFragment;
                break;

            case R.id.drawer_parametres:
                newFragment = settingsFragment;
                //startActivity(new Intent(mContext, Settings.class));
                break;

            default:
                newFragment = mapFragment;
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, newFragment).commit();
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setTitle(mTitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            finish();
            return;
        }

        displayUserInfos();
    }

    private void displayUserInfos() {
        ((TextView) headerView.findViewById(R.id.drawer_header_title)).setText(mUser.getPseudo());
    }

    @Override
    public void onLocationChanged(Location location) {
        mapFragment.setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ASK_COARSE_LOCATION_PERMISSION);
            }
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void update(LatLng position, float zoom) {
        mUser.setLastLatLng(position);
        mUser.setZoom(zoom);
        listeObservationFragment.update(position);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ASK_COARSE_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // VIENT D'ACCEPTER LA PERM
                } else {
                    // VIENT DE REFUSER LA PERM
                }
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // REFUS DE LA PERM DEPUIS UN CERTAIN TEMPS
                        Log.v("PERMISSION_ASK", "DEJA REFUSE");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ASK_FINE_LOCATION_PERMISSION);
                    } else {
                        // PAS ENCORE DEMANDE LA PERM
                        Log.v("PERMISSION_ASK", "DEMANDE");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ASK_FINE_LOCATION_PERMISSION);
                    }
                }
            }

            case ASK_FINE_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("PERMISSION_RESULT", "ACCEPTE");
                    finish();
                    startActivity(new Intent(mContext, MyPOV.class));
                } else {
                    Log.v("PERMISSION_RESULT", "REFUSE");
                }
            }
        }

        Log.v("LOGGED", "LOGGED = " + mUser.isLogged());

        if (!mUser.isLogged()) {
            startActivity(new Intent(mContext, Login.class));
            finish();
        }
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case OBSERVATION_CLICK:
                if (resultCode == RESULT_OK) {
                    final Observation observation = (Observation) data.getExtras().get("observation");
                    onNavigationItemSelected(navigationView.getMenu().getItem(0));
                    mDrawerActionHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mapFragment.setCameraPosition(new LatLng(observation.getLat(), observation.getLng()), 16);
                        }
                    }, DRAWER_CLOSE_DELAY_MS + 500);
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }

    @Override
    public void onObservationClick(Observation observation) {
        Intent intent = new Intent(mContext, ObservationDetails.class);
        intent.putExtra("observation", observation);
        ((MyPOV) mContext).startActivityForResult(intent, MyPOV.OBSERVATION_CLICK);
    }
}
