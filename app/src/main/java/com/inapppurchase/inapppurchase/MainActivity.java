package com.inapppurchase.inapppurchase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.inapppurchase.util.IabHelper;
import com.inapppurchase.util.IabResult;
import com.inapppurchase.util.Inventory;
import com.inapppurchase.util.Purchase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "lala";
    private static final String ITEM_SKU = "android.test.purchased";
//    private static final String ITEM_SKU = "com.example.buttonclick";
//    private static final String ITEM_SKU = "android.test.canceled";
//    private static final String ITEM_SKU = "android.test.refunded";
//    private static final String ITEM_SKU = "android.test.item_unavailable";

    private Button buyButton;
    private Button clickButton;
    private IabHelper mHelper;

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        clickButton.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                buyButton.setEnabled(false);
            }

        }
    };

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buyButton = (Button) findViewById(R.id.buy_button);
        clickButton = (Button) findViewById(R.id.click_button);

        clickButton.setEnabled(false);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inAppPurchase();
            }
        });

        IabHelper.context = this;

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAunkyiXBAyDGoQxCsH0w+wfV2E8WKTF6nAiQFWD0m3IhH8Ad35SOhg3B4nePmE/M8VKZZr3zjzE8GlG8JQ4DrFto5yl+m2e3hoFtV9HqjIuLTxdDVjt6eWtKUSFVKzhKmt+74wiUmZXBdeshTxJ5SACqP5eIXElELOd/2ZGqrHyu5jwXiVNVwkEuXVJkYJlk7dcoKy2mP0vHfEElEdbcKsNOKRyNmTX6cDbkTEowsqs+bYxsMrEsaWvDH4H50OCJWgz57ueI24r9Sp0/8G2FFp2PhPDL9i9Ws0McT+ntk/a4GC9RcXQlsdcX/7xzHHUdRvM5vkSDcVF+q44/G7gSxMwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
           public void onIabSetupFinished(IabResult result) {
               if (!result.isSuccess()) {
                   Log.d(TAG, "In-app Billing setup failed: " +
                           result);
               } else {
                   Log.d(TAG, "In-app Billing is set up OK");
               }
           }
       });
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    public void buttonClicked (View view) {
        clickButton.setEnabled(false);
        buyButton.setEnabled(true);
    }

    public void inAppPurchase() {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
