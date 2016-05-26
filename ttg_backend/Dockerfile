FROM python:3

# Runtime
ENV DJANGO_SETTINGS_MODULE=settings.prod
CMD ["uwsgi", "--http", ":8080", "--module", "wsgi"]

# Installation time
WORKDIR /code
ADD requirements.txt ./
RUN pip install -r requirements.txt && pip install uwsgi
ADD wsgi.py urls.py manage.py __init__.py settings/ apps/ ./
