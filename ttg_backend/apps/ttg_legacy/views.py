import json
import os
import logging
from pathlib import Path
from sys import stderr
from django.shortcuts import render
from django.conf import settings
from django.core.cache import cache
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.renderers import JSONRenderer
from rest_framework_yaml.renderers import YAMLRenderer
from .permissions import ScraperPermission
from .parsers import GZippedJSONParser


class Schedule(APIView):

    parser_classes = (GZippedJSONParser,)
    renderer_classes = (JSONRenderer, YAMLRenderer)
    permission_classes = (ScraperPermission,)

    def _get_data(self, school_id, api_level):
        cache_name = "%s%s" % (school_id, api_level)
        cached_data = cache.get(cache_name)
        if cached_data:
            return cached_data
        try:
            with open(str(Path(settings.LEGACY_DIR, 'V%s' % api_level,
                               '%s.json' % school_id)), 'r') as f:
                cached_data = json.loads(f.read())
                cache.set(cache_name, cached_data, None)
                return cached_data
        except Exception as e:
            logging.error(e)
            return None

    def get(self, request, school_id, api_level):
        data = None
        if request.query_params.get('refresh', '') in ('true', '1'):
            cache.delete('%s%s' % (school_id, api_level))
            if self._get_data(school_id, api_level):
                data = {'message': 'Cache successfully refreshed '
                                   'for "%s"' % school_id}
        else:
            data = self._get_data(school_id, api_level)
        if data:
            return Response(data=data, status=200)
        else:
            return Response({'error': 'No such school under API V%s: "%s"'
                             % (api_level, school_id)}, status=404)

    def post(self, request, school_id, api_level):
        try:
            dest_dir = Path(settings.LEGACY_DIR, "V%s" % api_level)
            if not dest_dir.exists():
                os.makedirs(str(dest_dir))
            with open(str(Path(dest_dir, '%s.json' % school_id)), 'w') as f:
                f.write(json.dumps(request.data))
        except Exception as e:
            logging.error(e)
            return Response({'error': str(e)}, status=500)
        return Response({'message': 'Data successfully updated for '
                                    '"%s"' % school_id})
