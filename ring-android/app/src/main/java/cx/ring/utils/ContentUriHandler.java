/*
 *  Copyright (C) 2004-2019 Savoir-faire Linux Inc.
 *
 *  Author: Alexandre Lision <alexandre.lision@savoirfairelinux.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package cx.ring.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cx.ring.BuildConfig;

/**
 * This class distributes content uri used to pass along data in the app
 */
public class ContentUriHandler {
    private final static String TAG = ContentUriHandler.class.getSimpleName();

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final String AUTHORITY_FILES = AUTHORITY + ".file_provider";

    private static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri CONVERSATION_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "conversation");
    public static final Uri ACCOUNTS_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "accounts");
    public static final Uri CONTACT_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "contact");

    private ContentUriHandler() {
        // hidden constructor
    }

    /**
     * The following is a workaround used to mitigate getUriForFile exceptions
     * on Huawei devices taken from stackoverflow
     * https://stackoverflow.com/a/41309223
     */
    private static final String HUAWEI_MANUFACTURER = "Huawei";

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        if (HUAWEI_MANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
            Log.w(TAG, "Using a Huawei device Increased likelihood of failure...");
            try {
                return FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Log.w(TAG, "Returning Uri.fromFile to avoid Huawei 'external-files-path' bug for pre-N devices", e);
                    return Uri.fromFile(file);
                } else {
                    Log.w(TAG, "ANR Risk -- Copying the file the location cache to avoid Huawei 'external-files-path' bug for N+ devices", e);
                    // Note: Periodically clear this cache
                    final File cacheFolder = new File(context.getCacheDir(), HUAWEI_MANUFACTURER);
                    final File cacheLocation = new File(cacheFolder, file.getName());
                    try (InputStream in = new FileInputStream(file); OutputStream out = new FileOutputStream(cacheLocation)) {
                        FileUtils.copyFile(in, out);
                        Log.i(TAG, "Completed Android N+ Huawei file copy. Attempting to return the cached file");
                        return FileProvider.getUriForFile(context, authority, cacheLocation);
                    } catch (IOException e1) {
                        Log.e(TAG, "Failed to copy the Huawei file. Re-throwing exception", e1);
                        throw new IllegalArgumentException("Huawei devices are unsupported for Android N", e1);
                    }
                }
            }
        } else {
            return FileProvider.getUriForFile(context, authority, file);
        }
    }
}
