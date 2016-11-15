package com.macys.sdt.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.gson.Gson;
import com.macys.sdt.framework.model.*;
import com.macys.sdt.framework.utils.rest.services.UserProfileService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.macys.sdt.framework.utils.StepUtils.macys;
import static com.macys.sdt.framework.utils.StepUtils.prodEnv;
import static com.macys.sdt.framework.utils.Utils.getResourceFile;

/**
 * This class creates and manages test user information
 */
public class TestUsers {

    /**
     * Current login email
     */
    public static String currentEmail = null;

    /**
     * Current login password
     */
    public static String currentPassword = null;

    /**
     * Current locked prod customer
     */
    public static String lockedProductionCustomer = null;
    private static UserProfile customer = null;
    private static UserProfile prodCustomer = null;
    private static LoyalistDetails loyaltyDetailCustomer = null;
    private static UslInfo uslInfo = null;
    private static HashMap<String, String> paypalInfo = null;
    private static User user = null;

    /**
     * Checks whether you're on macy's or bloomingdales website
     *
     * @return "mcom" if on macys, "bcom" if on bloomingdales
     */
    public static String getSiteType() {
        return macys() ? "mcom" : "bcom";
    }

    /**
     * Release lock on current production customer
     */
    public static void releaseProductionCustomer() {
        if (lockedProductionCustomer == null) {
            return;
        }
        try {
            Utils.httpGet(Utils.getEEUrl() + "/sdt/releaseProductionCustomer/" + lockedProductionCustomer, null);
        } catch (Exception e) {
            System.err.println("Cannot release production customer:" + lockedProductionCustomer);
        }
    }

    /**
     * Gets a production customer from EE
     *
     * @return UserProfile with customer data
     * @throws Exceptions.UserException thrown if prod customer retrieval failed
     */
    public static UserProfile getProdCustomer() throws Exceptions.UserException {
        String url = Utils.getEEUrl() + "/sdt/lockProductionCustomer/" + getSiteType();
        try {
            if (prodCustomer == null) {
                User user1 = new User(null, null, null, new UserPasswordHint(), new ProfileAddress(), new LoginCredentials());
                prodCustomer = new UserProfile(user1, new Registry());
            }

            String lockCustomer = Utils.httpGet(url, null);
            Map<String, Map<String, String>> customer = new Gson().fromJson(lockCustomer, Map.class);
            prodCustomer.getUser().getLoginCredentials().setPassword(customer.get("login").get("password"));
            prodCustomer.getUser().getProfileAddress().setEmail(customer.get("login").get("email"));
            currentEmail = prodCustomer.getUser().getProfileAddress().getEmail();
            lockedProductionCustomer = getSiteType() + "_" + currentEmail;
            currentPassword = prodCustomer.getUser().getLoginCredentials().getPassword();
            prodCustomer.getRegistry().setCoRegistrantFirstName(generateRandomFirstName());
            prodCustomer.getRegistry().setCoRegistrantLastName(generateRandomLastName());
            prodCustomer.getUser().getProfileAddress().setBestPhone(generateRandomPhoneNumber());

            if (customer.get("registry") != null) {
                Map<String, String> registryMap = customer.get("registry");
                Registry reg = prodCustomer.getRegistry();

                reg.setEventType(registryMap.get("event_type"));
                reg.setEventMonth(registryMap.get("event_month"));
                reg.setEventDay(registryMap.get("event_day"));
                reg.setEventYear(registryMap.get("event_year"));
                reg.setEventLocation(registryMap.get("event_location"));
                reg.setNumberOfGuest(registryMap.get("number_of_guest"));
                reg.setPreferredStoreState(registryMap.get("preferred_store_state"));
                reg.setPreferredStore(registryMap.get("preferred_store"));
            }

            return prodCustomer;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exceptions.UserException("Cannot lock a production customer: " + url);
        }
    }

