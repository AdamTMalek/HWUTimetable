# HWU Timetable
[![Build Status](https://travis-ci.com/AdamTMalek/HWUTimetable.svg?branch=master)](https://travis-ci.com/AdamTMalek/HWUTimetable)

HWU Timetable or in full Heriot Watt University Timetable is an **unofficial** Android application that makes it much 
easier to view student timetables on a mobile device.

## What is it and why it exists?
The student timetable website is not mobile friendly. The aim of this project is to not only change that, 
but also provide some of the functionalities that will make student's life at the University easier.

The app allows you to add a timetable to "followed", this saves it locally on your device so you can view it
at any time and place you want without having to be connected to the Internet. 

The word choice of "followed" is deliberate. The app will check the website regularly to see if there are any updates
to the timetables that are stored on the device. If there are, the app will download the newest version of every
timetable and notify you about the update.
## Current state of the project
The app, at this stage, is still far from being ready to be deployed.

Currently, the main focus goes on implementing the core functionality rather than User Experience. At the current state
the app may go through many major changes from one update to another without giving any notice, therefore it is strongly
recommended that (assuming you are interested) you stay patient until the app will be considered ready to be deployed.

Functionalities that have been implemented so far are:
- Saving a timetable to the device
- Viewing the parsed timetable in a very simple form
- Updating the stored timetables based on user preferences (in settings)
- Detecting clashes and notifying the user about them

## Using the app
First, you will see the empty screen. The majority of the screen is blank, this is actually a list of the stored 
timetables. You will see how it works after we add the first timetable.

![main_empty](https://user-images.githubusercontent.com/23484014/72175465-1fee0f80-33d4-11ea-9840-36d39d6de7a2.png)


Click the circle add button to add your timetable. This will take you to another screen where you need to choose
the department, the level and the group. There is no need to choose the semester like on the website - this will
be done automatically by the app.

![adding](https://user-images.githubusercontent.com/23484014/72175464-1fee0f80-33d4-11ea-855f-8f695356961c.png)


After adding the timetable, you will be taken to the timetable view. Here you see your timetable, there are a couple
of features that were implemented into the app so please read the following carefully so you know what is happening

![tt_view](https://user-images.githubusercontent.com/23484014/72175467-1fee0f80-33d4-11ea-9459-acf07a401a93.png)


The displayed day depends on the current day. That is, if you are viewing the timetable on Wednesday, the app will
automatically go to Wednesday items. To view a different day, simply swipe left or right.

The same goes for weeks, you can select weeks above the timetable view. By default, you will see the timetable
for the current week.

Now, if you go back to the main view you will see that the list has been populated with the recently added timetable.

![main_wcontent](https://user-images.githubusercontent.com/23484014/72175466-1fee0f80-33d4-11ea-84f8-e7a13f4d7516.png)

The circle appearing beside the name indicates the semester of the timetable.

### Update checks
To enable update checks, go into settings by clicking the so-called "kebab menu" or "overflow menu" go into settings 
and set the update checks based on your preferences.
