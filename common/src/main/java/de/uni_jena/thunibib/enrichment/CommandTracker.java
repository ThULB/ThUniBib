package de.uni_jena.thunibib.enrichment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycore.frontend.cli.MCRCommand;

/**
 * Class allows to track {@link MCRCommand}s split over more than one transaction.
 *
 * @author shermann
 * */
class CommandTracker<S, M> {

    private HashMap<String, List<S>> tracks;
    private HashMap<String, Integer> trackSizes;
    private HashMap<String, List<M>> trackedItems;

    /**
     * Creates a new instance of this class.
     * */
    <S, M> CommandTracker() {
        tracks = new HashMap<>();
        trackSizes = new HashMap<>();
        trackedItems = new HashMap<>();
    }

    /**
     * Register a size for certain track.
     *
     * @param trackId
     * @param expectedSize
     * */
    synchronized void trackSize(String trackId, int expectedSize) {
        if (!trackSizes.containsKey(trackId)) {
            trackSizes.put(trackId, expectedSize);
        }
    }

    /**
     * Decrement the size for certain track.
     *
     * @param trackId
     * */
    synchronized void decrementTrackSize(String trackId) {
        if (trackSizes.containsKey(trackId)) {
            trackSizes.put(trackId, (trackSizes.get(trackId) - 1));
        }
    }

    /**
     * Inrement the size for certain track.
     *
     * @param trackId
     * */
    synchronized void incrementTrackSize(String trackId) {
        if (trackSizes.containsKey(trackId)) {
            trackSizes.put(trackId, (trackSizes.get(trackId) + 1));
        }
    }

    /**
     * Register a value to certain track. If the track is not existing, it will be created for you.
     *
     * @param trackId
     * @param value
     * */
    synchronized void track(String trackId, S value) {
        if (tracks.containsKey(trackId)) {
            tracks.get(trackId).add(value);
        } else {
            List<S> l = new ArrayList<>();
            l.add(value);
            tracks.put(trackId, l);
        }
    }

    /**
     * Unregister the value from the track and provide the item the command may have created by use of the given value.
     *
     * @param trackId
     * @param value
     * @param item
     * */
    synchronized void untrack(String trackId, S value, M item) {
        if (!tracks.containsKey(trackId)) {
            return;
        }

        tracks.get(trackId).remove(value);

        if (trackedItems.containsKey(trackId)) {
            trackedItems.get(trackId).add(item);
        } else {
            List<M> l = new ArrayList<>();
            l.add(item);
            trackedItems.put(trackId, l);
        }
    }

    /**
     * Check whether a track is done or not.
     *
     * @return <code>true</code> when the size for the track equals the number of items in list for the track.
     * */
    boolean isDone(String trackId) {
        if (!tracks.containsKey(trackId)) {
            return false;
        }

        return trackSizes.get(trackId) == trackedItems.get(trackId).size();
    }

    /**
     * Removes all data associated to a given track.
     *
     * @param trackId
     * */
    synchronized void clear(String trackId) {
        if (!tracks.containsKey(trackId)) {
            return;
        }
        tracks.remove(trackId);
        trackSizes.remove(trackId);
        trackedItems.remove(trackId);
    }

    List<M> getItems(String trackId) {
        return trackedItems.containsKey(trackId) ? trackedItems.get(trackId) : new ArrayList<>();
    }
}
