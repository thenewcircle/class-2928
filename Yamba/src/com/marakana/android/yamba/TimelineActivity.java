/* $Id: $
   Copyright 2013, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.marakana.android.yamba;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.marakana.android.yamba.svc.YambaSvc;


/**
 *
 * @version $Revision: $
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 */
public class TimelineActivity extends ListActivity implements LoaderCallbacks<Cursor> {

    private static final int TIMELINE_LOADER = 288;
    private static final String[] PROJ = new String[] {
        YambaContract.Timeline.Columns.ID,
        YambaContract.Timeline.Columns.TIMESTAMP,
        YambaContract.Timeline.Columns.USER,
        YambaContract.Timeline.Columns.STATUS
    };

    private static final String[] FROM = new String[PROJ.length - 1];
    static { System.arraycopy(PROJ, 1, FROM, 0, FROM.length); }

    private static final int[] TO = new int[] {
        R.id.timeline_timestamp,
        R.id.timeline_user,
        R.id.timeline_status
    };

    static class TimelineBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View v, Cursor c, int col) {
            if (R.id.timeline_timestamp != v.getId()) { return false; }

            CharSequence s = "long ago";
            long t = c.getLong(col);
            if (0 < t) {
                s = DateUtils.getRelativeTimeSpanString(t, System.currentTimeMillis(), 0);
            }

            ((TextView) v).setText(s);

            return true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg) {
        return new CursorLoader(
                this,
                YambaContract.Timeline.URI,
                PROJ,
                null,
                null,
                YambaContract.Timeline.Columns.TIMESTAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> c) {
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        YambaSvc.stopPolling(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        YambaSvc.startPolling(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getListView().setBackgroundResource(R.drawable.mavericks);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.timeline_row, null, FROM, TO, 0);
        adapter.setViewBinder(new TimelineBinder());
        setListAdapter(adapter);

        getLoaderManager().initLoader(TIMELINE_LOADER, null, this);
    }
}