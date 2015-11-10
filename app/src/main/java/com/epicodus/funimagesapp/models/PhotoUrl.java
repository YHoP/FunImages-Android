package com.epicodus.funimagesapp.models;

/**
 * Created by Guest on 11/10/15.
 */
public class PhotoUrl {
    private String mPhotoId, mUserId;

    public PhotoUrl(String photoId, String userId){
        mPhotoId = photoId;
        mUserId = userId;
    }

    public String getPhotoId() {
        return mPhotoId;
    }

    public String getUserId() {
        return mUserId;
    }
}
