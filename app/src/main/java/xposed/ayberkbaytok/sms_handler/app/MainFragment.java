package xposed.ayberkbaytok.sms_handler.app;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;
import xposed.ayberkbaytok.sms_handler.utils.PermissionUtils;

@SuppressWarnings("deprecation")
public class MainFragment extends Fragment {
    public MainActivity getMainActivity() {
        return (MainActivity)getActivity();
    }

    @Override
    public Context getContext() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return super.getContext();
        } else {
            return getActivity();
        }
    }

    private Snackbar makeSnackbar(int textId) {
        return getMainActivity().makeSnackbar(textId);
    }

    private Snackbar makeSnackbar(int textId, int actionTextId, View.OnClickListener listener) {
        return makeSnackbar(textId).setAction(actionTextId, listener);
    }

    public void showSnackbar(int textId, int actionTextId, View.OnClickListener listener) {
        makeSnackbar(textId, actionTextId, listener).show();
    }

    public void showSnackbar(int textId) {
        makeSnackbar(textId).show();
    }

    private Toast makeToast(int textId) {
        return Toast.makeText(getContext(), textId, Toast.LENGTH_SHORT);
    }

    public void showToast(int textId) {
        makeToast(textId).show();
    }

    public void setTitle(int titleId) {
        getMainActivity().setTitle(titleId);
    }

    public void enableFab(int iconId, View.OnClickListener listener) {
        getMainActivity().enableFab(iconId, listener);
    }

    public void disableFab() {
        getMainActivity().disableFab();
    }

    public void requestPermissionsCompat(String permission, int requestCode, boolean openSettings) {
        requestPermissionsCompat(new String[] {permission}, requestCode, openSettings);
    }

    private void openPermissionSettings(int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, requestCode);
    }

    public void requestPermissionsCompat(String[] permissions, int requestCode, boolean openSettings) {
        int[] grantResults = new int[permissions.length];
        boolean hasPermissions = PermissionUtils.checkPermissions(getContext(), permissions, grantResults);
        if (!hasPermissions && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (openSettings) {
                for (String permission : permissions) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        openPermissionSettings(requestCode);
                        return;
                    }
                }
            }

            requestPermissions(permissions, requestCode);
        } else {
            onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int permissionStatus : grantResults) {
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult(requestCode, false);
                return;
            }
        }

        boolean requestCancelled = (grantResults.length == 0);
        onRequestPermissionsResult(requestCode, !requestCancelled);
    }

    protected void onRequestPermissionsResult(int requestCode, boolean granted) {

    }

    public void onNewArguments(Bundle args) {

    }
}
