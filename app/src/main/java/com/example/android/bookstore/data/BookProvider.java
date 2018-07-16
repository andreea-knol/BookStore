package com.example.android.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the books table
     */
    private static final int BOOKS = 1;
    /**
     * URI matcher code for the content URI for a single book in the books table
     */
    private static final int BOOK_ID = 2;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for
     * the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /**
     * The object that will help gain access to the books database
     */
    private BookDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection,
     * selection, selection arguments and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        //Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // For the BOOKS code, query the books table directly with the
                // given projection, selection, selection arguments, and sort order.
                // The cursor could contain multiple rows of the books table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder, null);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Cursor containing the wanted row of the table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }

        // Set notification URI on the Cursor,
        // so we know which content URI the Cursor was created for.
        // If the data at this URI changed, then the Cursor needs to be updated.
        // The activity that is listening will automatically be notified.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + "with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values.
     *
     * @param contentValues represents the values that will be inserted into the table
     * @return the new Content URI for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues contentValues) {
        // Data validation.
        // The product name cannot be null.
        String productName = contentValues.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
        Log.v(LOG_TAG, "The product name: " + productName);
        if (productName == null || productName.isEmpty()) {
            Log.v(LOG_TAG, "The product name is null!");
            throw new IllegalArgumentException("Book requires a name.");
        }
        // The price cannot be null.
        // The price should be a positive value.
        Float price = contentValues.getAsFloat(BookEntry.COLUMN_PRICE);
        Log.v(LOG_TAG, "The price: " + price);
        if (price == null || price < 0) {
            Log.v(LOG_TAG, "The price is not valid!");
            throw new IllegalArgumentException("Book requires valid a price.");
        }
        // The quantity cannot be null.
        // The quantity should be a positive value.
        Integer quantity = contentValues.getAsInteger(BookEntry.COLUMN_QUANTITY);
        Log.v(LOG_TAG, "The quantity: " + quantity);
        if (quantity == null || quantity < 0) {
            Log.v(LOG_TAG, "The quantity is not valid.");
            throw new IllegalArgumentException("Book requires a valid quantity.");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(BookEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the book content URI
        // uri: content://com.example.android.books/books
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it.
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // The number of rows that are deleted
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all the rows that match the selection and selection arguments
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                // Extract the book id from the URI.
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for uri " + uri);
        }

        // If one or more rows were deleted, notify all listeners that the data
        // at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments,
     * with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update books in the database with the given content values.
     * Apply the changes to the rows specified in the selection and selection arguments.
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues contentValues, String selection,
                           String[] selectionArgs) {
        // Data validation.
        // The product name cannot be null.
        if (contentValues.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String productName = contentValues.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (productName == null || productName.isEmpty()) {
                throw new IllegalArgumentException("Book requires a name.");
            }
        }
        // The price cannot be null.
        // The price should be a positive value.
        if (contentValues.containsKey(BookEntry.COLUMN_PRICE)) {
            Float price = contentValues.getAsFloat(BookEntry.COLUMN_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Book requires valid a price.");
            }
        }
        // The quantity cannot be null.
        // The quantity should be a positive value.
        if (contentValues.containsKey(BookEntry.COLUMN_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Book requires a valid quantity.");
            }
        }

        // If there are no values to update, don't try to update database
        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(BookEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        // If one or more rows were updated, then notify all listeners
        // that the data at the URI has changed.
        if (rowsUpdated != 0) {
            // Notify all listeners that the data has changed for the book content URI
            // uri: content://com.example.android.books/#
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}
