package com.soapp.registration.country_codes;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.global.Preferences;
import com.soapp.registration.country_codes.AlphabetListAdapter.Item;
import com.soapp.registration.country_codes.AlphabetListAdapter.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import androidx.core.content.ContextCompat;

public class CountryCodes extends ListActivity {
    //basics
    private Preferences preferences = Preferences.getInstance();

    public static String[][] strArrCountryName = new String[][]{{"Afghanistan", "93"}, {"Aland Islands", "358"}, {"Albania", "355"}, {"Algeria", "213"}, {"American Samoa", "1"},
            {"Andorra", "376"}, {"Angola", "244"}, {"Anguilla", "1"}, {"Antigua & Barbuda", "1"}, {"Argentina", "54"}, {"Armenia", "374"}, {"Aruba", "297"}, {"Ascension Island", "247"},
            {"Australia", "61"}, {"Austria", "43"}, {"Azerbaijan", "994"}, {"Bahamas", "1"}, {"Bahrain", "973"}, {"Bangladesh", "880"}, {"Barbados", "1"}, {"Belarus", "375"},
            {"Belgium", "32"}, {"Belize", "501"}, {"Benin", "229"}, {"Bermuda", "1"}, {"Bhutan", "975"}, {"Bolivia", "591"}, {"Bosnia Herzegovina", "387"}, {"Botswana", "267"}, {"Brazil", "55"},
            {"British Indian Ocean", "246"}, {"British Virgin Island", "1"}, {"Brunei", "673"}, {"Bulgaria", "359"}, {"Burkina Faso", "226"}, {"Burundi", "257"},
            {"Cambodia", "855"}, {"Cameroon", "237"}, {"Canada", "1"}, {"Cape Verde Islands", "238"}, {"Caribbean Netherlands", "599"}, {"Cayman Islands", "1"},
            {"Central African Republic", "236"}, {"Chad", "235"}, {"Chile", "56"}, {"China", "86"}, {"Christmas Islands", "61"}, {"Cocos Islands", "61"}, {"Colombia", "57"},
            {"Comoros", "269"}, {"Congo", "242"}, {"Cook Islands", "682"}, {"Costa Rica", "506"}, {"Croatia", "385"}, {"Cuba", "53"}, {"Curacao", "599"}, {"Cyprus", "357"},
            {"Czech Republic", "420"}, {"Denmark", "45"}, {"Djibouti", "253"}, {"Dominica", "1"}, {"Dominican Republic", "1"}, {"Ecuador", "593"}, {"Egypt", "20"},
            {"El Salvador", "503"}, {"Equatorial Guinea", "240"}, {"Eritrea", "291"}, {"Estonia", "372"}, {"Ethiopia", "251"}, {"Falkland Islands", "500"},
            {"Faroe Islands", "298"}, {"Fiji", "679"}, {"Finland", "358"}, {"France", "33"}, {"French Guiana", "594"}, {"French Polynesia", "689"}, {"Gabon", "241"},
            {"Gambia", "220"}, {"Georgia", "995"}, {"Germany", "49"}, {"Ghana", "233"}, {"Gilbraltar", "350"}, {"Greece", "30"}, {"Greenland", "299"}, {"Grenada", "1"},
            {"Guadeloupe", "590"}, {"Guam", "1"}, {"Guantemala", "502"}, {"Guernsey", "44"}, {"Guinea", "224"}, {"Guinea - Bissau", "245"}, {"Guyana", "592"}, {"Haiti", "509"},
            {"Heard & Mcdonalds Islands", "672"}, {"Honduras", "504"}, {"Hong Kong", "852"}, {"Hungary", "36"}, {"Iceland", "354"}, {"India", "91"}, {"Indonesia", "62"},
            {"Iran", "98"}, {"Iraq", "964"}, {"Ireland", "353"}, {"Israel", "972"}, {"Italy", "39"}, {"Jamaica", "1"}, {"Japan", "81"}, {"Jersey", "44"},
            {"Jordan", "962"}, {"Kazakhstan", "7"}, {"Kenya", "254"}, {"Kiribati", "686"}, {"Kuwait", "965"}, {"Kyrgyzstan", "996"}, {"Laos", "856"}, {"Latvia", "371"},
            {"Lebanon", "961"}, {"Lesotho", "266"}, {"Liberia", "231"}, {"Libya", "218"}, {"Liechtenstein", "417"}, {"Lithuania", "370"}, {"Luxembourg", "352"}, {"Macao", "853"}, {"Macedonia", "389"}, {"Madagascar", "261"}, {"Malawi", "265"},
            {"Malaysia", "60"}, {"Maldives", "960"}, {"Mali", "223"}, {"Malta", "356"}, {"Marshall Islands", "692"}, {"Martinique", "596"}, {"Mauritania", "222"}, {"Mauritius", "230"},
            {"Mayotte", "262"}, {"Mexico", "52"}, {"Micronesia", "691"}, {"Moldova", "373"}, {"Monaco", "377"}, {"Mongolia", "976"}, {"Montserrat", "1"}, {"Montenegro", "382"}, {"Morocco", "212"},
            {"Mozambique", "258"}, {"Myanmar", "95"}, {"Namibia", "264"}, {"Nauru", "674"}, {"Nepal", "977"}, {"Netherlands", "31"}, {"New Caledonia", "687"}, {"New Zealand", "64"},
            {"Nicaragua", "505"}, {"Niger", "227"}, {"Nigeria", "234"}, {"Niue", "683"}, {"Norfolk Islands", "672"}, {"Northern Marianas", "1"}, {"Norway", "47"},
            {"Oman", "968"}, {"Pakistan", "92"}, {"Palau", "680"}, {"Palestinian Territories", "970"}, {"Panama", "507"}, {"Papua New Guinea", "675"}, {"Paraguay", "595"}, {"Peru", "51"}, {"Philippines", "63"},
            {"Poland", "48"}, {"Portugal", "351"}, {"Puerto Rico", "1"}, {"Qatar", "974"}, {"Reunion", "262"}, {"Romania", "40"}, {"Russia", "7"}, {"Rwanda", "250"}, {"Samao", "685"}, {"San Marino", "378"}, {"Sao Tome & Principe", "239"},
            {"Saudi Arabia", "966"}, {"Senegal", "221"}, {"Serbia", "381"}, {"Seychelles", "248"}, {"Sierra Leone", "232"}, {"Singapore", "65"}, {"Sint Maarten", "1"}, {"Slobakia", "421"}, {"Slovenia", "386"}, {"So.Georgia & So.Sandwich", "500"},
            {"Solomon Islands", "677"}, {"Somalia", "252"}, {"South Africa", "27"}, {"South Korea", "82"}, {"South Sudan", "211"}, {"Spain", "34"}, {"Sri Lanka", "94"}, {"St. Barthelemy", "590"}, {"St. Helena", "290"}, {"St. Kitts & Nevis", "1"}, {"St. Lucia", "1"}, {"St. Martin", "590"},
            {"St. Pierre & Miquelon", "508"}, {"St. Vincent & Grenadines", "1"}, {"Sudan", "249"}, {"Suriname", "597"}, {"Svalbard & Jan Mayen", "47"},
            {"Swaziland", "268"}, {"Sweden", "46"}, {"Switzerland", "41"}, {"Syria", "963"}, {"Taiwan", "866"}, {"Tajikistan", "992"}, {"Tanzania", "255"}, {"Thailand", "66"}, {"Timor-Leste", "670"},
            {"Togo", "228"}, {"Tokelau", "690"}, {"Trinidad & Tobago", "1"}, {"Tunisia", "216"}, {"Turkey", "90"}, {"Turkmenistan", "993"}, {"Turks & Caicos Islands", "1"}, {"Tuvalu", "688"}, {"U.S Virgin Islands", "1"},
            {"Uganda", "256"}, {"Ukraine", "380"}, {"UAE", "971"}, {"United Kingdom", "44"}, {"United States", "1"}, {"Urugray", "598"}, {"Uzbekistan", "998"}, {"Vanuatu", "678"}, {"Vatican City", "379"},
            {"Venezuela", "58"}, {"Vietnam", "84"}, {"Wallis & Futuna", "681"}, {"Western Sahara", "212"}, {"Yemen", "967"}, {"Zambia", "260"}, {"Zimbabwe", "263"}};
    private static float sideIndexX;
    private static float sideIndexY;
    List<String> countries = new ArrayList<String>();
    List<Row> rows;
    private AlphabetListAdapter adapter = new AlphabetListAdapter();
    private final TextWatcher textWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence arg0, int start, int before, int count) {
            if (arg0.length() > 0) {
                List<Row> rows = new ArrayList<Row>();
                for (String country : countries) {
                    if (country.toUpperCase().contains(arg0.toString().toUpperCase())) {
                        rows.add(new Item(country));
                    }
                }
                adapter.setRows(rows);
                setListAdapter(adapter);
            } else {
                adapter.setRows(rows);
                setListAdapter(adapter);
            }
        }

        public void afterTextChanged(Editable s) {

        }
    };
    private GestureDetector mGestureDetector;
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private int sideIndexHeight;
    private int indexListSize;
    private EditText edtxt_searchCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_countrycodes);

        mGestureDetector = new GestureDetector(this, new SideIndexGestureListener());
        edtxt_searchCountry = findViewById(R.id.edtxt_searchCountry);
        edtxt_searchCountry.addTextChangedListener(textWatcher);
        countries = populateCountries();
        Collections.sort(countries);

        rows = new ArrayList<Row>();
        int start = 0;
        int end = 0;
        String previousLetter = null;
        Object[] tmpIndexItem = null;
        Pattern numberPattern = Pattern.compile("[0-9]");

        for (String country : countries) {
            String firstLetter = country.substring(0, 1);
            if (numberPattern.matcher(firstLetter).matches()) {
                firstLetter = "#";
            }
            if (previousLetter != null && !firstLetter.equals(previousLetter)) {
                end = rows.size() - 1;
                tmpIndexItem = new Object[3];
                tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
                tmpIndexItem[1] = start;
                tmpIndexItem[2] = end;
                alphabet.add(tmpIndexItem);

                start = end + 1;
            }
            if (!firstLetter.equals(previousLetter)) {
//                rows.add(new Section(firstLetter));
                sections.put(firstLetter, start);
            }
            rows.add(new Item(country));
            previousLetter = firstLetter;
        }

        if (previousLetter != null) {
            tmpIndexItem = new Object[3];
            tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
            tmpIndexItem[1] = start;
            tmpIndexItem[2] = rows.size() - 1;
            alphabet.add(tmpIndexItem);
        }

        adapter.setRows(rows);
        setListAdapter(adapter);

        updateList();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item item = (Item) l.getAdapter().getItem(position);
        if (item.text.length() != 1) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", item.text);
            setResult(Activity.RESULT_OK, returnIntent);

            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void updateList() {
        LinearLayout sideIndex = findViewById(R.id.sideIndex);
        sideIndex.removeAllViews();
        indexListSize = alphabet.size();
        if (indexListSize < 1) {
            return;
        }

        int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
        int tmpIndexListSize = indexListSize;
        while (tmpIndexListSize > indexMaxSize) {
            tmpIndexListSize = tmpIndexListSize / 2;
        }
        double delta;
        if (tmpIndexListSize > 0) {
            delta = indexListSize / tmpIndexListSize;
        } else {
            delta = 1;
        }

        TextView tmpTV;
        for (double i = 1; i <= indexListSize; i = i + delta) {
            Object[] tmpIndexItem = alphabet.get((int) i - 1);
            String tmpLetter = tmpIndexItem[0].toString();

            tmpTV = new TextView(this);
            tmpTV.setText(tmpLetter);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextColor(ContextCompat.getColor(CountryCodes.this, R.color.primaryLogo));
            tmpTV.setTextSize(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tmpTV.setLayoutParams(params);
            sideIndex.addView(tmpTV);
        }

        sideIndexHeight = sideIndex.getHeight();

        sideIndex.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sideIndexX = event.getX();
                sideIndexY = event.getY();

                displayListItem();

                return false;
            }
        });
    }

    public void displayListItem() {
        LinearLayout sideIndex = findViewById(R.id.sideIndex);
        sideIndexHeight = sideIndex.getHeight();
        double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;
        int itemPosition = (int) (sideIndexY / pixelPerIndexItem);
        if (itemPosition < alphabet.size()) {
            Object[] indexItem = alphabet.get(itemPosition);
            int subitemPosition = sections.get(indexItem[0]);
            getListView().setSelection(subitemPosition);
        }
    }

    private List<String> populateCountries() {
        List<String> countries = new ArrayList<String>();
        for (int i = 0; i < strArrCountryName.length; i++) {
            countries.add((strArrCountryName[i][0]).trim() + "(" + (strArrCountryName[i][1]).trim() + ")");
        }
        return countries;
    }

    class SideIndexGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            sideIndexX = sideIndexX - distanceX;
            sideIndexY = sideIndexY - distanceY;
            if (sideIndexX >= 0 && sideIndexY >= 0) {
                displayListItem();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

}
