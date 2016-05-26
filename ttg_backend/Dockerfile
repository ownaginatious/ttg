FROM python:3

# Runtime
ENV DJANGO_SETTINGS_MODULE=settings.prod
CMD ["uwsgi", "--http", ":8080", "--module", "wsgi"]

# Installation time
WORKDIR /code
COPY requirements.txt ./
RUN pip install -r requirements.txt && pip install uwsgi
COPY settings/ ./settings
COPY apps/ ./apps
COPY wsgi.py urls.py manage.py __init__.py ./
