# HWU Timetable
[![Build Status](https://travis-ci.com/AdamTMalek/HWUTimetable.svg?branch=master)](https://travis-ci.com/AdamTMalek/HWUTimetable)

HWU Timetable or in full Heriot Watt University Timetable is an **unofficial** Android application that makes it much 
easier to view student timetables on a mobile device.

## What is it and why it exists?
The student timetable website is not mobile friendly, which is quite a big issue considering how students are encouraged
to check their timetables regularly. The aim of this project is to make accessing timetables much easier, and bring some
other useful functionalities.

The app allows you to save a timetable to your Android device and be notified of any changes that are made to it, by
automatically checking for differences between the saved timetable and the online one. Frequency of these checks depends
on the settings.

Additionally, the app lets students pick individual courses and combine them into one timetable. This is something
that is unique to the app, as the website displays each timetable separately.

## Current state of the project
The project is now at the stage where the majority of desired functionalities have been implemented and tested by
automated tests. There are a few functionalities that would be nice to have, however currently, the focus is to enhance
User Experience. If you would like to know what functionalities will be implemented in the future, please check the 
"Issues".
## Using the app
First, you will see the screen shown below. All the stored timetables will be shown here, but since you do not have any
at the moment, use the "Add Timetable" to add one. Here you can choose if you want to add a programme timetable, or
generate timetable by selecting individual courses. 

![hwut-main-empty-add-expanded](https://user-images.githubusercontent.com/23484014/92588334-bf7bde80-f290-11ea-9ee2-5eb618ed4327.png)

### Adding a programme timetable
Clicking "Add Programme" will take you to another screen where you can either choose the department, the level and then
type in the name of the programme, or you can type the name straight away.

There is no need to choose the semester like on the website - this will be done automatically by the app.

![hwut-add-programme](https://user-images.githubusercontent.com/23484014/92616310-f9131080-f2b5-11ea-963a-e63413b39c02.png)

### Adding a timetable by selecting individual courses

![hwut-add-course](https://user-images.githubusercontent.com/23484014/92588330-bd198480-f290-11ea-9812-d737bca527a8.png)

Here, the process is a little different. First you need to name the timetable, this can be something like
"4th year - semester 1". Then, you can either choose a department which will filter the courses, or you can start
typing the course name. After choosing the course, click "Add Course" button. You can now either add more courses, or
click "Generate Timetable" which will scrape and combine all timetables of all the selected courses into one.

### Timetable view
After adding the timetable, you will be taken to the timetable view. Here you see your timetable, there are a couple
of features that were implemented into the app so please read the following carefully so you know what is happening

By default, you will see a simplified class view, which only shows the class name and the room.
 
![hwut-view-simple](https://user-images.githubusercontent.com/23484014/90335436-eebe6900-dfcc-11ea-9ed5-65c3c74cab46.png)

You can change to original view, shown below, by going into settings and disabling "Use simplified timetable class view"
setting.

![tt_view](https://user-images.githubusercontent.com/23484014/85850903-9fff0a80-b7a5-11ea-949e-560b3476e44c.png)

To get more information about a class, long-click on the class and a popup window will come up with the information.
This feature works on both simplified and original views, however it only comes useful in the simplified view, as in
the original view all the information is already displayed.

![hwut-view-popup](https://user-images.githubusercontent.com/23484014/90335448-08f84700-dfcd-11ea-9cca-3155991bbbc6.png)

The displayed day depends on the current day. That is, if you are viewing the timetable on Wednesday, the app will
automatically go to Wednesday lectures/labs. To view a different day, simply swipe left or right, or you can also click 
on the previous/next day label at the top of the screen.

The same goes for weeks, by default you will see the timetable for the current week. You can choose a different week
using the dropdown at the top of the screen.

Now, if you go back to the main view you will see that the list has been populated with the recently added timetable.

![hwut-main-populated](https://user-images.githubusercontent.com/23484014/92589430-73319e00-f292-11ea-9ea9-08e1f856dd7f.png)

### Update checks
To enable update checks, go into settings by clicking the so-called "kebab menu" or "overflow menu" go into settings 
and set the update checks based on your preferences.
