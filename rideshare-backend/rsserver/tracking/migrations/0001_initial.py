# Generated by Django 2.2.3 on 2019-07-17 15:34

from django.conf import settings
import django.contrib.gis.db.models.fields
import django.contrib.postgres.fields.jsonb
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='Staypoint',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('started_at', models.DateTimeField()),
                ('finished_at', models.DateTimeField()),
                ('purpose_detected', models.CharField(max_length=16)),
                ('purpose_validated', models.CharField(max_length=16)),
                ('validated', models.BooleanField(default=False)),
                ('validated_at', models.DateTimeField(null=True)),
                ('activity', models.BooleanField(default=False)),
                ('radius', models.FloatField()),
                ('context', django.contrib.postgres.fields.jsonb.JSONField(null=True)),
                ('elevation', models.FloatField(null=True)),
                ('geom', django.contrib.gis.db.models.fields.PointField(srid=4326)),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.CreateModel(
            name='Tripleg',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('started_at', models.DateTimeField()),
                ('finished_at', models.DateTimeField()),
                ('mode_detected', models.CharField(max_length=16)),
                ('mode_validated', models.CharField(max_length=16)),
                ('validated', models.BooleanField(default=False)),
                ('validated_at', models.DateTimeField(null=True)),
                ('context', django.contrib.postgres.fields.jsonb.JSONField(null=True)),
                ('geom_raw', django.contrib.gis.db.models.fields.LineStringField(srid=4326)),
                ('geom', django.contrib.gis.db.models.fields.LineStringField(srid=4326)),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.CreateModel(
            name='Trackpoint',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('tracked_at', models.DateTimeField()),
                ('accuracy', models.FloatField(null=True)),
                ('tracking_tech', models.CharField(max_length=12)),
                ('context', django.contrib.postgres.fields.jsonb.JSONField(null=True)),
                ('elevation', models.FloatField(null=True)),
                ('geom', django.contrib.gis.db.models.fields.PointField(srid=4326)),
                ('staypoint', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='tracking.Staypoint')),
                ('tripleg', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='tracking.Tripleg')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
    ]
