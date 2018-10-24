import gzip
from io import BytesIO
from rest_framework.renderers import JSONRenderer
from rest_framework.parsers import BaseParser, JSONParser

class GZippedJSONParser(BaseParser):
    """
    Parses (possibly) GZipped JSON-serialized data.
    """
    media_type = 'application/json'
    renderer_class = JSONRenderer

    def parse(self, stream, media_type=None, parser_context=None):
        """
        Parses the incoming bytestream as JSON and returns the resulting data.
        """
        # Check if the stream is GZipped.
        #import pdb; pdb.set_trace()
        encoding = parser_context['request'].META\
            .get('HTTP_CONTENT_ENCODING', '').lower()
        if encoding == 'gzip':
            f = BytesIO(stream.read())
            stream = gzip.GzipFile(fileobj=f)
            #import pdb; pdb.set_trace()
        return JSONParser().parse(stream, media_type, parser_context)
