# Project Rideshare Server (Django)

This document outlines the most important configurations taken and how you can run everything.

## How the App is Built

Most things follow the tutorial at [on the Django website](https://docs.djangoproject.com/en/2.2/intro).
The database is PostgreSQL, which can be set up with `python manage.py migrate`.
You can migrate the database using the following two commands: `python manage.py makemigrations` and `python manage.py migrate`.
Useful commands are also `python manage.py sqlmigrate tracking 0001` (the last number is the migration id) and `python manage.py check`.
You can play around with the app using `python manage.py shell`, and then for example `from tracking.models import Trackpoint`.

With `python manage.py createsuperuser` you can create a superuser to log in to the application.
At [http://127.0.0.1:8000/admin/login/?next=/admin/](http://127.0.0.1:8000/admin/login/?next=/admin/), we can then log in and look around.

## Run (Development)

Use the command:

```{bash}
python manage.py runserver 8000
```

You can import some trackpoints/positionfixes by running `scripts/import_test_data.py`.
This requires having some test data in a file `test_data.sql` in the same folder.

## Testing

Run the tests using one of the two:

```{bash}
python manage.py test
python manage.py test --keepdb
```

You can dump the fixtures using: `python manage.py dumpdata --format=json > data/fixtures/testdata.json`.

## Deployment

As for now, you can run it on the server as `python3.6 manage.py runserver 10000`.
If the file `on.production` is available in the project directory (unversioned), it will additionally load `settings_prod.py`.
