import json
import traceback
from datetime import datetime

from django.db import transaction, IntegrityError
from django.shortcuts import render
from django.http import HttpResponse
from django.utils import timezone
from django.contrib.gis.geos import GEOSGeometry
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
import pytz
from shapely.geometry import LineString, Point

from tracking.models import Trackpoint
from processing import processing


def default_json_converter(obj):
    if isinstance(obj, datetime):
        return { '_isoformat': obj.to_pydatetime().isoformat(timespec='milliseconds') }
    if isinstance(obj, LineString):
        return { 'coords': [(x, y) for x, y in obj.coords] }
    if isinstance(obj, Point):
        return { 'coords': (obj.x, obj.y) }


@api_view(['GET'])
def index(request):
    return HttpResponse("Please use the API.")


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def post_trackpoints(request):
    data = json.loads(request.body)
    print(f"Receiving data ({len(data)} trackpoints) from user {request.user}.")

    try:
        with transaction.atomic():
            for trackpoint_data in data:
                tracked_at = datetime.utcfromtimestamp(trackpoint_data['timestamp'] / 1000) \
                    .replace(tzinfo=pytz.utc)
                accuracy = trackpoint_data['accuracy']
                tracking_tech = trackpoint_data['provider']

                context = { }
                if 'bearing' in trackpoint_data:
                    context['bearing'] = trackpoint_data['bearing']
                if 'bearingAccuracy' in trackpoint_data:
                    context['bearingAccuracy'] = trackpoint_data['bearingAccuracy']
                if 'speed' in trackpoint_data:
                    context['speed'] = trackpoint_data['speed']
                if 'speedAccuracy' in trackpoint_data:
                    context['speedAccuracy'] = trackpoint_data['speedAccuracy']
                if 'verticalAccuracy' in trackpoint_data:
                    context['verticalAccuracy'] = trackpoint_data['verticalAccuracy']

                elevation = trackpoint_data['elevation']
                longitude = trackpoint_data['longitude']
                latitude = trackpoint_data['latitude']
                geom = GEOSGeometry(f'POINT({longitude} {latitude})')

                trackpoint = Trackpoint(user=request.user, 
                    tracked_at=tracked_at, accuracy=accuracy, tracking_tech=tracking_tech,
                    context=context, elevation=elevation, geom=geom)
                    
                trackpoint.save()
            
            return HttpResponse(json.dumps({ 'status': 'ok' }), content_type='application/json')
    except IntegrityError:
        return HttpResponse(json.dumps({ 'status': 'Internal server error when uploading data.' }), content_type='application/json', status=500)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def process_trackpoints(request, date):
    try:
        response_data = processing.process_trackpoints_of_user(request.user, date)
        # print(json.dumps(response_data, default=default_json_converter))
        print("Returning", len(response_data['triplegs']), "triplegs and", len(response_data['staypoints']), "staypoints.")
        return HttpResponse(json.dumps(response_data, default=default_json_converter), content_type='application/json')
        
    except Exception:
        traceback.print_exc()
        return HttpResponse(json.dumps({ 'status': 'Internal server error when processing data.' }), content_type='application/json', status=500)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def get_mobility_data(request, date):
    try:
        existing_staypoints = Staypoint.objects.filter(user=user, started_at__date=day)
        existing_triplegs = Tripleg.objects.filter(user=user, started_at__date=day)
        response_data = {'triplegs': existing_triplegs, 'staypoints': existing_staypoints}
        return HttpResponse(json.dumps(response_data, default=default_json_converter), content_type='application/json')
        
    except Exception:
        traceback.print_exc()
        return HttpResponse(json.dumps({ 'status': 'Internal server error when processing data.' }), content_type='application/json', status=500)
