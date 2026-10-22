package de.uni_jena.thunibib.his.cli;

import org.mycore.datamodel.metadata.MCRObject;

/**
 * Checks if a {@link MCRObject} is transferable to HISinOne.
 *
 * @author shermann (Silvio Hermann)
 * */
public interface HISinOneTransferableVerifier {

    /**
     * Checks if a given {@link MCRObject} is transferable to HISinOne.
     *
     * @return {@code true} when {@link MCRObject} is transferable {@code false} otherwise
     * */
    boolean isTransferable(MCRObject mcrObject);
}
