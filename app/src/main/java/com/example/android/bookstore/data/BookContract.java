package com.example.android.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    // To prevent someone from accidentally instantiating the contract class,
    // we give it an empty constructor.
    private BookContract() {
    }

    /**
     * The Content Authority which identifies the Content Provider
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.books";

    /**
     * The Uri which will be shared by every URI associated with BookContract
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * The path for the books table.
     * This will be appended to the base content URI.
     */
    public static final String PATH_BOOKS = "books";

    /* Inner class that defines the table contents of the books table */
    public static final class BookEntry implements BaseColumns {

        /**
         * The full content URI for this class used to access the books data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of books.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single book.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /* Table name */
        public final static String TABLE_NAME = "books";
        /* The string for the _id column */
        public final static String _ID = BaseColumns._ID;
        /* The string for the productName column */
        public final static String COLUMN_PRODUCT_NAME = "product_name";
        /* The string for the price column */
        public final static String COLUMN_PRICE = "price";
        /* The string for the quantity column */
        public final static String COLUMN_QUANTITY = "quantity";
        /* The string for the supplierName column */
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";
        /* The string for the supplierPhoneNumber column */
        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
    }
}

