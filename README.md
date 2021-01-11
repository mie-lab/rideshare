# SCCER Ridesharing Project
This repository contains parts of the code for the SCCER rideshare project.

The idea of the project is to facilitate short-distance and ad-hoc ridesharing by using automatically and passively tracked mobility data to predict where someone is moving in the near future.
Once these movements are known, people with similar itineraries are matched together, and notified via the same app that previously recorded their movements.
Upon receiving a notification, users can simply accept or reject a suggestion for ridesharing.
In the end, they do not have to manually search for potential partners (days in advance), but simply will get notified if there is a possibility to rideshare with someone else.

## External Library Usage

The applications in this repository rely on the library https://github.com/mie-lab/trackintel, which was partially developed within and for this project.

## Code Organization

It is split up into several folders:

* `rideshare-android`: The Android tracking application that automatically uploads tracking data to the backend.
* `rideshare-assets`: Graphics that are used within the application.
* `rideshare-backend`: The Django backend incl. the logic to process the tracking data (as far as not externalized within the `trackintel` library).
* `rideshare-explorations`: Experiments, analyses, studies and explorations.
* `rideshare-mockups`: Mockups of the Android application.

## Publications

Please consider the following publications for method and algorithm developments associated with the project (additional code can be found in the respective repositories or upon request):

* Bucher, Dominik. Spatio-Temporal Information and Communication Technologies Supporting Sustainable Personal Mobility. Diss. ETH Zurich, 2020. https://www.research-collection.ethz.ch/handle/20.500.11850/457564. Consider "Chapter 5: Planning Integrated and Sustainable Mobility" in particular.
* Grass, Christian. Identification of potential ride-sharing paths from GPS taxi trajectory data. MSc Thesis. University of Zurich, ETH Zurich, 2020. https://lean-gate.geo.uzh.ch/prod/typo3conf/ext/qfq/Classes/Api/download.php?s=5ffcbebd78a9d.
* Ruf, Sven. Evaluation and Implementation of Trajectory Similarity Measures within the Context of a Mobility Processing Framework. BSc Thesis. ETH Zurich, 2020. Code developed as part of this thesis can be found in: https://github.com/mie-lab/trackintel 
