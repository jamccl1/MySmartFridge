package fridge02.mysmartfridge;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;

import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;

import android.widget.Toast;



import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import android.text.TextUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {


    private ArrayList<LinearLayout> recipesInList, orderableItemsInList,  groceriesInList;
    private ArrayList<String> itemsInCart, groceryItemNames;
    private String[] paymentInfo, currentRecipe;
    private String lastRecipeSearch, lastOnlineOrderSearch, safeModePassword, routerPassword;
    private int checkoutFieldsComplete, passwordFieldsComplete, temperature, selectedLanguage, selectedRouter;
    private final int LOW_TEMP = 32, HIGH_TEMP = 50;
    private float cartTotal;
    private boolean isTablet, saveCheckoutInfo, useExpressShipping, isCelcius, inSafeMode;
    private boolean[] systemsOptions;
    private HashMap<String, String> itemsInFridge;
    private boolean groceriesAdded = false;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Do basic home screen things for tablet or phone. Note that the IDE will get upset if
        // you lock the orientation (which I think we want to do), hence the suppression above
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // Set temperature
        temperature = 35; // Default to 35 degrees F.
        setHomeScreenTemperature(temperature);

        if (!isTablet) {
            final TextView tempText = findViewById(R.id.temperature);
            SeekBar tempBar = findViewById(R.id.homeTempBar);
            tempBar.setProgress(temperature - LOW_TEMP);
            tempBar.setMax(HIGH_TEMP - LOW_TEMP);

            tempText.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isCelcius = !isCelcius;

                    if (isCelcius) {
                        tempText.setText(getString(R.string.temperature_cel, toCelcius(temperature)));
                    } else {
                        tempText.setText(getString(R.string.temperature_far, temperature));
                    }
                }
            });

            tempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    temperature = LOW_TEMP + i;

                    if (isCelcius) {
                        tempText.setText(getString(R.string.temperature_cel, toCelcius(temperature)));
                    } else {
                        tempText.setText(getString(R.string.temperature_far, temperature));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        // Set current displayed recipes to an empty list;
        recipesInList = new ArrayList<>();
        orderableItemsInList = new ArrayList<>();
        itemsInCart = new ArrayList<>();
        paymentInfo = new String[9];
        itemsInFridge = new HashMap<>();
        systemsOptions = new boolean[] {true, true, true};
        currentRecipe = null;

        // Set last searched recipe/online order
        lastRecipeSearch = "";
        lastOnlineOrderSearch = "";

        //Set groceries shopping list to be empty list
        groceriesInList = new ArrayList<>();
        groceryItemNames = new ArrayList<>();

        // Settings menu spinner storage
        selectedLanguage = 0;
        selectedRouter = 0;

        // Initial passwords
        safeModePassword = null;
        routerPassword = "";

        // Set booleans
        saveCheckoutInfo = false;
        useExpressShipping = false;
        isCelcius = false;
        inSafeMode = false;

        // Set numbers
        cartTotal = 0;
        checkoutFieldsComplete = 0;
        passwordFieldsComplete = 0;

        if (isTablet) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String[] timeStr = formatter.format(new Date()).split(":");
            int hours = Integer.parseInt(timeStr[0]) % 12;
            int minutes = Integer.parseInt(timeStr[1]);
            TextView time = findViewById(R.id.time);
            time.setText(getString(R.string.time, hours, minutes < 10 ? "0" : "", minutes));

            // There won't be anything in the fridge yet, so nothing is expired!
            ImageView expiredIcon = findViewById(R.id.expiredIcon);
            expiredIcon.setImageDrawable(getResources().getDrawable(R.drawable.fresh_food));
        }
    }

    //FRIDGE STUFF:

    public void toWhatsInFridge(View view) {
        setContentView(R.layout.whats_in_fridge);

        for (String name: itemsInFridge.keySet()) {
            addFridgeItem(name, itemsInFridge.get(name));
        }

    }

    public void addFridgeItem(String name, String expiration) {
        float[] phoneWeights = {2.5f, 2.5f, 1.5f};
        float[] tabletWeights = {2.5f, 2.5f, 1.5f};
        float[] weights = isTablet ? tabletWeights : phoneWeights;

        LinearLayout fridgeScrollLayout = findViewById(R.id.fridgeScrollLayout);

        TextView itemName = new TextView(this);
        itemName.setText(name);
        itemName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[0]));
        itemName.setGravity(Gravity.CENTER);
        itemName.setTextSize(STP(R.dimen.small_text_size));
        itemName.setTextColor(Color.BLACK);
        //itemName.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_image_width), DTP(R.dimen.recipes_image_height)));
        //itemName.setGravity(Gravity.CENTER);
        //itemName.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        TextView expirationDate = new TextView(this);
        expirationDate.setText(expiration);
        expirationDate.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[1]));
        expirationDate.setGravity(Gravity.CENTER);
        expirationDate.setTextSize(STP(R.dimen.smaller_text_size));
        expirationDate.setTextColor(Color.BLACK);
        //expirationDate.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_name_width), DTP(R.dimen.recipes_name_height)));
        //expirationDate.setGravity(Gravity.CENTER);
        //expirationDate.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        Button button = new Button(this);
        button.setText(R.string.fridge_item_remove);
        button.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_button_height), weights[2]));
        //button.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_select_button_width), DTP(R.dimen.recipes_select_button_height)));
        button.setAllCaps(true);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(STP(R.dimen.what_in_fridge_remove));

        LinearLayout inFridge = new LinearLayout(this);
        inFridge.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
        inFridge.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.order_item_height)));
        //inFridge.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.order_item_height)));
        inFridge.setOrientation(LinearLayout.HORIZONTAL);
        inFridge.setGravity(Gravity.CENTER_VERTICAL);
        inFridge.addView(itemName);
        inFridge.addView(expirationDate);
        inFridge.addView(button);

        //value to be passed into remove method
        final LinearLayout inFridgeFinal = inFridge;
        final String key = name;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof Button) {
                    //String[] recipeInfo = (String[]) view.getTag();
                    //int imageId = getResources().getIdentifier(recipeInfo[3], "drawable", getPackageName());
                    remove(inFridgeFinal, key);
                }
            }
        });

        fridgeScrollLayout.addView(inFridge);
    }

    private void remove(LinearLayout inFridge, String key) {
        LinearLayout fridgeScrollLayout = findViewById(R.id.fridgeScrollLayout);
        fridgeScrollLayout.removeView(inFridge);

        itemsInFridge.remove(key);

    }

    public void toAddItem(View view) {
        setContentView(R.layout.add_food_item);
    }

    public void addNewItem(View view) {
        EditText nameText = findViewById(R.id.itemNameText);
        EditText countText = findViewById(R.id.itemCountText);
        EditText dayText = findViewById(R.id.itemExpText);

        String name = nameText.getText().toString();
        String countStr = countText.getText().toString();
        String day = dayText.getText().toString();


        if ((!name.equals("")) && (!day.equals("")) && (!countStr.equals("")) && (name.matches("^[a-zA-Z0-9]*$")) && (Integer.parseInt(countStr) > 0) && (day.matches("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/([0-9]{4})\\s*$"))) {

            Pattern p = Pattern.compile("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/([0-9]{4})\\s*$");
            Matcher m = p.matcher(day);
            m.matches();
            int year = Integer.parseInt(m.group(3));

            if (year < 2020) {
                dayText.getText().clear();
            } else {
                String key = name + " (" + Integer.parseInt(countStr) + ")";
                String value = "exp: " + day;

                if (itemsInFridge.get(key) != null) {
                    if (itemsInFridge.get(key).equals(value)) {
                        int actualCount = Integer.parseInt(countStr) * 2;
                        itemsInFridge.remove(key);
                        itemsInFridge.put(name + " (" + actualCount + ")", value);
                    } else {
                        String newKey = name + " (" + Integer.parseInt(countStr) + ") ";
                        itemsInFridge.put(newKey, value);
                    }
                } else {
                    itemsInFridge.put(key, value);
                }
                toWhatsInFridge(view);
            }

        }

        if (!day.matches("^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}\\s*$")) {
            //delete anything in the edit text field
            dayText.getText().clear();
        }

        if ((!countStr.equals("")) && (Integer.parseInt(countStr) <= 0)) {
            //delete anything in the edit text field
            countText.getText().clear();
        }

    }

    public void toShopping(View view) {
        //hi
        setContentView(R.layout.shopping);
        loadGroceryList(view);

    }

    // Sets up the Recipes screen.
    public void toRecipes(View view) {
        setContentView(R.layout.recipes);
        currentRecipe = null;
        loadRecipes(lastRecipeSearch); // Load all recipes

        final HorizontalScrollView scrollView = findViewById(R.id.recipesScrollView);
        final ImageView canScrollUp = findViewById(R.id.recipesCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.recipesCanScrollDown);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                canScrollDown.setAlpha(scrollView.canScrollHorizontally(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollHorizontally(-1) ? 1f : 0.25f);
            }
        });

        // Configures the search bar to, every time it's text is changed, clear the recipes list and
        // then re-add all recipes whose names contain the search text as a substring.
        SearchView searchbar = findViewById(R.id.recipeSearchBar);

        LinearLayout linearLayout1 = (LinearLayout) searchbar.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView test = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        test.setTextSize(STP(R.dimen.normal_text_size));

        searchbar.setQuery(lastRecipeSearch, false);
        if (!lastRecipeSearch.equals("")) {
            searchbar.setIconified(false);
            searchbar.requestFocus();
        } else {
            searchbar.setIconified(true);
        }
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                lastRecipeSearch = s;
                clearRecipes();
                loadRecipes(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                lastRecipeSearch = s;
                clearRecipes();
                loadRecipes(s);
                return false;
            }
        });
    }

    public void toOrderOnline(View view) {
        setContentView(R.layout.order_online);
        populateOrderableItems(lastOnlineOrderSearch);

        final ScrollView scrollView = findViewById(R.id.orderScrollView);
        final ImageView canScrollUp = findViewById(R.id.orderCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.orderCanScrollDown);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });

        SearchView searchbar = findViewById(R.id.orderSearchBar);

        LinearLayout linearLayout1 = (LinearLayout) searchbar.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView test = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        test.setTextSize(STP(R.dimen.normal_text_size));

        searchbar.setQuery(lastOnlineOrderSearch, false);
        if (!lastOnlineOrderSearch.equals("")) {
            searchbar.setIconified(false);
            searchbar.requestFocus();
        } else {
            searchbar.setIconified(true);
        }

        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                lastOnlineOrderSearch = s;
                clearOrderableItems();
                populateOrderableItems(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                lastOnlineOrderSearch = s;
                clearOrderableItems();
                populateOrderableItems(s);

                return false;
            }
        });

        Button backButton = findViewById(R.id.orderBackButton);
        backButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentRecipe != null) {
                    createRecipeScreen(currentRecipe);
                } else {
                    toHome(view);
                }
            }
        });
    }

    public void toCart(View view) {
        setContentView(R.layout.order_online_cart);
        populateCart();
    }

    public void toCheckout(View view) {
        setContentView(R.layout.order_online_checkout);

        // Input Fields
        final EditText firstName = findViewById(R.id.chktFirstNameField);
        final EditText middleInitial = findViewById(R.id.chktMiddleInitialField);
        final EditText lastName = findViewById(R.id.chktLastNameField);
        final EditText address = findViewById(R.id.chktAddressField);
        final EditText city = findViewById(R.id.chktCityField);
        final Spinner state = findViewById(R.id.chktStateSpinner);
        final EditText zipCode = findViewById(R.id.chktZipCodeField);
        final EditText creditCardNumber = findViewById(R.id.chktCreditCardField);
        final EditText cvcNumber = findViewById(R.id.chktCvcField);

        if (isTablet) {
            // Make spinner text size normal
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.checkout_states, R.layout.custom_spinner);
            adapter.setDropDownViewResource(R.layout.custom_spinner);
            state.setAdapter(adapter);
        }

        final EditText[] fieldArray = {firstName, middleInitial, lastName, address, city, zipCode, creditCardNumber, cvcNumber};

        final Button payButton = findViewById(R.id.chktPayButton);
        payButton.setText(getString(R.string.chkt_pay_button_text, getPriceString(cartTotal * (useExpressShipping ? 1.05f : 1))));
        payButton.setEnabled(checkoutFieldsComplete >= 8);

        CheckBox saveInfo = findViewById(R.id.chktSaveCheckbox);
        saveInfo.setChecked(saveCheckoutInfo);
        saveInfo.setTextColor(saveCheckoutInfo ? Color.BLACK : Color.parseColor("#737373"));

        if (saveCheckoutInfo) {
            firstName.setText(paymentInfo[0]);
            middleInitial.setText(paymentInfo[1]);
            lastName.setText(paymentInfo[2]);
            address.setText(paymentInfo[3]);
            city.setText(paymentInfo[4]);

            String[] states = getResources().getStringArray(R.array.checkout_states);
            for (int i = 0; i < states.length; i++) {
                if (states[i].equals(paymentInfo[5])) {
                    state.setSelection(i, true);
                }
            }

            zipCode.setText(paymentInfo[6]);
            creditCardNumber.setText(paymentInfo[7]);
            cvcNumber.setText(paymentInfo[8]);
        }

        saveInfo.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    paymentInfo[0] = firstName.getText().toString();
                    paymentInfo[1] = middleInitial.getText().toString();
                    paymentInfo[2] = lastName.getText().toString();
                    paymentInfo[3] = address.getText().toString();
                    paymentInfo[4] = city.getText().toString();
                    paymentInfo[5] = state.getSelectedItem().toString();
                    paymentInfo[6] = zipCode.getText().toString();
                    paymentInfo[7] = creditCardNumber.getText().toString();
                    paymentInfo[8] = cvcNumber.getText().toString();

                    saveCheckoutInfo = true;

                    compoundButton.setTextColor(Color.BLACK);
                } else {
                    paymentInfo = new String[9];
                    saveCheckoutInfo = false;

                    compoundButton.setTextColor(Color.parseColor("#737373"));
                }
            }
        });

        final CheckBox expressShipping = findViewById(R.id.chktShippingCheckbox);
        expressShipping.setChecked(useExpressShipping);
        expressShipping.setText(getString(R.string.chkt_express_option, getPriceString(cartTotal * 0.05f)));
        expressShipping.setTextColor(useExpressShipping ? Color.BLACK : Color.parseColor("#737373"));
        expressShipping.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    payButton.setText(getString(R.string.chkt_pay_button_text, getPriceString(cartTotal * 1.05f)));
                    useExpressShipping = true;
                    compoundButton.setTextColor(Color.BLACK);
                } else {
                    payButton.setText(getString(R.string.chkt_pay_button_text, getPriceString(cartTotal)));
                    useExpressShipping = false;
                    compoundButton.setTextColor(Color.parseColor("#737373"));
                }
            }
        });

        Button backButton = findViewById(R.id.chktBackButton);
        backButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveCheckoutInfo) {
                    paymentInfo[0] = firstName.getText().toString();
                    paymentInfo[1] = middleInitial.getText().toString();
                    paymentInfo[2] = lastName.getText().toString();
                    paymentInfo[3] = address.getText().toString();
                    paymentInfo[4] = city.getText().toString();
                    paymentInfo[5] = state.getSelectedItem().toString();
                    paymentInfo[6] = zipCode.getText().toString();
                    paymentInfo[7] = creditCardNumber.getText().toString();
                    paymentInfo[8] = cvcNumber.getText().toString();
                } else {
                    checkoutFieldsComplete = 0;
                }

                toCart(view);
            }
        });

        final TextView warningText = isTablet ? (TextView) findViewById(R.id.chktWarningText) : new TextView(this);

        TextWatcher textChangedListener = new TextWatcher() {
            private String prevText;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevText = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currStr = charSequence.toString();
                if (prevText.equals("") && !currStr.equals("")) {
                    checkoutFieldsComplete++;

                    if (checkoutFieldsComplete >= 8) {
                        payButton.setEnabled(true);

                        if (isTablet) {
                            warningText.setVisibility(View.INVISIBLE);
                        }
                    }
                } else if (!prevText.equals("") && currStr.equals("")) {
                    checkoutFieldsComplete--;
                    payButton.setEnabled(false);

                    if (isTablet) {
                        warningText.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        for (EditText field : fieldArray) {
            field.addTextChangedListener(textChangedListener);
        }

        final TextView thanksBackground = findViewById(R.id.chktBlur);
        final TextView thanksText = findViewById(R.id.chktThankYou);
        final ProgressBar thanksBar = findViewById(R.id.chktThinkingBar);
        payButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveCheckoutInfo) {
                    paymentInfo[0] = firstName.getText().toString();
                    paymentInfo[1] = middleInitial.getText().toString();
                    paymentInfo[2] = lastName.getText().toString();
                    paymentInfo[3] = address.getText().toString();
                    paymentInfo[4] = city.getText().toString();
                    paymentInfo[5] = state.getSelectedItem().toString();
                    paymentInfo[6] = zipCode.getText().toString();
                    paymentInfo[7] = creditCardNumber.getText().toString();
                    paymentInfo[8] = cvcNumber.getText().toString();
                } else {
                    checkoutFieldsComplete = 0;
                }

                thanksBackground.setVisibility(View.VISIBLE);
                thanksText.setVisibility(View.VISIBLE);
                thanksBar.setVisibility(View.VISIBLE);
                final View holdView = view; // So we can use it in the runnable call

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        thanksText.setText(R.string.chkt_complete_text);
                    }
                }, 1500);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cartTotal = 0;
                        itemsInCart.clear();

                        if (currentRecipe != null) {
                            createRecipeScreen(currentRecipe);
                        } else {
                            toHome(holdView);
                        }
                    }
                }, 2500);
            }
        });
    }

    public void toLookInside(View view) {
        setContentView(R.layout.look_inside);
        showInsideFridge(null);
    }

    public void toSettings(View view) {
        setContentView(R.layout.settings);

        // Make spinner text size normal
        final Spinner languageSpinner = findViewById(R.id.settingsLanguageSpinner);
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this, R.array.languages, R.layout.custom_spinner);
        languageAdapter.setDropDownViewResource(R.layout.custom_spinner);
        languageSpinner.setAdapter(languageAdapter);
        languageSpinner.setSelection(selectedLanguage, true);

        final Spinner routerSpinner = findViewById(R.id.settingsRouterSpinner);
        ArrayAdapter<CharSequence> routerAdapter = ArrayAdapter.createFromResource(this, R.array.routers, R.layout.custom_spinner);
        routerAdapter.setDropDownViewResource(R.layout.custom_spinner);
        routerSpinner.setAdapter(routerAdapter);
        routerSpinner.setSelection(selectedRouter, true);

        final TextView temperatureText = findViewById(R.id.settingsTempText);
        temperatureText.setText(getString(R.string.temperature_far, isCelcius ? toCelcius(temperature) : temperature));

        final SeekBar temperatureBar = findViewById(R.id.settingsTempBar);
        temperatureBar.setProgress(temperature - LOW_TEMP);
        temperatureBar.setMax(HIGH_TEMP - LOW_TEMP);

        temperatureBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                temperature = LOW_TEMP + i;

                if (isCelcius) {
                    temperatureText.setText(getString(R.string.temperature_cel, toCelcius(temperature)));
                } else {
                    temperatureText.setText(getString(R.string.temperature_far, temperature));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final Button toCelciusButton = findViewById(R.id.settingsDegreeButton);
        toCelciusButton.setText(isCelcius ? R.string.settings_dg_btn_to_far : R.string.settings_dg_btn_to_celcius);
        toCelciusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCelcius) {
                    toCelciusButton.setText(R.string.settings_dg_btn_to_celcius);
                    temperatureText.setText(getString(R.string.temperature_far, temperature));
                    isCelcius = false;
                } else {
                    toCelciusButton.setText(R.string.settings_dg_btn_to_far);
                    temperatureText.setText(getString(R.string.temperature_cel, toCelcius(temperature)));
                    isCelcius = true;
                }
            }
        });

        Switch coolSwitch = findViewById(R.id.settingsCoolingSwitch);
        Switch filterSwitch = findViewById(R.id.settingsFilterSwitch);
        Switch lightSwitch = findViewById(R.id.settingsLightSwitch);

        coolSwitch.setChecked(systemsOptions[0]);
        filterSwitch.setChecked(systemsOptions[1]);
        lightSwitch.setChecked(systemsOptions[2]);

        coolSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                systemsOptions[0] = b;
            }
        });

        filterSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                systemsOptions[1] = b;
            }
        });

        lightSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                systemsOptions[2] = b;
            }
        });

        final EditText routerPasswordField = findViewById(R.id.settingsRouterPasswordField);
        routerPasswordField.setText(routerPassword);

        Button backButton = findViewById(R.id.settingsBackButton);
        backButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedLanguage = languageSpinner.getSelectedItemPosition();
                selectedRouter = routerSpinner.getSelectedItemPosition();
                routerPassword = routerPasswordField.getText().toString();

                toHome(view);
            }
        });

        Button safeModeButton = findViewById(R.id.settingsSafeModeButton);

        if (safeModePassword == null) {
            safeModeButton.setText(R.string.settings_safe_mode_password_create);
        } else {
            safeModeButton.setText(R.string.settings_safe_mode_password_change);
        }

        final TextView blur = findViewById(R.id.settingsBlur);
        blur.setVisibility(View.INVISIBLE);

        safeModeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                blur.setVisibility(View.VISIBLE);
                createSetPasswordDialog(view);
            }
        });

        final Switch safeModeSwitch = findViewById(R.id.settingsSafeModeSwitch);
        safeModeSwitch.setChecked(inSafeMode);

        safeModeSwitch.setOnClickListener(new Switch.OnClickListener() {
            @Override
            public void onClick(View view) {
                safeModeSwitch.setChecked(!safeModeSwitch.isChecked());
                blur.setVisibility(View.VISIBLE);
                createEnterPasswordDialog(view);
            }
        });
    }

    private void createEnterPasswordDialog(View view) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            View dialogView = inflater.inflate(R.layout.enter_password_dialog, null);
            int width = DTP(R.dimen.enter_password_dialog_width);
            int height = DTP(R.dimen.enter_password_dialog_height);
            final PopupWindow dialog = new PopupWindow(dialogView, width, height, true);
            dialog.showAtLocation(view, Gravity.CENTER, 0, 0);

            final TextView warningText = dialogView.findViewById(R.id.enterPasswordWarning);
            if (safeModePassword == null) {
                warningText.setText(R.string.enter_password_warning);
            } else {
                warningText.setVisibility(View.INVISIBLE);
            }

            final Button continueButton = dialogView.findViewById(R.id.enterPasswordContinueButton);
            continueButton.setEnabled(false);

            TextWatcher textChangedListener = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String currStr = charSequence.toString();
                    continueButton.setEnabled(!currStr.equals("") && safeModePassword != null);
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            };

            final EditText passwordField = dialogView.findViewById(R.id.enterPasswordField);
            passwordField.addTextChangedListener(textChangedListener);

            continueButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String password = passwordField.getText().toString();

                    if (password.equals(safeModePassword)) {
                        inSafeMode = !inSafeMode;

                        Switch safeModeSwitch = findViewById(R.id.settingsSafeModeSwitch);
                        safeModeSwitch.setChecked(inSafeMode);

                        TextView blur = findViewById(R.id.settingsBlur);
                        blur.setVisibility(View.INVISIBLE);

                        dialog.dismiss();
                    } else {
                        warningText.setVisibility(View.VISIBLE);
                    }
                }
            });

            Button backButton = dialogView.findViewById(R.id.enterPasswordBackButton);
            backButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView blur = findViewById(R.id.settingsBlur);
                    blur.setVisibility(View.INVISIBLE);
                    dialog.dismiss();
                }
            });
        }
    }

    private void createSetPasswordDialog(View view) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            View dialogView = inflater.inflate(R.layout.create_password_dialog, null);
            int width = DTP(R.dimen.create_password_dialog_width);
            int height = DTP(R.dimen.create_password_dialog_height);
            final PopupWindow dialog = new PopupWindow(dialogView, width, height, true);
            dialog.showAtLocation(view, Gravity.CENTER, 0, 0);

            final TextView warningText = dialogView.findViewById(R.id.setPasswordWarning);
            warningText.setVisibility(View.INVISIBLE);

            if (safeModePassword != null) {
                TextView title = dialogView.findViewById(R.id.setPasswordTitle);
                title.setText(R.string.create_password_change_title);

                TextView passwordLabel = dialogView.findViewById(R.id.setPasswordLabel);
                passwordLabel.setText(R.string.create_password_current_label);

                TextView confirmLabel = dialogView.findViewById(R.id.setPasswordConfirmLabel);
                confirmLabel.setText(R.string.create_password_new_label);

                warningText.setText(R.string.create_password_incorrect);
            }

            final Button setButton = dialogView.findViewById(R.id.setPasswordButton);
            setButton.setEnabled(false);

            TextWatcher textChangedListener = new TextWatcher() {
                private String prevText;

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    prevText = charSequence.toString();
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String currStr = charSequence.toString();

                    if (prevText.equals("") && !currStr.equals("")) {
                        passwordFieldsComplete++;

                        if (passwordFieldsComplete >= 2) {
                            setButton.setEnabled(true);
                        }
                    } else if (!prevText.equals("") && currStr.equals("")) {
                        passwordFieldsComplete--;

                        if (passwordFieldsComplete < 2) {
                            setButton.setEnabled(false);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            };

            final EditText passwordField = dialogView.findViewById(R.id.setPasswordField);
            final EditText confirmField = dialogView.findViewById(R.id.setPasswordConfirmField);
            passwordField.addTextChangedListener(textChangedListener);
            confirmField.addTextChangedListener(textChangedListener);

            setButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean initial = safeModePassword == null;
                    String password = passwordField.getText().toString();
                    String confirm = confirmField.getText().toString();

                    if (initial) {
                        if (password.equals(confirm)) {
                            Button setButton = findViewById(R.id.settingsSafeModeButton);
                            setButton.setText(R.string.settings_safe_mode_password_change);

                            TextView blur = findViewById(R.id.settingsBlur);
                            blur.setVisibility(View.INVISIBLE);

                            safeModePassword = password;
                            passwordFieldsComplete = 0;
                            dialog.dismiss();
                        } else {
                            warningText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (password.equals(safeModePassword)) {
                            TextView blur = findViewById(R.id.settingsBlur);
                            blur.setVisibility(View.INVISIBLE);

                            safeModePassword = confirm;
                            passwordFieldsComplete = 0;
                            dialog.dismiss();
                        } else {
                            warningText.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            Button backButton = dialogView.findViewById(R.id.setPasswordBackButton);
            backButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView blur = findViewById(R.id.settingsBlur);
                    blur.setVisibility(View.INVISIBLE);

                    passwordFieldsComplete = 0;
                    dialog.dismiss();
                }
            });
        }
    }

    private int toCelcius(int tempf) {
        float sub = tempf - 32;
        float tempc = sub * (5f / 9f);
        return Math.round(tempc);
    }

    public void toHome(View view) {
        setContentView(R.layout.home);
        setHomeScreenTemperature(temperature);

        Button orderOnlineButton = findViewById(R.id.orderOnlineButton);
        orderOnlineButton.setEnabled(!inSafeMode);

        if (isTablet) {
            SimpleDateFormat clockFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String[] timeStr = clockFormatter.format(new Date()).split(":");
            int hours = Integer.parseInt(timeStr[0]) % 12;
            int minutes = Integer.parseInt(timeStr[1]);
            TextView time = findViewById(R.id.time);
            time.setText(getString(R.string.time, hours, minutes < 10 ? "0" : "", minutes));

            boolean somethingIsExpired = false;

            for (String value : itemsInFridge.values()) {
                SimpleDateFormat expFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

                try {
                    Date expDate = expFormatter.parse(value.replace("exp: ", ""));
                    String currentDateStr = expFormatter.format(new Date());
                    Date currentDate = expFormatter.parse(currentDateStr);
                    if (currentDate.after(expDate)) {
                        somethingIsExpired = true;
                    }
                } catch (ParseException e) {
                    System.out.println("Could not parse date");
                }
            }

            ImageView expiredIcon = findViewById(R.id.expiredIcon);
            if (somethingIsExpired) {
                expiredIcon.setImageDrawable(getResources().getDrawable(R.drawable.expired_food));
            } else {
                expiredIcon.setImageDrawable(getResources().getDrawable(R.drawable.fresh_food));
            }
        } else {
            final TextView tempText = findViewById(R.id.temperature);
            SeekBar tempBar = findViewById(R.id.homeTempBar);
            tempBar.setProgress(temperature - LOW_TEMP);
            tempBar.setMax(HIGH_TEMP - LOW_TEMP);

            tempText.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isCelcius = !isCelcius;

                    if (isCelcius) {
                        tempText.setText(getString(R.string.temperature_cel, toCelcius(temperature)));
                    } else {
                        tempText.setText(getString(R.string.temperature_far, temperature));
                    }
                }
            });

            tempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    temperature = LOW_TEMP + i;

                    if (isCelcius) {
                        tempText.setText(getString(R.string.temperature_cel, toCelcius(temperature)));
                    } else {
                        tempText.setText(getString(R.string.temperature_far, temperature));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    // RECIPES METHODS //

    // Goes through the list of recipes in recipes.xml and adds them to the UI.
    public void loadRecipes(String searchString) {
        Resources r = getResources();
        String[] recipeIds = r.getStringArray(R.array.recipe_data);
        for (String recipeId : recipeIds) {
            int id = r.getIdentifier(recipeId, "array", getPackageName());
            String[] recipeInfo = r.getStringArray(id);
            String recipeName = recipeInfo[0];
            if (recipeName.toLowerCase().contains(searchString.toLowerCase())) {
                addRecipe(recipeInfo);
            }
        }
    }

    // Removes all recipes from the UI.
    private void clearRecipes() {
        for (LinearLayout layout : recipesInList) {
            layout.setVisibility(View.GONE);
        }

        recipesInList.clear();
    }

    // Adds a recipe to the Recipes UI.
    private void addRecipe(String[] recipeInfo) {
        LinearLayout recipeScrollLayout = findViewById(R.id.recipesScrollLayout);

        LinearLayout recipe = new LinearLayout(this);
        recipe.setOrientation(LinearLayout.VERTICAL);
        recipe.setGravity(Gravity.CENTER_HORIZONTAL);
        recipe.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
        recipe.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_item_width), LinearLayout.LayoutParams.MATCH_PARENT));
        recipe.setClickable(true);
        recipe.setTag(recipeInfo);

        recipe.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] recipeInfo = (String[]) view.getTag();
                currentRecipe = recipeInfo;
                createRecipeScreen(recipeInfo);
            }
        });

        ImageView image = new ImageView(this);
        int imageId = getResources().getIdentifier(recipeInfo[7], "drawable", getPackageName());
        image.setImageDrawable(getResources().getDrawable(imageId));
        image.setBackgroundColor(Color.parseColor("#ECECEC"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 3.3f);
        int margin = DTP(R.dimen.recipes_image_margin);
        params.setMargins(margin, margin, margin, margin);
        image.setLayoutParams(params);

        TextView recipeName = new TextView(this);
        recipeName.setText(recipeInfo[0]);
        recipeName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.6f));
        recipeName.setGravity(Gravity.CENTER);
        recipeName.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        recipeName.setTextSize(STP(R.dimen.normal_text_size));

        boolean hasAllFoods = true;
        boolean someFoodExpired = false;
        boolean hasEnoughFood = true;
        boolean hasEnoughFreshFood = true;
        String[] ingredientsList = recipeInfo[1].split(",");
        String[] ingredientsNumbers = recipeInfo[2].split(",");

        for (int i = 0; i < ingredientsList.length; i++) {
            String ingredientName = ingredientsList[i].substring(0, ingredientsList[i].lastIndexOf("(") - 1);
            boolean[] foodStatus = fridgeHasFood(ingredientName, ingredientsNumbers[i]);

            hasAllFoods = hasAllFoods && foodStatus[0];
            someFoodExpired = someFoodExpired || foodStatus[1];
            hasEnoughFood = hasEnoughFood && foodStatus[2];
            hasEnoughFreshFood = hasEnoughFreshFood && foodStatus[3];
        }

        TextView haveIngredients = new TextView(this);
        haveIngredients.setGravity(Gravity.CENTER);
        haveIngredients.setTextSize(STP(R.dimen.small_text_size));
        haveIngredients.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.3f));

        if (!hasAllFoods) {
            haveIngredients.setText(R.string.recipes_no_ingredients);
            haveIngredients.setTextColor(getResources().getColor(R.color.red));
        } else if (!hasEnoughFood) {
            haveIngredients.setText(R.string.recipes_not_enough);
            haveIngredients.setTextColor(getResources().getColor(R.color.red));
        } else if (someFoodExpired && !hasEnoughFreshFood) {
            haveIngredients.setText(R.string.recipes_expired_ingredients);
            haveIngredients.setTextColor(getResources().getColor(R.color.orange));
        } else {
            haveIngredients.setText(R.string.recipes_have_ingredients);
            haveIngredients.setTextColor(getResources().getColor(R.color.green));
        }

        recipe.addView(image);
        recipe.addView(recipeName);
        recipe.addView(haveIngredients);

        recipeScrollLayout.addView(recipe);
        recipesInList.add(recipe);

        final HorizontalScrollView scrollView = findViewById(R.id.recipesScrollView);
        final ImageView canScrollUp = findViewById(R.id.recipesCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.recipesCanScrollDown);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                canScrollDown.setAlpha(scrollView.canScrollHorizontally(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollHorizontally(-1) ? 1f : 0.25f);
            }
        });
    }

    // cooktime is 2
    private void createRecipeScreen(String[] recipeInfo) {
        setContentView(R.layout.recipes_individual);
        LinearLayout recipeScrollLayout = findViewById(R.id.recipesIndvScrollLayout);

        TextView recipeTitle = findViewById(R.id.recipesIndvTitle);
        recipeTitle.setText(recipeInfo[0]);

        TextView cookingTimeText = findViewById(R.id.recipesIndvCookTime);
        cookingTimeText.setText(recipeInfo[3]);

        TextView servesText = findViewById(R.id.recipesIndvServes);
        servesText.setText(recipeInfo[4]);

        TextView experienceText = findViewById(R.id.recipesIndvExperience);
        experienceText.setText(recipeInfo[5]);

        TextView ingredientsTitle = new TextView(this);
        ingredientsTitle.setText(R.string.recipes_ingredients_header);
        ingredientsTitle.setTextSize(STP(R.dimen.normal_text_size));
        ingredientsTitle.setTypeface(ingredientsTitle.getTypeface(), Typeface.BOLD);
        ingredientsTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        ingredientsTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        recipeScrollLayout.addView(ingredientsTitle);

        int index = -1; // Probably should incorporate this into the for loop properly, but this is easier
        String[] ingredientsArray = recipeInfo[1].split(",");
        String[] ingredientsAmounts = recipeInfo[2].split(",");
        for (String i : ingredientsArray) {
            index++;
            boolean[] fridgeInfo = fridgeHasFood(i.substring(0, i.indexOf("(")), ingredientsAmounts[index]);
            boolean inFridge = fridgeInfo[0];
            boolean isExpired = fridgeInfo[1];
            boolean isEnough = fridgeInfo[2];
            boolean isEnoughFresh = fridgeInfo[3];

            LinearLayout ingredientLayout = new LinearLayout(this);
            ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
            ingredientLayout.setGravity(Gravity.CENTER);
            ingredientLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.recipes_indv_item_height)));

            TextView bullet = new TextView(this);
            bullet.setText("• ");
            bullet.setTextSize(STP(R.dimen.normal_text_size));
            bullet.setGravity(Gravity.CENTER);
            bullet.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f));

            TextView ingredientName = new TextView(this);
            ingredientName.setText(i);
            ingredientName.setTextSize(STP(R.dimen.normal_text_size));
            ingredientName.setGravity(Gravity.START); // Left
            ingredientName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10));

            ingredientLayout.addView(bullet);
            ingredientLayout.addView(ingredientName);

            //if (!inFridge || isExpired || !isEnough) {
            if (!inFridge || !isEnoughFresh) {
                Button ingredientButton = new Button(this);
                ingredientButton.setText(R.string.recipes_order_button);
                ingredientButton.setTextSize(STP(R.dimen.small_text_size));
                ingredientButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4));
                ingredientButton.setEnabled(!inSafeMode);
                String tag = i.split(" [(]")[0]; // Name of the ingredient, in a string
                ingredientButton.setTag(tag);

                ingredientButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastOnlineOrderSearch = ((String) view.getTag()).toLowerCase();
                        toOrderOnline(view);
                    }
                });

                ingredientLayout.addView(ingredientButton);
            } else {
                Space buttonSpace = new Space(this);
                buttonSpace.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4));

                ImageView checkmark = new ImageView(this);
                checkmark.setImageDrawable(getResources().getDrawable(R.drawable.check));
                checkmark.setScaleType(ImageView.ScaleType.FIT_CENTER);
                checkmark.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 4));

                ingredientLayout.addView(checkmark);
            }

            recipeScrollLayout.addView(ingredientLayout);

            //if (!inFridge || isExpired || !isEnough) {
            if (!inFridge || !isEnoughFresh) {
                LinearLayout alertLayout = new LinearLayout(this);
                alertLayout.setOrientation(LinearLayout.HORIZONTAL);
                alertLayout.setGravity(Gravity.CENTER);
                alertLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                ImageView alertIcon = new ImageView(this);
                if (!inFridge) {
                    alertIcon.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                } else if (!isEnough) {
                    alertIcon.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_offline));
                } else {
                    alertIcon.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
                }
                alertIcon.setScaleType(ImageView.ScaleType.FIT_END);
                alertIcon.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f));

                Space alertSpace = new Space(this);
                alertSpace.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f));

                TextView alertText = new TextView(this);

                if (!inFridge) {
                    alertText.setText(R.string.recipes_no_ingredients_line);
                    alertText.setTextColor(getResources().getColor(R.color.red));
                } else if (!isEnough) {
                    alertText.setText(R.string.recipes_not_enough_line);
                    alertText.setTextColor(Color.GRAY);
                } else {
                    alertText.setText(R.string.recipes_expired_ingredients_line);
                    alertText.setTextColor(getResources().getColor(R.color.orange));
                }

                alertText.setTextSize(STP(R.dimen.small_text_size));
                alertText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10));

                alertLayout.addView(alertIcon);
                alertLayout.addView(alertSpace);
                alertLayout.addView(alertText);
                recipeScrollLayout.addView(alertLayout);
            }
        }

        Space space3 = new Space(this);
        space3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.small_vertical_space)));

        TextView instructionsTitle = new TextView(this);
        instructionsTitle.setText(R.string.recipes_instructions_header);
        instructionsTitle.setTextSize(STP(R.dimen.normal_text_size));
        instructionsTitle.setTypeface(ingredientsTitle.getTypeface(), Typeface.BOLD);
        instructionsTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        instructionsTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView instructionsText = new TextView(this);
        instructionsText.setText(recipeInfo[6].replace("\n", "\n\n"));
        instructionsText.setTextSize(STP(R.dimen.normal_text_size));
        instructionsText.setGravity(Gravity.CENTER_HORIZONTAL);
        instructionsText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        recipeScrollLayout.addView(space3);
        recipeScrollLayout.addView(instructionsTitle);
        recipeScrollLayout.addView(instructionsText);

        final ScrollView scrollView = findViewById(R.id.recipesIndvScrollView);
        final ImageView canScrollUp = findViewById(R.id.recipesIndvCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.recipesIndvCanScrollDown);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });
    }

    private boolean[] fridgeHasFood(String name, String number) {
        boolean hasFood = false;
        int freshIngredients = 0;
        int expiredIngredients = 0;
        int numNeeded = Integer.parseInt(number);

        for (String key : itemsInFridge.keySet()) {
            boolean thisIsFood = key.toLowerCase().contains(name.toLowerCase());

            hasFood = hasFood || thisIsFood;

            if (thisIsFood) {
                String num = key.substring(key.lastIndexOf("(") + 1, key.lastIndexOf(")"));
                System.out.println("Key is: " + key);
                System.out.println("There are " + num + " " + name + "s.");
                int numInFridge = Integer.parseInt(num);

                String expDateStr = itemsInFridge.get(key).replace("exp: ", "");
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

                try {
                    Date expDate = formatter.parse(expDateStr);
                    String currentDateStr = formatter.format(new Date());
                    Date currentDate = formatter.parse(currentDateStr);
                    if (currentDate.after(expDate)) {
                        //thisIsExpired = true;
                        expiredIngredients += numInFridge;
                    } else {
                        freshIngredients += numInFridge;
                    }
                } catch (ParseException e) {
                    System.out.println("Could not parse date");
                }

            }
        }

        return new boolean[] {hasFood, expiredIngredients != 0, freshIngredients + expiredIngredients >= numNeeded, freshIngredients >= numNeeded};
    };

    // Converts from dp to pixels.
    private int DTP(int dimensionId) {
        Resources r = getResources();
        float dpValue = r.getDimension(dimensionId) / r.getDisplayMetrics().density;
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics()));
    }

    // Converts from dp to pixels. Takes in a value directly, rather than a dimension.
    private int DTPDirect(int value) {
        float dpValue = value / getResources().getDisplayMetrics().density;
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics()));
    }

    // Converts from sp to pixels.
    private int STP(int dimensionId) {
        Resources r = getResources();
        float spValue = r.getDimension(dimensionId) / r.getDisplayMetrics().scaledDensity;
        return Math.round(spValue);
    }

    // Adjusts the temperature on the home screen.
    private void setHomeScreenTemperature(int temperature) {
        TextView uiTemperature = findViewById(R.id.temperature);

        if (isCelcius) {
            uiTemperature.setText(getString(R.string.temperature_cel, toCelcius(temperature)));
        } else {
            uiTemperature.setText(getString(R.string.temperature_far, temperature));
        }
    }


    // Grocery List Methods
    private void alertDialogDuplicate(String item) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setMessage("The item " + item +" is already in your grocery shopping list!");

        TextView myView = new TextView(this);
        myView.setText("Duplicate item:");
        myView.setTextSize(STP(R.dimen.normal_text_size));
        myView.setGravity(Gravity.CENTER);
        dialog.setCustomTitle(myView);

        dialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Pop-up closed",Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog=dialog.create();
        alertDialog.show();


        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(STP(R.dimen.normal_text_size));
        textView.setGravity(Gravity.CENTER);
    }

    private void alertDialogFirst() {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setMessage("The grocery items you add can be removed when the check box to the left of them are selected.");

        TextView myView = new TextView(this);
        myView.setText("How To Remove A Grocery List Item");
        myView.setTextSize(STP(R.dimen.normal_text_size));
        myView.setGravity(Gravity.CENTER);
        dialog.setCustomTitle(myView);

        dialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Pop-up closed",Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog=dialog.create();
        alertDialog.show();


        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(STP(R.dimen.normal_text_size));
        textView.setGravity(Gravity.CENTER);
    }

    public void addToGroceryList(View view){

        //Check for empty string

        final LinearLayout linearLay = (LinearLayout) findViewById(R.id.groceryListScrollLayout);

        int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");


        EditText userItem = findViewById(R.id.new_item_input);
        EditText userItemQuantity = findViewById(R.id.new_item_quantity);
        String userItemString =  userItem.getText().toString();
        String userItemQuantityString =  userItemQuantity.getText().toString();

        userItem.getText().clear();
        userItemQuantity.getText().clear();

        if (userItemQuantityString.equals("")){
            userItemQuantityString = "1";
        }

        if(groceryItemNames.contains(userItemString.toUpperCase())){
            alertDialogDuplicate(userItemString);
        }

        else if (!userItemString.equals("") && Integer.parseInt(userItemQuantityString)>=1) {

            LinearLayout col = new LinearLayout(getApplicationContext());
            LinearLayout.LayoutParams layoutParamsCol= new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            col.setLayoutParams(layoutParamsCol);
            col.setOrientation(LinearLayout.HORIZONTAL);
            col.setGravity(Gravity.FILL);

            linearLay.addView(col);

            LinearLayout.LayoutParams layoutParamsQuantity = new  LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, .3f);


            final TextView quant = new TextView(getApplicationContext());
            quant.setTextSize(STP(R.dimen.normal_text_size));
            quant.setTextColor(Color.BLACK);
            quant.setText(" (" + userItemQuantityString + ")");
            quant.setLayoutParams(layoutParamsQuantity);


            LinearLayout.LayoutParams layoutParamsCheckBox = new  LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.1f);


            CheckBox newCheckBox = new CheckBox(getApplicationContext());
            newCheckBox.setText(userItemString);
            newCheckBox.setTextColor(Color.BLACK);
            newCheckBox.setButtonDrawable(id);
            newCheckBox.setTextSize(STP(R.dimen.normal_text_size));
            newCheckBox.setLayoutParams(layoutParamsCheckBox);


            Button incrementButton = new Button(this);
            incrementButton.setText("+1");
            incrementButton.setGravity(Gravity.CENTER);
            //incrementButton.setBackgroundColor(0xFF008000);
            incrementButton.setBackgroundColor(0xFFA4C639);


            incrementButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Integer curr = Integer.parseInt(quant.getText().subSequence(2,quant.length()-1).toString());
                    curr++;
                    quant.setText(" (" + curr.toString() + ")");

                }
            });



            Button decrementButton = new Button(this);
            decrementButton.setText("-1");
            decrementButton.setGravity(Gravity.CENTER);
            //decrementButton.setBackgroundColor(0xFFFF4040);
            decrementButton.setBackgroundColor(0xFFDB5A6B);


            decrementButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Integer curr = Integer.parseInt(quant.getText().subSequence(2,quant.length()-1).toString());
                    if(!curr.equals(1)) {
                        curr--;
                    }
                    quant.setText(" (" + curr.toString() + ")");

                }
            });

            LinearLayout.LayoutParams layoutParamsButton = new  LinearLayout.LayoutParams(0, 100, .40f);

            layoutParamsButton.setMargins(10, 10, 0, 0);

            incrementButton.setLayoutParams(layoutParamsButton);
            decrementButton.setLayoutParams(layoutParamsButton);

            col.addView(newCheckBox);
            col.addView(quant);
            col.addView(incrementButton);
            col.addView(decrementButton);

            groceriesInList.add(col);
            groceryItemNames.add(userItemString.toUpperCase());

            if (!groceriesAdded){
                alertDialogFirst();
            }
            groceriesAdded = true;
            }

    }


    public void loadGroceryList(View view){

        ArrayList <LinearLayout> viewsToDelete = new ArrayList<>();

        for (LinearLayout x: groceriesInList){

            CheckBox checkBox = (CheckBox) x.getChildAt(0);
            TextView quantity = (TextView) x.getChildAt(1);
            Integer curr = Integer.parseInt(quantity.getText().subSequence(2,quantity.length()-1).toString());

            if(checkBox.isChecked() || curr.equals(0)){
                viewsToDelete.add(x);
                groceryItemNames.remove(checkBox.getText().toString().toUpperCase());
            }
        }

        for (View x: viewsToDelete){
            groceriesInList.remove(x);

        }

        LinearLayout linearLay = (LinearLayout) findViewById(R.id.groceryListScrollLayout);

        for (View x: groceriesInList){
            ((ViewGroup)x.getParent()).removeView(x);
            linearLay.addView(x);
        }

    }




