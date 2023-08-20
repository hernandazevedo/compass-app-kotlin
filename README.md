# Compass

## Introduction

Your company is about to complete their new location-based application.
One last feature is the compass functionality that does not involve GPS data.

## Problem Statement

The aim of this project is to rotate the image displayed so it shows North in real time.
Use the accelerometer and magnetic field sensors to obtain the required data.

Provide the missing code in the `MainActivity.kt` file.

Good Luck!

## Instructions

Source code to gracefully close the app if access to the required sensors could not be provided by the OS has already been provided.
Functionality to clean-up eventually registered sensors is also in its place.

Make sure the application registers itself to required sensors and reacts to their events.
Provide an implementation for the following methods:

- `com.devskiller.gyrocompass.MainActivity.AccelerometerSensorDataChangedListener#onSensorChanged`
- `com.devskiller.gyrocompass.MainActivity.MagneticFieldSensorDataChangedListener#onSensorChanged`
- `com.devskiller.gyrocompass.MainActivity.onStart`
- `com.devskiller.gyrocompass.MainActivity.tryToCalculateRotation`
