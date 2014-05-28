package com.oldwei.yifavor.activity;

import java.sql.SQLException;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.oldwei.yifavor.R;
import com.oldwei.yifavor.fragment.HomeLeftFragment;
import com.oldwei.yifavor.fragment.LinksFragment;
import com.oldwei.yifavor.model.CategoryModel;
import com.oldwei.yifavor.service.LinkService;

public class HomeActivity extends BaseActivty {

    private DrawerLayout mLeftDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private LinksFragment mLinkFragment;
    private HomeLeftFragment mHomeLeftFragment;
    private LinearLayout mHomeLeftLayout;
    private LinkService mLinkService = new LinkService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        initView();
        initFragment();
    }

    private void initView() {
        mHomeLeftLayout = (LinearLayout) findViewById(R.id.left_drawer_layout);
        mHomeLeftFragment = (HomeLeftFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        mLeftDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mLeftDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
        R.string.drawer_open, /* "open drawer" description */
        R.string.drawer_close /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setTitle(R.string.app_name);
            }
        };
        // Set the drawer toggle as the DrawerListener
        mLeftDrawerLayout.setDrawerListener(mDrawerToggle);

        mHomeLeftFragment.setCategoryItemClickListener(new HomeLeftFragment.CategoryItemClickListener() {

            @Override
            public void getCurCategory(CategoryModel model) {
                try {
                    mLinkFragment.refreshData(mLinkService.getLinksByCategory(model));
                } catch (SQLException e) {
                    Log.e(TAG, e.toString());
                }
                mLeftDrawerLayout.closeDrawer(mHomeLeftLayout);
                setTitle(model.getName());
            }
        });
    }

    private void initFragment() {
        mLinkFragment = new LinksFragment();
        Bundle args = new Bundle();
        // args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        mLinkFragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.content_frame, mLinkFragment).commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
}
