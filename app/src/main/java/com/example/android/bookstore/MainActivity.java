package com.example.android.bookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract.BookEntry;

/**
 * This class is used to display a list of books that were stored in the app
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // This is the tag that will be used for logging
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    // This constant identifies a particular loader being used in this component
    private static final int BOOK_LOADER = 0;
    // This is the Adapter being used to display the list's data
    private BookCursorAdapter mCursorAdapter;
    // This is the ListView that displays book data
    private ListView booksList;
    // This is the cursor that contains the data obtained from the table
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with book data
        booksList = findViewById(R.id.books_list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        booksList.setEmptyView(emptyView);

        // Setup the Adapter to create a list item for every row of book data in the Cursor.
        // There is no book data yet until the Loader has finished so pass in null for the Cursor for now.
        mCursorAdapter = new BookCursorAdapter(this, null);
        booksList.setAdapter(mCursorAdapter);

        // Setup item click listener
        booksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri bookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                intent.setData(bookUri);
                Log.i(LOG_TAG, bookUri.toString());
                startActivity(intent);
            }
        });

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    /**
     * This method is used when the user selects the SALE button.
     * Then the quantity of that product is reduced by one.
     */
    public void decrement(View view) {

        // Get the position of the product that the button was pressed for
        int position = booksList.getPositionForView((View) view.getParent());
        Log.i(LOG_TAG, "the id of the view: " + position);

        // Prepare the URI for the book that we need to update
        Uri bookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, position + 1);
        Log.i(LOG_TAG, "book URI: " + bookUri);

        // Before we can update it, we need to obtain the current information about the selected book
        if (mCursor.moveToPosition(position)) {

            int productNameColumnIndex = mCursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = mCursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = mCursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = mCursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = mCursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String productName = mCursor.getString(productNameColumnIndex);
            float price = mCursor.getFloat(priceColumnIndex);
            int quantity = mCursor.getInt(quantityColumnIndex);
            String supplierName = mCursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = mCursor.getString(supplierPhoneNumberColumnIndex);

            Log.i(LOG_TAG, "the quantity for the current book: " + String.valueOf(quantity));

            // Reduce the quantity by one.
            // Make sure no negative values are entered.
            // If the quantity is already 0, notify the user.
            if (quantity == 0) {
                // Show message on the screen saying that the min number of books was reached
                Toast.makeText(this, getString(R.string.min_number_books_message),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            quantity--;

            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_PRODUCT_NAME, productName);
            values.put(BookEntry.COLUMN_PRICE, price);
            values.put(BookEntry.COLUMN_QUANTITY, quantity);
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
            values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);

            int rowsUpdated = getContentResolver().update(bookUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this,
                        getResources().getString(R.string.book_not_updated),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.book_updated),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/main_menu.xml file
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * This method is used to insert some dummy data into the database table
     */
    private void insertData() {

        // Create some arrays that contain dummy data
        String[] product_names = {"Close to Home", "Small Change", "Gone With The Wind", "Where Rainbow Ends"};
        double[] prices = {14.5, 7.99, 5.99, 10.5};
        int[] quantities = {4, 15, 7, 21};
        String[] suppliers = {"BookExpres", "UNISA", "Red Pepper", "Bookshelf"};
        String[] suppliersPhoneNumbers = {"+407854561230", "+31654123456", "+40784222159", "+39765489124"};

        // Create some maps of values to be inserted in the database using the arrays
        for (int i = 0; i < product_names.length; i++) {
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_PRODUCT_NAME, product_names[i]);
            // cast the double value to a float
            float price = (float) prices[i];
            values.put(BookEntry.COLUMN_PRICE, price);
            values.put(BookEntry.COLUMN_QUANTITY, quantities[i]);
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, suppliers[i]);
            values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, suppliersPhoneNumbers[i]);

            getContentResolver().insert(BookEntry.CONTENT_URI, values);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The user clicked on a menu option in the app bar overflow menu
        // At the moment we only have the "Insert dummy data" menu option
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                // Respond to a click on the "Insert dummy data" menu option
                insertData();
                return true;
            case R.id.action_delete_all_entries:
                // Respond to a click on the "Delete all entries" menu option
                getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method created a Loader if one hasn't been created yet
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Select the columns we're interested in
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(
                this,           // Parent activity context.
                BookEntry.CONTENT_URI,  // Table to query.
                projection,             // Projection to return.
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.
        // The framework will take care of closing the old cursor once we return.
        mCursor = cursor;
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }
}
