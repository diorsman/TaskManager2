package com.personal.taskmanager2.utilities;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.personal.taskmanager2.adapters.ProjectAdapter.SectionedRecycleViewAdapter;
import com.personal.taskmanager2.model.parse.Project;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Omid Ghomeshi on 1/26/15.
 */
public class ProjectQueryHelper implements NotifyingThreadPoolExecutor.Callback {

    private static final String TAG = "ProjectQueryHelper";

    private static final int MAIN_PROJECT_QUERY   = 0;
    private static final int SEARCH_PROJECT_QUERY = 1;

    private int     mQueryType;
    private boolean mArchive;
    private boolean mTrash;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Runnable> mQueryQueue = new ArrayBlockingQueue<>(6);
    private       ThreadPoolExecutor      mExecutor   = new NotifyingThreadPoolExecutor(
            NUMBER_OF_CORES,
            NUMBER_OF_CORES,
            0L,
            TimeUnit.MILLISECONDS,
            mQueryQueue,
            this);

    Future<Integer>       mGetNumProjectsOverdue;
    Future<Integer>       mGetNumProjectsDueToday;
    Future<Integer>       mGetNumProjectsDueThisWeek;
    Future<Integer>       mGetNumProjectsDueThisMonth;
    Future<Integer>       mGetNumProjectsCompleted;
    Future<List<Project>> mGetProjects;

    private Callable<List<Project>> mQueryProjectCallable = new Callable<List<Project>>() {
        @Override
        public List<Project> call() throws ParseException {
            return queryProjectsInBackground();
        }
    };

