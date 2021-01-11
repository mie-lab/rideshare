import random

import psycopg2
import trackintel as ti
import pandas as pd
import geopandas as gpd
from django.conf import settings
from django.db import connection
from django.contrib.gis.geos import GEOSGeometry, LineString, Point

from tracking.models import Trackpoint, Staypoint, Tripleg


def process_trackpoints_of_user(user, day):
    sql = """SELECT * FROM tracking_trackpoint WHERE user_id=%s AND tracking_tech='gps' """ + \
        """AND tracked_at::date='%s'"""
    positionfixes = gpd.GeoDataFrame.from_postgis(sql % (user.id, day), connection, index_col='id')

    staypoints = positionfixes.as_positionfixes.extract_staypoints()
    staypoints['user_id'] = user.id

    triplegs = positionfixes.as_positionfixes.extract_triplegs(staypoints=staypoints)
    triplegs['user_id'] = user.id

    # TODO Radius is wrong, geom/geom_raw are wrong too.
    staypoint_objs = [Staypoint(user=user, started_at=s['started_at'], finished_at=s['finished_at'], 
        radius=5, elevation=s['elevation'], geom=Point(s['geom'].x, s['geom'].y))
        for _, s in staypoints.iterrows()]
    tripleg_objs = []
    for _, t in triplegs.iterrows():
        geom = LineString([(c[0], c[1]) for c in t['geom'].coords])
        t = Tripleg(user=user, started_at=t['started_at'], finished_at=t['finished_at'], 
            geom=geom, geom_raw=geom)
        tripleg_objs.append(t)

    # TODO Implement this check.
    # We check now if this processing step yielded any new triplegs or staypoints, and update
    # the database if so (otherwise), we send the user the already in the database available
    # triplegs and staypoints as the users want to keep their validations, etc.
    existing_staypoints = Staypoint.objects.filter(user=user, started_at__date=day)
    existing_triplegs = Tripleg.objects.filter(user=user, started_at__date=day)

    print(staypoint_objs)
    print(tripleg_objs)

    # print(existing_staypoints)
    # print(existing_triplegs)

    # staypoints.as_staypoints.to_postgis(conn_string, 'tracking_staypoint')
    # places = staypoints.as_staypoints.extract_places(method='dbscan', 
    #     epsilon=ti.geogr.distances.meters_to_decimal_degrees(120, 47.5), num_samples=3)
    # places['user_id'] = user.id
    # print(places)

    modes = ['car', 'walk', 'bike', 'tram', 'train']

    # TODO Use trackintel functionality to get the transport modes.
    triplegs['mode_validated'] = [random.sample(modes, 1)[0] for _ in range(len(triplegs))]
    
    return {
        'triplegs': triplegs.to_dict('records'),
        'staypoints': staypoints.to_dict('records')
    }
