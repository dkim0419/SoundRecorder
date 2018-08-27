package by.naxa.soundrecorder.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import by.naxa.soundrecorder.fragments.RecordFragment;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class PermissionsHelper {
    public static boolean checkAndRequestPermissions(RecordFragment fragment, int permissionsRequestId) {
        final Context context = fragment.getActivity();
        int permissionRecordAudio = checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        int permissionWriteStorage = checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionRecordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            // Some permission(s) is/are not granted -> request the permission(s)
            fragment.requestPermissions(
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    permissionsRequestId);
            return false;
        }

        return true;
    }
}
