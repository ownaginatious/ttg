# The Quintessential Timetable Generator

## What was the timetable generator?
The Quintessential Timetable Generator was a website used for creating timetables for a number of Canadian universities from 2012 to 2018. It operated by periodically scraping publicly available course scheduling information for consumption by the website.

In November 2018 it was shut down due to a lack of resources to continue its development and because its usefulness was generally superceded by better scheduling tools offered by the supported universities. It was afterward open-sourced for the community to use for their own purposes.

## What is this repository?

This respository is a mono-repo created with all the components that were part of the overall timetable generator project. Each of the top-level folders in this directory represent what was once a separate repo.

This table summarizes what each one represents:

| Component | Description |
| -----------|------------|
| [infrastructure](../infrastructure) | Docker-based infrastructure for bringing up services used for the production environment. |
| [model](../model) | Language specific implementations of the data model that website data is parsed into by the scrapers.|
| [roadmap](../roadmap) | Old planning documentation describing the current state of the timetable generator and its future development trajectory.|
| [saved-schedules](../saved-schedules) | Node + mongo based service for saving schedules|
| [scrapers](../scrapers) | The Java-based framework for scraping course data into JSON. Includes all supported university implementations.|
| [web](../web) | Web front-end (jQuery + Grunt) and backend (Django) for the website.|

Thorough documentation is provided for getting each of the components running separately (if possible). Please open an issue if you feel anything needs further clarification.

[roadmap](../roadmap) is a good starting point to understanding how this all fits together.

## How do I contribute?

This repository isn't intended to promote continued development of the timetable generator. Pull requests for improving documentation, bug fixes or refactoring (particularly around scrapers) to promote better understanding will be accepted. New features, however, are discouraged.

## Do I need permission to use code from this repository?

If you would like to use a component of the timetable generator in your own project, you are free to do so without any restrictions. This project is totally open source.

## Can I takeover development of the timetable generator?

All the resources for running the website have been shut down. You are encouraged to start your own project independently if you want to maintain such a service.

If you would like to use the domains, they can be transferred provided that you have a working project that _at least_ has the same functionality of the original timetable generator. Otherwise, they will eventually expire on their own.