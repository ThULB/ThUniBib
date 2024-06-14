package de.uni_jena.thunibib.his.api.client;

public class HISinOneClientFactory {

    public static HISInOneClient create() {
        return new HISInOneClientDefaultImpl();
    }
}
