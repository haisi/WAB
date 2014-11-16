package ch.m837.wab.fragments;

import ch.m837.wab.WabMainFragment;
import ch.m837.wab.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Main fragment WABMain
 */
public class WABMainFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_section_main, container, false);
    WabMainFragment.setPgbNextFetch((ProgressBar) rootView.findViewById(R.id.pgbNextFetch));
    WabMainFragment.setTvUser((TextView) rootView.findViewById(R.id.tvUser));
    WabMainFragment.t.scheduleAtFixedRate(WabMainFragment.task, 0, 1000);
    return rootView;
  }
}
