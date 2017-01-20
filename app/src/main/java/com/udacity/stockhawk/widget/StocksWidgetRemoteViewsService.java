package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StocksWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StocksWidgetRemoteViewsFactory();
    }

    private class StocksWidgetRemoteViewsFactory implements RemoteViewsFactory {

        private Cursor data = null;
        private DecimalFormat dollarFormat;
        private DecimalFormat dollarFormatWithPlus;
        private DecimalFormat percentageFormat;

        @Override
        public void onCreate() {
            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onDataSetChanged() {
            if (data != null) {
                data.close();
            }

            final long identityToken = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    null);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (data == null || position == AdapterView.INVALID_POSITION || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_quote);
            String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            remoteViews.setTextViewText(R.id.symbol, symbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(data.getDouble(Contract.Quote.POSITION_PRICE)));
            remoteViews.setTextViewText(R.id.change, percentageFormat.format(data.getDouble(Contract.Quote.POSITION_PERCENTAGE_CHANGE)/100));

            // Set FillInIntent
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Intent.EXTRA_TEXT, symbol);
            remoteViews.setOnClickFillInIntent(R.id.list_item_quote_row, fillInIntent);
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.list_item_quote);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (data.moveToPosition(position))
                return data.getLong(Contract.Quote.POSITION_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
