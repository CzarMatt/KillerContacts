package net.devmobility.killercontacts;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import devmobility.net.killercontacts.R;

/**
 * Created by mossmanm on 3/31/15.
 */
public class SearchTask implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = SearchTask.class.getSimpleName();

    Context context;

    public static final String QUERY_KEY = "query";

    public SearchTask(Context context) {
        this.context = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderIndex, Bundle args) {

        // string argument defining the type of query
        String query = args.getString(QUERY_KEY);

        // construct a parsable URI path segment and appends to already created base URI
        Uri uri = Uri.withAppendedPath(CommonDataKinds.Contactables.CONTENT_FILTER_URI, query);

        // we only want rows that contain phone numbers
        String selection = CommonDataKinds.Contactables.HAS_PHONE_NUMBER + " = " + 1;

        // sorting parameter
        String sortBy = CommonDataKinds.Contactables.LOOKUP_KEY;

        return new CursorLoader(
                context,  // Context
                uri,       // URI pointing to the database/table/resource to be queried
                null,      // optional projection (aka: the list of columns to return)  Null = all
                selection, // selection - Which rows to return
                null,      // selection args
                sortBy);   // sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_main);
        ArrayList<String> arrayList = new ArrayList<>();

        // Return if the cursor is null.
        if (cursor.getCount() == 0) {
            Log.e(TAG, "The cursor was NULL.");
            return;
        }

        //store off IDs for columns we want to extract data from
        int phoneColumnIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
        int emailColumnIndex = cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS);
        int nameColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.DISPLAY_NAME);
        int typeColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.MIMETYPE);
        int lookupColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.LOOKUP_KEY);

        //move the cursor to first row before iterating
        cursor.moveToFirst();

        String entry = "";
        String lookupKey = "";

        do {

            String currentLookupKey = cursor.getString(lookupColumnIndex);

            // only add array list entry if we are on a unique row
            if (!lookupKey.equals(currentLookupKey)) {
                if (!entry.isEmpty()) arrayList.add(entry);
                entry = cursor.getString(nameColumnIndex);
                lookupKey = currentLookupKey;
            }

            String mimeType = cursor.getString(typeColumnIndex);
            if (mimeType.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                entry = String.format("%s\n\tEmail Address: %s\n",
                        entry,
                        cursor.getString(emailColumnIndex));
            }

            if (mimeType.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                entry = String.format("%s\n\tPhone number: %s\n",
                        entry,
                        cursor.getString(phoneColumnIndex));
            }

//            // a log of all the columns we can pull data from if available
//            // WARNING: Lots of data!
//            for(String column : cursor.getColumnNames()) {
//                Log.d(TAG, String.format("\nColumn: %s  \nName = %s \n----------\n",
//                        column,
//                        cursor.getString(cursor.getColumnIndex(column))));
//            }

        } while (cursor.moveToNext());

        // set our listView adapter to bind the data into the view
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, arrayList));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // unused
    }
}