package com.kaniblu.naver.test;

import java.util.*;

public class InteractiveConsole extends Console
{
    public interface OnExitListener
    {
        public void onExit(Console console);
    }

    public interface OnMenuSelectedListener
    {
        public boolean onMenuSelected(Console console, int id);
    }

    protected static final int ID_EXIT = 0xFFFF;

    protected List<OnMenuSelectedListener> mOnMenuSelectedListeners = new ArrayList<OnMenuSelectedListener>();
    protected List<String> mMenuTitles = new ArrayList<String>();
    protected List<Integer> mMenuIds = new ArrayList<Integer>();
    protected OnExitListener mOnExitListener;
    protected String mExitTitle;
    protected String mHeaderTitle;

    public OnExitListener getOnExitListener()
    {
        return mOnExitListener;
    }

    public void setOnExitListener(OnExitListener onExitListener)
    {
        mOnExitListener = onExitListener;
    }

    public String getExitTitle()
    {
        return mExitTitle;
    }

    public void setExitTitle(String exitTitle)
    {
        mExitTitle = exitTitle;
    }

    public String getHeaderTitle()
    {
        return mHeaderTitle;
    }

    public void setHeaderTitle(String headerTitle)
    {
        mHeaderTitle = headerTitle;
    }

    protected int mMenuCount;
    protected boolean mContinue;

    public InteractiveConsole()
    {
        mHeaderTitle = "Choose a menu:";
        mExitTitle = "Exit.";
        mMenuCount = 0;
        mContinue = true;

        addMenu(ID_EXIT, mExitTitle, new OnMenuSelectedListener()
        {
            @Override
            public boolean onMenuSelected(Console console, int id)
            {
                if (mOnExitListener != null)
                    mOnExitListener.onExit(console);
                return false;
            }
        });
    }

    public void addMenu(int id, String title, OnMenuSelectedListener listener)
    {
        int insertIndex = Math.max(0, getMenuCount() - 1);
        mMenuTitles.add(insertIndex, title);
        mOnMenuSelectedListeners.add(insertIndex, listener);
        mMenuIds.add(insertIndex, id);
        mMenuCount++;
    }

    public int getMenuCount()
    {
        return mMenuCount;
    }

    public void run()
    {
        while (mContinue) {
            writeln(mHeaderTitle);

            for (int i = 0; i < getMenuCount(); ++i)
                writeln(String.format("%d. %s", i + 1, mMenuTitles.get(i)));

            write(String.format("Your choice? [1-%d]: ", getMenuCount()));

            Integer choice = tryParseInt(read());

            if (choice == null || choice < 1 || choice > getMenuCount()) {
                writeln(String.format("Choose a number between 1 to %d.", getMenuCount()));
                continue;
            }

            if (mOnMenuSelectedListeners.get(choice - 1).onMenuSelected(this, mMenuIds.get(choice - 1)))
                continue;
            else
                break;
        }
    }

    public boolean isContinue()
    {
        return mContinue;
    }

    public void setContinue(boolean aContinue)
    {
        mContinue = aContinue;
    }
}
