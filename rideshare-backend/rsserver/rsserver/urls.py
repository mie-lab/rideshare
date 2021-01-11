"""rsserver URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/2.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import re_path, path, include
from rsserver import views

urlpatterns = [
    # The URLs where we can up-/download tracking data.
    path('', include('tracking.urls')),
    # We mostly use the REST framework from Django for its JSON user auth API, 
    # cf. https://www.django-rest-framework.org/tutorial/4-authentication-and-permissions/.
    re_path(r'^api/v1/auth/', include('rest_framework.urls')),
    # For some reason, for the token authentification, we need to build our own tokens.
    re_path(r'^api/v1/auth/token/', views.obtain_auth_token, name='obtain_auth_token'),
    # The usual admin panel, visit rideshare.ethz.ch/admin for access.
    path('admin/', admin.site.urls),
]
