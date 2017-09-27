package com.example.rxjavatest;

import java.util.List;

/**
 * Created by taofq on 2017/9/27.
 */

public class MobileAddress {


    public int followersCount;
    public String href;
    public boolean acceptSubmission;
    public boolean firstTime;
    public boolean canManage;
    public String description;
    public String reason;
    public int banUntil;
    public String slug;
    public String name;
    public int postsCount;

    @Override
    public String toString() {
        return "MobileAddress{" +
                "followersCount=" + followersCount +
                ", href='" + href + '\'' +
                ", acceptSubmission=" + acceptSubmission +
                ", firstTime=" + firstTime +
                ", canManage=" + canManage +
                ", description='" + description + '\'' +
                ", reason='" + reason + '\'' +
                ", banUntil=" + banUntil +
                ", slug='" + slug + '\'' +
                ", name='" + name + '\'' +
                ", postsCount=" + postsCount +
                '}';
    }
}
