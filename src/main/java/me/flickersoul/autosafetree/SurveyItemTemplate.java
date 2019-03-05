package me.flickersoul.autosafetree;

import java.util.HashMap;

public class SurveyItemTemplate extends ItemTemplate<SurveyMember>{

    public SurveyItemTemplate(String key, String display) {
        super(key, display);
    }

    private static HashMap<String, SurveyMember> members;

    public static void setMembers(HashMap<String, SurveyMember> list){
        members = list;
    }

    public SurveyMember getItem(){
        return members.get(this.getKey());
    }
}
