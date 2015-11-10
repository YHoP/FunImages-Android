package com.epicodus.funimagesapp.models;

/**
 * Created by Guest on 11/10/15.
 */
public class PhotoUrl {
    private String mPhotoId, mSecret, mServer;

    public PhotoUrl(String photoId, String secret, String server){
        mPhotoId = photoId;
        mSecret = secret;
        mServer = server;

    }

    public String getPhotoId() {
        return mPhotoId;
    }

    public String getServer() {
        return mServer;
    }

    public String getSecret() {
        return mSecret;
    }
}
