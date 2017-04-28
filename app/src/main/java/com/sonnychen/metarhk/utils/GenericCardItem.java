package com.sonnychen.metarhk.utils;

/**
 * Created by Sonny on 4/25/2017.
 */

public class GenericCardItem {
    public String Tag;
    public String ImageURL;
    public String Text;

    public GenericCardItem() {}
    public GenericCardItem(String Tag, String ImageURL, String Text) {
        this.Tag = Tag;
        this.ImageURL = ImageURL;
        this.Text = Text;
    }
}
