package com.cpiss.genunie;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReaderActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    public static final String INFO_READ_TEXT = "Lütfen NFC Tagını Okutunuz.";

    TextView readerInfo;

    Button closeOrginalLButton;
    LinearLayout orginalLayout;

    LinearLayout errorLayout;
    Button feedbackButton;
    Button closeELButton;

    PopupWindow currentWindow;

    String productInf;

    ImageLoader imageLoader = ImageLoader.getInstance();
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_reader);

        readerInfo = (TextView) findViewById(R.id.readerInfo);
        closeOrginalLButton = (Button) findViewById(R.id.closeOrginalLButton);
        orginalLayout = (LinearLayout) findViewById(R.id.orginalLayout);
        errorLayout = (LinearLayout) findViewById(R.id.errorLayout);

        feedbackButton = (Button) findViewById(R.id.feedbackButton);
        closeELButton = (Button) findViewById(R.id.closeELButton);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(false).build(); // Profil fotosu değiştiği için cacheden almasını istemiyoruz
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(defaultOptions).build();
        imageLoader.init(config);

        //NFC ------
        checkNFC();
        //------

        handleIntent(getIntent());

        //--Layout initialize
        orginalLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);

        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent reportIntent = new Intent(ReaderActivity.this, ReportLocationActivity.class);
                reportIntent.putExtra("productId", productInf);
                startActivity(reportIntent);

                errorLayout.setVisibility(View.GONE);
            }
        });

        closeELButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorLayout.setVisibility(View.GONE);
            }
        });
        closeOrginalLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orginalLayout.setVisibility(View.GONE);
            }
        });
    }

    public void checkNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            readerInfo.setText("Bu cihaz NFC okuyamıyor.");
        } else if (!mNfcAdapter.isEnabled()) {
            new AlertDialog.Builder(ReaderActivity.this)
                    .setTitle("NFC özelliğiniz açık değil. Lütfen açınız.")
                    .setCancelable(false)
                    .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkNFC();
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            readerInfo.setText(INFO_READ_TEXT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        if (mNfcAdapter != null)
            stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                new NdefReaderTask().execute(tag);

            } else {
                readerInfo.setText("Tag Hatası, tekrar okutunuz.");
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            currentWindow = LoadingBar.showLoadingBar(ReaderActivity.this, "Ürün Kontrol Ediliyor.");
        }

        @Override
        protected JSONObject doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return validateProduct(Base64.encodeToString(tag.getId(), Base64.DEFAULT), readText(ndefRecord));

                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text


            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        public JSONObject validateProduct(String tagId, String tagMessage) {
            List<NameValuePair> values = new ArrayList<NameValuePair>();
            values.add(new BasicNameValuePair("tagId", tagId + ""));
            values.add(new BasicNameValuePair("tagMessage", tagMessage + ""));

            String resultString = Utils.getStringFromConnection("https://f-otantik.cpiss.org/api/validate/valid", values);

            try {
                JSONObject product = new JSONObject(resultString).getJSONObject("data").getJSONObject("product");
                return product;
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            currentWindow.dismiss();
            if (result != null) {
                L.l("result>>>" + result.toString());
                setOrginalLayout(result);
            } else {
                errorLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setOrginalLayout(JSONObject product) {
        TextView productBrand = (TextView) this.findViewById(R.id.productBrand);
        TextView productModel = (TextView) this.findViewById(R.id.productModel);
        TextView productSerial = (TextView) this.findViewById(R.id.productSerial);
        ImageView productImage = (ImageView) this.findViewById(R.id.productImage);
        productImage.setImageResource(R.drawable.orginal);

        try {
            productBrand.setText("Marka : "+product.getString("brand"));
        } catch (JSONException e) {
            productBrand.setText("Marka bilgisi yok");
        }

        try {
            productModel.setText("Model : "+product.getString("model"));
        } catch (JSONException e) {
            productModel.setText("Model bilgisi yok");
        }

        try {
            productSerial.setText("Seri Numarası : "+product.getString("seri"));
        } catch (JSONException e) {
            productSerial.setText("Seri numarası yok");
        }
        try {
            imageLoader.displayImage(product.getString("image"),productImage );
        }catch (JSONException e) {
            productSerial.setText("Seri numarası yok");
        }


        orginalLayout.setVisibility(View.VISIBLE);
    }
}