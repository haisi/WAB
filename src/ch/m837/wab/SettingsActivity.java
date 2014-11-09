package ch.m837.wab;

import ch.m837.wab.model.Gender;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;


public class SettingsActivity extends Activity {
  private RadioGroup radioGroup1, radioGroup2;
  private WABApp wab = WABApp.getInstance();
  private SeekBar sbMaxDistance;
  private TextView tvSearchRadius;
  private EditText etName, etWhitelistedName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);


    etWhitelistedName = (EditText) findViewById(R.id.etWhitelistedName);
    radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
    radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
    sbMaxDistance = (SeekBar) findViewById(R.id.sbMaxDistance);
    tvSearchRadius = (TextView) findViewById(R.id.tvSearchRadius);
    etName = (EditText) findViewById(R.id.etName);



    radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {
          case R.id.radio0Male:
            wab.getLocaluser().setGender(Gender.Male);
            break;
          case R.id.radio1Female:
            wab.getLocaluser().setGender(Gender.Female);
            break;
        }
      }
    });

    radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.radio2Male:
            wab.getLocaluser().setGenderLookingFor(Gender.Male);
            break;
          case R.id.radio3Female:
            wab.getLocaluser().setGenderLookingFor(Gender.Female);
            break;
          case R.id.radio4Both:
            wab.getLocaluser().setGenderLookingFor(Gender.Both);
            break;
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

        wab.getLocaluser().setMaxSearchRadius(progress);
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
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.settings, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