    /**
     * Generates a new customer with random data
     *
     * @param country Country the profile should have (US if null)
     * @return UserProfile with customer data
     */
    public static UserProfile getCustomer(String country) {
        if (customer == null) {
            if (country == null) {
                country = "United States";
            }
            HashMap<String, String> opts = new HashMap<>();
            opts.put("country", country);
            customer = new UserProfile();
            user = new User();
            ProfileAddress profileAddress = getRandomValidAddress(opts);
            UserPasswordHint userPasswordHint = new UserPasswordHint();
            LoginCredentials loginCredentials = new LoginCredentials();
            try {
                userPasswordHint.setId(1L);
                userPasswordHint.setAnswer(generateRandomSecurityAnswer());
                userPasswordHint.setQuestion("What was the first concert you attended?");

                loginCredentials.setPassword(getPassword());

                user.setGender(generateRandomGender());
                user.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").format(TestUsers.generateRandomDate()));

                user.setLoginCredentials(loginCredentials);
                user.setProfileAddress(profileAddress);
                user.setUserPasswordHint(userPasswordHint);
                customer.setUser(user);
            } catch (Exception e) {
                Assert.fail("Unable to parse JSON: " + e);
                return null;
            }
        }
        currentEmail = customer.getUser().getProfileAddress().getEmail();
        currentPassword = customer.getUser().getLoginCredentials().getPassword();
        System.out.println("Your New Email Address is: " + currentEmail);
        return customer;

    }

    /**
     * Generates a new customer with random data and creates the profile through REST api
     *
     * @param country Country the profile should have (US if null)
     * @return UserProfile with customer data
     * @throws Exceptions.ProductionException when used on production environment
     */
    public static UserProfile getRESTUser(String country) throws Exceptions.ProductionException {
        if (prodEnv()) {
            throw new Exceptions.ProductionException("Cannot access REST APIs on production");
        }

        UserProfile profile = getCustomer(country);
        if (!UserProfileService.createUserProfile(profile)) {
            Assert.fail("Unable to create user profile through REST");
        }
        return profile;
    }

    private static String getPassword() {
        return "Macys12345";
    }

    /**
     * Gets a new customer with default registry data
     *
     * @return UserProfile object with customer data
     * @throws Exceptions.UserException thrown if registry user creation fails
     */
    public static UserProfile getNewRegistryUser() throws Exceptions.UserException {
        if (StepUtils.prodEnv()) {
            return getProdCustomer();
        }
        UserProfile userProfile = getCustomer(null);

        Registry registry = new Registry();
        registry.setCoRegistrantFirstName(generateRandomFirstName());
        registry.setCoRegistrantLastName(generateRandomLastName());
        registry.setEventType("WEDDING");
        registry.setEventMonth("April");
        registry.setEventDay("18");
        registry.setEventYear("2016");
        registry.setEventLocation("Alaska");
        registry.setNumberOfGuest("110");
        registry.setPreferredStoreState("New York");
        registry.setPreferredStore("New York - Herald Square");

        if (userProfile != null) {
            userProfile.setRegistry(registry);
        }
        currentEmail = userProfile != null ? userProfile.getUser().getProfileAddress().getEmail() : currentEmail;
        currentPassword = userProfile != null ? userProfile.getUser().getLoginCredentials().getPassword() : currentPassword;
        return userProfile;
    }

    /**
     * Adds registry data to existing customer
     *
     * @return UserProfile with customer data
     */
    public static UserProfile getExistingRegistryUser() {
        Registry registry = new Registry();
        registry.setCoRegistrantFirstName(generateRandomFirstName());
        registry.setCoRegistrantLastName(generateRandomLastName());
        registry.setEventType("WEDDING");
        registry.setEventMonth("April");
        registry.setEventDay("18");
        registry.setEventYear("2016");
        registry.setEventLocation("Alaska");
        registry.setNumberOfGuest("110");
        registry.setPreferredStoreState("New York");
        registry.setPreferredStore("New York - Herald Square");

        if (customer != null) {
            customer.setRegistry(registry);
        }
        return customer;
    }

    /**
     * Creates a new customer with random data and valid USL info
     *
     * @param country         Country the profile should have (US if null)
     * @param profileCreation "Profile_Creation" or "checkout" based on what you need
     * @return UserProfile with customer data
     */
    public static UserProfile getuslCustomer(String country, String profileCreation) {
        if (country == null) {
            country = "United States";
        }
        HashMap<String, String> opts = new HashMap<>();
        opts.put("country", country);
        if (customer == null) {
            customer = new UserProfile();
            user = new User();
            ProfileAddress profileAddress = getRandomValidAddress(opts);
            UserPasswordHint userPasswordHint = new UserPasswordHint();
            LoginCredentials loginCredentials = new LoginCredentials();
            try {
                userPasswordHint.setId(1L);
                userPasswordHint.setAnswer(generateRandomSecurityAnswer());
                userPasswordHint.setQuestion("What was the first concert you attended?");

                loginCredentials.setPassword(getPassword());

                user.setGender(generateRandomGender());
                user.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").format(TestUsers.generateRandomDate()));

                user.setLoginCredentials(loginCredentials);
                user.setProfileAddress(profileAddress);
                user.setUserPasswordHint(userPasswordHint);
                customer.setUser(user);
            } catch (Exception e) {
                Assert.fail("Unable to parse JSON: " + e);
                return null;
            }
        }
        currentEmail = customer.getUser().getProfileAddress().getEmail();
        currentPassword = customer.getUser().getLoginCredentials().getPassword();
        return customer;
    }

