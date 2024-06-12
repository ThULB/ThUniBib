package de.uni_jena.thunibib.his.api;

public class HISinOneClientFactory {

    public static HISInOneClient create() {
        return new HISInOneClientDefaultImpl();
    }
}
