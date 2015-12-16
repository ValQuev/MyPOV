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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.fragments.ListeObservations;
import fr.valquev.mypov.fragments.Map;
import fr.valquev.mypov.fragments.Settings;

public class MyPOV extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener, PositionMapCenter {

    private static final long DRAWER_CLOSE_DELAY_MS = 0;
    private static final String NAV_ITEM_ID = "navItemId";
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    private final Handler mDrawerActionHandler = new Handler();
    private ActionBarDrawerToggle mDrawerToggle;
    private View headerView;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private User mUser;
    private CharSequence mTitle;
    private int mNavItemId;

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

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.mypov_navigation);
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

        navigate(mNavItemId);

        mapFragment.setPositionMapCenter(this);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                //startActivity(new Intent(mContext, Settings.class));
                break;

            default:
                return mDrawerToggle.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigate(int itemId) {
        Fragment newFragment;
        switch (itemId) {
            case R.id.drawer_map:
                newFragment = mapFragment;
                break;

            case R.id.drawer_observations:
                newFragment = listeObservationFragment;
                break;

            case R.id.drawer_parametres:
                newFragment = settingsFragment;
                //startActivity(new Intent(mContext, Settings.class));
                break;

            case R.id.drawer_satellite:
                newFragment = mapFragment;
                mapFragment.toggleView();
                navigate(R.id.drawer_map);
                onNavigationItemSelected(((NavigationView) findViewById(R.id.mypov_navigation)).getMenu().getItem(0));
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
        }

        displayUserInfos();
    }

    private void displayUserInfos() {
        ((TextView) headerView.findViewById(R.id.drawer_header_title)).setText(mUser.getPseudo());
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mapFragment.setCameraPosition(cameraUpdate);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
    public void update(LatLng position) {
        listeObservationFragment.update(position);
    }
}
