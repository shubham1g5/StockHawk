package com.udacity.stockhawk.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_LOADER = 0;
    private static final int ANIMATION_DURATION_IN_MILLIS = 1000;

    @BindView(R.id.symbol)
    TextView symbolTv;
    @BindView(R.id.price)
    TextView priceTv;
    @BindView(R.id.change)
    TextView changeTv;
    @BindView(R.id.changePercentage)
    TextView changePercentageTv;
    @BindView(R.id.chart)
    LineChart chart;

    private String mSymbol;
    private double mPrice;
    private double mChange;
    private double mChangePercentage;
    private String mHistoryString;
    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;
    private String[] dateStrings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFormats();
    }

    private void initFormats() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSymbol = getArguments().getString(Intent.EXTRA_TEXT);
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        initChart();
        return view;
    }

    private void initChart() {

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getDescription().setEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);

        chart.getLegend().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);

        YAxis y = chart.getAxisLeft();
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setValueFormatter((value, axis) -> dollarFormat.format(value));

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(Color.WHITE);
        x.setValueFormatter(new MyDateFormatter());

        chart.animateXY(ANIMATION_DURATION_IN_MILLIS, ANIMATION_DURATION_IN_MILLIS);
        chart.invalidate();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.makeUriForStock(mSymbol),
                Contract.Quote.QUOTE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mPrice = data.getDouble(Contract.Quote.POSITION_PRICE);
            mChange = data.getDouble(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            mChangePercentage = data.getDouble(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            mHistoryString = data.getString(Contract.Quote.POSITION_HISTORY);
            updateUI();
        }
    }

    private void updateUI() {

        symbolTv.setText(mSymbol);
        priceTv.setText(dollarFormat.format(mPrice));

        if (mChange > 0) {
            changeTv.setBackgroundResource(R.drawable.percent_change_pill_green);
            changePercentageTv.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            changeTv.setBackgroundResource(R.drawable.percent_change_pill_red);
            changePercentageTv.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

        changeTv.setText(dollarFormatWithPlus.format(mChange));
        changePercentageTv.setText(percentageFormat.format(mChangePercentage / 100));

        addHistoryChartData();
    }

    private void addHistoryChartData() {
        if (TextUtils.isEmpty(mHistoryString)) {
            return;
        }
        List<Entry> entries = new ArrayList<>();
        String[] rowsStr = mHistoryString.split("\n");
        int size = rowsStr.length;
        dateStrings = new String[size];
        for (int i = size - 1; i >= 0; i--) {
            int commaIndex = rowsStr[i].indexOf(",");
            dateStrings[i] = rowsStr[size - i - 1].substring(0, commaIndex);
            entries.add(new Entry(size - i - 1, Float.parseFloat(rowsStr[i].substring(commaIndex + 1))));
        }

        LineDataSet dataSet;
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet) chart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            dataSet = new LineDataSet(entries, getString(R.string.stock_price_graph_label));
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawHighlightIndicators(false);
            dataSet.setHighlightEnabled(true);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setDrawFilled(true);
            dataSet.setValueTextSize(8);
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class MyDateFormatter implements IAxisValueFormatter {

        private final SimpleDateFormat mDateFormat;

        public MyDateFormatter() {
            mDateFormat = new SimpleDateFormat("MMM,''yy");
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            String dateStr = dateStrings[(int) value];
            return mDateFormat.format(new Date(Long.parseLong(dateStr)));
        }
    }
}
