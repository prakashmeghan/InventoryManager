package com.conceptappsworld.inventorymanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.conceptappsworld.inventorymanager.data.InventoryContract.ProductEntry;

import java.text.DecimalFormat;


/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();
    private Context _context;

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        _context = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        _context = context;
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        _context = context;
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView lblCurrency = (TextView) view.findViewById(R.id.lbl_currency);
        ImageView ivSell = (ImageView) view.findViewById(R.id.iv_sell);

        // Find the columns of product attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);


        // Read the product attributes from the Cursor for the current product
        final int productId = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);

        // If the product price is empty string or null, then use some default text
        // that says "Unknown price", so the TextView isn't blank.
        if (TextUtils.isEmpty(productPrice)) {
            productPrice = context.getString(R.string.unknown_price);
            lblCurrency.setVisibility(View.GONE);
        } else {
            lblCurrency.setVisibility(View.VISIBLE);
            Double productPriceDbl = Double.parseDouble(productPrice);
            DecimalFormat df = new DecimalFormat("#.00");
            productPrice = df.format(productPriceDbl);
        }

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        quantityTextView.setText(String.valueOf(productQuantity));

        ivSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "_id: " + productId);
                int newProductQty = Integer.parseInt(quantityTextView.getText().toString());
                int newQty = newProductQty - 1;
                if (newQty >= 0) {
                    quantityTextView.setText(String.valueOf(newQty));

                    // Form the content URI that represents the specific product that was clicked on,
                    // by appending the "id" (passed as input to this method) onto the
                    // {@link ProductEntry#CONTENT_URI}.
                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);

                    // Create a ContentValues object where column names are the keys,
                    // and product attributes from the editor are the values.

                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQty);

                    int rowsAffected = _context.getContentResolver().update(currentProductUri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(_context, _context.getString(R.string.editor_update_product_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(_context, _context.getString(R.string.editor_update_product_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }
}
