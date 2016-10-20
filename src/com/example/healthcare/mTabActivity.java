package com.example.healthcare;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class mTabActivity extends TabActivity {
	static TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mtab);

		tabHost = getTabHost();

		tabHost.addTab(tabHost
				.newTabSpec("Tab1")
				.setIndicator(getResources().getString(R.string.information),
						getResources().getDrawable(android.R.drawable.ic_menu_agenda))
				// ic_menu_addÎª+Í¼±ê http://www.bubuko.com/infodetail-344018.html
				.setContent(new Intent(this, informationActivity.class)));

		tabHost.addTab(tabHost
				.newTabSpec("Tab2")
				.setIndicator(getResources().getString(R.string.record),
						getResources().getDrawable(android.R.drawable.ic_menu_agenda))
				.setContent(new Intent(this, recordActivity.class)));

		tabHost.addTab(tabHost
				.newTabSpec("Tab3")
				.setIndicator(getResources().getString(R.string.chart),
						getResources().getDrawable(android.R.drawable.ic_menu_agenda))
				.setContent(new Intent(this, chartActivity.class)));

		tabHost.setCurrentTab(1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
