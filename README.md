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

## Current state of the project
The project is now at the stage where the majority of desired functionalities have been implemented and tested by
automated tests. There are a few functionalities that would be nice to have, however currently, the focus is to enhance
User Experience. If you would like to know what functionalities will be implemented in the future, please check the 
"Issues".
## Using the app
First, you will see the screen shown below. All the stored timetables will be shown here, but since you do not have any
at the moment, use the "Add Button" to add one. 

![main_empty](https://user-images.githubusercontent.com/23484014/88577282-ab608280-d03e-11ea-8c13-ac0632e17781.png)


This will take you to another screen where you need to choose the department, the level and the group. There is no need 
to choose the semester like on the website - this will be done automatically by the app.

![adding](https://user-images.githubusercontent.com/23484014/88577279-aac7ec00-d03e-11ea-9f67-3eed41750213.png)


After adding the timetable, you will be taken to the timetable view. Here you see your timetable, there are a couple
of features that were implemented into the app so please read the following carefully so you know what is happening

![tt_view](https://user-images.githubusercontent.com/23484014/85850903-9fff0a80-b7a5-11ea-949e-560b3476e44c.png)


The displayed day depends on the current day. That is, if you are viewing the timetable on Wednesday, the app will
automatically go to Wednesday lectures/labs. To view a different day, simply swipe left or right, or you can also click 
on the previous/next day label at the top of the screen.

The same goes for weeks, by default you will see the timetable for the current week. You can choose a different week
using the dropdown at the top of the screen.

Now, if you go back to the main view you will see that the list has been populated with the recently added timetable.

![main_wcontent](https://user-images.githubusercontent.com/23484014/88577283-ab608280-d03e-11ea-8ff8-4b76437710f3.png)

### Update checks
To enable update checks, go into settings by clicking the so-called "kebab menu" or "overflow menu" go into settings 
and set the update checks based on your preferences.