    /**
     * Gets value from a current customer
     *
     * @return current customer
     */
    public static UserProfile getCustomerInformation() {
        return customer;
    }

    /**
     * Creates a new customer with random data and valid loyalist info
     *
     * @param loyallistType Type of loyallist. Currently only "toptier_loyallist" is available
     * @return UserProfile with customer data
     */
    public static LoyalistDetails getLoyallistInformation(String loyallistType) {
        LoyalistDetails loyallistData = getLoyallistDetails(loyallistType);
        if (loyaltyDetailCustomer == null && loyallistData != null) {
            loyaltyDetailCustomer = new LoyalistDetails();
            try {
                loyaltyDetailCustomer.setLoyaltyId(loyallistData.getLoyaltyId());
                loyaltyDetailCustomer.setLastName(loyallistData.getLastName());
                loyaltyDetailCustomer.setZipCode(loyallistData.getZipCode());

            } catch (Exception e) {
                Assert.fail("Unable to parse JSON: " + e);
                return null;
            }
        }
        return loyaltyDetailCustomer;
    }

    /**
     * Deletes the active customer
     */
    public static void clearCustomer() {
        customer = null;
    }

    /**
     * Gets an enrolled USL ID
     * <p>
     * Will contain values for the USL id,
     * phone number, and pin respectively
     * </p>
     *
     * @return usl information
     */
    public static UslInfo getUSLInformation() {
        if (uslInfo == null) {
            try {
                uslInfo = getEnrolledUslId();
            } catch (Exception e) {
                Assert.fail("Unable to get USL information" + e.getMessage());
                uslInfo = null;
            }
        }
        return uslInfo;
    }

    /**
     * Gets valid paypal login info for sandbox site (NOT for production)
     * <p>
     * Contains "email" and "password" values.
     * </p>
     *
     * @return HashMap with paypal info
     */
    public static HashMap<String, String> getPayPalInformation() {
        File paypalFile;

        if (paypalInfo == null) {
            paypalInfo = new HashMap<>();
            paypalFile = getResourceFile("paypal_info.json");
            try {
                InputStream is = new FileInputStream(paypalFile);
                String jsonTxt = IOUtils.toString(is);
                JSONObject json = new JSONObject(jsonTxt);
                paypalInfo.put("email", json.getString("email"));
                paypalInfo.put("password", json.getString("password"));
            } catch (Exception e) {
                paypalInfo = null;
                Assert.fail("Unable to read Paypal Information from file" + e);
            }
        }
        return paypalInfo;
    }

    /**
     * Generates a random email address of any length
     *
     * @param length how long to make the email address
     * @return random email address
     */
    public static String generateRandomEmail(int length) {
        if (length == 0) {
            length = 16;
        }
        
        String allowedChars = "abcdefghijklmnopqrstuvwxyz" + "1234567890";
        String email = RandomStringUtils.random(length, allowedChars);
        email = email.substring(0, email.length()) + "@blackhole.macys.com";
        return email;
    }

    /**
     * Generates a random string of any length
     *
     * @param length how long to make the string
     * @return random string
     */
    public static String generateRandomString(int length) {
        String allowedChars = "abcdefghijklmnopqrstuvwxyz" + "1234567890";
        return RandomStringUtils.random(length, allowedChars);
    }

    /**
     * Generates a random first name
     *
     * @return random first name
     */
    public static String generateRandomFirstName() {
        String[] firstNames = {"JAMES", "JOHN", "ROBERT", "MICHAEL", "WILLIAM", "DAVID", "RICHARD", "CHARLES", "JOSEPH", "THOMAS", "CHRISTOPHER"};
        return firstNames[new Random().nextInt(firstNames.length)];
    }

