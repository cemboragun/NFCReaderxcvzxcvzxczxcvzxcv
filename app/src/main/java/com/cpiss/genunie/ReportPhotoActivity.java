package com.cpiss.genunie;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;

public class ReportPhotoActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    Bitmap productImage;
    ImageView productImageView;
    Intent intent;
    PopupWindow lb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_report_photo);
        productImageView = (ImageView) this.findViewById(R.id.productImageView);
        intent = getIntent();

        new AlertDialog.Builder(ReportPhotoActivity.this)
                .setTitle("Ürünün fotoğrafını çekmek istiyor musunuz?")
                .setCancelable(false)
                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fotoIntentiniBaslat();
                        dialog.dismiss();
                    }
                }).setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SendReportTask().execute();
                        dialog.dismiss();
                    }
        }).create().show();
    }

    private class SendReportTask extends AsyncTask<URL, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            lb = LoadingBar.showLoadingBar(ReportPhotoActivity.this,"Rapor Gönderiliyor");
        }

        @Override
        protected Integer doInBackground(URL... urls) {

            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            lb.dismiss();
            toastIt("Raporunuz Gönderildi");
            finish();
        }
    }

    public void toastIt(String message) {
        Toast.makeText(ReportPhotoActivity.this, message, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // Resim çekmek için başlatıığımız activity geri dönüşü buraya yapıyor bizde buradan extra olarak fotoyu alıyoruz
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            productImage = (Bitmap) extras.get("data");
            productImageView.setImageBitmap(productImage);
            new SendReportTask().execute();
        }
    }

    private void fotoIntentiniBaslat() {// Fotoğraf çekme intentini buradan başlatıyoruz. Ancak burada start activity demiyoruz onn yerine startActivityForResult diyoruz
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
