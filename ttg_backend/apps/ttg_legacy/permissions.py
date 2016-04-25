from django.conf import settings
from rest_framework.permissions import BasePermission, SAFE_METHODS


class ScraperPermission(BasePermission):
    """
    Permission for scrapers to modify data.
    """

    def has_permission(self, request, view):
        return request.method in SAFE_METHODS or \
               request.query_params.get('token', '') == \
               settings.LEGACY_POST_KEY
