package org.startpad.android.enigma;

import org.startpad.Enigma;

import org.startpad.android.enigma.R;

import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.OnTabChangeListener;

public class EnigmaApp extends TabActivity
	{
	private static final String TAG = "Enigma";
	
	// Machine settings
    Spinner[] aspnRotors = new Spinner[3];
    Spinner[] aspnStart = new Spinner[3];
    Spinner[] aspnRings = new Spinner[3];
    EditText plugboard;
    
    int[] aRotors = new int[] {0,1,2};
    ToggleButton toggleGroup;
    boolean fGroup = true;
    EditText edit;
    TextView output;
    Enigma.Settings settings = new Enigma.Settings();
    Enigma machine = new Enigma(null);
    Toast toast;
    boolean fLegalSettings = true;
    String sSettingsError;
    
    private void updateEncoding()
        {
        String code;
        
        try
            {
            machine.init(null);
            code = machine.encode(edit.getText().toString());
            }
        catch (Exception e)
            {
            output.setText("- error -");
            return;
            }
        
        if (fGroup)
            code = Enigma.groupLetters(code);
        
        output.setText(code);
        }
    
    private void updateSettings()
        {
    	for (int i = 0; i < 3; i++)
    	    {
    	    String s = (String) aspnRotors[i].getSelectedItem();
    	    Log.d(TAG, "Rotor: " + s + "." + s.length());
    		settings.rotors[i] = s;
    	    }
    	
    	for (int i = 0; i < 3; i++)
    		settings.position[i] = ((String) aspnStart[i].getSelectedItem()).charAt(0);
    	
    	for (int i = 0; i < 3; i++)
    		settings.rings[i] = ((String) aspnRings[i].getSelectedItem()).charAt(0);
    	
    	settings.plugs = plugboard.getText().toString();
    	
    	try
    	    {
    	    machine.init(settings);
    	    }
    	catch (Exception e)
    	    {
    	    fLegalSettings = false;
    	    sSettingsError = e.getMessage();
    	    Log.d(TAG, e.getMessage());
    	    }
    	
    	fLegalSettings = true;
    	updateEncoding();
        }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        
        TabHost tabHost = getTabHost();
        
        toast = Toast.makeText(this, "", toast.LENGTH_LONG);
        
        // Setup top-level tabbled layout screen 
        
        LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec("sim")
                .setIndicator("Simulation")
                .setContent(R.id.sim));
        tabHost.addTab(tabHost.newTabSpec("encoder")
                .setIndicator("Encoder")
                .setContent(R.id.encoder));
        tabHost.addTab(tabHost.newTabSpec("settings")
                .setIndicator("Settings")
                .setContent(R.id.settings));
        tabHost.addTab(tabHost.newTabSpec("info")
                .setIndicator("Info")
                .setContent(R.id.enigma_info));
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener()
            {
            public void onTabChanged(String tabId)
                {
                if (!tabId.equals("settings"))
                    {
                    updateSettings();
                    if (!fLegalSettings)
                        {
                        toast.cancel();
                        toast.setText(sSettingsError);
                        toast.show();
                        }
                    }
                }
            });
       
        // Initialize Encoder view
        
        output = (TextView) findViewById(R.id.output);
        edit = (EditText) findViewById(R.id.input);
        
        toggleGroup = (ToggleButton) findViewById(R.id.group_text);
        toggleGroup.setOnClickListener(
        		new ToggleButton.OnClickListener()
	        		{
					public void onClick(View v)
						{
						fGroup = !fGroup;
						updateEncoding();
						}
	        		});
        toggleGroup.setChecked(fGroup);
        
        edit.addTextChangedListener(new TextWatcher()
        	{

			public void afterTextChanged(Editable arg0)
				{
				Log.d(TAG, "Changed!");
				updateEncoding();
				}

			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
        	});
        
        // Initialize Wikipedia-based info WebView
        
        WebView wv = (WebView) findViewById(R.id.enigma_info);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("http://en.m.wikipedia.org/wiki/Enigma_machine");
        
        // Initialize Settings view
        
        ArrayAdapter<CharSequence> adapter;
        
        adapter = ArrayAdapter.createFromResource(this, R.array.rotor_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (int i = 0; i < 3; i++)
        	{
        	aspnRotors[i] = (Spinner) findViewById(R.id.spn_rotors_1 + i);
        	aspnRotors[i].setAdapter(adapter);
        	aspnRotors[i].setSelection(aRotors[i]);
        	
        	aspnRotors[i].setOnItemSelectedListener(
        			// TODO: Send to public method on this class instead of making new
        			// class instance?
            		new OnItemSelectedListener()
            			{
    					public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    						{
    						int iMe;
    						for (iMe = 0; iMe < 3; iMe++)
    							if (aspnRotors[iMe] == parent)
    								break;

    						Log.d(TAG, "Selected:" + iMe + " to:"+ id);
    						
    						// If rotor already used - swap it out with this rotor's value
    						for (int i = 0; i < 3; i++)
	    						{
    							if (i == iMe)
    								continue;
    							 if (aRotors[i] == id)
    							 	{
    								aspnRotors[i].setSelection(aRotors[iMe]);
    								aRotors[i] = aRotors[iMe];
    							 	}
	    						}
    						
    						aRotors[iMe] = (int) id;
    						
    						Log.d(TAG, "Rotors: " + aRotors[0] + ", " + aRotors[1] + ", " + aRotors[2]);
    						
    						updateEncoding();
    						}

    					public void onNothingSelected(AdapterView<?> arg0) {}
            			});
        	}
        	
        adapter = ArrayAdapter.createFromResource(this, R.array.alpha, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        class SettingsListener implements OnItemSelectedListener
            {
            private EnigmaApp app;
            
            public SettingsListener(EnigmaApp app)
                {
                this.app = app;
                }
            
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                {
                app.updateSettings();
                }
    
            public void onNothingSelected(AdapterView<?> arg0) {}
            }
        
        for (int i = 0; i < 3; i++)
            {
            aspnRings[i] = (Spinner) findViewById(R.id.spn_rings_1 + i);
            aspnRings[i].setAdapter(adapter);
            aspnRings[i].setOnItemSelectedListener(new SettingsListener(this));
            }
        
        for (int i = 0; i < 3; i++)
            {
            aspnStart[i] = (Spinner) findViewById(R.id.spn_start_1 + i);
            aspnStart[i].setAdapter(adapter);
            aspnStart[i].setOnItemSelectedListener(new SettingsListener(this));
            }
        
        plugboard = (EditText) findViewById(R.id.plugboard);
        
        updateSettings();
    	}
    

    }