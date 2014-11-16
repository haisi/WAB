package ch.m837.wab.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import ch.m837.wab.R;
import ch.m837.wab.WABApp;
import ch.m837.wab.model.Gender;

/**
 * Settings fragment SettingsFragment
 */
public class SettingsFragment extends Fragment {

  private RadioGroup radioGroup1, radioGroup2;
  private WABApp wab = WABApp.getInstance();
  private SeekBar sbMaxDistance;
  private TextView tvSearchRadius;
  private EditText etName, etWhitelistedName;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_section_settings, container, false);



    etWhitelistedName = (EditText) rootView.findViewById(R.id.etWhitelistedName);
    radioGroup1 = (RadioGroup) rootView.findViewById(R.id.radioGroup1);
    radioGroup2 = (RadioGroup) rootView.findViewById(R.id.radioGroup2);
    sbMaxDistance = (SeekBar) rootView.findViewById(R.id.sbMaxDistance);
    tvSearchRadius = (TextView) rootView.findViewById(R.id.tvSearchRadius);
    etName = (EditText) rootView.findViewById(R.id.etName);



    radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.radio0Male) {
          wab.getLocaluser().setGender(Gender.Male);
        } else if (checkedId == R.id.radio1Female) {
          wab.getLocaluser().setGender(Gender.Female);
        }
      }
    });

    radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.radio2Male) {
          wab.getLocaluser().setGenderLookingFor(Gender.Male);
        } else if (checkedId == R.id.radio3Female) {
          wab.getLocaluser().setGenderLookingFor(Gender.Female);
        } else if (checkedId == R.id.radio4Both) {
          wab.getLocaluser().setGenderLookingFor(Gender.Both);
        }
      }
    });


    sbMaxDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        wab.getLocaluser().setMaxSearchRadius((double) progress);
        tvSearchRadius.setText("Search Radius: " + progress);
      }
    });

    etName.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub
      }

      @Override
      public void afterTextChanged(Editable s) {
        wab.getLocaluser().setUserName(s.toString());
      }
    });

    etWhitelistedName.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub
      }

      @Override
      public void afterTextChanged(Editable s) {
        wab.getLocaluser().setWhiteListedName(s.toString());
      }
    });

    return rootView;
  }
}
