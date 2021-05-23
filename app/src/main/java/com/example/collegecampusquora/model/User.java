package com.example.collegecampusquora.model;

public class User {
    public static final String FIELD_PROFILE_PIC = "profile pic";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_MOBILE_NO = "mobile";
    public static final String FIELD_BIO = "bio";

    public String Uid;
    public String ProfilePic;
    public String email;
    public String name;
    public String mobile;
    public String bio;

    public User(){
    }

    public User(String uid, String email) {
        Uid = uid;
        this.email = email;
    }
    public static String getFieldProfilePic() {
        return FIELD_PROFILE_PIC;
    }

    public static String getFieldEmail() {
        return FIELD_EMAIL;
    }

    public static String getFieldName() {
        return FIELD_NAME;
    }

    public static String getFieldMobileNo() {
        return FIELD_MOBILE_NO;
    }

    public static String getFieldBio() {
        return FIELD_BIO;
    }
    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public void setProfilePic(String profilePic) {
        ProfilePic = profilePic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
