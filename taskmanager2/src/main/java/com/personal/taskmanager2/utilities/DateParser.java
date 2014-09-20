package com.personal.taskmanager2.utilities;

import android.os.AsyncTask;
import android.os.Handler;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class DateParser {

    public static final String TAG = "DateParser";

    public static final int DEFAULT = 0;
    public static final int DETAIL  = 1;

    private final static DateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm a");
    private final static DateFormat DETAIL_DATE_FORMAT =
            new SimpleDateFormat("'Due' 'on' MMM dd, yyyy 'at' hh:mm a");

    private int mType;

    public DateParser(int type) {

        mType = type;
    }

    public void parse(Date date, TextView dateText) {

        resetPurgeTimer();
        String parsedDate = getParsedDateFromCache(date);

        if (parsedDate == null) {
            forceParse(date, dateText);
        }
        else {
            cancelPotentialParse(date, dateText);
            dateText.setText(parsedDate);
        }
    }

    private void forceParse(Date date, TextView dateText) {

        DateParserTask task = new DateParserTask(dateText);
        DateTextTag tag = new DateTextTag(task);
        dateText.setTag(tag);
        task.execute(date);
    }

    private static boolean cancelPotentialParse(Date date, TextView dateText) {

        DateParserTask task = getDateParserTask(dateText);

        if (task != null) {
            Date taskDate = task.date;
            if (taskDate == null || !taskDate.equals(date)) {
                task.cancel(true);
            }
            else {
                return false;
            }
        }
        return true;
    }

    private static DateParserTask getDateParserTask(TextView dateText) {

        if (dateText != null) {
            Object tag = dateText.getTag();
            if (tag instanceof DateTextTag) {
                DateTextTag dateTag = (DateTextTag) tag;
                return dateTag.getDateParser();
            }
        }
        return null;
    }

    String parseDate(Date date) {

        if (mType == DEFAULT) {
            return SIMPLE_DATE_FORMAT.format(date);
        }
        else {
            return DETAIL_DATE_FORMAT.format(date);
        }

    }

    class DateParserTask extends AsyncTask<Date, Void, String> {

        private Date                    date;
        private WeakReference<TextView> dateTextReference;

        public DateParserTask(TextView dateText) {

            dateTextReference = new WeakReference<>(dateText);
        }

        @Override
        protected String doInBackground(Date... params) {

            date = params[0];
            return parseDate(date);
        }

        @Override
        protected void onPostExecute(String s) {

            if (isCancelled()) {
                s = null;
            }

            if (dateTextReference != null) {
                TextView dateText = dateTextReference.get();
                DateParserTask task = getDateParserTask(dateText);

                addParsedDateToCache(date, s);

                if (this == task) {
                    dateText.setText(s);
                }
            }
        }
    }

    static class DateTextTag {

        private final WeakReference<DateParserTask> dateParserTaskReference;

        public DateTextTag(DateParserTask task) {

            dateParserTaskReference = new WeakReference<>(task);
        }

        public DateParserTask getDateParser() {

            return dateParserTaskReference.get();
        }
    }

    private static final int HARD_CACHE_CAPACITY = 100;
    private static final int DELAY_BEFORE_PURGE  = 10 * 1000;

    private final HashMap<Date, String> sHardParsedDateCache =
            new LinkedHashMap<Date, String>(HARD_CACHE_CAPACITY / 2,
                                            .75f,
                                            true) {
                @Override
                protected boolean removeEldestEntry(Entry<Date, String> eldest) {

                    if (size() > HARD_CACHE_CAPACITY) {
                        sSoftParsedDate.put(eldest.getKey(),
                                            new SoftReference<>(eldest.getValue()));
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            };

    private final static ConcurrentHashMap<Date, SoftReference<String>>
            sSoftParsedDate = new ConcurrentHashMap<>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        @Override
        public void run() {

            clearCache();
        }
    };

    private void addParsedDateToCache(Date date, String parsedDate) {

        if (parsedDate != null) {
            synchronized (sHardParsedDateCache) {
                sHardParsedDateCache.put(date, parsedDate);
            }
        }
    }

    private String getParsedDateFromCache(Date date) {

        synchronized (sHardParsedDateCache) {
            final String parsedDate = sHardParsedDateCache.get(date);
            if (parsedDate != null) {
                sHardParsedDateCache.remove(date);
                sHardParsedDateCache.put(date, parsedDate);
                return parsedDate;
            }
        }

        SoftReference<String> parsedDateReference = sSoftParsedDate.get(date);
        if (parsedDateReference != null) {
            final String parsedDate = parsedDateReference.get();
            if (parsedDate != null) {
                return parsedDate;
            }
            else {
                sSoftParsedDate.remove(date);
            }
        }

        return null;
    }

    public void clearCache() {

        sHardParsedDateCache.clear();
        sSoftParsedDate.clear();
    }

    private void resetPurgeTimer() {

        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }

}
