package com.DizWARE.ShuffleTone.Activites;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.DizWARE.ShuffleTone.R;
import com.DizWARE.ShuffleTone.Others.PreferenceWriter;
import com.android.vending.billing.Billing;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Purchase;

public class Donation extends Activity
{
	//Item Sku list
	private final String[] SKU_LIST = {"donate_1", "donate_5", "donate_10", "donate_15"};
	
	private Button[] donateButtons = new Button[4];
	IabHelper mHelper;
	
	SharedPreferences settings;
	
	/***
	 * Create a donation menu 
	 */
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		this.setContentView(R.layout.donation);
		
		settings = this.getSharedPreferences("settings", 0);
		
		donateButtons[0] = (Button)this.findViewById(R.id.btn_donate_1);
		donateButtons[1] = (Button)this.findViewById(R.id.btn_donate_5);
		donateButtons[2] = (Button)this.findViewById(R.id.btn_donate_10);
		donateButtons[3] = (Button)this.findViewById(R.id.btn_donate_15);
		
		for(Button b : donateButtons) 
			b.setEnabled(!settings.getBoolean("donate", false));
		
		initializeDonations();
		
		setupButtons();
		
		super.onCreate(savedInstanceState);
	}
	
	/**Destroys our in-app manager**/
	@Override protected void onDestroy()
	{
		 if (mHelper != null) mHelper.dispose();
		 mHelper = null;
		 
		 super.onDestroy();
	}
	
	/***
	 * Sets up communication with Google Wallet to allow for in-app purchases. Handles when it is finished as well
	 */
	public void initializeDonations()
	{
		try
		{
			/*This is our in app purchase manager*/
			mHelper = new IabHelper(this, Billing.getKey());
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() 
			{
				/***Runs when the setup with the s tore has completed***/
			   public void onIabSetupFinished(IabResult result)
			   {
				   /**If we failed to set up**/
			      if (!result.isSuccess()) 
			      {
			    	  Toast.makeText(Donation.this, "Donation setup failed.", 
								Toast.LENGTH_LONG).show();
			         return;
			      }            				        
			   }
			});
			
		} catch (Exception e)
		{
			Toast.makeText(this, "Could not decode public key\nWill not be able to do in-app purchases", 
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	/***
	 * Sets up the donation buttons with the correct amounts.
	 */
	public void setupButtons()
	{
		
		
		for(int i = 0; i < 4; i++)
		{
			donateButtons[i].setTag(i);
			donateButtons[i].setOnClickListener(new Button.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					//Turn off the purchase buttons so that we don't have a problem with purchase syncing
					for(Button b : donateButtons) b.setEnabled(false);
					
					int i = (Integer)v.getTag();
					mHelper.launchSubscriptionPurchaseFlow(Donation.this, SKU_LIST[i], 9876 + i, new OnIabPurchaseFinishedListener()
					{						
						@Override public void onIabPurchaseFinished(IabResult result, Purchase info)
						{
							//Turn buttons back on
							for(Button b : donateButtons) b.setEnabled(true);
							
							if(result.isSuccess()) 
								PreferenceWriter.booleanWriter(settings, "donated", true);
								
						}
					}, Billing.getKey(i));
				}
			});
		}
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{		
	    // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } 
	}
}
