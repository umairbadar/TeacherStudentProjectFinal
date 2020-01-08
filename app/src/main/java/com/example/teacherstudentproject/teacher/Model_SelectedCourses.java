package com.example.teacherstudentproject.teacher;

public class Model_SelectedCourses {

    private String course_id;
    private String course_name;

    public Model_SelectedCourses(String course_id, String course_name) {
        this.course_id = course_id;
        this.course_name = course_name;
    }

    public String getCourse_id() {
        return course_id;
    }

    public String getCourse_name() {
        return course_name;
    }
}
