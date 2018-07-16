package com.example.android.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookstore.data.BookContract.BookEntry;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source.
 * This adapter knows how to create list item views for each row of
 * book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    /**
     * This method constructs a new {@link BookCursorAdapter}.
     *
     * @param context represents the context
     * @param cursor  represents the cursor from which to get the data
     */
    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * This method makes a new blank list item view.
     * No data is set to the views yet.
     *
     * @param context is the app context
     * @param cursor  is the cursor from which to get the data.
     *                The cursor is already moved to the correct position.
     * @param parent  is the ViewGroup
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,
                parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the product_name
     * TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView productNameTextView = view.findViewById(R.id.product_name);
        TextView priceTextView = view.findViewById(R.id.price);
        final TextView quantityTextView = view.findViewById(R.id.quantity);

        int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        // Extract properties from cursor
        String productName = cursor.getString(productNameColumnIndex);
        String price = "$" + cursor.getString(priceColumnIndex);
        String quantityString = cursor.getString(quantityColumnIndex);

        // Populate fields with extracted properties
        productNameTextView.setText(productName);
        priceTextView.setText(price);
        quantityTextView.setText(quantityString);

    }
}
