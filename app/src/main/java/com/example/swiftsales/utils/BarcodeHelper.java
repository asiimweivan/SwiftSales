package com.example.swiftsales.utils;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * BarcodeHelper — thin wrapper around ZXing IntentIntegrator.
 *
 * Usage in any Activity:
 *
 *   // 1. Launch scanner:
 *   BarcodeHelper.startScan(this, REQUEST_BARCODE);
 *
 *   // 2. Handle result in onActivityResult():
 *   @Override
 *   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *       if (requestCode == BarcodeHelper.REQUEST_BARCODE) {
 *           String barcode = BarcodeHelper.parseResult(data);
 *           if (barcode != null) { // use barcode }
 *       }
 *       super.onActivityResult(requestCode, resultCode, data);
 *   }
 *
 * Usage from a Fragment (BottomSheetDialogFragment):
 *
 *   BarcodeHelper.startScanFromFragment(this, REQUEST_BARCODE);
 *
 *   // Handle in Fragment.onActivityResult()
 */
public class BarcodeHelper {

    public static final int REQUEST_BARCODE        = 7001;
    public static final int REQUEST_BARCODE_SEARCH = 7002;

    /**
     * Launch the ZXing barcode scanner from an Activity.
     * Results come back to onActivityResult(REQUEST_BARCODE, ...).
     */
    public static void startScan(Activity activity, int requestCode) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setRequestCode(requestCode);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Point camera at barcode");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    /**
     * Launch the ZXing barcode scanner from a Fragment.
     * Results come back to Fragment.onActivityResult(REQUEST_BARCODE, ...).
     */
    public static void startScanFromFragment(
            androidx.fragment.app.Fragment fragment, int requestCode) {
        IntentIntegrator integrator =
                IntentIntegrator.forSupportFragment(fragment);
        integrator.setRequestCode(requestCode);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Point camera at barcode");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    /**
     * Parse the barcode string from onActivityResult data.
     * Returns null if scan was cancelled or failed.
     */
    public static String parseResult(Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(
                Activity.RESULT_OK, data);
        if (result == null) return null;
        String contents = result.getContents();
        return (contents != null && !contents.isEmpty()) ? contents : null;
    }
}