package com.volunteer.thc.volunteerapp.callback;

/**
 * Created by Cristi on 10/15/2017.
 */

public interface ActionListener {
    interface FeedbackDoneListener {
        void onFeedbackCompleted();
    }
    interface NewsDeletedListener {
        void onNewsDeleted();
    }
    interface VolunteersRemovedListener {
        void onAllVolunteersRemoved();
    }
    interface EventPicturesLoadingListener {
        void onPicturesLoaded();
    }
}