    /**
     * Generates a random last name
     *
     * @return random last name
     */
    public static String generateRandomLastName() {
        String[] lastNames = {"SMITH", "JOHNSON", "BROWN", "JONES", "MILLER", "GARCIA", "RODRIGUEZ", "ANDERSON", "TAYLOR", "THOMAS", "MOORE"};
        return lastNames[new Random().nextInt(lastNames.length)];
    }

    /**
     * Generates a random month
     *
     * @return random month as String
     */
    public static String generateRandomMonth() {
        String[] monthArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthArray[new Random().nextInt(monthArray.length)];
    }

    /**
     * Generates a random month with index
     *
     * @return random month as String
     */
    public static String[] generateRandomMonthWithIndex() {
        String[] monthArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        int monthIndex = new Random().nextInt(monthArray.length);
        return new String[]{monthArray[monthIndex], String.valueOf(monthIndex + 1)};
    }

    /**
     * Generates a random date
     *
     * @return random date as int
     */
    public static int generateRandomDateIndex() {
        int min = 1;
        int max = 31;
        Random dates = new Random();
        return dates.nextInt(max - min) + min;
    }

    /**
     * Generates a random date
     *
     * @return random date
     */
    public static Date generateRandomDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(generateRandomYearIndex(), Integer.parseInt(generateRandomMonthWithIndex()[1]), generateRandomDateIndex());
        return calendar.getTime();
    }

    /**
     * Generates a random year
     *
     * @return random year as int
     */
    public static int generateRandomYearIndex() {
        int min = 1917;
        int max = 1987;
        Random years = new Random();
        return years.nextInt(max - min) + min;
    }

    /**
     * Generates a random gender
     *
     * @return random gender as String
     */
    public static String generateRandomGender() {
        String[] genderArray = {"Female", "Male"};
        return genderArray[new Random().nextInt(genderArray.length)];
    }

    /**
     * Generates a random security question answer
     * <p>
     * Note: this will only make sense with the question that asks the first concert you attended.
     * It will still work for the other ones, though.
     * </p>
     *
     * @return random
     */
    public static String generateRandomSecurityAnswer() {
        String[] answers = {"Metallica", "Billy Joel", "The Beatles", "Michael Jackson", "Justin Bieber"};
        return answers[new Random().nextInt(answers.length)];
    }

    /**
     * Generates a random phone number
     *
     * @return random phone number as String
     */
    public static String generateRandomPhoneNumber() {
        int length = 10;
        char[] digits = new char[length];
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            digits[i] = (char) ('0' + rand.nextInt(10));
        }
        // phone number cannot be started with '0' or '1'
        if (digits[0] == '0' || digits[0] == '1') {
            digits[0] = '2';
        }
        return new String(digits);
    }

    /**
     * Generates a random area code or exchange code
     *
     * @return random area code or exchange code
     */
    public static String generateRandomPhoneAreaCodeExchange() {
        int length = 3;
        char[] digits = new char[length];
        // Make sure the leading digit isn't 0.
        Random rand = new Random();
        digits[0] = (char) ('1' + rand.nextInt(9));
        for (int i = 1; i < length; i++) {
            digits[i] = (char) ('0' + rand.nextInt(10));
        }
        return new String(digits);
    }

    /**
     * Generates a random phone subscriber number (last 4 digits)
     *
     * @return random subscriber number
     */
    public static String generateRandomPhoneSubscriber() {
        int length = 4;
        char[] digits = new char[length];
        // Make sure the leading digit isn't 0.
        Random rand = new Random();
        digits[0] = (char) ('1' + rand.nextInt(9));
        for (int i = 1; i < length; i++) {
            digits[i] = (char) ('0' + rand.nextInt(10));
        }
        return new String(digits);
    }

    /**
     * Gets a valid ISHIP address from the valid_addresses.json file
     * <p>
     * See "valid_addresses.json" for info on what fields will be present.
     * </p>
     *
     * @param country country to get address for
     * @return JSONObject containing address information
     */
    public static JSONObject getValidIshipAddress(String country) {
        try {
            File addressFile = getResourceFile("valid_addresses.json");
            String jsonTxt = Utils.readTextFile(addressFile);
            JSONObject json = new JSONObject(jsonTxt);
            JSONArray addresses = (JSONArray) json.get("addresses");

            for (int i = 0; i < addresses.length(); i++) {
                JSONObject ishipAddress = addresses.getJSONObject(i);
                if (ishipAddress.get("country").equals(country)) {
                    return ishipAddress;
                }
            }
            return null;
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

    /**
     * Gets a random valid visa credit card from "valid_cards.json"
     *
     * @return JSONObject containing credit card information
     */
    public static CreditCard getValidVisaCreditCard() {
        try {
            JSONArray creditCards;
            File cardFile = getResourceFile("valid_cards.json");
            String jsonTxt = Utils.readTextFile(cardFile);
            JSONObject json = new JSONObject(jsonTxt);
            if (macys()) {
                creditCards = (JSONArray) json.get("macys");
            } else {
                creditCards = (JSONArray) json.get("bloomingdales");
            }

            for (int i = 0; i < creditCards.length(); i++) {
                JSONObject card = creditCards.getJSONObject(i);
                if (card.get("card_type").equals("Visa")) {
                    return new CreditCard(
                            CreditCard.CardType.VISA,
                            card.getString("card_number"),
                            card.getString("security_code"),
                            card.getDouble("balance"),
                            card.getString("expiry_month"),
                            card.getString("expiry_month_index"),
                            card.getString("expiry_year")
                    );
                }
            }
            return null;
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

    /**
     * Gets a random valid 3DSecure card from "valid_cards.json"
     *
     * @param cardType the type of card to look for (Visa, Discover, etc.)
     * @return JSONObject containing 3DSecure card information
     */
    public static CreditCard getValid3DSecureCard(String cardType) {
        try {
            JSONArray creditCards;
            File cardFile = getResourceFile("valid_cards.json");
            String jsonTxt = Utils.readTextFile(cardFile);
            JSONObject json = new JSONObject(jsonTxt);
            if (macys()) {
                creditCards = (JSONArray) json.get("macys");
            } else {
                creditCards = (JSONArray) json.get("bloomingdales");
            }

            for (int i = 0; i < creditCards.length(); i++) {
                JSONObject card = creditCards.getJSONObject(i);
                if (card.has("3d_secure") && card.get("card_type").equals(cardType)) {
                    return new CreditCard(
                            CreditCard.CardType.fromString(cardType),
                            card.getString("card_number"),
                            card.getString("security_code"),
                            card.getDouble("balance"),
                            card.getString("expiry_month"),
                            card.getString("expiry_month_index"),
                            card.getString("expiry_year"),
                            true,
                            card.getString("3d_secure_password")
                    );
                }
            }
            return null;
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

    /**
     * Gets a random valid USL id from "enrolled_usl_id.json"
     *
     * @return JSONObject containing USL information
     */
    public static UslInfo getEnrolledUslId() {
        try {
            File uslFile = getResourceFile("enrolled_usl_id.json");
            String jsonTxt = Utils.readTextFile(uslFile);
            //JSON from file to Object
            List<UslInfo> uslInfoList = new ObjectMapper().readValue(jsonTxt,
                    TypeFactory.defaultInstance().constructCollectionType(List.class,
                            UslInfo.class));
            return uslInfoList.get(0);
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

    /**
     * Gets a random valid USL id from "loyalty.json"
     *
     * @param loyallistType type of loyallist - currently only "toptier_loyallist" is available
     * @return JSONObject containing loyallist information
     */
    public static LoyalistDetails getLoyallistDetails(String loyallistType) {
        try {
            File addressFile = getResourceFile("loyalty.json");
            String jsonTxt = Utils.readTextFile(addressFile);
            Random rand = new Random();
            //JSON from file to Object
            List<LoyalistDetails> loyalistDetailsList = new ObjectMapper().readValue(jsonTxt,
                    TypeFactory.defaultInstance().constructCollectionType(List.class,
                            LoyalistDetails.class));

            List<LoyalistDetails> loyalists = loyalistDetailsList.stream().filter(loyalistDetails -> loyalistDetails.getLoyallistType().equalsIgnoreCase(loyallistType)).collect(Collectors.toList());
            return loyalists.get(rand.nextInt(loyalists.size()));

        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

    /**
     * Gets a random valid promo code from "valid_promo_codes.json"
     *
     * @return JSONObject containing promo code information
     */
    public static JSONObject getValidPromotion() {
        JSONObject promotion;
        try {
            String jsonTxt = Utils.readTextFile(getResourceFile("valid_promo_codes.json"));
            JSONArray promotions = (JSONArray) new JSONObject(jsonTxt).get(macys() ? "macys" : "bloomingdales");
            promotion = promotions.getJSONObject(new Random().nextInt(promotions.length()));
        } catch (JSONException e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        } catch (FileNotFoundException e) {
            Assert.fail("File not found" + e);
            return null;
        } catch (IOException e) {
            Assert.fail("IO Exception: " + e);
            return null;
        }
        return promotion;
    }

    /**
     * Gets a random valid address with given options and type from "valid_addresses.json"
     *
     * @param options Address options
     * @return JSONObject with address info
     */
    public static ProfileAddress getRandomValidAddress(HashMap<String, String> options) {
        if (options == null) {
            options = new HashMap<>();
        }
        options.putIfAbsent("country", "United States");
        JSONObject address = null;
        try {
            ArrayList<JSONObject> addresses;
            String jsonText = Utils.readTextFile(getResourceFile("valid_addresses.json"));
            JSONArray addressesJSON = new JSONObject(jsonText).getJSONArray("addresses");
            if (addressesJSON == null) {
                System.err.println("Unable to get a valid address");
                return null;
            }

            addresses = Utils.jsonArrayToList(addressesJSON);
            Collections.shuffle(addresses);
            boolean found;
            for (JSONObject obj : addresses) {
                found = true;
                for (String key : options.keySet()) {
                    try {
                        if (!obj.getString(key).equalsIgnoreCase(options.get(key))) {
                            found = false;
                            break;
                        }
                    } catch (JSONException e) {
                        //attribute not present, not a valid match
                        found = false;
                        break;
                    }
                }
                if (found) {
                    address = obj;
                    break;
                }
            }
            if (address == null) {
                throw new Exception("Unable to find address matching given options");
            }
        } catch (Exception e) {
            Assert.fail("Failed to retrieve address info: " + e);
            return null;
        }
        ProfileAddress profileAddress = new ProfileAddress();
        try {
            profileAddress.setFirstName(generateRandomFirstName());
            profileAddress.setLastName(generateRandomLastName());
            profileAddress.setAddressLine1(address.getString("address_line_1"));
            profileAddress.setAddressLine2(address.getString("address_line_2"));
            profileAddress.setCity(address.getString("address_city"));
            profileAddress.setState(address.getString("address_state"));
            profileAddress.setZipCode(Integer.valueOf(address.getString("address_zip_code").trim()));
            profileAddress.setEmail(generateRandomEmail(16));
            profileAddress.setBestPhone(generateRandomPhoneNumber());
        } catch (JSONException e) {
            System.err.println("Unable to get random address: " + e);
        }

        return profileAddress;
    }

    /**
     * Gets a random valid shipping address from given country from "valid_shipping_addresses.json"
     *
     * @param country country to get address from
     * @return JSONObject with address info
     */
    public static JSONObject getRandomValidShippingAddress(String country) {
        if (country == null) {
            country = "United States";
        }
        try {
            File addressFile = getResourceFile("valid_shipping_addresses.json");
            String jsonTxt = Utils.readTextFile(addressFile);
            JSONObject json = new JSONObject(jsonTxt);
            JSONArray addresses = (JSONArray) json.get("addresses");

            Random rand = new Random();
            JSONObject address = (JSONObject) addresses.get(rand.nextInt(addresses.length()));
            while (!address.getString("country").equalsIgnoreCase(country)) {
                address = (JSONObject) addresses.get(rand.nextInt(addresses.length()));
            }

            return address;
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

    /**
     * Get a random product with attributes listed in options
     *
     * @param options attributes the product should have
     * @return Product object with all attributes
     */
    public static Product getRandomProduct(HashMap<String, Boolean> options) {
        try {
            JSONArray products;
            if (prodEnv()) {
                options.putIfAbsent("prod_available", true);
            }
            File addressFile = getResourceFile("orderable_products.json");
            // for now all our products are orderable
            options.remove("orderable");
            String jsonTxt = Utils.readTextFile(addressFile);
            JSONObject json = new JSONObject(jsonTxt);
            if (macys()) {
                products = (JSONArray) json.get("macys");
            } else {
                products = (JSONArray) json.get("bloomingdales");
            }

            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                boolean found = true;
                for (String key : options.keySet()) {
                    try {
                        if (!options.get(key).equals(product.getBoolean(key))) {
                            found = false;
                            break;
                        }
                    } catch (JSONException e) {
                        //attribute not present, assumed false
                        if (options.get(key)) {
                            found = false;
                            break;
                        }
                    }
                }
                if (found) {
                    return new Product(product);
                }
            }

            return null;
        } catch (Exception e) {
            Assert.fail("Unable to parse JSON: " + e);
            return null;
        }
    }

}