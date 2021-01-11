from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    # Upload trackpoints.
    path('api/v1/trackpoints/', views.post_trackpoints, name='post_trackpoints'),
    # Process trackpoints of a given day (usually called after uploading new trackpoints).
    path('api/v1/process-trackpoints/<str:date>/', views.process_trackpoints, name='process_trackpoints'),
    # Retrieve data from a given day (can be called anytime).
    path('api/v1/mobility-data/<str:date>/', views.get_mobility_data, name='get_mobility_data'),
]
