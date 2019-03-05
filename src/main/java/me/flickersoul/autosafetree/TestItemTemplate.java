package me.flickersoul.autosafetree;

import java.util.HashMap;

public class TestItemTemplate extends ItemTemplate<TestMember>{

    public TestItemTemplate(String key, String display) {
        super(key, display);
    }

    private static HashMap<String, TestMember> members;

    public static void setMembers(HashMap<String, TestMember> list){
        members = list;
    }

    public TestMember getItem(){
        return members.get(this.getKey());
    }
}