// Look Inside METHODS //

    public void showInsideFridge(View view) {

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        ImageView lookInsideImage = (ImageView) findViewById(R.id.lookInsideImage);

        switch(radioButtonID) {
            case R.id.topShelfRadio:
                lookInsideImage.setImageResource(R.drawable.top_fridge_image);
                break;

            case R.id.middleShelfRadio:
                lookInsideImage.setImageResource(R.drawable.middle_fridge_image);
                break;

            case R.id.bottomSelfRadio:
                lookInsideImage.setImageResource(R.drawable.bottom_fridge_image);
                break;

            case R.id.drawer_one:
                lookInsideImage.setImageResource(R.drawable.drawer1_fridge_image);
                break;

            case R.id.drawer_two:
                lookInsideImage.setImageResource(R.drawable.drawer2_fridge_image);
                break;
        }


    }

    private void clearOrderableItems() {
        for (LinearLayout layout : orderableItemsInList) {
            layout.setVisibility(View.GONE);
        }

        orderableItemsInList.clear();
    }

    // Order Online Methods //
    private void populateOrderableItems(String searchString) {
        LinearLayout scrollLayout = findViewById(R.id.orderScrollLayout);
        Resources r = getResources();
        String[] foods = r.getStringArray(R.array.food_list);

        // Weights for the image, item name, item price, and add button (respectively) in each layout
        float[] phoneWeights = {1.4f, 3.4f, 2.6f, 1.2f};
        float[] tabletWeights = {1.4f, 3.8f, 3f, 1.2f};
        float[] weights = isTablet ? tabletWeights : phoneWeights;

        for (String foodId : foods) {
            int id = r.getIdentifier(foodId, "array", getPackageName());
            String[] foodInfo = r.getStringArray(id);

            if (foodInfo[0].toLowerCase().contains(searchString.toLowerCase())) {
                LinearLayout orderableItem = new LinearLayout(this);
                orderableItem.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
                orderableItem.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.order_item_height)));
                orderableItem.setOrientation(LinearLayout.HORIZONTAL);
                orderableItem.setGravity(Gravity.CENTER_VERTICAL);

                ImageView itemPicture = new ImageView(this);
                String imageIdString = foodInfo[0].toLowerCase().replaceAll(" ", "_") + "_image";
                int imageId = r.getIdentifier(imageIdString, "drawable", getPackageName());
                itemPicture.setImageDrawable(r.getDrawable(imageId));
                itemPicture.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_image_height), weights[0]));

                TextView itemName = new TextView(this);
                itemName.setText(foodInfo[0]);
                itemName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[1]));
                itemName.setGravity(Gravity.CENTER);
                itemName.setTextSize(STP(R.dimen.normal_text_size));

                orderableItem.addView(itemPicture);
                orderableItem.addView(itemName);

                if (isTablet) {
                    // For whether the item is in the fridge, is expired, or isn't in, resp.
                    float[] fridgeIconWeights = {1f, 1.1f, 0.7f};
                    float [] fridgeTextWeights = {2.2f, 2.1f, 2.5f};

                    // Info about the food item
                    boolean[] fridgeInfo = fridgeHasFood(foodInfo[0], null);
                    boolean inFridge = fridgeInfo[0];
                    boolean isExpired = fridgeInfo[1];

                    ImageView inFridgeIcon = new ImageView(this);
                    inFridgeIcon.setScaleType(ImageView.ScaleType.FIT_END);

                    TextView inFridgeText = new TextView(this);
                    inFridgeText.setTextSize(STP(R.dimen.normal_text_size));

                    if (inFridge && !isExpired) {
                        inFridgeIcon.setImageDrawable(r.getDrawable(android.R.drawable.presence_online));
                        inFridgeIcon.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_fridge_icon_height), fridgeIconWeights[0]));

                        inFridgeText.setText(getString(R.string.order_in_fridge_text, " "));
                        inFridgeText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, fridgeTextWeights[0]));
                    } else if (inFridge) {
                        inFridgeIcon.setImageDrawable(r.getDrawable(android.R.drawable.presence_away));
                        inFridgeIcon.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_fridge_icon_height), fridgeIconWeights[1]));

                        inFridgeText.setText(getString(R.string.order_expired_text, " "));
                        inFridgeText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, fridgeTextWeights[1]));
                    } else {
                        inFridgeIcon.setImageDrawable(r.getDrawable(android.R.drawable.presence_busy));
                        inFridgeIcon.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_fridge_icon_height), fridgeIconWeights[2]));

                        inFridgeText.setText(getString(R.string.order_not_in_fridge_text, " "));
                        inFridgeText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, fridgeTextWeights[2]));
                    }

                    orderableItem.addView(inFridgeIcon);
                    orderableItem.addView(inFridgeText);
                }

                TextView itemPrice = new TextView(this);
                if (isTablet) {
                    String priceString = getString(R.string.order_price_t, foodInfo[1], foodInfo[2]);
                    priceString = priceString.replace("[", "<b>").replace("]", "</b>").replace("\n", "<br>");
                    itemPrice.setText(Html.fromHtml(priceString));
                } else {
                    itemPrice.setText(getString(R.string.order_price, foodInfo[1]));
                }
                itemPrice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[2]));
                itemPrice.setGravity(Gravity.CENTER);
                itemPrice.setTextSize(STP(R.dimen.normal_text_size));

                ImageButton itemAdd = new ImageButton(this);
                itemAdd.setScaleType(ImageView.ScaleType.FIT_CENTER);
                itemAdd.setImageDrawable(r.getDrawable(R.drawable.cart));
                itemAdd.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_button_height), weights[3]));
                itemAdd.setTag(foodInfo);

                itemAdd.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createDialog(view, (String[]) view.getTag());
                    }
                });

                orderableItem.addView(itemPrice);
                orderableItem.addView(itemAdd);

                Space test = new Space(this);
                test.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f));
                orderableItem.addView(test);

                scrollLayout.addView(orderableItem);
                orderableItemsInList.add(orderableItem);
            }
        }

        final ScrollView scrollView = findViewById(R.id.orderScrollView);
        final ImageView canScrollUp = findViewById(R.id.orderCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.orderCanScrollDown);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });
    }

    public void createDialog(View view, String[] foodInfo) {
        final TextView blur = findViewById(R.id.orderBlur);
        blur.setBackgroundColor(Color.parseColor("#BF424242"));

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            View dialogView = inflater.inflate(R.layout.order_online_dialog, null);
            int width = DTP(R.dimen.add_dialog_width);
            int height = DTP(R.dimen.add_dialog_height);
            final PopupWindow dialog = new PopupWindow(dialogView, width, height, true);
            dialog.showAtLocation(view, Gravity.CENTER, 0, 0);

            if (isTablet) {
                // Increase offset between edges of dialog and content
                ConstraintLayout dialogLayout = dialogView.findViewById(R.id.orderDialogLayout);
                ConstraintSet test = new ConstraintSet();
                test.clone(dialogLayout);
                test.setMargin(R.id.orderDialogButtonsLayout, ConstraintSet.BOTTOM, DTPDirect(96));
                test.setMargin(R.id.orderDialogTitle, ConstraintSet.TOP, DTPDirect(96));
                test.applyTo(dialogLayout);
            }

            TextView dialogTitle = dialogView.findViewById(R.id.orderDialogTitle);
            dialogTitle.setText(getString(R.string.dialog_title, foodInfo[0]));

            TextView dialogUnit = dialogView.findViewById(R.id.orderDialogUnit);
            String unit = foodInfo[2].replace("one ", "");
            dialogUnit.setText(getString(R.string.order_unit, foodInfo[1], unit));

            ImageButton decButton = dialogView.findViewById(R.id.orderDialogDecButton);
            decButton.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText input = dialog.getContentView().findViewById(R.id.orderDialogNumberField);
                    int inputNumber = Integer.parseInt(input.getText().toString());
                    if (inputNumber > 0) {
                        // It complains if I don't add the locale, so...yeah.
                        input.setText(String.format(Locale.ENGLISH, "%d", --inputNumber));
                    }
                }
            });

            ImageButton incButton = dialogView.findViewById(R.id.orderDialogIncButton);
            incButton.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText input = dialog.getContentView().findViewById(R.id.orderDialogNumberField);
                    int inputNumber = Integer.parseInt(input.getText().toString());
                    // It complains if I don't add the locale, so...yeah.
                    input.setText(String.format(Locale.ENGLISH, "%d", ++inputNumber));
                }
            });

            Button cancelButton = dialogView.findViewById(R.id.orderDialogCancelButton);
            cancelButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    blur.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
            });

            Button addButton = dialogView.findViewById(R.id.orderDialogOkButton);
            String foodInfoStr = foodInfo[0] + "," + foodInfo[1] + "," + foodInfo[3]; // Name, price, num per unit, resp.
            addButton.setTag(foodInfoStr);
            addButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String foodString = (String) view.getTag();
                    EditText input = dialog.getContentView().findViewById(R.id.orderDialogNumberField);
                    int inputNumber = Integer.parseInt(input.getText().toString());
                    int numberInCart = 0; // How many of this item are already in the cart
                    int indexInCart = -1; // Position of this item that's already in the cart list

                    // Scan the cart to see if this item is already in it.
                    for (String item : itemsInCart) {
                        if (item.contains(foodString)) {
                            indexInCart = itemsInCart.indexOf(item);

                            String numInCartStr = item.split(",")[2];
                            numberInCart = Integer.parseInt(numInCartStr);
                        }
                    }

                    if (indexInCart != -1) {
                        // Some items already in the cart, so add them together
                        itemsInCart.set(indexInCart, foodString + "," + (inputNumber + numberInCart));
                    } else {
                        // Item is not in the cart yet, so add it
                        itemsInCart.add(foodString + "," + inputNumber);
                    }

                    dialog.dismiss();
                    blur.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
            });
        }
    }

    private void populateCart() {
        LinearLayout cartScrollLayout = findViewById(R.id.cartScrollLayout);
        float total = 0;
        Collections.sort(itemsInCart);

        // Gives the items in each listing different weights depending on layout
        float[] phoneWeights = {4.5f, 1, 2, 1};
        float[] tabletWeights = {4.2f, 0.7f, 2, 0.7f};
        float[] weights = isTablet ? tabletWeights : phoneWeights;

        // This is only used for the tablet
        ImageButton minusButton = null, plusButton = null;

        final TextView totalText = findViewById(R.id.cartTotalAmount);

        for (String item : itemsInCart) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.cart_item_height)));

            String[] itemInfo = item.split(",");

            TextView itemName = new TextView(this);
            itemName.setText(itemInfo[0]);
            itemName.setTextSize(STP(R.dimen.normal_text_size));
            itemName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[0]));
            itemLayout.addView(itemName);

            TextView itemNum = new TextView(this);
            itemNum.setText(itemInfo[3]);
            itemNum.setTextSize(STP(R.dimen.normal_text_size));
            itemNum.setTag(item);
            itemNum.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[1]));

            if (isTablet) {
                minusButton = new ImageButton(this);
                minusButton.setImageDrawable(getResources().getDrawable(R.drawable.minus_button));
                minusButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                minusButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                minusButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));
                minusButton.setTag(itemNum);

                minusButton.setEnabled(Integer.parseInt(itemInfo[3]) != 1);

                itemLayout.addView(minusButton);
            }

            // Had to add the minus button before we could do this
            itemLayout.addView(itemNum);

            if (isTablet) {
                itemNum.setGravity(Gravity.CENTER);

                plusButton = new ImageButton(this);
                plusButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                plusButton.setImageDrawable(getResources().getDrawable(R.drawable.plus_button));
                plusButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                plusButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));
                plusButton.setTag(minusButton);

                TextView priceEachText = new TextView(this);
                priceEachText.setText(getString(R.string.order_price, itemInfo[1]));
                priceEachText.setTextSize(STP(R.dimen.normal_text_size));
                priceEachText.setGravity(Gravity.END);
                priceEachText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

                itemLayout.addView(plusButton);
                itemLayout.addView(priceEachText);
            }

            final TextView itemPrice = new TextView(this);
            float price = Float.parseFloat(itemInfo[1]) * Integer.parseInt(itemInfo[3]);
            total += price;
            itemPrice.setText(getResources().getString(R.string.order_price, getPriceString(price)));
            itemPrice.setTextSize(STP(R.dimen.normal_text_size));
            itemPrice.setGravity(Gravity.END); // right
            itemPrice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[2]));

            Space space = new Space(this);
            space.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0.5f));

            final ImageButton removeButton = new ImageButton(this);
            removeButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            removeButton.setImageDrawable(getResources().getDrawable(R.drawable.remove_button));
            removeButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            removeButton.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.cart_subitem_height), weights[3]));
            removeButton.setTag(item);

            if (plusButton != null && minusButton != null) {
                minusButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView itemNum = (TextView) view.getTag();
                        int num = Integer.parseInt(itemNum.getText().toString());

                        itemNum.setText(getString(R.string.number, --num));

                        String cartElement = (String) itemNum.getTag();
                        int index = itemsInCart.indexOf(cartElement);
                        String[] cartElements = cartElement.split(",");
                        String newCartElement = cartElements[0] + "," + cartElements[1] + "," + cartElements[2] + "," + num;
                        itemsInCart.set(index, newCartElement);

                        itemNum.setTag(newCartElement);
                        float individualPrice = Float.parseFloat(cartElements[1]);
                        float newPrice =  individualPrice * num;
                        itemPrice.setText(getString(R.string.order_price, getPriceString(newPrice)));
                        removeButton.setTag(newCartElement);

                        float total = Float.parseFloat(totalText.getText().toString().replace("$", ""));
                        totalText.setText(getString(R.string.order_price, getPriceString(total - individualPrice)));

                        if (num == 1) {
                            // If there's only one item left on our checkout list, don't let us remove any more this way
                            view.setEnabled(false);
                        }
                    }
                });

                plusButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImageButton minusButton = (ImageButton) view.getTag();
                        TextView itemNum = (TextView) minusButton.getTag();
                        int num = Integer.parseInt(itemNum.getText().toString());

                        itemNum.setText(getString(R.string.number, ++num));

                        String cartElement = (String) itemNum.getTag();
                        int index = itemsInCart.indexOf(cartElement);
                        String[] cartElements = cartElement.split(",");
                        String newCartElement = cartElements[0] + "," + cartElements[1] + "," + cartElements[2] + "," + num;
                        itemsInCart.set(index, newCartElement);

                        itemNum.setTag(newCartElement);
                        float individualPrice = Float.parseFloat(cartElements[1]);
                        float newPrice = individualPrice * num;
                        itemPrice.setText(getString(R.string.order_price, getPriceString(newPrice)));
                        removeButton.setTag(newCartElement);

                        float total = Float.parseFloat(totalText.getText().toString().replace("$", ""));
                        totalText.setText(getString(R.string.order_price, getPriceString(total + individualPrice)));

                        if (num > 1) {
                            // If we increased from one item, we can subtract now
                            minusButton.setEnabled(true);
                        }
                    }
                });
            }

            removeButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemsInCart.remove((String) view.getTag());

                    for (LinearLayout layout : orderableItemsInList) {
                        layout.setVisibility(View.GONE);
                    }

                    orderableItemsInList.clear();
                    populateCart();
                }
            });

            itemLayout.addView(itemPrice);
            itemLayout.addView(space);
            itemLayout.addView(removeButton);

            cartScrollLayout.addView(itemLayout);
            orderableItemsInList.add(itemLayout);
        }

        totalText.setText(getString(R.string.order_price, getPriceString(total)));
        cartTotal = total;

        Button checkoutButton = findViewById(R.id.cartCheckoutButton);
        checkoutButton.setEnabled(!itemsInCart.isEmpty());

        findViewById(R.id.cartEmptyLabel).setVisibility(itemsInCart.isEmpty() ? View.VISIBLE : View.INVISIBLE);

        final ScrollView scrollView = findViewById(R.id.cartScrollView);
        final ImageView canScrollUp = findViewById(R.id.cartCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.cartCanScrollDown);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });
    }

    private String getPriceString(float price) {
        if (price == 0) {
            return "0.00";
        } else {
            int temp = Math.round(price * 100f);
            boolean evenDime = temp % 10 == 0;
            price = temp / 100f;

            return price + (evenDime ? "0" : "");
        }
    }

}
