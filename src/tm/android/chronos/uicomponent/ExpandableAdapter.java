/*
 *   ExpandableAdapter
 *
 *    Copyright (c) 2014 Thierry Margenstern under MIT license
 *    http://opensource.org/licenses/MIT
 *
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import tm.android.chronos.core.StopwatchDataRow;

import java.util.Vector;

/**
 *
 */
public class ExpandableAdapter implements ExpandableListAdapter , IntermediateTimeListener {
    private Vector<LinearLayout> chs;
    private Context context;

    private ExpandableListView hisExpandableListView;
    public ExpandableAdapter(Context context){
        this.context = context;
        chs = new Vector<LinearLayout>(5);

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLeft(0);
        ll.setRight(Chronographe.getScreenWidth());
        ll.setTop(0);
        ll.setBottom(Chronographe.getPrefHeight());

        Chronographe chronographe = new Chronographe(context);
        chronographe.setIntermediateTimeListener(this);
        chronographe.setId(0);
        chronographe.getStopwatch().setName("Chrono-1");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Chronographe.getPrefWidth(),Chronographe.getPrefHeight());
        params.leftMargin=Chronographe.getLeftMargin();
        ll.addView(chronographe,params);

        chs.add(ll);
    }

//    public void addGroup(Chronographe chrono, Vector<String> children)
//    {
//        chrono.setLayoutParams(new AbsListView.LayoutParams(Chronographe.getPrefWidth(),80));
//        chrono.setPadding(Chronographe.getLeftMargin(), 0, 0, 0);
//                root.addLast(chrono, children);
//
//    }



    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }



    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }



    @Override
    public boolean hasStableIds() {
        return false;
    }


    public void setHisExpandableListView(ExpandableListView hisExpandableListView) {
        this.hisExpandableListView = hisExpandableListView;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public Object getChild(int i, int i1) {
        StopwatchDataRow dataRow = ((Chronographe)chs.get(i).findViewById(0)).getStopwatch().getStopwatchData().getStopwatchDataRow(i1);
        if (dataRow!=null)
            return dataRow;
        return "child not found !";

    }

    @Override
    public int getGroupCount() {
        return chs.size();
        //return root.getNodeCount();
    }

    @Override
    public int getChildrenCount(int i) {
        return ((Chronographe)chs.get(i).findViewById(0)).getStopwatch().getStopwatchData().getCount();

    }

    @Override
    public Object getGroup(int i) {
        return chs.get(i).findViewById(0);
        //return root.getNode(i);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view!=null && view instanceof Chronographe)
            return view;

        return chs.get(i);
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view==null || !(view instanceof TextView)) {
            LapTimeView lapTimeView = new LapTimeView(context);
            lapTimeView.init(((Chronographe) chs.get(i).findViewById(0)).getStopwatch().getStopwatchData(), i1);

            view=lapTimeView;
        }
        return  view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public void onGroupExpanded(int i) {

    }

    @Override
    public void onGroupCollapsed(int i) {

    }

    @Override
    public long getCombinedChildId(long l, long l1) {
        return 100*l+l1;
    }

    @Override
    public long getCombinedGroupId(long l) {
        return l;
    }

    @Override
    public void onIntermediateTimeUpdate(int id) {
        hisExpandableListView.collapseGroup(id);
        hisExpandableListView.expandGroup(id);
    }


}
