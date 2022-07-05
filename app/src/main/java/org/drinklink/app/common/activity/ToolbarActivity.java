package org.drinklink.app.common.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.drinklink.app.R;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.ui.fragments.NavigationFragment;
import org.drinklink.app.ui.navigation.NavigationManager;
import org.drinklink.app.ui.navigation.NavigationManagerImpl;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


/**
 *
 */
public class ToolbarActivity extends AppCompatActivity {

    private static final String TAG = "ToolabarActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    protected String title;

    private Unbinder unbinder;
    private DrawerLayout drawer;
    private Fragment fragment;
    protected Fragment contentFragment;
    private NavigationManagerImpl navigationManager;
    private NavigationManagerImpl headerNavigationManager;

    @Inject
    Gson gson;

    @Inject
    IOrderProcessor processor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DependencyResolver.getComponent().inject(this);

        setContentView(getActivityLayout());

        unbinder = ButterKnife.bind(this);
        setToolbar();

        prepareNavigation();
        Bundle extras = getIntent().getExtras();
        fragment = getHeaderNavigation().next(getNavigationFragment(), extras, false);
        navigationFragmentToUseMainNavigation();

        loadFragment();
    }

    protected void loadFragment() {
        if (!getNavigation().hasFragments()) {
            Class<? extends Fragment> fragmentExtra = getFragmentExtra();
            // extras are potentially updated in getFragmentExtra
            Bundle extras = getBundle();
            Logger.d(TAG, "loadFragment: " + fragmentExtra);
            contentFragment = getNavigation().next(fragmentExtra, extras, addToBackStack());
        }
    }

    @NonNull
    protected Class<? extends NavigationFragment> getNavigationFragment() {
        return NavigationFragment.class;
    }

    private void navigationFragmentToUseMainNavigation() {
        if (fragment instanceof DrinkLinkFragment) {
            ((DrinkLinkFragment)fragment).setNavigation(getNavigation());
        }
    }

    protected boolean addToBackStack() {
        return true;
    }

    protected Bundle getBundle() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
            getIntent().putExtras(extras);
        }
        return extras;
    }

    protected int getActivityLayout() {
        return R.layout.activity_main_navigation;
    }

    private void prepareNavigation() {
        drawer = findViewById(R.id.drawer_layout);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {

        // close navigation

        if (backPressOnFragment()) {
            closeDrawer();
            return;
        }

        if (backPressOnFragment(contentFragment)) {
            closeDrawer();
            return;
        }

        if (closeDrawer()) {
            return;
        }

        Logger.d(TAG, "onBackPressed");
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    private boolean backPressOnFragment() {
        return backPressOnFragment(fragment);
    }

    private boolean backPressOnFragment(Fragment fragment) {
        if (fragment != null && fragment instanceof DrinkLinkFragment) {
            boolean handledByFragment = ((DrinkLinkFragment) fragment).onBackPress();
            return handledByFragment;
        }
        return false;
    }

    public boolean closeDrawer() {
        DrawerLayout drawer = getDrawer();
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
            return true;
        }
        return false;
    }

    private DrawerLayout getDrawer() {
        return drawer;
    }

    protected Class<? extends Fragment> getFragmentExtra() {
        return (Class<Fragment>) getIntent().getSerializableExtra(ExtrasKey.FRAGMENT_EXTRA);
    }

    protected String getTitleExtra() {
        String stringExtra = getIntent().getStringExtra(ExtrasKey.TITLE_EXTRA);
        if (stringExtra == null) {
            stringExtra = "";
        }
        return stringExtra;
    }

    protected void setToolbar() {
        toolbar.setOnClickListener(view -> toolbarClicked());
        CharSequence title = getTitleExtra();
        if (title != null) {
            toolbar.setTitle(title);
        }
        toolbar.setNavigationIcon(R.drawable.btn_back);
        setSupportActionBar(toolbar);
        displayBackArrow(displayBackArrow());
        // toolbar.setNavigationOnClickListener(getNavigationListener()); // of arrow doesn't work
    }

    protected void displayBackArrow(Boolean showHomeAsUp) {
        if (showHomeAsUp != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
            getSupportActionBar().setDisplayShowHomeEnabled(showHomeAsUp);
        }
    }

    protected boolean displayBackArrow() {
        return false;
    }

    protected void toolbarClicked() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getMenuResource(), menu);
        return true;
    }

    protected int getMenuResource() {
        return R.menu.settings;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return onPrepareOptionsMenuBase(menu);
    }

    protected boolean onPrepareOptionsMenuBase(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.d(TAG, "onOptionsItemSelected " + item);
        switch (item.getItemId()) {
            case android.R.id.home:

                onBackPressed();
                break;
            case R.id.menu_settings:
                if (drawer != null && !closeDrawer()) {
//                    navigationView.getMenu().clear(); //clear old inflated items.
//                    navigationView.inflateMenu(R.menu.new_navigation_drawer_items);
                    drawer.openDrawer(GravityCompat.END);
                }
                Logger.d(TAG, "settings menu selected, (open drawer): " + item);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    public NavigationManager getNavigation() {
        if (navigationManager == null) {
            navigationManager = createNavigation();
        }
        return navigationManager;
    }

    @NonNull
    protected NavigationManagerImpl createNavigation() {
        return new NavigationManagerImpl(this, R.id.container);
    }

    public NavigationManager getHeaderNavigation() {
        if (headerNavigationManager == null) {
            headerNavigationManager = new NavigationManagerImpl(this, R.id.nav_view);
        }
        return headerNavigationManager;
    }

    protected Gson getGson() {
        return gson;
    }

    protected IOrderProcessor getProcessor() {
        return processor;
    }
}
