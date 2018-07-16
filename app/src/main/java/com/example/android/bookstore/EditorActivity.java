package com.example.android.bookstore;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract.BookEntry;

/**
 * This activity allows the user to add a new book or to edit an existing one.
 */

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * This is the tag that will be used for logging
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * This constant identifies a particular Loader being used in this component
     */
    private static final int BOOK_LOADER = 0;

    /**
     * EditText field to enter the product name
     */
    private EditText mProductName;

    /**
     * EditText field to enter the price
     */
    private EditText mPrice;

    /**
     * EditText field to enter the quantity
     */
    private EditText mQuantity;

    /**
     * EditText field to enter the supplier name
     */
    private EditText mSupplierName;

    /**
     * EditText field to enter the supplier phone number
     */
    private EditText mSupplierPhoneNumber;

    /**
     * This is a boolean that indicated whether the book has been successfully saved or not.
     * It will become true when we know for sure that the book was saved and we can close the ediitor.
     */
    private boolean wasSaved = false;

    /**
     * This is the URI for the book that was selected from the ListView
     */
    private Uri mBookUri;

    /**
     * This variable is used to remember if the user made any changes on the form.
     * In the beginning there are no changes so it can be false.
     */
    private boolean mBookHasChanged = false;

    /**
     * This is an OnTouchListener that listens for any user touches on a View, implying
     * that they modified the view, and we change the mBookHasChanged boolean to true
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the associated URI that was sent with the intent
        Intent intent = getIntent();
        mBookUri = intent.getData();

        // Find all relevant views that will need to read user input from
        // or display info about the selected book.
        mProductName = findViewById(R.id.edit_product_name);
        mPrice = findViewById(R.id.edit_price);
        mQuantity = findViewById(R.id.edit_quantity);
        mSupplierName = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneNumber = findViewById(R.id.edit_supplier_phone_number);
        Button mDecreaseQuantityButton = findViewById(R.id.button_decrement);
        Button mIncreaseQuantityButton = findViewById(R.id.button_increment);


        // Set the title of the EditorActivity based on which situation we have.
        // If the EditorActivity was opened using the ListView item,
        // then we will have the URI of the book so we will change the app bar
        // to say "Edit Book"

        if (mBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getResources().getString(R.string.editor_title_new_book));
            // Invalidate the options menu, so "Delete" menu option can be hidden
            // It doesn't make sense to delete a book that hasn't been created yet.
            invalidateOptionsMenu();
            // Display a quantity of 0
            mQuantity.setText(String.valueOf(0));
        } else {
            // The user is editing a book so change the app bar to say "Edit Book"
            Log.i(LOG_TAG, mBookUri.toString());
            setTitle(getResources().getString(R.string.editor_title_edit_book));
            // Prepare the loader. Either re-connect with an existing one
            // or start a new one.
            getLoaderManager().initLoader(BOOK_LOADER, null, this);
        }

        // Listen if the user touches any of the fields
        mProductName.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumber.setOnTouchListener(mTouchListener);
        mDecreaseQuantityButton.setOnTouchListener(mTouchListener);
        mIncreaseQuantityButton.setOnTouchListener(mTouchListener);
    }

    /**
     * This is the method that is triggered when the user presses the "-" button
     */
    public void decreaseQuantity(View view) {
        int quantity = Integer.valueOf(mQuantity.getText().toString());
        // Allow user to decrement the quantity only if it is above 0
        if (quantity == 0) {
            // Show message on the screen saying that the min number of books was reached
            Toast.makeText(this, getString(R.string.min_number_books_message),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        quantity--;
        mQuantity.setText(String.valueOf(quantity));
    }

    /**
     * This is the method that is triggered when the user presses the "+" button
     */
    public void increaseQuantity(View view) {
        int quantity = Integer.valueOf(mQuantity.getText().toString());
        quantity++;
        mQuantity.setText(String.valueOf(quantity));
    }

    /**
     * This method is triggered when the user presses the "Contact Supplier" button
     */
    public void contactSupplier(View view) {
        // Get the phone number from the form
        String phoneNumber = mSupplierPhoneNumber.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this,
                    getResources().getString(R.string.null_phone_number_message),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Create an intent to start the phone app
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    /**
     * This method is used to create a "Discard changes" dialog
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder, set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Keep editing" button so dismiss the dialog
                // and continue editing the book
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be
        // discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button.
                        // Close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes.
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Get user input from the editor and save new book in database
     */
    private void saveBook() {
        String productNameString = mProductName.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumber.getText().toString().trim();
        // Validate the user input.
        // If the user wants to save while having null values,
        // we'll display a message saying that he needs to enter valid information
        // before saving.
        // A quantity that equals to 0 will be accepted.
        if (productNameString.isEmpty() ||
                mPrice.getText().toString().isEmpty() ||
                mSupplierName.getText().toString().isEmpty() ||
                mSupplierPhoneNumber.getText().toString().isEmpty()) {
            Toast.makeText(this,
                    getResources().getString(R.string.null_values_message),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        float price = 0;
        if (!mPrice.getText().toString().isEmpty()) {
            price = Float.parseFloat(mPrice.getText().toString().trim());
        }
        int quantity = 0;
        if (!mQuantity.getText().toString().isEmpty()) {
            quantity = Integer.parseInt(mQuantity.getText().toString().trim());
        }

        if (mBookUri == null &&
                TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(mPrice.getText().toString()) &&
                TextUtils.isEmpty(mQuantity.getText().toString()) &&
                TextUtils.isEmpty(mSupplierName.getText().toString()) &&
                TextUtils.isEmpty(mSupplierPhoneNumber.getText().toString())) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(BookEntry.COLUMN_PRICE, price);
        values.put(BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

        if (mBookUri == null) {
            // This means we are in the "Add a Book" mode
            Uri uri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            if (uri == null) {
                Toast.makeText(this,
                        getResources().getString(R.string.book_not_saved),
                        Toast.LENGTH_SHORT).show();
            } else {
                wasSaved = true;
                Toast.makeText(this,
                        getResources().getString(R.string.book_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // This means we are in the "Edit Book" mode
            int rowsUpdated = getContentResolver().update(mBookUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this,
                        getResources().getString(R.string.book_not_updated),
                        Toast.LENGTH_SHORT).show();
            } else {
                wasSaved = true;
                Toast.makeText(this,
                        getResources().getString(R.string.book_updated),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        if (mBookUri != null) {
            int rowsAffected = getContentResolver().delete(mBookUri, null, null);
            if (rowsAffected > 0) {
                Toast.makeText(this,
                        getResources().getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveBook();
                // Exit activity if book was saved
                if (wasSaved) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to the parent activity
                // which is the {@link MainActivity}
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user that they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we are interested in
        String[] projection = {BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        // This Loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,           // Parent activity context.
                mBookUri,               // Query the content URI for the current book.
                projection,             // Columns to include in the resulting cursor.
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(productNameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mProductName.setText(productName);
            mPrice.setText(String.valueOf(price));
            mQuantity.setText(String.valueOf(quantity));
            mSupplierName.setText(supplierName);
            mSupplierPhoneNumber.setText(supplierPhoneNumber);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mPrice.setText("");
        mQuantity.setText("");
        mSupplierName.setText("");
        mSupplierPhoneNumber.setText("");
    }
}
