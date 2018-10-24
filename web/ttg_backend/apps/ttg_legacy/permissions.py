from django.conf import settings
from rest_framework.permissions import BasePermission, SAFE_METHODS


class ScraperPermission(BasePermission):
    """
    Permission for scrapers to modify data.
    """

    def has_permission(self, request, view):
        valid_token = request.query_params.get('token', '')\
                         == settings.LEGACY_POST_KEY
        if request.query_params.get('refresh', None):
            return valid_token
        return request.method in SAFE_METHODS or valid_token
