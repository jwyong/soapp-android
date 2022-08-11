package com.soapp.global;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.soapp.R;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

public class SharingHelper {
    private Preferences preferences = Preferences.getInstance();

    public void shareSoappToFriends(Context context, View progressBarView, View viewToDisable) {
        if (viewToDisable != null) {
            viewToDisable.setEnabled(false);
        }

        progressBarView.setVisibility(View.VISIBLE);

        String user_jid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
        String user_displayname = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

        //need to customise further
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle(context.getString(R.string.soapp_name))
                .setContentDescription(context.getString(R.string.soapp_desc))
                .setContentImageUrl("https://www.soappchat.com/images/soapp_logo_withoutbg-01-178.png")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("key1", "value1"));

        //need to customise further
        LinkProperties lp = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")
                .addControlParameter("$ios_url", "https://itunes.apple.com/us/app/soapp/id1211943707?mt=8")
                .addControlParameter("invitedby", user_jid);

        buo.generateShortUrl(context, lp, (url, error) -> {
            if (error == null) {
                String google_url = "https://play.google.com/store/apps/details?id=com.soapp&referrer=" + user_jid;

                String share_content = Html.fromHtml("has invited you to try Soapp.<br><br>iOS:<br>" +
                        url +
                        "<br><br>Android:<br>" +
                        google_url +
                        "<br><br>What are you waiting for? Give it a go!").toString();

                //create our intent with a action of ACTION_SEND
                Intent sendIntent = new Intent(Intent.ACTION_SEND);

                //we want to send a simple 'text' message
                sendIntent.setType("text/plain");

                //this is the text we are sending
                sendIntent.putExtra(Intent.EXTRA_TEXT, user_displayname + " " + share_content);

                //ask android to show apps that can handle this intent
                context.startActivity(Intent.createChooser(sendIntent, "Share via:"));
            } else {
                Toast.makeText(context, context.getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            }

            //re-enable button
            if (viewToDisable != null) {
                viewToDisable.setEnabled(true);
            }
            progressBarView.setVisibility(View.GONE);

        });
    }
}