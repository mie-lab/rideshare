from django.db import models
from django.conf import settings
from django.contrib.gis.db import models as gismodels
from django.contrib.postgres.fields import JSONField


class Trackpoint(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=False)

    tripleg = models.ForeignKey('tracking.Tripleg', on_delete=models.SET_NULL, null=True, blank=True)
    staypoint = models.ForeignKey('tracking.Staypoint', on_delete=models.SET_NULL, null=True, blank=True)

    tracked_at = models.DateTimeField()

    accuracy = models.FloatField(null=True)
    tracking_tech = models.CharField(max_length=12)
    context = JSONField(null=True)

    elevation = models.FloatField(null=True)
    geom = gismodels.PointField()

    def __str__(self):
        return f"({self.user}:{self.tracked_at},{self.tracking_tech},{self.geom})"


class Staypoint(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=False)
    
    # trip = models.ForeignKey('tracking.Trip', on_delete=models.SET_NULL, null=True, blank=True)
    # place = models.ForeignKey('tracking.Place', on_delete=models.SET_NULL, null=True, blank=True)

    started_at = models.DateTimeField()
    finished_at = models.DateTimeField()

    purpose_detected = models.CharField(max_length=16)
    purpose_validated = models.CharField(max_length=16)
    validated = models.BooleanField(default=False)
    validated_at = models.DateTimeField(null=True)
    activity = models.BooleanField(default=False)

    radius = models.FloatField()
    context = JSONField(null=True)

    elevation = models.FloatField(null=True)
    geom = gismodels.PointField()

    def __str__(self):
        return f"({self.user}:{self.started_at})"

    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return self.started_at == other.started_at and self.finished_at == other.finished_at and \
                self.radius == other.radius and self.elevation == other.elevation and self.geom == other.geom
        else:
            return False


class Tripleg(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=False)

    # trip = models.ForeignKey('tracking.Trip', on_delete=models.SET_NULL, null=True, blank=True)

    started_at = models.DateTimeField()
    finished_at = models.DateTimeField()

    mode_detected = models.CharField(max_length=16)
    mode_validated = models.CharField(max_length=16)
    validated = models.BooleanField(default=False)
    validated_at = models.DateTimeField(null=True)

    context = JSONField(null=True)

    geom_raw = gismodels.LineStringField()
    geom = gismodels.LineStringField()

    def __str__(self):
        return f"({self.user}:{self.started_at})"

    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return self.started_at == other.started_at and self.finished_at == other.finished_at and \
                self.geom == other.geom and self.geom_raw == other.geom_raw
        else:
            return False
