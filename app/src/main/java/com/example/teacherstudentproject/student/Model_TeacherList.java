package com.example.teacherstudentproject.student;

public class Model_TeacherList {

    private String ID;
    private String Image;
    private String Name;
    private String Distance;


    public Model_TeacherList(String ID, String name, String distance) {
        this.ID = ID;
        Name = name;
        Distance = distance;
    }

    public String getID() {
        return ID;
    }

    public String getImage() {
        return Image;
    }

    public String getName() {
        return Name;
    }

    public String getDistance() {
        return Distance;
    }
}
