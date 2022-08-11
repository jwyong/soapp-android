package com.soapp.global;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class AddContactHelper {

    Context context;
    Activity activity;

    public AddContactHelper(Context context) {

        this.context = context;
        this.activity = (Activity) context;
    }

    public void createContactPhoneBook(String name, String phone) {
        ContentResolver cr = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
        }
    }


//    private void getUserProfile(final String jid, final String access_token) {
//        //build retrofit
//        String size = miscHelper.getDeviceDensity(context);
//        IndiAPIInterface indiAPIInterface = RetrofitAPIClient.getClient().create(IndiAPIInterface.class);
//        retrofit2.Call<GetUserRepo> call = indiAPIInterface.getIndiProfile(jid, size, "Bearer " + access_token);
//
//        call.enqueue(new retrofit2.Callback<GetUserRepo>() {
//            @Override
//            public void onResponse(retrofit2.Call<GetUserRepo> call, final retrofit2.Response<GetUserRepo> response) {
//                if (!response.isSuccessful()) {
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (dialog != null && dialog.isShowing()) {
//                                dialog.dismiss();
//                            }
//                            new MiscHelper().retroLogUnsuc(context, response, "QrReader  ", "JAY");
//                            Toast.makeText(context, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
//
////                            recreate();
//                        }
//                    });
//                    return;
//                }
//                final String phone = String.valueOf(response.body().getPhone());
//                final String name = String.valueOf(response.body().getName());
//                final String imageURL = String.valueOf(response.body().getResource_url());
//                final String image_data = String.valueOf(response.body().getImage_data());
//
//                Log.d("jason" , phone + " " + name + " " + imageURL + " ++ many ++" + image_data);
//
//                //qr alert dialog
//                //action for allow notification
//                Runnable pushNotificationPositive = () -> {
//
//                    //add contact to phone book
//
////                    new AddContactHelper().createContactPhoneBook(QrReader.this, phoneName, phoneNumber);
//
//                    if (!image_data.equals("Empty")) {
//                        //get the image data
//                        databaseHelper.updateNewContactToContactRoster(jid, phoneNumber, phoneName, imageURL, image_data);
//
//                    } else {
//                        databaseHelper.updateNewContactToContactRoster(jid, phoneNumber, phoneName, imageURL, null);
//                    }
//
////                final String user_id = preferences.getValue(QrReader.this, GlobalVariables.STRPREF_USER_ID);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (dialog != null && dialog.isShowing()) {
//                                dialog.dismiss();
//                            }
//                            Toast.makeText(QrReader.this, "Created a new contact with name: " + phoneName + " and Phone No: " + phoneNumber, Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    });
//
//                };
//
//                //action for cancel notification
//                Runnable pushNotificationNegative = () -> {
//                    //function to refresh qr scanning for next qr code
//                    recreate();
//                };
//
//                if (dialog != null && dialog.isShowing()) {
//                    dialog.dismiss();
//                    if (qrCodeReaderView != null) {
//                        qrCodeReaderView.stopCamera();
//                    }
//                }
//
//                uiHelper.dialog2Btns2Tview(QrReader.this,
//                        getString(R.string.add_contacts),
//                        name,
//                        phone,
//                        pushNotificationPositive,
//                        pushNotificationNegative,
//                        false);
//
//
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<GetUserRepo> call, Throwable t) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (dialog != null && dialog.isShowing()) {
//                            dialog.dismiss();
//                        }
//                        new MiscHelper().retroLogFailure(QrReader.this, t, "QrReader  ", "JAY");
//                        Toast.makeText(QrReader.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
//
//                        //function to refresh qr scanning for next qr code
//                        recreate();
//                    }
//                });
//            }
//        });
//    }
//
//
//    private class AsyncQr extends AsyncTask<String, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            //Display progressDialog before download starts
//            dialog = ProgressDialog.show(QrReader.this, null, getString(R.string.saving_contact));
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//
//        }
//
//        @Override
//        protected Void doInBackground(String... params) {
//            String String2 = params[0];
//            String access_token = params[1];
//
//
//            getUserProfile(String2, access_token);
//
//            return null;
//        }
//    }
//

}
