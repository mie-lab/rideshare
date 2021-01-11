# Documentation Android App "Rideshare"

The Android app "Rideshare" continuously collects movement data.
The associated backend processes this data to identify segments of mobility, and use them to predict where someone is going to move in the near future.
Once these predictions are available, people with similar (predicted) routes are matched together, and notifications are sent to their applications.
Upon receiving these (push) notifications, app users can accept or reject them.

The idea is to make short-distance and ad-hoc ridesharing easier, i.e., potential rideshare participants simply have to "swipe" to rideshare with someone else and not to manually search for potential partners.

## Trackpoint Collection and Upload

Every ~15 min, the app will send all collected (and non-synchronized) trackpoints to the server.
The server will then use *trackintel* (<https://github.com/mie-lab/trackintel>) to process the whole day's data and segment the trackpoints into triplegs.
The triplegs are sent back to the app.
After a successful upload, the trackpoints are deleted locally.
Similarly, previous triplegs of the day are deleted and replaced with the new ones.

During tracking, trackpoints are constantly shown, until they are uploaded and transformed into triplegs.
