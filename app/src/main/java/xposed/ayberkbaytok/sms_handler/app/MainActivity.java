package xposed.ayberkbaytok.sms_handler.app;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import xposed.ayberkbaytok.sms_handler.BuildConfig;
import xposed.ayberkbaytok.sms_handler.R;
import xposed.ayberkbaytok.sms_handler.consts.PreferenceConsts;
import xposed.ayberkbaytok.sms_handler.provider.DatabaseContract;
import xposed.ayberkbaytok.sms_handler.utils.IOUtils;
import xposed.ayberkbaytok.sms_handler.utils.Xlog;
import xposed.ayberkbaytok.sms_handler.utils.XposedUtils;

import main.java.com.mindscapehq.android.raygun4android.RaygunClient; 
import main.java.com.mindscapehq.android.raygun4android.messages.RaygunUserInfo;

import java.util.*;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRA_SECTION = "section";
    public static final String EXTRA_SECTION_RECEIVED_BOX = "received_box";
    public static final String EXTRA_SECTION_SENT_BOX = "sent_box";

    private static final String VERSION_NAME = BuildConfig.VERSION_NAME;
    private static final int VERSION_CODE = BuildConfig.VERSION_CODE;
    private static final String TWITTER_URL = "http://visualcv.com/ayberk_baytok";

    private static final String[] TASK_KILLER_PACKAGES = {
            "me.piebridge.forcestopgb",
            "com.oasisfeng.greenify",
            "me.piebridge.brevent",
            "com.click369.controlbp",
    };

    private CoordinatorLayout mCoordinatorLayout;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;
    private ActionBarDrawerToggle mDrawerToggle;
    private Set<Snackbar> mSnackbars;
    private Fragment mContentFragment;
    private String mContentSection;
    private SharedPreferences mInternalPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_coordinator);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.main_drawer);
        mNavigationView = (NavigationView)findViewById(R.id.main_navigation);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mFloatingActionButton = (FloatingActionButton)findViewById(R.id.main_fab);

        // Load preferences
        mInternalPrefs = getSharedPreferences(PreferenceConsts.FILE_INTERNAL, MODE_PRIVATE);
        mInternalPrefs.edit().putInt(PreferenceConsts.KEY_APP_VERSION, VERSION_CODE).apply();

        // Setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup navigation drawer
        mNavigationView.setNavigationItemSelectedListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mSnackbars = Collections.newSetFromMap(new WeakHashMap<Snackbar, Boolean>());

        // Raygun Init

        RaygunClient.init(getApplicationContext());
		RaygunClient.attachExceptionHandler();


        NotificationHelper.createNotificationChannel(this);


        if (savedInstanceState == null) {

            if (handleIntent(getIntent())) {
                return;
            }


            if (!XposedUtils.isModuleEnabled()) {
                if (XposedUtils.isXposedInstalled(this)) {
                    //showEnableModuleDialog();
                } else {
                    showEnableModuleDialog();
                }
            } else if (XposedUtils.isModuleUpdated()) {
                showModuleUpdatedDialog();
            } else {
                showTaskKillerDialogIfNecessary();
            }

            String section = mInternalPrefs.getString(PreferenceConsts.KEY_SELECTED_SECTION, EXTRA_SECTION_RECEIVED_BOX);
            setContentSection(section);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawer(mNavigationView);
        switch (item.getItemId()) {
            case R.id.main_drawer_received_box:
                setContentSection(EXTRA_SECTION_RECEIVED_BOX);
                return true;
            case R.id.main_drawer_sent_box:
                setContentSection(EXTRA_SECTION_SENT_BOX);
                return true;
            case R.id.main_drawer_about:
                showAboutDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                if (IOUtils.isParentUri(DatabaseContract.ReceivedMessages.CONTENT_URI, uri)) {
                    Xlog.i("Got ACTION_VIEW intent with received message URI");
                    Bundle args = new Bundle(1);
                    args.putParcelable(ReceivedMessagesFragment.ARG_MESSAGE_URI, uri);
                    setContentSection(EXTRA_SECTION_RECEIVED_BOX, args);
                } else {
                    Xlog.i("Got ACTION_VIEW intent with sent message URI");
                    Bundle args = new Bundle(1);
                    args.putParcelable(SentMessagesFragment.ARG_MESSAGE_URI, uri);
                    setContentSection(EXTRA_SECTION_SENT_BOX, args);
                }

                intent.setData(null);
                return true;
            }
        }

        String section = intent.getStringExtra(EXTRA_SECTION);
        if (section != null) {
            intent.removeExtra(EXTRA_SECTION);
            setContentSection(section);
            return true;
        }

        return false;
    }

    private boolean setContentSection(String key, Bundle args) {

        if (key.equals(mContentSection)) {
            if (args != null) {
                if (mContentFragment instanceof MainFragment) {
                    ((MainFragment)mContentFragment).onNewArguments(args);
                }
            }
            return false;
        }

        Fragment fragment;
        int navId;
        switch (key) {
            case EXTRA_SECTION_RECEIVED_BOX:
                fragment = new ReceivedMessagesFragment();
                if (args == null) {
                    args = new Bundle();
                }
                navId = R.id.main_drawer_received_box;
                break;
            case EXTRA_SECTION_SENT_BOX:
                fragment = new SentMessagesFragment();
                if (args == null) {
                    args = new Bundle();
                }
                navId = R.id.main_drawer_sent_box;
                break;

            default:
                Xlog.e("Unknown context section: %s", key);
                return setContentSection(EXTRA_SECTION_RECEIVED_BOX);
        }

        if (args != null) {
            fragment.setArguments(args);
        }
        dismissSnackbar();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
        mContentFragment = fragment;
        mContentSection = key;
        mNavigationView.setCheckedItem(navId);
        mInternalPrefs.edit().putString(PreferenceConsts.KEY_SELECTED_SECTION, key).apply();
        return true;
    }

    private boolean setContentSection(String key) {
        return setContentSection(key, null);
    }

    public void enableFab(int iconId, View.OnClickListener listener) {
        mFloatingActionButton.setImageResource(iconId);
        mFloatingActionButton.setOnClickListener(listener);
        mFloatingActionButton.show();
    }

    public void disableFab() {
        mFloatingActionButton.setOnClickListener(null);
        mFloatingActionButton.hide();
    }

    public Snackbar makeSnackbar(int textId) {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, textId, Snackbar.LENGTH_LONG);
        mSnackbars.add(snackbar);
        return snackbar;
    }

    public void dismissSnackbar() {
        for (Snackbar snackbar : mSnackbars) {
            snackbar.dismiss();
        }
    }

    private void startBrowserActivity(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void startXposedActivity(XposedUtils.Section section) {
        if (!XposedUtils.startXposedActivity(this, section)) {
            Toast.makeText(this, R.string.xposed_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean shouldShowTaskKillerDialog(List<PackageInfo> taskKillers) {
        if (taskKillers.isEmpty()) {
            return false;
        }

        Set<String> knownTaskKillers = mInternalPrefs.getStringSet(PreferenceConsts.KEY_KNOWN_TASK_KILLERS, null);
        if (knownTaskKillers == null) {
            return true;
        }

        for (PackageInfo pkgInfo : taskKillers) {
            if (!knownTaskKillers.contains(pkgInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private List<PackageInfo> getInstalledTaskKillers() {
        PackageManager packageManager = getPackageManager();
        ArrayList<PackageInfo> apps = new ArrayList<>();
        for (String pkgName : TASK_KILLER_PACKAGES) {
            PackageInfo pkgInfo;
            try {
                pkgInfo = packageManager.getPackageInfo(pkgName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }
            apps.add(pkgInfo);
        }
        return apps;
    }

    private String getAppDisplayName(PackageInfo pkgInfo) {
        PackageManager packageManager = getPackageManager();
        CharSequence name = packageManager.getApplicationLabel(pkgInfo.applicationInfo);
        if (name != null) {
            return name.toString();
        } else {
            return pkgInfo.packageName;
        }
    }

    private void showTaskKillerDialogIfNecessary() {
        final List<PackageInfo> taskKillers = getInstalledTaskKillers();
        if (!shouldShowTaskKillerDialog(taskKillers)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (PackageInfo pkgInfo : taskKillers) {
            sb.append(getAppDisplayName(pkgInfo));
            sb.append('\n');
        }
        sb.setLength(sb.length() - 1);
        String message = getString(R.string.task_killer_message, sb.toString());

        new AlertDialog.Builder(this)
                .setTitle(R.string.task_killer_title)
                .setMessage(message)
                .setIcon(R.drawable.ic_sent)
                .setPositiveButton(R.string.task_killer_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashSet<String> knownTaskKillers = new HashSet<>();
                        for (PackageInfo pkgInfo : taskKillers) {
                            knownTaskKillers.add(pkgInfo.packageName);
                        }
                        mInternalPrefs.edit().putStringSet(PreferenceConsts.KEY_KNOWN_TASK_KILLERS, knownTaskKillers).apply();
                    }
                })
                .setNegativeButton(R.string.ignore, null)
                .show();
    }

    private void showEnableModuleDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.enable_xposed_module_title)
                .setMessage(R.string.must_enable_xposed_module)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startXposedActivity(XposedUtils.Section.MODULES);
                    }
                })
                .setNegativeButton(R.string.ignore, null)
                .show();
    }

    private void showModuleUpdatedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.module_outdated_message)
                .setMessage(R.string.module_outdated_message)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(R.string.reboot, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startXposedActivity(XposedUtils.Section.INSTALL);
                    }
                })
                .setNegativeButton(R.string.ignore, null)
                .show();
    }

    private void showAboutDialog() {
        Spanned html = Html.fromHtml(getString(R.string.format_about_message,
                TWITTER_URL));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name) + ' ' + VERSION_NAME)
                .setMessage(html)
                .setPositiveButton(R.string.close, null)
                .show();

        TextView textView = (TextView)dialog.findViewById(android.R.id.message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