    private Callable<Integer> mGetNumProjectsDueTodayCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountDueToday();
        }
    };

    private Callable<Integer> mGetNumProjectsDueThisWeekCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountDueThisWeek();
        }
    };

    private Callable<Integer> mGetNumProjectDueThisMonthCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountDueThisMonth();
        }
    };

    private Callable<Integer> mGetNumProjectsOverdueCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountOverdue();
        }
    };

    private Callable<Integer> mGetNumProjectsCompletedCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountCompleted();
        }
    };

    public interface ProjectQueryCallback {

        void onProjectsRetrieved(List<Project> projects,
                                 List<SectionedRecycleViewAdapter.Section> sections,
                                 int numProjectsOverdue,
                                 int numProjectsDueToday,
                                 int numProjectsDueThisWeek,
                                 int numProjectsDueThisMonth,
                                 int numProjectsDueLater,
                                 int numProjectsCompleted);

        void onNoProjectsFound();

        void onProjectQueryError(Exception e);
    }

    private ProjectQueryCallback mProjectQueryCallback;

    public void initMainProjectQuery(ProjectQueryCallback projectQueryCallback,
                                     boolean archive,
                                     boolean trash) {
        mProjectQueryCallback = projectQueryCallback;
        mQueryType = MAIN_PROJECT_QUERY;
        mArchive = archive;
        mTrash = trash;

        mGetNumProjectsOverdue = mExecutor.submit(mGetNumProjectsOverdueCallable);
        mGetNumProjectsDueToday = mExecutor.submit(mGetNumProjectsDueTodayCallable);
        mGetNumProjectsDueThisWeek = mExecutor.submit(mGetNumProjectsDueThisWeekCallable);
        mGetNumProjectsDueThisMonth =
                mExecutor.submit(mGetNumProjectDueThisMonthCallable);
        mGetNumProjectsCompleted = mExecutor.submit(mGetNumProjectsCompletedCallable);
        mGetProjects = mExecutor.submit(mQueryProjectCallable);
    }

    @Override
    public void onAllTasksComplete() {
        switch (mQueryType) {
            case MAIN_PROJECT_QUERY:
                onMainQueryComplete();
                break;
            case SEARCH_PROJECT_QUERY:
                break;
        }
    }

    private void onMainQueryComplete() {
        try {
            int numProjectsOverdue = mGetNumProjectsOverdue.get();
            int numProjectsDueToday = mGetNumProjectsDueToday.get();
            int numProjectsDueThisWeek = mGetNumProjectsDueThisWeek.get();
            int numProjectsDueThisMonth = mGetNumProjectsDueThisMonth.get();
            int numProjectsCompleted = mGetNumProjectsCompleted.get();
            List<Project> projects = mGetProjects.get();
            int numProjects = projects.size();

            if (numProjects == 0) {
                mProjectQueryCallback.onNoProjectsFound();
                return;
            }

            int numProjectsDueLater = numProjects - numProjectsOverdue - numProjectsDueToday -
                                      numProjectsDueThisWeek - numProjectsDueThisMonth -
                                      numProjectsCompleted;

            List<SectionedRecycleViewAdapter.Section> sections = createSections(numProjectsOverdue,
                                                                                numProjectsDueToday,
                                                                                numProjectsDueThisWeek,
                                                                                numProjectsDueThisMonth,
                                                                                numProjectsDueLater,
                                                                                numProjectsCompleted,
                                                                                numProjects);


            mProjectQueryCallback.onProjectsRetrieved(projects,
                                                      sections,
                                                      numProjectsOverdue,
                                                      numProjectsDueToday,
                                                      numProjectsDueThisWeek,
                                                      numProjectsDueThisMonth,
                                                      numProjectsDueLater,
                                                      numProjectsCompleted);
        }
        catch (InterruptedException | ExecutionException e) {
            mProjectQueryCallback.onProjectQueryError(e);
        }
    }

    private List<SectionedRecycleViewAdapter.Section> createSections(int numProjectsOverdue,
                                                                     int numProjectsDueToday,
                                                                     int numProjectsDueThisWeek,
                                                                     int numProjectsDueThisMonth,
                                                                     int numProjectsDueLater,
                                                                     int numProjectsCompleted,
                                                                     int numProjects) {
        List<SectionedRecycleViewAdapter.Section> sections = new ArrayList<>();

        //Sections
        if (numProjectsOverdue > 0) {
            sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                 "Overdue",
                                                                 numProjectsOverdue));
        }
        if (numProjectsDueToday > 0) {
            if (sections.isEmpty()) {
                sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                     "Due Today",
                                                                     numProjectsDueToday));
            }
            else {
                sections.add(new SectionedRecycleViewAdapter.Section(
                        numProjectsOverdue,
                        "Due Today",
                        numProjectsDueToday));
            }
        }
        if (numProjectsDueThisWeek > 0) {
            if (sections.isEmpty()) {
                sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                     "Due This Week",
                                                                     numProjectsDueThisWeek));
            }
            else {
                sections.add(new SectionedRecycleViewAdapter.Section(
                        numProjectsOverdue + numProjectsDueToday,
                        "Due This Week",
                        numProjectsDueThisWeek));
            }
        }
        if (numProjectsDueThisMonth > 0) {
            if (sections.isEmpty()) {
                sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                     "Due This Month",
                                                                     numProjectsDueThisMonth));
            }
            else {
                sections.add(new SectionedRecycleViewAdapter.Section(
                        numProjectsOverdue + numProjectsDueToday + numProjectsDueThisWeek,
                        "Due This Month",
                        numProjectsDueThisMonth));
            }
        }
        if (numProjectsDueLater > 0) {
            if (sections.isEmpty()) {
                sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                     "Due Later",
                                                                     numProjectsDueLater));
            }
            else {
                sections.add(new SectionedRecycleViewAdapter.Section(
                        numProjectsOverdue + numProjectsDueToday + numProjectsDueThisWeek +
                        numProjectsDueThisMonth, "Due Later", numProjectsDueLater));
            }
        }
        if (numProjectsCompleted > 0) {
            if (sections.isEmpty()) {
                sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                     "Completed",
                                                                     numProjectsCompleted));
            }
            else {
                sections.add(new SectionedRecycleViewAdapter.Section(
                        numProjects - numProjectsCompleted,
                        "Completed",
                        numProjectsCompleted));
            }
        }
        // add footer view
        sections.add(new SectionedRecycleViewAdapter.Section(numProjects,
                                                             "",
                                                             0));
        return sections;
    }


    private int getProjectCountOverdue() throws ParseException {
        Calendar end = Calendar.getInstance();
        //printCal(end, "overdue = ");

        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereLessThan(Project.DUE_DATE_COL, end.getTime());
        projectQuery.whereEqualTo(Project.STATUS_COL, false);
        int count = projectQuery.count();
        Log.d(TAG, "num projects overdue = " + count);

        return count;
    }

    private int getProjectCountDueToday() throws ParseException {
        Calendar startToday = Calendar.getInstance();
        Calendar endToday = Calendar.getInstance();
        setCalendarToEndOfDay(endToday);

        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereGreaterThanOrEqualTo(Project.DUE_DATE_COL, startToday.getTime());
        projectQuery.whereLessThanOrEqualTo(Project.DUE_DATE_COL, endToday.getTime());
        projectQuery.whereEqualTo(Project.STATUS_COL, false);
        int count = projectQuery.count();
        Log.d(TAG, "Num projects due today = " + count);

        return count;
    }

    private int getProjectCountDueThisWeek() throws ParseException {
        Calendar startWeek = Calendar.getInstance();
        startWeek.add(Calendar.DATE, 1);
        setCalendarToBeginningOfDay(startWeek);
        //printCal(startWeek, "start week =");

        Calendar lastDayOfWeek = Calendar.getInstance();
        int curDay = lastDayOfWeek.get(Calendar.DAY_OF_WEEK);
        lastDayOfWeek.add(Calendar.DATE, Calendar.SATURDAY - curDay);
        setCalendarToEndOfDay(lastDayOfWeek);
        //printCal(lastDayOfWeek, "end week =");

        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereGreaterThanOrEqualTo(Project.DUE_DATE_COL, startWeek.getTime());
        projectQuery.whereLessThanOrEqualTo(Project.DUE_DATE_COL, lastDayOfWeek.getTime());
        projectQuery.whereEqualTo(Project.STATUS_COL, false);
        int count = projectQuery.count();
        Log.d(TAG, "Num projects due this week = " + count);

        return count;
    }

    private int getProjectCountDueThisMonth() throws ParseException {
        Calendar startMonth = Calendar.getInstance();
        int curDay = startMonth.get(Calendar.DAY_OF_WEEK);
        startMonth.add(Calendar.DATE, Calendar.SATURDAY - curDay + 1);
        setCalendarToBeginningOfDay(startMonth);
        //printCal(startMonth, "start month=");

        Calendar endMonth = Calendar.getInstance();
        endMonth.set(Calendar.DAY_OF_MONTH, endMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        setCalendarToEndOfDay(endMonth);
        //printCal(endMonth, "end month=");

        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereGreaterThanOrEqualTo(Project.DUE_DATE_COL, startMonth.getTime());
        projectQuery.whereLessThanOrEqualTo(Project.DUE_DATE_COL, endMonth.getTime());
        projectQuery.whereEqualTo(Project.STATUS_COL, false);
        int count = projectQuery.count();
        Log.d(TAG, "Num projects due this month = " + count);

        return count;
    }

    private int getProjectCountCompleted() throws ParseException {
        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereEqualTo(Project.STATUS_COL, true);
        int count = projectQuery.count();
        Log.d(TAG, "Num projects completed = " + count);
        return count;
    }

    private void setCalendarToBeginningOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void setCalendarToEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }

    /*DateFormat dateFormat = DateFormat.getDateTimeInstance();

    private void printCal(Calendar cal, String init) {
        Log.d(TAG, init + " " + dateFormat.format(cal.getTime()));
    }*/

    private List<Project> queryProjectsInBackground() throws ParseException {
        ParseQuery<Project> projectQuery = initQuery();
        //setSortMethod(projectQuery);
        List<Project> projectList = projectQuery.find();
        return projectList;
    }

    private ParseQuery<Project> initQuery() {
        ParseQuery<Project> projectAdmin = ParseQuery.getQuery(Project.class);
        projectAdmin.whereEqualTo(Project.ADMIN_COL, ParseUser.getCurrentUser());

        ParseQuery<Project> projectUser = ParseQuery.getQuery(Project.class);
        projectUser.whereEqualTo(Project.USERS_ID_COL,
                                 ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<Project>> orQueries = new ArrayList<>();
        orQueries.add(projectAdmin);
        orQueries.add(projectUser);

        ParseQuery<Project> projectQuery = ParseQuery.or(orQueries);
        projectQuery.whereEqualTo(Project.ARCHIVED_COL, mArchive);
        projectQuery.whereEqualTo(Project.TRASH_COL, mTrash);

        projectQuery.setLimit(1000);

        projectQuery.orderByAscending(Project.STATUS_COL);
        projectQuery.addAscendingOrder(Project.DUE_DATE_COL);

        return projectQuery;
    }

    /*public void setSortMethod(int sortMethod) {
        if (sortMethod == SORT_BY_DUE_DATE) {
            mProjectQuery.orderByAscending(Project.STATUS_COL);
        }
        else if (sortMethod == SORT_BY_COLOR) {
            mProjectQuery.orderByAscending(Project.COLOR_COL);
        }
        else if (sortMethod == SORT_BY_NAME) {
            mProjectQuery.orderByAscending(Project.NAME_COL);
        }
        else if (sortMethod == SORT_BY_DESCRIPTION) {
            mProjectQuery.orderByAscending(Project.DESCRIPTION_COL);
        }
        mProjectQuery.addAscendingOrder(Project.DUE_DATE_COL);
    }*/
}
