package fr.valquev.mypov;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import fr.valquev.mypov.activities.Login;
import fr.valquev.mypov.fragments.Map;

public class MyPOV extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final long DRAWER_CLOSE_DELAY_MS = 0;
    private static final String NAV_ITEM_ID = "navItemId";

    private final Handler mDrawerActionHandler = new Handler();
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private User mUser;
    private CharSequence mTitle;
    private int mNavItemId;

    private Fragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypov);

        mContext = this;

        mUser = new User(mContext);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        mapFragment = Map.instantiate(mContext, Map.class.getName());

        if(savedInstanceState == null) {
            mNavItemId = R.id.drawer_map;
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().findItem(mNavItemId).setChecked(true);
        mTitle = navigationView.getMenu().findItem(mNavItemId).getTitle();

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                (Toolbar) findViewById(R.id.main_toolbar),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        navigate(mNavItemId);
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
                newFragment = mapFragment;
                break;

            case R.id.drawer_parametres:
                newFragment = mapFragment;
                //startActivity(new Intent(mContext, Settings.class));
                break;

            case R.id.drawer_satellite:
                newFragment = mapFragment;
                ((Map) mapFragment).toggleView();
                navigate(R.id.drawer_map);
                onNavigationItemSelected(((NavigationView) findViewById(R.id.navigation)).getMenu().getItem(0));
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

        ((TextView) findViewById(R.id.drawer_header_title)).setText(mUser.getPseudo());
    }
}
