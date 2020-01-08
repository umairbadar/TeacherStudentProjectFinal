package com.example.teacherstudentproject.endpoints;

public class Api {

    private static String Base_URL = "http://110.93.225.221/tschat/index.php?route=feed/rest_api/";

    public static String States_URL = Base_URL + "states&key=ssred1";
    public static String Countries_URL = Base_URL + "countries&key=ssred1";
    public static String Signup_URL = Base_URL + "signup&key=ssred1";
    public static String Login_URL = Base_URL + "login&key=ssred1";
    public static String CoursesCategory_URL = Base_URL + "course_categories&key=ssred1";
    public static String Courses_URL = Base_URL + "courses&key=ssred1";
    public static String TeacherListing_URL = Base_URL + "teachers&key=ssred1";
    public static String TeacherDetail_URL = Base_URL + "teacher&key=ssred1";
    public static String UpdateProfile_URL = Base_URL + "edit_profile&key=ssred1";
    public static String ChangePassword_URL = Base_URL + "change_password&key=ssred1";
    public static String SelectedCourses_URL = Base_URL + "user_selected_courses&key=ssred1";
    public static String NonSelectedCourses_URL = Base_URL + "user_non_selected_courses&key=ssred1";

}
