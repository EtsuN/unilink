package com.teamxod.unilink.house;

class HousePost {
    //item shown below the image
    private String room_key;

    private String room_type;

    private String room_title;

    private int room_price;

    private String room_location;

    private String imageResourceId;

    private String term;


    public HousePost() {
    }


    public HousePost(String key, String p_room_type, String p_room_title, int p_room_price, String lease,
                     String p_room_location, String p_imageResourceId) {
        room_key = key;
        room_type = p_room_type;
        room_title = p_room_title;
        room_price = p_room_price;
        term = lease;
        room_location = p_room_location;
        imageResourceId = p_imageResourceId;
    }


    public String getRoom_type() {
        return room_type;
    }

    public String getRoom_title() {
        return room_title;
    }

    public int getRoom_price() {
        return room_price;
    }

    public String getRoom_location() {
        return room_location;
    }

    public String getImageResourceId() {
        return imageResourceId;
    }

    public String getTerm() {
        return term;
    }

    public String getRoom_key() {
        return room_key;
    }
}
