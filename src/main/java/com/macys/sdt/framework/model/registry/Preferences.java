package com.macys.sdt.framework.model.registry;

/**
 * The preferences associated with a Registry
 */
public class Preferences {

    private String datePrintFormat = "MM_DD_YY";
    private boolean availableOnInternet = true;
    private boolean publicRegistry = true;
    private boolean subscribeWeddingSale = false;
    private boolean goGreen = true;
    private Store store = new Store();

    public String getDatePrintFormat() {
        return datePrintFormat;
    }

    public void setDatePrintFormat(String datePrintFormat) {
        this.datePrintFormat = datePrintFormat;
    }

    public boolean isAvailableOnInternet() {
        return availableOnInternet;
    }

    public void setAvailableOnInternet(boolean availableOnInternet) {
        this.availableOnInternet = availableOnInternet;
    }

    public boolean isPublicRegistry() {
        return publicRegistry;
    }

    public void setPublicRegistry(boolean publicRegistry) {
        this.publicRegistry = publicRegistry;
    }

    public boolean isSubscribeWeddingSale() {
        return subscribeWeddingSale;
    }

    public void setSubscribeWeddingSale(boolean subscribeWeddingSale) {
        this.subscribeWeddingSale = subscribeWeddingSale;
    }

    public boolean isGoGreen() {
        return goGreen;
    }

    public void setGoGreen(boolean goGreen) {
        this.goGreen = goGreen;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

}
