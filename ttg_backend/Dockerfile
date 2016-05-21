FROM python:3

ADD . /code
WORKDIR /code
RUN pip install -r requirements.txt && pip install uwsgi
ENV DJANGO_SETTINGS_MODULE=settings.prod
CMD uwsgi --http :8080 --module wsgi
